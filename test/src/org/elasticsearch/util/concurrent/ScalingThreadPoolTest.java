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

package org.elasticsearch.util.concurrent;

import org.testng.annotations.Test;

import java.util.concurrent.ThreadPoolExecutor;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author kimchy (Shay Banon)
 */
public class ScalingThreadPoolTest {

    @Test public void testScaleUp() throws Exception {
        final int min = 2;
        final int max = 4;
        final ThreadBarrier barrier = new ThreadBarrier(max + 1);

        ThreadPoolExecutor pool = (ThreadPoolExecutor) DynamicExecutors.newScalingThreadPool(min, max, Long.MAX_VALUE);
        assertThat("Min property", pool.getCorePoolSize(), equalTo(min));
        assertThat("Max property", pool.getMaximumPoolSize(), equalTo(max));

        for (int i = 0; i < max; ++i) {
            pool.execute(new Runnable() {
                public void run() {
                    try {
                        barrier.await();
                        barrier.await();
                    }
                    catch (Throwable e) {
                        barrier.reset(e);
                    }
                }
            });

            //wait until thread executes this task
            //otherwise, a task might be queued
            Thread.sleep(100);
        }

        barrier.await();
        assertThat("wrong pool size", pool.getPoolSize(), equalTo(max));
        assertThat("wrong active size", pool.getActiveCount(), equalTo(max));
        barrier.await();
        pool.shutdown();
    }

    @Test public void testScaleDown() throws Exception {
        final int min = 2;
        final int max = 4;
        final ThreadBarrier barrier = new ThreadBarrier(max + 1);

        ThreadPoolExecutor pool = (ThreadPoolExecutor) DynamicExecutors.newScalingThreadPool(min, max, 0 /*keep alive*/);
        assertThat("Min property", pool.getCorePoolSize(), equalTo(min));
        assertThat("Max property", pool.getMaximumPoolSize(), equalTo(max));

        for (int i = 0; i < max; ++i) {
            pool.execute(new Runnable() {
                public void run() {
                    try {
                        barrier.await();
                        barrier.await();
                    }
                    catch (Throwable e) {
                        barrier.reset(e);
                    }
                }
            });

            //wait until thread executes this task
            //otherwise, a task might be queued
            Thread.sleep(100);
        }

        barrier.await();
        assertThat("wrong pool size", pool.getPoolSize(), equalTo(max));
        assertThat("wrong active size", pool.getActiveCount(), equalTo(max));
        barrier.await();
        Thread.sleep(1000);

        assertThat("not all tasks completed", pool.getCompletedTaskCount(), equalTo((long) max));
        assertThat("wrong active count", pool.getActiveCount(), equalTo(0));
        //Assert.assertEquals("wrong pool size. ", min, pool.getPoolSize()); //BUG in ThreadPool - Bug ID: 6458662
        assertThat("idle threads didn't shrink below max. (" + pool.getPoolSize() + ")", pool.getPoolSize(), greaterThan(0));
        assertThat("idle threads didn't shrink below max. (" + pool.getPoolSize() + ")", pool.getPoolSize(), lessThan(max));
    }


    @Test public void testScaleAbove() throws Exception {
        final int min = 2;
        final int max = 4;
        final int ntasks = 16;
        final ThreadBarrier barrier = new ThreadBarrier(max + 1);

        ThreadPoolExecutor pool = (ThreadPoolExecutor) DynamicExecutors.newScalingThreadPool(min, max, Long.MAX_VALUE);
        assertThat("Min property", pool.getCorePoolSize(), equalTo(min));
        assertThat("Max property", pool.getMaximumPoolSize(), equalTo(max));

        for (int i = 0; i < ntasks; ++i) {
            final int id = i;
            pool.execute(new Runnable() {
                public void run() {
                    try {
                        if (id < max) {
                            barrier.await();
                        }
                    }
                    catch (Throwable e) {
                        barrier.reset(e);
                    }
                }
            });

            //wait until thread executes this task
            //otherwise, a task might be queued
            Thread.sleep(100);
        }

        assertThat("wrong number of pooled tasks", pool.getQueue().size(), equalTo(ntasks - max));
        barrier.await();

        //wait around for one second
        Thread.sleep(1000);
        assertThat("tasks not complete", pool.getCompletedTaskCount(), equalTo((long) ntasks));
        assertThat("didn't scale above core pool size. (" + pool.getLargestPoolSize() + ")", pool.getLargestPoolSize(), greaterThan(min));
        assertThat("Largest pool size exceeds max. (" + pool.getLargestPoolSize() + ")", pool.getLargestPoolSize(), lessThanOrEqualTo(max));
    }
}
