/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.monitor;

import com.codahale.metrics.JvmAttributeGaugeSet;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
@Component
public class CollectorMetric {

    public static final String REPOTER_LOGGER_NAME = "com.navercorp.pinpoint.collector.StateReport";

    private final Logger reporterLogger = LoggerFactory.getLogger(REPOTER_LOGGER_NAME);

    @Autowired
    private MetricRegistry metricRegistry;

    private ScheduledReporter reporter;


    @PostConstruct
    public void start() {
        initRegistry();
        initReporters();
    }

    private void initRegistry() {
        // add JVM statistics
        metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
        metricRegistry.register("jvm.vm", new JvmAttributeGaugeSet());
        metricRegistry.register("jvm.garbage-collectors", new GarbageCollectorMetricSet());
        metricRegistry.register("jvm.thread-states", new ThreadStatesGaugeSet());
    }


    private void initReporters() {
        Slf4jReporter.Builder builder = Slf4jReporter.forRegistry(metricRegistry);
        builder.convertRatesTo(TimeUnit.SECONDS);
        builder.convertDurationsTo(TimeUnit.MILLISECONDS);

        builder.outputTo(reporterLogger);
        reporter = builder.build();

        reporter.start(60, TimeUnit.SECONDS); // print every 1 min.
    }


    @PreDestroy
    private void shutdown() {
        if (reporter == null) {
            return;
        }
        reporter.stop();
        reporter = null;
    }
}
