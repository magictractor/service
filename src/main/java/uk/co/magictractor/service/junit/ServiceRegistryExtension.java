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
package uk.co.magictractor.service.junit;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import uk.co.magictractor.service.SearchableService;
import uk.co.magictractor.service.ServiceRegistry;

/**
 *
 */
public class ServiceRegistryExtension implements InvocationInterceptor {

    private final Class<? extends SearchableService<?>> serviceImplementationType;

    public ServiceRegistryExtension(Class<? extends SearchableService<?>> serviceImplementationType) {
        this.serviceImplementationType = serviceImplementationType;
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {

        // hmmm, some compilers don't like this because proceed has "throws Throwable"
        ServiceRegistry.runWithSiblingServices(serviceImplementationType, () -> invocation.proceed());
    }

}
