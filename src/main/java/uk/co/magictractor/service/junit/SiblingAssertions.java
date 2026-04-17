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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class SiblingAssertions {

    private SiblingAssertions() {
    }

    /**
     * <p>
     * Use reflection to find methods that return Objects that are then checked
     * to see if they are siblings of the given Object.
     * <p>
     * <p>
     * Handles some common indirection, such as methods returning Lists, the
     * elements of which are checked to see if they are siblings. But this
     * method won't handle non-trivial values, in which case XXX can be used
     * instead.
     * </p>
     */
    public static void assertGetterValuesAreSiblings(Object object) {
        List<Method> methods = findObjectGetterMethods(object);
        assertSiblings(object, methods);
    }

    /** Find public methods with no args that return a non-primitive value. */
    private static List<Method> findObjectGetterMethods(Object object) {
        List<Method> result = new ArrayList<>();
        for (Method candidate : object.getClass().getMethods()) {
            if (candidate.getParameterCount() == 0
                    && !candidate.getReturnType().isPrimitive()
                    && !candidate.getReturnType().isEnum()
                    // String, Integer etc.
                    && !candidate.getReturnType().getName().startsWith("java.lang.")
                    // A final class with no interfaces cannot have an alternative implementation.
                    && (!Modifier.isFinal(candidate.getReturnType().getModifiers()) || candidate.getReturnType().getInterfaces().length > 0)) {
                result.add(candidate);
                System.out.println(candidate);
            }
        }

        return result;
    }

    public static void assertSiblings(Object object, String... methodNames) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    private static void assertSiblings(Object object, List<Method> methods) {
        StringBuilder failBuilder = new StringBuilder();

        for (Method method : methods) {
            Object other;
            try {
                other = method.invoke(object, new Object[] {});
            }
            catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
            System.out.println(method.getName() + "() -> " + other);

            if (other instanceof List) {
                List<?> otherList = (List<?>) other;
                for (int i = 0; i < otherList.size(); i++) {
                    boolean areSiblings = checkAndAppendFailures(failBuilder, object, otherList.get(0), method.getName() + "().get(" + i + ")");
                    if (!areSiblings) {
                        // Likely that all elements are the same type so don't bother testing them all.
                        break;
                    }
                }
            }
            else {
                checkAndAppendFailures(failBuilder, object, other, method.getName() + "()");
            }
        }

        if (failBuilder.length() != 0) {
            fail("Non-sibling Objects returned from getters of " + object.getClass().getName() + "\n" + failBuilder.toString());
        }
    }

    private static boolean checkAndAppendFailures(StringBuilder failBuilder, Object object, Object other, String context) {
        boolean areSiblings = areSiblings(object, other);
        if (!areSiblings) {
            failBuilder.append(context);
            failBuilder.append(" -> ");
            failBuilder.append(other.getClass().getName());
            failBuilder.append('\n');
        }

        return areSiblings;
    }

    /**
     * <p>
     * Check that two {@code Objects} have the same package.
     * </p>
     * <p>
     * This is typically used with "BestOf" service implementations that wrap
     * results from other service implementations to ensure that objects
     * returned by getters are also wrapped.
     */
    public static void assertSiblings(Object object1, Object object2) {
        if (!areSiblings(object1, object2)) {
            // Use AssertionError directly rather than via a fail() method to avoid a dependency on JUnit.
            fail("Objects were expected to be siblings, but they are not in the same package: "
                    + object1.getClass().getCanonicalName()
                    + " and "
                    + object2.getClass().getCanonicalName());
        }
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }

    private static boolean areSiblings(Object object1, Object object2) {
        // getClass().getPackageName() introduced in Java 9 (this project using Java 8)
        return object1.getClass().getPackage().getName().equals(object2.getClass().getPackage().getName());
    }

}
