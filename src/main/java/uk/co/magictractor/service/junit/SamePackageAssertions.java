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
import java.util.function.Predicate;

public final class SamePackageAssertions {

    private SamePackageAssertions() {
    }

    /**
     * <p>
     * Use reflection to find methods that return Objects that are then checked
     * to see if they are in the same package as the given Object.
     * <p>
     * <p>
     * Handles some common indirection, such as methods returning Lists, the
     * elements of which are checked to see if they are in the same package. But
     * this method won't handle non-trivial values, in which case XXX can be
     * used instead.
     * </p>
     */
    public static void assertGetterValuesAreInSamePackage(Object object) {
        assertGetterValuesAreInSamePackage(object, (method) -> false);
    }

    public static void assertGetterValuesAreInSamePackage(Object object, Predicate<Method> excludeMethods) {
        StringBuilder failBuilder = new StringBuilder();

        assertGetterValuesAreInSamePackage(failBuilder, "", object, excludeMethods);

        if (failBuilder.length() != 0) {
            fail("Objects returned from getters are not in the same package as the base Object:" + object.getClass().getName() + "\n" + failBuilder.toString());
        }
    }

    private static void assertGetterValuesAreInSamePackage(StringBuilder failBuilder, String context, Object object, Predicate<Method> excludeMethods) {
        List<Method> methods = findGetterMethods(object, excludeMethods);

        for (Method method : methods) {
            Object other;
            try {
                other = method.invoke(object, new Object[] {});
            }
            catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }

            if (other instanceof List) {
                List<?> otherList = (List<?>) other;
                for (int i = 0; i < otherList.size(); i++) {
                    checkAndAppendFailures(failBuilder, method.getName() + "().get(" + i + ")", object, otherList.get(i));
                }
            }
            else {
                checkAndAppendFailures(failBuilder, method.getName() + "()", object, other);
            }
        }

    }

    /** Find public methods with no args that return a non-primitive value. */
    // TODO! apply the predicate to filter some methods
    private static List<Method> findGetterMethods(Object object, Predicate<Method> excludeMethods) {
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
            }
        }

        return result;
    }

    private static boolean checkAndAppendFailures(StringBuilder failBuilder, String context, Object object, Object other) {
        boolean areInSamePackage = areInSamePackage(object, other);
        if (!areInSamePackage) {
            failBuilder.append(context);
            failBuilder.append(" -> ");
            failBuilder.append(other == null ? null : other.getClass().getName());
            failBuilder.append('\n');
        }

        return areInSamePackage;
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
    public static void assertInSamePackage(Object object1, Object object2) {
        if (!areInSamePackage(object1, object2)) {
            // Use AssertionError directly rather than via a fail() method to avoid a dependency on JUnit.
            fail("Objects were expected to be in the same package, but they are not: "
                    + object1.getClass().getCanonicalName()
                    + " and "
                    + object2.getClass().getCanonicalName());
        }
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }

    private static boolean areInSamePackage(Object object1, Object object2) {
        if (object1 == null) {
            throw new IllegalStateException();
        }

        //        if (object1 == null || object2 == null) {
        //            return object1 == object2;
        //        }
        if (object2 == null) {
            return false;
        }

        // getClass().getPackageName() introduced in Java 9 (this project using Java 8)
        return object1.getClass().getPackage().getName().equals(object2.getClass().getPackage().getName());
    }

}
