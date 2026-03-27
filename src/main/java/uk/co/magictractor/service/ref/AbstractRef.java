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

import uk.co.magictractor.service.SearchableService;
import uk.co.magictractor.service.ServiceRegistry;

/**
 *
 */
public abstract class AbstractRef<KEY, ELEMENT, SERVICE extends SearchableService<ELEMENT>>
        implements Ref<ELEMENT> {

    private final SERVICE service;
    private final RefResolver<KEY, ELEMENT, SERVICE> resolver;
    private ELEMENT element;

    protected AbstractRef(Class<SERVICE> serviceInterface, RefResolver<KEY, ELEMENT, SERVICE> resolver) {
        this.service = ServiceRegistry.getOrCreate(serviceInterface);
        this.resolver = resolver;
    }

    @Override
    public ELEMENT get() {
        if (element == null) {
            try {
                element = resolver.resolve(service);
            }
            catch (RuntimeException e) {
                throw new IllegalStateException(
                    "Failed to resolved reference " + getClass().getSimpleName() + " " + toString(), e);
            }
            if (element == null) {
                throw new IllegalStateException("Ref resolved to null: " + this.getClass().getSimpleName() + " " + resolver.describe());
            }
        }
        return element;
    }

    @SuppressWarnings("unchecked")
    @Override
    public KEY key() {
        return resolver.getKey();
    }

    @Override
    public String toString() {
        return resolver.describe();
    }

}
