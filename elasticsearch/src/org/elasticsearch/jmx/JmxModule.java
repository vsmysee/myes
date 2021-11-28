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

package org.elasticsearch.jmx;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.elasticsearch.jmx.action.GetJmxServiceUrlAction;
import org.elasticsearch.util.logging.Loggers;
import org.elasticsearch.util.settings.Settings;

/**
 * @author kimchy (Shay Banon)
 */
public class JmxModule extends AbstractModule {

    private final Settings settings;

    public JmxModule(Settings settings) {
        this.settings = settings;
    }

    @Override protected void configure() {
        JmxService jmxService = new JmxService(Loggers.getLogger(JmxService.class, settings.get("name")), settings);
        bind(JmxService.class).toInstance(jmxService);
        bind(GetJmxServiceUrlAction.class).asEagerSingleton();
        bindListener(Matchers.any(), new JmxExporterTypeListener(jmxService));
    }

    private static class JmxExporterTypeListener implements TypeListener {

        private final JmxService jmxService;

        private JmxExporterTypeListener(JmxService jmxService) {
            this.jmxService = jmxService;
        }

        @Override public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
            Class<? super I> type = typeLiteral.getRawType();
            if (type.isAnnotationPresent(MBean.class)) {
                typeEncounter.register(new JmxExporterInjectionListener<I>(jmxService));
            }
        }
    }

    private static class JmxExporterInjectionListener<I> implements InjectionListener<I> {

        private final JmxService jmxService;

        private JmxExporterInjectionListener(JmxService jmxService) {
            this.jmxService = jmxService;
        }

        @Override public void afterInjection(I instance) {
            jmxService.registerMBean(instance);
        }
    }
}
