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
package uk.co.magictractor.service.ref.resolvers;

import java.util.ArrayList;
import java.util.List;

import uk.co.magictractor.service.HasId;
import uk.co.magictractor.service.HasIdService;
import uk.co.magictractor.service.ref.RefResolver;

/**
 *
 */
public class HasIdRefResolver<ELEMENT extends HasId, SERVICE extends HasIdService<ELEMENT>>
        implements RefResolver<Integer, ELEMENT, SERVICE> {

    public static <ELEMENT extends HasId, SERVICE extends HasIdService<ELEMENT>> List<RefResolver<Integer, ELEMENT, SERVICE>> list(
            List<Integer> ids) {

        List<RefResolver<Integer, ELEMENT, SERVICE>> refResolvers = new ArrayList<>(ids.size());
        for (Integer id : ids) {
            refResolvers.add(new HasIdRefResolver<>(id));
        }

        return refResolvers;
    }

    private final int id;

    public HasIdRefResolver(int id) {
        this.id = id;
    }

    @Override
    public Integer getKey() {
        return id;
    }

    @Override
    public ELEMENT resolve(SERVICE service) {
        return service.findById(id);
    }

    @Override
    public String describe() {
        return "has id " + id;
    }

}
