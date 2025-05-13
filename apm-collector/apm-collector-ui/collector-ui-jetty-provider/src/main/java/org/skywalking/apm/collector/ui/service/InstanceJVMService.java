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

package org.skywalking.apm.collector.ui.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Set;
import org.skywalking.apm.collector.core.UnexpectedException;
import org.skywalking.apm.collector.core.module.ModuleManager;
import org.skywalking.apm.collector.core.util.ObjectUtils;
import org.skywalking.apm.collector.storage.StorageModule;
import org.skywalking.apm.collector.storage.dao.ICpuMetricUIDAO;
import org.skywalking.apm.collector.storage.dao.IGCMetricUIDAO;
import org.skywalking.apm.collector.storage.dao.IInstPerformanceUIDAO;
import org.skywalking.apm.collector.storage.dao.IInstanceUIDAO;
import org.skywalking.apm.collector.storage.dao.IMemoryMetricUIDAO;
import org.skywalking.apm.collector.storage.dao.IMemoryPoolMetricUIDAO;
import org.skywalking.apm.collector.storage.table.register.Instance;
import org.skywalking.apm.network.proto.PoolType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author peng-yongsheng
 */
public class InstanceJVMService {

    private final Logger logger = LoggerFactory.getLogger(InstanceJVMService.class);

    private final Gson gson = new Gson();

    private final IInstanceUIDAO instanceDAO;
    private final ICpuMetricUIDAO cpuMetricDAO;
    private final IGCMetricUIDAO gcMetricDAO;
    private final IMemoryMetricUIDAO memoryMetricDAO;
    private final IMemoryPoolMetricUIDAO memoryPoolMetricDAO;
    private final IInstPerformanceUIDAO instPerformanceDAO;

    public InstanceJVMService(ModuleManager moduleManager) {
        this.instanceDAO = moduleManager.find(StorageModule.NAME).getService(IInstanceUIDAO.class);
        this.cpuMetricDAO = moduleManager.find(StorageModule.NAME).getService(ICpuMetricUIDAO.class);
        this.gcMetricDAO = moduleManager.find(StorageModule.NAME).getService(IGCMetricUIDAO.class);
        this.memoryMetricDAO = moduleManager.find(StorageModule.NAME).getService(IMemoryMetricUIDAO.class);
        this.memoryPoolMetricDAO = moduleManager.find(StorageModule.NAME).getService(IMemoryPoolMetricUIDAO.class);
        this.instPerformanceDAO = moduleManager.find(StorageModule.NAME).getService(IInstPerformanceUIDAO.class);
    }

    public JsonObject getInstanceOsInfo(int instanceId) {
        Instance instance = instanceDAO.getInstance(instanceId);
        if (ObjectUtils.isEmpty(instance)) {
            throw new UnexpectedException("instance id: " + instanceId + " not exist.");
        }

        return gson.fromJson(instance.getOsInfo(), JsonObject.class);
    }

    public JsonObject getInstanceJvmMetric(int instanceId, Set<String> metricTypes, long timeBucket) {
        JsonObject metrics = new JsonObject();
        for (String metricType : metricTypes) {
            if (metricType.toLowerCase().equals(MetricType.cpu.name())) {
                metrics.addProperty(MetricType.cpu.name(), cpuMetricDAO.getMetric(instanceId, timeBucket));
            } else if (metricType.toLowerCase().equals(MetricType.gc.name())) {
                metrics.add(MetricType.gc.name(), gcMetricDAO.getMetric(instanceId, timeBucket));
            } else if (metricType.toLowerCase().equals(MetricType.tps.name())) {
                metrics.addProperty(MetricType.tps.name(), instPerformanceDAO.getTpsMetric(instanceId, timeBucket));
            } else if (metricType.toLowerCase().equals(MetricType.resptime.name())) {
                metrics.addProperty(MetricType.resptime.name(), instPerformanceDAO.getRespTimeMetric(instanceId, timeBucket));
            } else if (metricType.toLowerCase().equals(MetricType.heapmemory.name())) {
                metrics.add(MetricType.heapmemory.name(), memoryMetricDAO.getMetric(instanceId, timeBucket, true));
            } else if (metricType.toLowerCase().equals(MetricType.nonheapmemory.name())) {
                metrics.add(MetricType.nonheapmemory.name(), memoryMetricDAO.getMetric(instanceId, timeBucket, false));
            } else if (metricType.toLowerCase().equals(MetricType.permgen.name())) {
                metrics.add(MetricType.permgen.name(), memoryPoolMetricDAO.getMetric(instanceId, timeBucket, PoolType.PERMGEN_USAGE_VALUE));
            } else if (metricType.toLowerCase().equals(MetricType.metaspace.name())) {
                metrics.add(MetricType.metaspace.name(), memoryPoolMetricDAO.getMetric(instanceId, timeBucket, PoolType.METASPACE_USAGE_VALUE));
            } else if (metricType.toLowerCase().equals(MetricType.newgen.name())) {
                metrics.add(MetricType.newgen.name(), memoryPoolMetricDAO.getMetric(instanceId, timeBucket, PoolType.NEWGEN_USAGE_VALUE));
            } else if (metricType.toLowerCase().equals(MetricType.oldgen.name())) {
                metrics.add(MetricType.oldgen.name(), memoryPoolMetricDAO.getMetric(instanceId, timeBucket, PoolType.OLDGEN_USAGE_VALUE));
            } else if (metricType.toLowerCase().equals(MetricType.survivor.name())) {
                metrics.add(MetricType.survivor.name(), memoryPoolMetricDAO.getMetric(instanceId, timeBucket, PoolType.SURVIVOR_USAGE_VALUE));
            } else {
                throw new UnexpectedException("unexpected metric type");
            }
        }
        return metrics;
    }

