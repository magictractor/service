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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.magictractor.util.exception.ExceptionUtil;
import uk.co.magictractor.util.exception.ExceptionUtil.RunnableWithThrowable;

/**
 * <p>
 * Service implementations are expected to be located in the same package as
 * each other and have no-args constructors.
 * </p>
 * <p>
 * The intention of this is to provide a general use case where an instance of
 * one service is created by application code, and elements returned by that
 * service can use this registry to get related data from compatible services.
 * For example, Troops can use this to link to a Kingdom element.
 * </p>
 */
public final class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private static final Comparator<Class> COMPARATOR_CLASS_NAME = Comparator.comparing(Class::getName);

    // Keyed by class name.
    private static Map<String, Object> SERVICE_IMPLEMENTATIONS = new HashMap<>();
    // TODO! ThreadLocal
    // TODO! alt strategies?
    private static String implementationPackage;

    private ServiceRegistry() {
    }

    public static <INTERFACE, IMPLEMENTATION extends INTERFACE> void runWithSiblingServices(
            Class<IMPLEMENTATION> serviceImplementationType, RunnableWithThrowable runnable) {
        String servicePrefix = determineClassNamePrefix(serviceImplementationType);
        runWithSiblingServices(servicePrefix, runnable);
    }

    public static <IMPLEMENTATION> void runWithSiblingServices(String implPackage, RunnableWithThrowable runnable) {

        String previousPrefix = implementationPackage;

        try {
            implementationPackage = implPackage;
            ExceptionUtil.call(() -> runnable.run());
        }
        finally {
            implementationPackage = previousPrefix;
        }
    }

    public static String getServicePackage() {
        if (implementationPackage == null) {
            throw new IllegalStateException();
        }
        return implementationPackage;
    }

    @SuppressWarnings("unchecked")
    private static <INTERFACE, IMPLEMENTATION extends INTERFACE> Class<INTERFACE> findServiceInterface(
            Class<IMPLEMENTATION> serviceImplementationType) {
        // Use TreeSet so that class names are consistently ordered in error messages. Gives the first() method too.
        TreeSet<Class<?>> interfaces = new TreeSet<>(COMPARATOR_CLASS_NAME);
        addServiceInterfaces(interfaces, serviceImplementationType);

        if (interfaces.isEmpty()) {
            // throw new IllegalArgumentException(
            //     "Service interface not found for implementation: " + serviceImplementationType.getName());
            // Allowed now for auxilliary services used by another service implementation
            // Maybe log?
            return null;
        }

        if (interfaces.size() > 1) {
            throw new IllegalArgumentException(
                "Multiple service interfaces  found for implementation: " + serviceImplementationType.getName());
        }

        return (Class<INTERFACE>) interfaces.first();
    }

    private static <IMPLEMENTATION> void addServiceInterfaces(Set<Class<?>> interfaces, Class<?> serviceImplementationType) {

        for (Class<?> candidate : serviceImplementationType.getInterfaces()) {
            if (candidate.getSimpleName().startsWith("Has") && Character.isUpperCase(candidate.getSimpleName().charAt(3))) {
                // Allowed now. For example, some implementations might use a
                // HasPrimaryKeyService interface that does not belong on a common interface.
                break;
            }

            if (SearchableService.class.isAssignableFrom(candidate)
                    && !SearchableService.class.equals(candidate)) {
                interfaces.add(candidate);
            }
        }

        if (serviceImplementationType.getSuperclass() != null) {
            addServiceInterfaces(interfaces, serviceImplementationType.getSuperclass());
        }
    }

    private static <IMPLEMENTATION> String determineClassNamePrefix(
            Class<IMPLEMENTATION> serviceImplementationType) {

        Class<IMPLEMENTATION> serviceInterface = findServiceInterface(serviceImplementationType);
        if (serviceInterface == null) {
            return null;
        }
        String interfaceName = serviceInterface.getSimpleName();

        String implementationFullName = serviceImplementationType.getName();
        if (!implementationFullName.endsWith(interfaceName)) {
            throw new IllegalArgumentException("Expected service implementation name "
                    + implementationFullName
                    + " to end with the interface name "
                    + interfaceName);
        }

        int prefixLength = implementationFullName.length() - interfaceName.length();
        return implementationFullName.substring(0, prefixLength);

        //implementationClassNamePrefix = implementationFullName.substring(0, prefixLength);

        //LOGGER.debug("Initialised class name prefix: {}", implementationClassNamePrefix);
    }

    // Application code should call static getOrCreate() methods on the service interfaces.
    public static <INTERFACE> INTERFACE getOrCreate(Class<INTERFACE> serviceInterface) {
        assertServiceInterface(serviceInterface);

        String implementationClassName = getImplementationClassName(serviceInterface);
        //        @SuppressWarnings("unchecked")
        //        INTERFACE serviceImplementation = (INTERFACE) SERVICE_IMPLEMENTATIONS.get(implementationClassName);
        //        if (serviceImplementation == null) {
        //            LOGGER.debug("Creating implementation for service interface: {}", serviceInterface.getName());
        //            serviceImplementation = create(implementationClassName);
        //            SERVICE_IMPLEMENTATIONS.put(implementationClassName, serviceImplementation);
        //        }
        //
        //        return serviceImplementation;
        return getOrCreateImplementation(implementationClassName);
    }

    public static <IMPLEMENTATION> IMPLEMENTATION getOrCreateImplementation(Class<IMPLEMENTATION> serviceImplementationType) {
        if (serviceImplementationType.isInterface()) {
            throw new IllegalStateException("Parameter must be a concrete implementation of a service, not an interface");
        }
        return getOrCreateImplementation(serviceImplementationType.getName());
    }

    public static <IMPLEMENTATION, SIBLING> SIBLING getOrCreateSibling(IMPLEMENTATION serviceImplementation, Class<SIBLING> serviceInterface) {
        String implementationClassName = determineClassNamePrefix(serviceImplementation.getClass())
                + serviceInterface.getSimpleName();
        return getOrCreateImplementation(implementationClassName);
    }

    //    public static <SIBLING, INTERFACE> INTERFACE getOrCreateSibling(Class<SIBLING> siblingImplementationType, Class<INTERFACE> serviceInterface) {
    //        String implementationClassName = determineClassNamePrefix(siblingImplementationType)
    //                + serviceInterface.getSimpleName();
    //        return getOrCreateImplementation(implementationClassName);
    //    }

    private static <IMPLEMENTATION> IMPLEMENTATION getOrCreateImplementation(String implementationClassName) {
        // assertServiceInterface(serviceInterface);

        @SuppressWarnings("unchecked")
        IMPLEMENTATION serviceImplementation = (IMPLEMENTATION) SERVICE_IMPLEMENTATIONS.get(implementationClassName);
        if (serviceImplementation == null) {
            // LOGGER.debug("Creating implementation for service interface: {}", serviceInterface.getName());
            serviceImplementation = create(implementationClassName);
            SERVICE_IMPLEMENTATIONS.put(implementationClassName, serviceImplementation);
        }

        return serviceImplementation;
    }

    private static <IMPLEMENTATION> IMPLEMENTATION create(String implementationClassName) {
        try {
            return create0(implementationClassName);
        }
        catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static <IMPLEMENTATION> IMPLEMENTATION create0(String implementationClassName) throws ReflectiveOperationException {
        @SuppressWarnings("unchecked")
        Class<IMPLEMENTATION> implementationClass = (Class<IMPLEMENTATION>) Class.forName(implementationClassName);

        String servicePrefix = determineClassNamePrefix(implementationClass);
        List<IMPLEMENTATION> implementations = new ArrayList<>(1);
        runWithSiblingServices(servicePrefix, () -> {
            IMPLEMENTATION implementation = implementationClass.getDeclaredConstructor().newInstance();
            // TODO! bin this (use refs instead)
            // populateBeans0(implementation.all());
            implementations.add(implementation);
        });

        return implementations.get(0);
    }

    private static <INTERFACE> String getImplementationClassName(Class<INTERFACE> serviceInterface) {
        assertServiceInterface(serviceInterface);
        if (implementationPackage == null) {
            throw new IllegalStateException(
                "Service package unknown for "
                        + serviceInterface.getSimpleName()
                        + ". Services should be accessed within calls to runWithSiblingServices().");
        }

        return implementationPackage + serviceInterface.getSimpleName();
    }

    private static <INTERFACE> void assertServiceInterface(Class<INTERFACE> serviceInterface) {
        if (!serviceInterface.isInterface()) {
            throw new IllegalStateException("Expected a service interface, but " + serviceInterface.getName() + " is an implementation");
        }
    }

}
