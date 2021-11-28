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

package org.elasticsearch.index.translog;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * @author kimchy (Shay Banon)
 */
public class TranslogSizeMatcher extends TypeSafeMatcher<Translog.Snapshot> {

    private final int size;

    public TranslogSizeMatcher(int size) {
        this.size = size;
    }

    @Override public boolean matchesSafely(Translog.Snapshot snapshot) {
        int count = 0;
        for (Translog.Operation op : snapshot) {
            count++;
        }
        return size == count;
    }

    @Override public void describeTo(Description description) {
        description.appendText("a translog with size ").appendValue(size);
    }

    public static Matcher<Translog.Snapshot> translogSize(int size) {
        return new TranslogSizeMatcher(size);
    }
}
