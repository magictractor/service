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

import java.util.Objects;
import java.util.Optional;

/**
 * Interface that extends {@code SearchableService} providing default methods
 * for finding elements by code.
 */
public interface HasCodeService<ELEMENT extends HasCode> extends SearchableService<ELEMENT> {

    default public ELEMENT findByCode(String code) {
        return findOnly("code", code, candidate -> Objects.equals(code, candidate.getCode()), false);
    }

    default public Optional<ELEMENT> findByCodeOptional(String code) {
        return Optional.ofNullable(findOnly("code", code, candidate -> Objects.equals(code, candidate.getCode()), true));
    }

}
