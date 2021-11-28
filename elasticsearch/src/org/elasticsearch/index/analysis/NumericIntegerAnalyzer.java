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

package org.elasticsearch.index.analysis;

import org.apache.lucene.util.NumericUtils;

import java.io.IOException;
import java.io.Reader;

/**
 * @author kimchy (Shay Banon)
 */
public class NumericIntegerAnalyzer extends NumericAnalyzer<NumericIntegerTokenizer> {

    private final int precisionStep;

    public NumericIntegerAnalyzer() {
        this(NumericUtils.PRECISION_STEP_DEFAULT);
    }

    public NumericIntegerAnalyzer(int precisionStep) {
        this.precisionStep = precisionStep;
    }

    @Override protected NumericIntegerTokenizer createNumericTokenizer(Reader reader, char[] buffer) throws IOException {
        return new NumericIntegerTokenizer(reader, precisionStep, buffer);
    }
}
