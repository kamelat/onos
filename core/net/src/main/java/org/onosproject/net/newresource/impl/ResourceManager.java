/*
 * Copyright 2015-2016 Open Networking Laboratory
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
package org.onosproject.net.newresource.impl;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.GuavaCollectors;
import org.onosproject.event.AbstractListenerManager;
import org.onosproject.net.newresource.ResourceAdminService;
import org.onosproject.net.newresource.ResourceAllocation;
import org.onosproject.net.newresource.ResourceConsumer;
import org.onosproject.net.newresource.ResourceEvent;
import org.onosproject.net.newresource.ResourceListener;
import org.onosproject.net.newresource.ResourceService;
import org.onosproject.net.newresource.ResourcePath;
import org.onosproject.net.newresource.ResourceStore;
import org.onosproject.net.newresource.ResourceStoreDelegate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of ResourceService.
 */
@Component(immediate = true)
@Service
@Beta
public final class ResourceManager extends AbstractListenerManager<ResourceEvent, ResourceListener>
        implements ResourceService, ResourceAdminService {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ResourceStore store;

    private final ResourceStoreDelegate delegate = new InternalStoreDelegate();

    @Activate
    public void activate() {
        store.setDelegate(delegate);
        eventDispatcher.addSink(ResourceEvent.class, listenerRegistry);
    }

    @Deactivate
    public void deactivate() {
        store.unsetDelegate(delegate);
        eventDispatcher.removeSink(ResourceEvent.class);
    }

    @Override
    public List<ResourceAllocation> allocate(ResourceConsumer consumer,
                                             List<ResourcePath> resources) {
        checkNotNull(consumer);
        checkNotNull(resources);

        boolean success = store.allocate(resources, consumer);
        if (!success) {
            return ImmutableList.of();
        }

        return resources.stream()
                .map(x -> new ResourceAllocation(x, consumer))
                .collect(Collectors.toList());
    }

    @Override
    public boolean release(List<ResourceAllocation> allocations) {
        checkNotNull(allocations);

        List<ResourcePath> resources = allocations.stream()
                .map(ResourceAllocation::resource)
                .collect(Collectors.toList());
        List<ResourceConsumer> consumers = allocations.stream()
                .map(ResourceAllocation::consumer)
                .collect(Collectors.toList());

        return store.release(resources, consumers);
    }

    @Override
    public boolean release(ResourceConsumer consumer) {
        checkNotNull(consumer);

        Collection<ResourceAllocation> allocations = getResourceAllocations(consumer);
        return release(ImmutableList.copyOf(allocations));
    }

    @Override
    public List<ResourceAllocation> getResourceAllocation(ResourcePath resource) {
        checkNotNull(resource);

        List<ResourceConsumer> consumers = store.getConsumers(resource);
        return consumers.stream()
                .map(x -> new ResourceAllocation(resource, x))
                .collect(GuavaCollectors.toImmutableList());
    }

    @Override
    public <T> Collection<ResourceAllocation> getResourceAllocations(ResourcePath parent, Class<T> cls) {
        checkNotNull(parent);
        checkNotNull(cls);

        // We access store twice in this method, then the store may be updated by others
        Collection<ResourcePath> resources = store.getAllocatedResources(parent, cls);
        return resources.stream()
                .flatMap(resource -> store.getConsumers(resource).stream()
                        .map(consumer -> new ResourceAllocation(resource, consumer)))
                .collect(GuavaCollectors.toImmutableList());
    }

    @Override
    public Collection<ResourceAllocation> getResourceAllocations(ResourceConsumer consumer) {
        checkNotNull(consumer);

        Collection<ResourcePath> resources = store.getResources(consumer);
        return resources.stream()
                .map(x -> new ResourceAllocation(x, consumer))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ResourcePath> getAvailableResources(ResourcePath parent) {
        checkNotNull(parent);

        Collection<ResourcePath> children = store.getChildResources(parent);
        return children.stream()
                // We access store twice in this method, then the store may be updated by others
                .filter(store::isAvailable)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAvailable(ResourcePath resource) {
        checkNotNull(resource);

        return store.isAvailable(resource);
    }

    @Override
    public boolean registerResources(List<ResourcePath> resources) {
        checkNotNull(resources);

        return store.register(resources);
    }

    @Override
    public boolean unregisterResources(List<ResourcePath> resources) {
        checkNotNull(resources);

        return store.unregister(resources);
    }

    private class InternalStoreDelegate implements ResourceStoreDelegate {
        @Override
        public void notify(ResourceEvent event) {
            post(event);
        }
    }
}
