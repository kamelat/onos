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

import java.util.Set;

import org.onlab.onos.net.ConnectPoint;
import org.onlab.onos.sdnip.config.Interface;
import org.onlab.packet.IpAddress;

/**
 * Provides information about the interfaces in the network.
 */
public interface InterfaceService {
    /**
     * Retrieves the entire set of interfaces in the network.
     *
     * @return the set of interfaces
     */
    Set<Interface> getInterfaces();

    /**
     * Retrieves the interface associated with the given connect point.
     *
     * @param connectPoint the connect point to retrieve interface information
     * for
     * @return the interface
     */
    Interface getInterface(ConnectPoint connectPoint);

    /**
     * Retrieves the interface that matches the given IP address. Matching
     * means that the IP address is in one of the interface's assigned subnets.
     *
     * @param ipAddress IP address to match
     * @return the matching interface
     */
    Interface getMatchingInterface(IpAddress ipAddress);
}
