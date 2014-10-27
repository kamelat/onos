/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.onlab.onos.metrics.intent;

import java.util.List;
import org.onlab.metrics.EventMetric;
import org.onlab.onos.net.intent.IntentEvent;

/**
 * Service interface exported by IntentMetrics.
 */
public interface IntentMetricsService {
    /**
     * Gets the last saved intent events.
     *
     * @return the last saved intent events.
     */
    public List<IntentEvent> getEvents();

    /**
     * Gets the Event Metric for the intent SUBMITTED events.
     *
     * @return the Event Metric for the intent SUBMITTED events.
     */
    public EventMetric intentSubmittedEventMetric();

    /**
     * Gets the Event Metric for the intent INSTALLED events.
     *
     * @return the Event Metric for the intent INSTALLED events.
     */
    public EventMetric intentInstalledEventMetric();

    /**
     * Gets the Event Metric for the intent WITHDRAW_REQUESTED events.
     *
     * TODO: This intent event is not implemented yet.
     *
     * @return the Event Metric for the intent WITHDRAW_REQUESTED events.
     */
    public EventMetric intentWithdrawRequestedEventMetric();

    /**
     * Gets the Event Metric for the intent WITHDRAWN events.
     *
     * @return the Event Metric for the intent WITHDRAWN events.
     */
    public EventMetric intentWithdrawnEventMetric();
}
