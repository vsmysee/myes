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

package org.elasticsearch.action.search.type;

import org.elasticsearch.search.SearchShardTarget;
import org.elasticsearch.search.dfs.DfsSearchResult;
import org.elasticsearch.search.fetch.FetchSearchResult;
import org.elasticsearch.search.fetch.QueryFetchSearchResult;
import org.elasticsearch.search.query.QuerySearchResultProvider;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author kimchy (Shay Banon)
 */
public class TransportSearchCache {

    private final Queue<Collection<DfsSearchResult>> cacheDfsResults = new ConcurrentLinkedQueue<Collection<DfsSearchResult>>();

    private final Queue<Map<SearchShardTarget, QuerySearchResultProvider>> cacheQueryResults = new ConcurrentLinkedQueue<Map<SearchShardTarget, QuerySearchResultProvider>>();

    private final Queue<Map<SearchShardTarget, FetchSearchResult>> cacheFetchResults = new ConcurrentLinkedQueue<Map<SearchShardTarget, FetchSearchResult>>();

    private final Queue<Map<SearchShardTarget, QueryFetchSearchResult>> cacheQueryFetchResults = new ConcurrentLinkedQueue<Map<SearchShardTarget, QueryFetchSearchResult>>();

    public Collection<DfsSearchResult> obtainDfsResults() {
        Collection<DfsSearchResult> dfsSearchResults;
        while ((dfsSearchResults = cacheDfsResults.poll()) == null) {
            cacheDfsResults.offer(new ConcurrentLinkedQueue<DfsSearchResult>());
        }
        dfsSearchResults.clear();
        return dfsSearchResults;
    }

    public void releaseDfsResults(Collection<DfsSearchResult> dfsResults) {
        dfsResults.clear();
        cacheDfsResults.offer(dfsResults);
    }

    public Map<SearchShardTarget, QuerySearchResultProvider> obtainQueryResults() {
        Map<SearchShardTarget, QuerySearchResultProvider> queryResults;
        while ((queryResults = cacheQueryResults.poll()) == null) {
            cacheQueryResults.offer(new ConcurrentHashMap<SearchShardTarget, QuerySearchResultProvider>());
        }
        queryResults.clear();
        return queryResults;
    }

    public void releaseQueryResults(Map<SearchShardTarget, QuerySearchResultProvider> queryResults) {
        queryResults.clear();
        cacheQueryResults.offer(queryResults);
    }

    public Map<SearchShardTarget, FetchSearchResult> obtainFetchResults() {
        Map<SearchShardTarget, FetchSearchResult> fetchResults;
        while ((fetchResults = cacheFetchResults.poll()) == null) {
            cacheFetchResults.offer(new ConcurrentHashMap<SearchShardTarget, FetchSearchResult>());
        }
        fetchResults.clear();
        return fetchResults;
    }

    public void releaseFetchResults(Map<SearchShardTarget, FetchSearchResult> fetchResults) {
        fetchResults.clear();
        cacheFetchResults.offer(fetchResults);
    }

    public Map<SearchShardTarget, QueryFetchSearchResult> obtainQueryFetchResults() {
        Map<SearchShardTarget, QueryFetchSearchResult> fetchResults;
        while ((fetchResults = cacheQueryFetchResults.poll()) == null) {
            cacheQueryFetchResults.offer(new ConcurrentHashMap<SearchShardTarget, QueryFetchSearchResult>());
        }
        fetchResults.clear();
        return fetchResults;
    }

    public void releaseQueryFetchResults(Map<SearchShardTarget, QueryFetchSearchResult> fetchResults) {
        fetchResults.clear();
        cacheQueryFetchResults.offer(fetchResults);
    }
}
