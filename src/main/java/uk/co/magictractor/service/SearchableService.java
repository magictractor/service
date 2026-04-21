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

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 */
public interface SearchableService<ELEMENT> {

    /**
     * A list of all elements, including unreleased items and non-player
     * characters.
     */
    List<ELEMENT> listAll();

    /**
     * A stream of all elements, including unreleased items and non-player
     * characters.
     */
    default Stream<ELEMENT> streamAll() {
        return listAll().stream();
    }

    /**
     * A list of all released elements available to the player, excluding items
     * from spoilers and non-player characters.
     */
    List<ELEMENT> list();

    /**
     * A stream of all released elements available to the player, excluding
     * items from spoilers and non-player characters.
     */
    default Stream<ELEMENT> stream() {
        return list().stream();
    }

    default boolean includeInList(ELEMENT element) {
        return true;
    }

    ELEMENT findOnly(String cacheName, Object cacheKey, Predicate<ELEMENT> matcher, boolean allowNotFound);

    @Deprecated
    default List<ELEMENT> findAll(String cacheName, Object cacheKey, Predicate<ELEMENT> matcher) {
        return findAll(cacheName, cacheKey, matcher, true, null);
    }

    default List<ELEMENT> findAll(String cacheName, Object cacheKey, Predicate<ELEMENT> matcher, boolean allowNotFound) {
        return findAll(cacheName, cacheKey, matcher, allowNotFound, null);
    }

    List<ELEMENT> findAll(String cacheName, Object cacheKey, Predicate<ELEMENT> matcher, boolean allowNotFound, Comparator<ELEMENT> sorter);

}
