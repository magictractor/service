/**
 * Copyright 2026 Ken Dobson
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

import java.util.Set;

import org.opentest4j.TestAbortedException;

import uk.co.magictractor.service.ServiceRegistry;
import uk.co.magictractor.service.feature.Features;

public class AbstractServiceTest<SERVICE> {

    private final SERVICE service;
    private final Set<String> unsupportedFeatures;

    protected AbstractServiceTest(Class<SERVICE> serviceType) {
        service = ServiceRegistry.getOrCreateImplementation(serviceType);
        unsupportedFeatures = Features.unsupportedFeatures(serviceType);
    }

    protected SERVICE getService() {
        return service;
    }

    protected <SIBLING> SIBLING getSiblingService(Class<SIBLING> siblingInterface) {
        // TODO! could cache?
        return ServiceRegistry.getOrCreateSibling(service, siblingInterface);
    }

    protected final void assumeSupported(String feature) {
        if (unsupportedFeatures.contains(feature)) {
            throw new TestAbortedException(feature + " not supported by this implementation");
        }
    }

    protected final boolean isSupported(String feature) {
        return !unsupportedFeatures.contains(feature);
    }

}
