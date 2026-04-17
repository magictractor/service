/**
 * Copyright 2019 Ken Dobson
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
package uk.co.magictractor.service.ref;

import java.util.List;
import java.util.stream.Collectors;

import uk.co.magictractor.service.ServiceRegistry;

/**
 *
 */
public abstract class AbstractListRef<KEY, ELEMENT, SERVICE>
        implements ListRef<List<KEY>, ELEMENT> {

    private final SERVICE service;
    private final List<RefResolver<KEY, ELEMENT, SERVICE>> refResolvers;
    private List<ELEMENT> elements;

    protected AbstractListRef(
            Class<SERVICE> serviceInterface, List<RefResolver<KEY, ELEMENT, SERVICE>> refResolvers) {

        // If there are no refs the service is not required. This can allow a service implementation to be skipped.
        this.service = refResolvers.isEmpty() ? null : ServiceRegistry.getOrCreate(serviceInterface);
        this.refResolvers = refResolvers;
    }

    @Override
    public int size() {
        return refResolvers.size();
    }

    @Override
    public List<ELEMENT> get() {
        if (elements == null) {
            elements = refResolvers.stream()
                    .map(resolver -> resolver.resolve(service))
                    .collect(Collectors.toList());
        }
        return elements;
    }

    @Override
    public List<KEY> key() {
        return refResolvers.stream()
                .map(RefResolver::getKey)
                .collect(Collectors.toList());
    }

}
