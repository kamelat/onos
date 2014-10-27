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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.onlab.onos.net.ConnectPoint;
import org.onlab.onos.net.DeviceId;
import org.onlab.onos.net.PortNumber;
import org.onlab.onos.net.host.HostService;
import org.onlab.onos.net.host.PortAddresses;
import org.onlab.onos.sdnip.config.Interface;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Unit tests for the HostToInterfaceAdaptor class.
 */
public class HostToInterfaceAdaptorTest {

    private HostService hostService;
    private HostToInterfaceAdaptor adaptor;

    private Set<PortAddresses> portAddresses;
    private Map<ConnectPoint, Interface> interfaces;

    private static final ConnectPoint CP1 = new ConnectPoint(
            DeviceId.deviceId("of:1"), PortNumber.portNumber(1));
    private static final ConnectPoint CP2 = new ConnectPoint(
            DeviceId.deviceId("of:1"), PortNumber.portNumber(2));
    private static final ConnectPoint CP3 = new ConnectPoint(
            DeviceId.deviceId("of:2"), PortNumber.portNumber(1));

    private static final ConnectPoint NON_EXISTENT_CP = new ConnectPoint(
            DeviceId.deviceId("doesnotexist"), PortNumber.portNumber(1));

    private static final PortAddresses DEFAULT_PA = new PortAddresses(
            NON_EXISTENT_CP, null, null);


    @Before
    public void setUp() throws Exception {
        hostService = createMock(HostService.class);

        portAddresses = Sets.newHashSet();
        interfaces = Maps.newHashMap();

        createPortAddressesAndInterface(CP1,
                Sets.newHashSet(IpPrefix.valueOf("192.168.1.1/24")),
                MacAddress.valueOf("00:00:00:00:00:01"));

        // Two addresses in the same subnet
        createPortAddressesAndInterface(CP2,
                Sets.newHashSet(IpPrefix.valueOf("192.168.2.1/24"),
                        IpPrefix.valueOf("192.168.2.2/24")),
                MacAddress.valueOf("00:00:00:00:00:02"));

        // Two addresses in different subnets
        createPortAddressesAndInterface(CP3,
                Sets.newHashSet(IpPrefix.valueOf("192.168.3.1/24"),
                        IpPrefix.valueOf("192.168.4.1/24")),
                MacAddress.valueOf("00:00:00:00:00:03"));

        expect(hostService.getAddressBindings()).andReturn(portAddresses).anyTimes();

        replay(hostService);

        adaptor = new HostToInterfaceAdaptor(hostService);
    }

    /**
     * Creates both a PortAddresses and an Interface for the given inputs and
     * places them in the correct global data stores.
     *
     * @param cp the connect point
     * @param ips the set of IP addresses
     * @param mac the MAC address
     */
    private void createPortAddressesAndInterface(
            ConnectPoint cp, Set<IpPrefix> ips, MacAddress mac) {
        PortAddresses pa = new PortAddresses(cp, ips, mac);
        portAddresses.add(pa);
        expect(hostService.getAddressBindingsForPort(cp)).andReturn(pa).anyTimes();

        Interface intf = new Interface(cp, ips, mac);
        interfaces.put(cp, intf);
    }

    /**
     * Tests {@link HostToInterfaceAdaptor#getInterfaces()}.
     * Verifies that the set of interfaces returned matches what is expected
     * based on the input PortAddresses data.
     */
    @Test
    public void testGetInterfaces() {
        Set<Interface> adaptorIntfs = adaptor.getInterfaces();

        assertEquals(3, adaptorIntfs.size());
        assertTrue(adaptorIntfs.contains(this.interfaces.get(CP1)));
        assertTrue(adaptorIntfs.contains(this.interfaces.get(CP2)));
        assertTrue(adaptorIntfs.contains(this.interfaces.get(CP3)));
    }

    /**
     * Tests {@link HostToInterfaceAdaptor#getInterface(ConnectPoint)}.
     * Verifies that the correct interface is returned for a given connect
     * point.
     */
    @Test
    public void testGetInterface() {
        assertEquals(this.interfaces.get(CP1), adaptor.getInterface(CP1));
        assertEquals(this.interfaces.get(CP2), adaptor.getInterface(CP2));
        assertEquals(this.interfaces.get(CP3), adaptor.getInterface(CP3));

        // Try and get an interface for a connect point with no addresses
        reset(hostService);
        expect(hostService.getAddressBindingsForPort(NON_EXISTENT_CP))
                .andReturn(DEFAULT_PA).anyTimes();
        replay(hostService);

        assertNull(adaptor.getInterface(NON_EXISTENT_CP));
    }

    /**
     * Tests {@link HostToInterfaceAdaptor#getInterface(ConnectPoint)} in the
     * case that the input connect point is null.
     * Verifies that a NullPointerException is thrown.
     */
    @Test(expected = NullPointerException.class)
    public void testGetInterfaceNull() {
        adaptor.getInterface(null);
    }

    /**
     * Tests {@link HostToInterfaceAdaptor#getMatchingInterface(IpAddress)}.
     * Verifies that the correct interface is returned based on the given IP
     * address.
     */
    @Test
    public void testGetMatchingInterface() {
        assertEquals(this.interfaces.get(CP1),
                adaptor.getMatchingInterface(IpAddress.valueOf("192.168.1.100")));
        assertEquals(this.interfaces.get(CP2),
                adaptor.getMatchingInterface(IpAddress.valueOf("192.168.2.100")));
        assertEquals(this.interfaces.get(CP3),
                adaptor.getMatchingInterface(IpAddress.valueOf("192.168.3.100")));
        assertEquals(this.interfaces.get(CP3),
                adaptor.getMatchingInterface(IpAddress.valueOf("192.168.4.100")));

        // Try and match an address we don't have subnet configured for
        assertNull(adaptor.getMatchingInterface(IpAddress.valueOf("1.1.1.1")));
    }

    /**
     * Tests {@link HostToInterfaceAdaptor#getMatchingInterface(IpAddress)} in the
     * case that the input IP address is null.
     * Verifies that a NullPointerException is thrown.
     */
    @Test(expected = NullPointerException.class)
    public void testGetMatchingInterfaceNull() {
        adaptor.getMatchingInterface(null);
    }

}
