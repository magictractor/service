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

import uk.co.magictractor.service.HasName;
import uk.co.magictractor.service.HasNameService;
import uk.co.magictractor.service.ref.RefResolver;

/**
 *
 */
public class HasNameRefResolver<ELEMENT extends HasName, SERVICE extends HasNameService<ELEMENT>>
        implements RefResolver<String, ELEMENT, SERVICE> {

    public static <ELEMENT extends HasName, SERVICE extends HasNameService<ELEMENT>> List<RefResolver<String, ELEMENT, SERVICE>> list(
            List<String> names) {

        List<RefResolver<String, ELEMENT, SERVICE>> refResolvers = new ArrayList<>(names.size());
        for (String name : names) {
            refResolvers.add(new HasNameRefResolver<>(name));
        }

        return refResolvers;
    }

    private final String name;

    public HasNameRefResolver(String name) {
        this.name = name;
    }

    @Override
    public String getKey() {
        return name;
    }

    @Override
    public ELEMENT resolve(SERVICE service) {
        return service.findByName(name);
    }

    @Override
    public String describe() {
        return "has name " + name;
    }

}
