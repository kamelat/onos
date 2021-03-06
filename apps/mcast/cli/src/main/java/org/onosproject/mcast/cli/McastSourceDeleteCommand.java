/*
 * Copyright 2016-present Open Networking Foundation
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
package org.onosproject.mcast.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.onlab.packet.IpAddress;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.mcast.api.McastRoute;
import org.onosproject.mcast.api.MulticastRouteService;
import org.onosproject.net.ConnectPoint;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deletes a multicast route.
 */
@Command(scope = "onos", name = "mcast-source-delete",
        description = "Delete a multicast route flow")
public class McastSourceDeleteCommand extends AbstractShellCommand {

    // Delete format for group line
    private static final String D_FORMAT_MAPPING = "Deleted the mcast route: " +
            "origin=%s, group=%s, source=%s";

    // Update format for group line
    private static final String U_FORMAT_MAPPING = "Updated the mcast route: " +
            "origin=%s, group=%s, source=%s";

    @Option(name = "-sAddr", aliases = "--sourceAddress",
            description = "IP Address of the multicast source. '*' can be used for any source (*, G) entry",
            valueToShowInHelp = "1.1.1.1",
            required = true, multiValued = false)
    String sAddr = null;

    @Option(name = "-gAddr", aliases = "--groupAddress",
            description = "IP Address of the multicast group",
            valueToShowInHelp = "224.0.0.0",
            required = true, multiValued = false)
    String gAddr = null;

    @Option(name = "-src", aliases = "--connectPoint",
            description = "Source port of:XXXXXXXXXX/XX",
            valueToShowInHelp = "of:0000000000000001/1",
            multiValued = true)
    String[] sourceList = null;


    @Override
    protected void execute() {
        MulticastRouteService mcastRouteManager = get(MulticastRouteService.class);
        // Clear all routes
        if ("*".equals(sAddr) && "*".equals(gAddr)) {
            mcastRouteManager.getRoutes().forEach(mcastRouteManager::remove);
            return;
        }
        // Removing/updating a specific entry
        IpAddress sAddrIp = null;
        // If the source Ip is * we want ASM so we leave it as null and the route will have it as an optional.empty()
        if (!sAddr.equals("*")) {
            sAddrIp = IpAddress.valueOf(sAddr);
        }
        McastRoute mRoute = new McastRoute(sAddrIp, IpAddress.valueOf(gAddr),
                                           McastRoute.Type.STATIC);
        // No specific connect points, we have to remove everything
        if (sourceList == null) {
            mcastRouteManager.remove(mRoute);
            printMcastRoute(D_FORMAT_MAPPING, mRoute);
            return;
        }
        // Otherwise we need to remove specific connect points
        if (!mcastRouteManager.getRoutes().contains(mRoute)) {
            print("Route is not present, store it first");
            return;
        }
        Set<ConnectPoint> sourcesSet = Arrays.stream(sourceList)
                .map(ConnectPoint::deviceConnectPoint)
                .collect(Collectors.toSet());
        mcastRouteManager.removeSources(mRoute, sourcesSet);
        printMcastRoute(U_FORMAT_MAPPING, mRoute);
    }

    private void printMcastRoute(String format, McastRoute mcastRoute) {
        // If the source is present let's use it, otherwise we need to print *
        print(format, mcastRoute.type(), mcastRoute.group(),
              mcastRoute.source().isPresent() ? mcastRoute.source().get() : "*");
    }
}