    public JsonObject getInstanceJvmMetrics(int instanceId, Set<String> metricTypes, long startTimeBucket,
        long endTimeBucket) {
        JsonObject metrics = new JsonObject();
        for (String metricType : metricTypes) {
            if (metricType.toLowerCase().equals(MetricType.cpu.name())) {
                metrics.add(MetricType.cpu.name(), cpuMetricDAO.getMetric(instanceId, startTimeBucket, endTimeBucket));
            } else if (metricType.toLowerCase().equals(MetricType.gc.name())) {
                metrics.add(MetricType.gc.name(), gcMetricDAO.getMetric(instanceId, startTimeBucket, endTimeBucket));
            } else if (metricType.toLowerCase().equals(MetricType.tps.name())) {
                metrics.add(MetricType.tps.name(), instPerformanceDAO.getTpsMetric(instanceId, startTimeBucket, endTimeBucket));
            } else if (metricType.toLowerCase().equals(MetricType.resptime.name())) {
                metrics.add(MetricType.resptime.name(), instPerformanceDAO.getRespTimeMetric(instanceId, startTimeBucket, endTimeBucket));
            } else if (metricType.toLowerCase().equals(MetricType.heapmemory.name())) {
                metrics.add(MetricType.heapmemory.name(), memoryMetricDAO.getMetric(instanceId, startTimeBucket, endTimeBucket, true));
            } else if (metricType.toLowerCase().equals(MetricType.nonheapmemory.name())) {
                metrics.add(MetricType.nonheapmemory.name(), memoryMetricDAO.getMetric(instanceId, startTimeBucket, endTimeBucket, false));
            } else if (metricType.toLowerCase().equals(MetricType.permgen.name())) {
                metrics.add(MetricType.permgen.name(), memoryPoolMetricDAO.getMetric(instanceId, startTimeBucket, endTimeBucket, PoolType.PERMGEN_USAGE_VALUE));
            } else if (metricType.toLowerCase().equals(MetricType.metaspace.name())) {
                metrics.add(MetricType.metaspace.name(), memoryPoolMetricDAO.getMetric(instanceId, startTimeBucket, endTimeBucket, PoolType.METASPACE_USAGE_VALUE));
            } else if (metricType.toLowerCase().equals(MetricType.newgen.name())) {
                metrics.add(MetricType.newgen.name(), memoryPoolMetricDAO.getMetric(instanceId, startTimeBucket, endTimeBucket, PoolType.NEWGEN_USAGE_VALUE));
            } else if (metricType.toLowerCase().equals(MetricType.oldgen.name())) {
                metrics.add(MetricType.oldgen.name(), memoryPoolMetricDAO.getMetric(instanceId, startTimeBucket, endTimeBucket, PoolType.OLDGEN_USAGE_VALUE));
            } else if (metricType.toLowerCase().equals(MetricType.survivor.name())) {
                metrics.add(MetricType.survivor.name(), memoryPoolMetricDAO.getMetric(instanceId, startTimeBucket, endTimeBucket, PoolType.SURVIVOR_USAGE_VALUE));
            } else {
                throw new UnexpectedException("unexpected metric type");
            }
        }

        return metrics;
    }

    public enum MetricType {
        cpu, gc, tps, resptime, heapmemory, nonheapmemory, permgen, metaspace, newgen,
        oldgen, survivor
    }
}
