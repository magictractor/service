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
package uk.co.magictractor.service.ref;

import java.util.Optional;
import java.util.function.Supplier;

import uk.co.magictractor.service.ServiceRegistry;

/**
 *
 */
public class SupplierRef<T> implements Ref<T> {

    private final String servicePackage = ServiceRegistry.getServicePackage();
    private final Supplier<T> supplier;

    private Optional<T> value;

    public SupplierRef(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    ServiceRegistry.runWithSiblingServices(servicePackage, this::read);
                }
            }
        }

        return value.orElse(null);
    }

    private void read() {
        value = Optional.ofNullable(supplier.get());
    }

    @Override
    public <KEY> KEY key() {
        return null;
    }

}
