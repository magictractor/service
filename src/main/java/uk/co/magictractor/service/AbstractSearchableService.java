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
package uk.co.magictractor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 */
public abstract class AbstractSearchableService<ELEMENT> implements SearchableService<ELEMENT> {

    private List<ELEMENT> listAllCache;
    private List<ELEMENT> listCache;
    private Map<String, Map<Object, ELEMENT>> findOnlyCaches = new HashMap<>();
    private Map<String, Map<Object, List<ELEMENT>>> findAllCaches = new HashMap<>();

    protected abstract List<ELEMENT> readAll();

    @Override
    public final List<ELEMENT> listAll() {
        if (listAllCache == null) {
            synchronized (this) {
                if (listAllCache == null) {
                    ServiceRegistry.runWithSiblingServices(getClass(), () -> {
                        listAllCache = readAll();
                    });
                }
            }
        }

        return listAllCache;
    }

    @Override
    public final List<ELEMENT> list() {
        if (listCache == null) {
            synchronized (this) {
                if (listCache == null) {
                    listCache = listAll().stream()
                            .filter(this::includeInList)
                            .collect(Collectors.toList());
                }
            }
        }

        return listCache;
    }

    @Override
    public ELEMENT findOnly(String cacheName, Object cacheKey, Predicate<ELEMENT> matcher, boolean allowNotFound) {
        Map<Object, ELEMENT> cache = findOnlyCaches.computeIfAbsent(cacheName, key -> new HashMap<>());
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        List<ELEMENT> found = findAll0(matcher);
        // TODO! cache errors too?
        ELEMENT result = null;
        if (found.isEmpty()) {
            if (!allowNotFound) {
                throw new IllegalArgumentException("No value found for key \"" + cacheKey + "\" in cache \"" + cacheName
                        + "\" of service " + getClass().getSimpleName());
            }
        }
        else if (found.size() > 1) {
            throw new IllegalArgumentException(
                "Multiple values found for key \"" + cacheKey + "\" in cache \"" + cacheName
                        + "\" of service " + getClass().getSimpleName()
                        + ". Found: " + found);
        }
        else {
            result = found.get(0);
        }

        cache.put(cacheKey, result);

        return result;
    }

    @Override
    public List<ELEMENT> findAll(String cacheName, Object cacheKey, Predicate<ELEMENT> matcher) {
        Map<Object, List<ELEMENT>> cache = findAllCaches.computeIfAbsent(cacheName, key -> new HashMap<>());
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        List<ELEMENT> result = findAll0(matcher);
        cache.put(cacheKey, result);

        return result;
    }

    private List<ELEMENT> findAll0(Predicate<ELEMENT> matcher) {
        return streamAll()
                .filter(matcher)
                .collect(Collectors.toList());
    }

    // Removed for now. Can likely avoid the dependency on Guava. Also the impl could/should be more interesting (cache size?)
    //    @Override
    //    public String toString() {
    //        return MoreObjects.toStringHelper(this)
    //                .toString();
    //    }

}
