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

package org.elasticsearch.cluster;

import org.elasticsearch.util.io.Streamable;
import org.elasticsearch.util.settings.Settings;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author kimchy (Shay Banon)
 */
public class ClusterName implements Streamable {

    public static final String SETTING = "cluster.name";

    public static final ClusterName DEFAULT = new ClusterName("elasticsearch");

    private String value;

    public static ClusterName clusterNameFromSettings(Settings settings) {
        return new ClusterName(settings.get("cluster.name", ClusterName.DEFAULT.value()));
    }

    private ClusterName() {

    }

    public ClusterName(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static ClusterName readClusterName(DataInput in) throws ClassNotFoundException, IOException {
        ClusterName clusterName = new ClusterName();
        clusterName.readFrom(in);
        return clusterName;
    }

    @Override public void readFrom(DataInput in) throws IOException, ClassNotFoundException {
        value = in.readUTF();
    }

    @Override public void writeTo(DataOutput out) throws IOException {
        out.writeUTF(value);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClusterName that = (ClusterName) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override public String toString() {
        return "Cluster [" + value + "]";
    }
}
