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

import uk.co.magictractor.service.HasCode;
import uk.co.magictractor.service.HasCodeService;
import uk.co.magictractor.service.ref.RefResolver;

/**
 *
 */
public class HasCodeRefResolver<ELEMENT extends HasCode, SERVICE extends HasCodeService<ELEMENT>>
        implements RefResolver<String, ELEMENT, SERVICE> {

    public static <SERVICE extends HasCodeService<ELEMENT>, ELEMENT extends HasCode> List<RefResolver<String, ELEMENT, SERVICE>> list(
            List<String> codes) {

        List<RefResolver<String, ELEMENT, SERVICE>> refResolvers = new ArrayList<>(codes.size());
        for (String code : codes) {
            refResolvers.add(new HasCodeRefResolver<>(code));
        }

        return refResolvers;
    }

    private final String code;

    public HasCodeRefResolver(String code) {
        this.code = code;
    }

    @Override
    public String getKey() {
        return code;
    }

    @Override
    public ELEMENT resolve(SERVICE service) {
        return service.findByCode(code);
    }

    @Override
    public String describe() {
        return "has code " + code;
    }

}
