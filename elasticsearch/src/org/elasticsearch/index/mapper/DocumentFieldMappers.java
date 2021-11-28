/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.mapper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.UnmodifiableIterator;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.FieldNameAnalyzer;
import org.elasticsearch.util.concurrent.Immutable;

import java.util.Map;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

/**
 * @author kimchy (Shay Banon)
 */
@Immutable
public class DocumentFieldMappers implements Iterable<FieldMapper> {

    private final ImmutableList<FieldMapper> fieldMappers;
    private final Map<String, FieldMappers> fullNameFieldMappers;
    private final Map<String, FieldMappers> nameFieldMappers;
    private final Map<String, FieldMappers> indexNameFieldMappers;

    private final FieldNameAnalyzer indexAnalyzer;
    private final FieldNameAnalyzer searchAnalyzer;

    public DocumentFieldMappers(DocumentMapper docMapper, Iterable<FieldMapper> fieldMappers) {
        final Map<String, FieldMappers> tempNameFieldMappers = newHashMap();
        final Map<String, FieldMappers> tempIndexNameFieldMappers = newHashMap();
        final Map<String, FieldMappers> tempFullNameFieldMappers = newHashMap();

        final Map<String, Analyzer> indexAnalyzers = newHashMap();
        final Map<String, Analyzer> searchAnalyzers = newHashMap();

        for (FieldMapper fieldMapper : fieldMappers) {
            FieldMappers mappers = tempNameFieldMappers.get(fieldMapper.name());
            if (mappers == null) {
                mappers = new FieldMappers(fieldMapper);
            } else {
                mappers = mappers.concat(fieldMapper);
            }
            tempNameFieldMappers.put(fieldMapper.name(), mappers);

            mappers = tempIndexNameFieldMappers.get(fieldMapper.indexName());
            if (mappers == null) {
                mappers = new FieldMappers(fieldMapper);
            } else {
                mappers = mappers.concat(fieldMapper);
            }
            tempIndexNameFieldMappers.put(fieldMapper.indexName(), mappers);

            mappers = tempFullNameFieldMappers.get(fieldMapper.fullName());
            if (mappers == null) {
                mappers = new FieldMappers(fieldMapper);
            } else {
                mappers = mappers.concat(fieldMapper);
            }
            tempFullNameFieldMappers.put(fieldMapper.fullName(), mappers);

            if (fieldMapper.indexAnalyzer() != null) {
                indexAnalyzers.put(fieldMapper.indexName(), fieldMapper.indexAnalyzer());
            }
            if (fieldMapper.searchAnalyzer() != null) {
                searchAnalyzers.put(fieldMapper.indexName(), fieldMapper.searchAnalyzer());
            }
        }
        this.fieldMappers = ImmutableList.copyOf(fieldMappers);
        this.nameFieldMappers = ImmutableMap.copyOf(tempNameFieldMappers);
        this.indexNameFieldMappers = ImmutableMap.copyOf(tempIndexNameFieldMappers);
        this.fullNameFieldMappers = ImmutableMap.copyOf(tempFullNameFieldMappers);

        this.indexAnalyzer = new FieldNameAnalyzer(indexAnalyzers, docMapper.indexAnalyzer());
        this.searchAnalyzer = new FieldNameAnalyzer(searchAnalyzers, docMapper.searchAnalyzer());
    }

    @Override public UnmodifiableIterator<FieldMapper> iterator() {
        return fieldMappers.iterator();
    }

    public FieldMappers name(String name) {
        return nameFieldMappers.get(name);
    }

    public FieldMappers indexName(String indexName) {
        return indexNameFieldMappers.get(indexName);
    }

    public FieldMappers fullName(String fullName) {
        return fullNameFieldMappers.get(fullName);
    }

    /**
     * A smart analyzer used for indexing that takes into account specific analyzers configured
     * per {@link org.elasticsearch.index.mapper.FieldMapper}.
     */
    public Analyzer indexAnalyzer() {
        return this.indexAnalyzer;
    }

    /**
     * A smart analyzer used for searching that takes into account specific analyzers configured
     * per {@link org.elasticsearch.index.mapper.FieldMapper}.
     */
    public Analyzer searchAnalyzer() {
        return this.searchAnalyzer;
    }

    public DocumentFieldMappers concat(DocumentMapper docMapper, FieldMapper... fieldMappers) {
        return concat(docMapper, newArrayList(fieldMappers));
    }

    public DocumentFieldMappers concat(DocumentMapper docMapper, Iterable<FieldMapper> fieldMappers) {
        return new DocumentFieldMappers(docMapper, Iterables.concat(this.fieldMappers, fieldMappers));
    }
}
