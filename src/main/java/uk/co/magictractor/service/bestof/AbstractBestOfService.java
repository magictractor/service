/**
 * Copyright 2021 Ken Dobson
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
package uk.co.magictractor.service.bestof;

import java.util.List;
import java.util.stream.Collectors;

import uk.co.magictractor.service.AbstractSearchableService;
import uk.co.magictractor.service.SearchableService;
import uk.co.magictractor.service.ServiceRegistry;

/**
 * <p>
 * Service implementation which constructs elements by merging elements from
 * other service implementations. One other service is deemed "primary", it
 * determines the number of elements and the keys used to look up values from
 * other service implementations.
 * </p>
 * <p>
 * The goal is that BestOf services should not not have any unsupported
 * features.
 * </p>
 */
public abstract class AbstractBestOfService<ELEMENT, INTERFACE extends SearchableService<ELEMENT>>
        extends AbstractSearchableService<ELEMENT> {

    private final Class<? extends INTERFACE> primaryServiceImplementationType;

    protected AbstractBestOfService(Class<INTERFACE> serviceInterface, Class<? extends INTERFACE> primaryServiceImplementationType) {
        this.primaryServiceImplementationType = primaryServiceImplementationType;
    }

    @Override
    protected List<ELEMENT> readAll() {
        return ServiceRegistry.getOrCreateImplementation(primaryServiceImplementationType)
                .streamAll()
                .map(this::wrapElement)
                .collect(Collectors.toList());
    }

    // TODO! does not respect includeInList?

    protected abstract ELEMENT wrapElement(ELEMENT primaryElement);

}
