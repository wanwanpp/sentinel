/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.node;

import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slots.statistic.metric.DebugSupport;
import com.alibaba.csp.sentinel.util.function.Predicate;

/**
 * Holds real-time statistics for resources.
 *
 * Sentinel 里面的各种种类的统计节点：
 *
 * StatisticNode：最为基础的统计节点，包含秒级和分钟级两个滑动窗口结构。
 * DefaultNode：链路节点，用于统计调用链路上某个资源的数据，维持树状结构。
 * ClusterNode：簇点，用于统计每个资源全局的数据（不区分调用链路），以及存放该资源的按来源区分的调用数据（类型为 StatisticNode）。特别地，Constants.ENTRY_NODE 节点用于统计全局的入口资源数据。
 * EntranceNode：入口节点，特殊的链路节点，对应某个 Context 入口的所有调用数据。Constants.ROOT 节点也是入口节点。
 * 构建的时机：
 *
 * EntranceNode 在 ContextUtil.enter(xxx) 的时候就创建了，然后塞到 Context 里面。
 * NodeSelectorSlot：根据 context 创建 DefaultNode，然后 set curNode to context。
 * ClusterBuilderSlot：首先根据 resourceName 创建 ClusterNode，并且 set clusterNode to defaultNode；然后再根据 origin 创建来源节点（类型为 StatisticNode），并且 set originNode to curEntry。
 * 几种 Node 的维度（数目）：
 *
 * ClusterNode 的维度是 resource
 * DefaultNode 的维度是 resource * context，存在每个 NodeSelectorSlot 的 map 里面
 * EntranceNode 的维度是 context，存在 ContextUtil 类的 contextNameNodeMap 里面
 * 来源节点（类型为 StatisticNode）的维度是 resource * origin，存在每个 ClusterNode 的 originCountMap 里面
 *
 * @author qinan.qn
 * @author leyou
 * @author Eric Zhao
 */
public interface Node extends OccupySupport, DebugSupport {

    /**
     * Get incoming request per minute ({@code pass + block}).
     *
     * @return total request count per minute
     */
    long totalRequest();

    /**
     * Get pass count per minute.
     *
     * @return total passed request count per minute
     * @since 1.5.0
     */
    long totalPass();

    /**
     * Get {@link Entry#exit()} count per minute.
     *
     * @return total completed request count per minute
     */
    long totalSuccess();

    /**
     * Get blocked request count per minute (totalBlockRequest).
     *
     * @return total blocked request count per minute
     */
    long blockRequest();

    /**
     * Get exception count per minute.
     *
     * @return total business exception count per minute
     */
    long totalException();

    /**
     * Get pass request per second.
     *
     * @return QPS of passed requests
     */
    double passQps();

    /**
     * Get block request per second.
     *
     * @return QPS of blocked requests
     */
    double blockQps();

    /**
     * Get {@link #passQps()} + {@link #blockQps()} request per second.
     *
     * @return QPS of passed and blocked requests
     */
    double totalQps();

    /**
     * Get {@link Entry#exit()} request per second.
     *
     * @return QPS of completed requests
     */
    double successQps();

    /**
     * Get estimated max success QPS till now.
     *
     * @return max completed QPS
     */
    double maxSuccessQps();

    /**
     * Get exception count per second.
     *
     * @return QPS of exception occurs
     */
    double exceptionQps();

    /**
     * Get average rt per second.
     *
     * @return average response time per second
     */
    double avgRt();

    /**
     * Get minimal response time.
     *
     * @return recorded minimal response time
     */
    double minRt();

    /**
     * Get current active thread count.
     *
     * @return current active thread count
     */
    int curThreadNum();

    /**
     * Get last second block QPS.
     */
    double previousBlockQps();

    /**
     * Last window QPS.
     */
    double previousPassQps();

    /**
     * Fetch all valid metric nodes of resources.
     *
     * @return valid metric nodes of resources
     */
    Map<Long, MetricNode> metrics();

    /**
     * Fetch all raw metric items that satisfies the time predicate.
     *
     * @param timePredicate time predicate
     * @return raw metric items that satisfies the time predicate
     * @since 1.7.0
     */
    List<MetricNode> rawMetricsInMin(Predicate<Long> timePredicate);

    /**
     * Add pass count.
     *
     * @param count count to add pass
     */
    void addPassRequest(int count);

    /**
     * Add rt and success count.
     *
     * @param rt      response time
     * @param success success count to add
     */
    void addRtAndSuccess(long rt, int success);

    /**
     * Increase the block count.
     *
     * @param count count to add
     */
    void increaseBlockQps(int count);

    /**
     * Add the biz exception count.
     *
     * @param count count to add
     */
    void increaseExceptionQps(int count);

    /**
     * Increase current thread count.
     */
    void increaseThreadNum();

    /**
     * Decrease current thread count.
     */
    void decreaseThreadNum();

    /**
     * Reset the internal counter. Reset is needed when {@link IntervalProperty#INTERVAL} or
     * {@link SampleCountProperty#SAMPLE_COUNT} is changed.
     */
    void reset();
}
