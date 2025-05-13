/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
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
 *
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.collector.agent.stream.worker.trace.global;

import java.util.ArrayList;
import java.util.List;
import org.skywalking.apm.collector.agent.stream.graph.TraceStreamGraph;
import org.skywalking.apm.collector.agent.stream.parser.FirstSpanListener;
import org.skywalking.apm.collector.agent.stream.parser.GlobalTraceIdsListener;
import org.skywalking.apm.collector.agent.stream.parser.standardization.SpanDecorator;
import org.skywalking.apm.collector.core.graph.Graph;
import org.skywalking.apm.collector.core.graph.GraphManager;
import org.skywalking.apm.collector.core.util.Const;
import org.skywalking.apm.collector.core.util.TimeBucketUtils;
import org.skywalking.apm.collector.storage.table.global.GlobalTrace;
import org.skywalking.apm.network.proto.UniqueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author peng-yongsheng
 */
public class GlobalTraceSpanListener implements FirstSpanListener, GlobalTraceIdsListener {

    private final Logger logger = LoggerFactory.getLogger(GlobalTraceSpanListener.class);

    private List<String> globalTraceIds = new ArrayList<>();
    private String segmentId;
    private long timeBucket;

    @Override
    public void parseFirst(SpanDecorator spanDecorator, int applicationId, int instanceId,
        String segmentId) {
        this.timeBucket = TimeBucketUtils.INSTANCE.getMinuteTimeBucket(spanDecorator.getStartTime());
        this.segmentId = segmentId;
    }

    @Override public void parseGlobalTraceId(UniqueId uniqueId) {
        StringBuilder globalTraceIdBuilder = new StringBuilder();
        for (int i = 0; i < uniqueId.getIdPartsList().size(); i++) {
            if (i == 0) {
                globalTraceIdBuilder.append(uniqueId.getIdPartsList().get(i));
            } else {
                globalTraceIdBuilder.append(".").append(uniqueId.getIdPartsList().get(i));
            }
        }
        globalTraceIds.add(globalTraceIdBuilder.toString());
    }

    @Override public void build() {
        logger.debug("global trace listener build");

        Graph<GlobalTrace> graph = GraphManager.INSTANCE.createIfAbsent(TraceStreamGraph.GLOBAL_TRACE_GRAPH_ID, GlobalTrace.class);
        for (String globalTraceId : globalTraceIds) {
            GlobalTrace globalTrace = new GlobalTrace(segmentId + Const.ID_SPLIT + globalTraceId);
            globalTrace.setGlobalTraceId(globalTraceId);
            globalTrace.setSegmentId(segmentId);
            globalTrace.setTimeBucket(timeBucket);
            graph.start(globalTrace);
        }
    }
}