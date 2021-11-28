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

package org.elasticsearch.search.query;

import org.apache.lucene.search.TopDocs;
import org.elasticsearch.search.SearchShardTarget;
import org.elasticsearch.search.facets.Facets;
import org.elasticsearch.util.io.Streamable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static org.elasticsearch.search.SearchShardTarget.*;
import static org.elasticsearch.search.facets.Facets.*;
import static org.elasticsearch.util.lucene.Lucene.*;

/**
 * @author kimchy (Shay Banon)
 */
public class QuerySearchResult implements Streamable, QuerySearchResultProvider {

    private long id;

    private SearchShardTarget shardTarget;

    private int from;

    private int size;

    private TopDocs topDocs;

    private Facets facets;

    private boolean searchTimedOut;

    public QuerySearchResult() {

    }

    public QuerySearchResult(long id, SearchShardTarget shardTarget) {
        this.id = id;
        this.shardTarget = shardTarget;
    }

    @Override public boolean includeFetch() {
        return false;
    }

    @Override public QuerySearchResult queryResult() {
        return this;
    }

    public long id() {
        return this.id;
    }

    public SearchShardTarget shardTarget() {
        return shardTarget;
    }

    public void searchTimedOut(boolean searchTimedOut) {
        this.searchTimedOut = searchTimedOut;
    }

    public boolean searchTimedOut() {
        return searchTimedOut;
    }

    public TopDocs topDocs() {
        return topDocs;
    }

    public void topDocs(TopDocs topDocs) {
        this.topDocs = topDocs;
    }

    public Facets facets() {
        return facets;
    }

    public void facets(Facets facets) {
        this.facets = facets;
    }

    public int from() {
        return from;
    }

    public QuerySearchResult from(int from) {
        this.from = from;
        return this;
    }

    public int size() {
        return size;
    }

    public QuerySearchResult size(int size) {
        this.size = size;
        return this;
    }

    public static QuerySearchResult readQuerySearchResult(DataInput in) throws IOException, ClassNotFoundException {
        QuerySearchResult result = new QuerySearchResult();
        result.readFrom(in);
        return result;
    }

    @Override public void readFrom(DataInput in) throws IOException, ClassNotFoundException {
        id = in.readLong();
        shardTarget = readSearchShardTarget(in);
        from = in.readInt();
        size = in.readInt();
        topDocs = readTopDocs(in);
        if (in.readBoolean()) {
            facets = readFacets(in);
        }
        searchTimedOut = in.readBoolean();
    }

    @Override public void writeTo(DataOutput out) throws IOException {
        out.writeLong(id);
        shardTarget.writeTo(out);
        out.writeInt(from);
        out.writeInt(size);
        writeTopDocs(out, topDocs, 0);
        if (facets == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            facets.writeTo(out);
        }
        out.writeBoolean(searchTimedOut);
    }
}
