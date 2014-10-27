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
package org.onlab.onos.sdnip;

import java.util.Collection;

import org.onlab.onos.sdnip.bgp.BgpRouteEntry;

/**
 * Service interface exported by SDN-IP.
 */
public interface SdnIpService {
    /**
     * Gets the BGP routes.
     *
     * @return the BGP routes
     */
    public Collection<BgpRouteEntry> getBgpRoutes();

    /**
     * Gets all the routes known to SDN-IP.
     *
     * @return the SDN-IP routes
     */
    public Collection<RouteEntry> getRoutes();
}
