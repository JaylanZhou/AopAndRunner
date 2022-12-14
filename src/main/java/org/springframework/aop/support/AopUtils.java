/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.support;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.*;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Utility methods for AOP support code.
 *
 * <p>Mainly for internal use within Spring's AOP support.
 *
 * <p>See {@link org.springframework.aop.framework.AopProxyUtils} for a
 * collection of framework-specific AOP utility methods which depend
 * on internals of Spring's AOP framework implementation.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see org.springframework.aop.framework.AopProxyUtils
 */
public abstract class AopUtils {

    private static final ThreadPoolExecutor AOP_ASPECTJ_POOL_EXECUTOR = new ThreadPoolExecutor(10, Integer.MAX_VALUE, 5000, TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>(1000), Thread::new, (r, executor) -> System.out.println("拒绝策略"));

    public static class EmptyAdvisor implements Advisor {

        @Override
        public Advice getAdvice() {
            return new EmptyAdvice();
        }

        @Override
        public boolean isPerInstance() {
            return false;
        }

    }

    public static class EmptyAdvice implements MethodInterceptor {


        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable {
            return methodInvocation.proceed();
        }

    }

    /**
     * Check whether the given object is a JDK dynamic proxy or a CGLIB proxy.
     * <p>This method additionally checks if the given object is an instance
     * of {@link SpringProxy}.
     * @param object the object to check
     * @see #isJdkDynamicProxy
     * @see #isCglibProxy
     */
    public static boolean isAopProxy(@Nullable Object object) {
        return (object instanceof SpringProxy &&
                (Proxy.isProxyClass(object.getClass()) || ClassUtils.isCglibProxyClass(object.getClass())));
    }

    /**
     * Check whether the given object is a JDK dynamic proxy.
     * <p>This method goes beyond the implementation of
     * {@link Proxy#isProxyClass(Class)} by additionally checking if the
     * given object is an instance of {@link SpringProxy}.
     * @param object the object to check
     * @see Proxy#isProxyClass
     */
    public static boolean isJdkDynamicProxy(@Nullable Object object) {
        return (object instanceof SpringProxy && Proxy.isProxyClass(object.getClass()));
    }

    /**
     * Check whether the given object is a CGLIB proxy.
     * <p>This method goes beyond the implementation of
     * {@link ClassUtils#isCglibProxy(Object)} by additionally checking if
     * the given object is an instance of {@link SpringProxy}.
     * @param object the object to check
     * @see ClassUtils#isCglibProxy(Object)
     */
    public static boolean isCglibProxy(@Nullable Object object) {
        return (object instanceof SpringProxy && ClassUtils.isCglibProxy(object));
    }

    /**
     * Determine the target class of the given bean instance which might be an AOP proxy.
     * <p>Returns the target class for an AOP proxy or the plain class otherwise.
     * @param candidate the instance to check (might be an AOP proxy)
     * @return the target class (or the plain class of the given object as fallback;
     * never {@code null})
     * @see TargetClassAware#getTargetClass()
     * @see org.springframework.aop.framework.AopProxyUtils#ultimateTargetClass(Object)
     */
    public static Class<?> getTargetClass(Object candidate) {
        Assert.notNull(candidate, "Candidate object must not be null");
        Class<?> result = null;
        if (candidate instanceof TargetClassAware) {
            result = ((TargetClassAware) candidate).getTargetClass();
        }
        if (result == null) {
            result = (isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
        }
        return result;
    }

    /**
     * Select an invocable method on the target type: either the given method itself
     * if actually exposed on the target type, or otherwise a corresponding method
     * on one of the target type's interfaces or on the target type itself.
     * @param method the method to check
     * @param targetType the target type to search methods on (typically an AOP proxy)
     * @return a corresponding invocable method on the target type
     * @throws IllegalStateException if the given method is not invocable on the given
     * target type (typically due to a proxy mismatch)
     * @since 4.3
     * @see MethodIntrospector#selectInvocableMethod(Method, Class)
     */
    public static Method selectInvocableMethod(Method method, @Nullable Class<?> targetType) {
        if (targetType == null) {
            return method;
        }
        Method methodToUse = MethodIntrospector.selectInvocableMethod(method, targetType);
        if (Modifier.isPrivate(methodToUse.getModifiers()) && !Modifier.isStatic(methodToUse.getModifiers()) &&
                SpringProxy.class.isAssignableFrom(targetType)) {
            throw new IllegalStateException(String.format(
                    "Need to invoke method '%s' found on proxy for target class '%s' but cannot " +
                            "be delegated to target bean. Switch its visibility to package or protected.",
                    method.getName(), method.getDeclaringClass().getSimpleName()));
        }
        return methodToUse;
    }

    /**
     * Determine whether the given method is an "equals" method.
     * @see Object#equals
     */
    public static boolean isEqualsMethod(@Nullable Method method) {
        return ReflectionUtils.isEqualsMethod(method);
    }

    /**
     * Determine whether the given method is a "hashCode" method.
     * @see Object#hashCode
     */
    public static boolean isHashCodeMethod(@Nullable Method method) {
        return ReflectionUtils.isHashCodeMethod(method);
    }

    /**
     * Determine whether the given method is a "toString" method.
     * @see Object#toString()
     */
    public static boolean isToStringMethod(@Nullable Method method) {
        return ReflectionUtils.isToStringMethod(method);
    }

    /**
     * Determine whether the given method is a "finalize" method.
     * @see Object#finalize()
     */
    public static boolean isFinalizeMethod(@Nullable Method method) {
        return (method != null && method.getName().equals("finalize") &&
                method.getParameterCount() == 0);
    }

    /**
     * Given a method, which may come from an interface, and a target class used
     * in the current AOP invocation, find the corresponding target method if there
     * is one. E.g. the method may be {@code IFoo.bar()} and the target class
     * may be {@code DefaultFoo}. In this case, the method may be
     * {@code DefaultFoo.bar()}. This enables attributes on that method to be found.
     * <p><b>NOTE:</b> In contrast to {@link ClassUtils#getMostSpecificMethod},
     * this method resolves Java 5 bridge methods in order to retrieve attributes
     * from the <i>original</i> method definition.
     * @param method the method to be invoked, which may come from an interface
     * @param targetClass the target class for the current invocation.
     * May be {@code null} or may not even implement the method.
     * @return the specific target method, or the original method if the
     * {@code targetClass} doesn't implement it or is {@code null}
     * @see ClassUtils#getMostSpecificMethod
     */
    public static Method getMostSpecificMethod(Method method, @Nullable Class<?> targetClass) {
        Class<?> specificTargetClass = (targetClass != null ? ClassUtils.getUserClass(targetClass) : null);
        Method resolvedMethod = ClassUtils.getMostSpecificMethod(method, specificTargetClass);
        // If we are dealing with method with generic parameters, find the original method.
        return BridgeMethodResolver.findBridgedMethod(resolvedMethod);
    }

    /**
     * Can the given pointcut apply at all on the given class?
     * <p>This is an important test as it can be used to optimize
     * out a pointcut for a class.
     * @param pc the static or dynamic pointcut to check
     * @param targetClass the class to test
     * @return whether the pointcut can apply on any method
     */
    public static boolean canApply(Pointcut pc, Class<?> targetClass) {
        return canApply(pc, targetClass, false);
    }

    /**
     * Can the given pointcut apply at all on the given class?
     * <p>This is an important test as it can be used to optimize
     * out a pointcut for a class.
     * @param pc the static or dynamic pointcut to check
     * @param targetClass the class to test
     * @param hasIntroductions whether or not the advisor chain
     * for this bean includes any introductions
     * @return whether the pointcut can apply on any method
     */
    public static boolean canApply(Pointcut pc, Class<?> targetClass, boolean hasIntroductions) {
        Assert.notNull(pc, "Pointcut must not be null");
        if (!pc.getClassFilter().matches(targetClass)) {
            return false;
        }

        MethodMatcher methodMatcher = pc.getMethodMatcher();
        if (methodMatcher == MethodMatcher.TRUE) {
            // No need to iterate the methods if we're matching any method anyway...
            return true;
        }

        IntroductionAwareMethodMatcher introductionAwareMethodMatcher = null;
        if (methodMatcher instanceof IntroductionAwareMethodMatcher) {
            introductionAwareMethodMatcher = (IntroductionAwareMethodMatcher) methodMatcher;
        }

        Set<Class<?>> classes = new LinkedHashSet<>();
        if (!Proxy.isProxyClass(targetClass)) {
            classes.add(ClassUtils.getUserClass(targetClass));
        }
        classes.addAll(ClassUtils.getAllInterfacesForClassAsSet(targetClass));

        for (Class<?> clazz : classes) {
            Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);
            for (Method method : methods) {
                if (introductionAwareMethodMatcher != null ?
                        introductionAwareMethodMatcher.matches(method, targetClass, hasIntroductions) :
                        methodMatcher.matches(method, targetClass)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Can the given advisor apply at all on the given class?
     * This is an important test as it can be used to optimize
     * out a advisor for a class.
     * @param advisor the advisor to check
     * @param targetClass class we're testing
     * @return whether the pointcut can apply on any method
     */
    public static boolean canApply(Advisor advisor, Class<?> targetClass) {
        return canApply(advisor, targetClass, false);
    }

    public static boolean compiledByAjc(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().startsWith("ajc$")) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAspectAnnotation(Class<?> clazz) {
        return (AnnotationUtils.findAnnotation(clazz, Aspect.class) != null);
    }

    /**
     * Can the given advisor apply at all on the given class?
     * <p>This is an important test as it can be used to optimize out a advisor for a class.
     * This version also takes into account introductions (for IntroductionAwareMethodMatchers).
     * @param advisor the advisor to check
     * @param targetClass class we're testing
     * @param hasIntroductions whether or not the advisor chain for this bean includes
     * any introductions
     * @return whether the pointcut can apply on any method
     */

    public static boolean canApply(Advisor advisor, Class<?> targetClass, boolean hasIntroductions) {
        if (advisor instanceof IntroductionAdvisor) {
            return ((IntroductionAdvisor) advisor).getClassFilter().matches(targetClass);
        }
        else if (advisor instanceof PointcutAdvisor) {
            PointcutAdvisor pca = (PointcutAdvisor) advisor;
            return canApply(pca.getPointcut(), targetClass, hasIntroductions);
        }
        else {
            // It doesn't have a pointcut so we assume it applies.
            return true;
        }
    }

    public static Map<String, Long> aopTime = new ConcurrentHashMap<>();
    /**
     * Determine the sublist of the {@code candidateAdvisors} list
     * that is applicable to the given class.
     * @param candidateAdvisors the Advisors to evaluate
     * @param clazz the target class
     * @return sublist of Advisors that can apply to an object of the given class
     * (may be the incoming List as-is)
     */
    public static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> clazz) {
        if (AopAspectjConstant.IS_USE_AOP_ASPECTJ) {
            List<Advisor> eligibleAdvisors = new ArrayList<>();
            if (compiledByAjc(clazz) && !Modifier.isFinal(clazz.getModifiers())) {
                eligibleAdvisors.add(new EmptyAdvisor());
            }
            if (candidateAdvisors.isEmpty()) {
                return eligibleAdvisors;
            }
            List<Future<Object>> subTaskList = new ArrayList<>();
            for (Advisor candidate : candidateAdvisors) {
                Future<Object> subTask = AOP_ASPECTJ_POOL_EXECUTOR.submit(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        if (candidate instanceof IntroductionAdvisor && canApply(candidate, clazz)) {
                            eligibleAdvisors.add(candidate);
                        }
                        return null;
                    }
                });
                subTaskList.add(subTask);
            }
            for (Future<Object> objectFuture : subTaskList) {
                try {
                    objectFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            subTaskList.clear();
            boolean hasIntroductions = !eligibleAdvisors.isEmpty();
            for (Advisor candidate : candidateAdvisors) {
                Future<Object> subTask = AOP_ASPECTJ_POOL_EXECUTOR.submit(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        if (candidate instanceof IntroductionAdvisor) {
                            // already processed
                            return null;
                        }
                        if (canApply(candidate, clazz, hasIntroductions)) {
                            eligibleAdvisors.add(candidate);
                        }
                        return null;
                    }
                });
                subTaskList.add(subTask);
            }
            for (Future<Object> objectFuture : subTaskList) {
                try {
                    objectFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            return eligibleAdvisors;
        } else {
            if (candidateAdvisors.isEmpty()) {
                return candidateAdvisors;
            }
            List<Advisor> eligibleAdvisors = new ArrayList<>();
            for (Advisor candidate : candidateAdvisors) {
                if (candidate instanceof IntroductionAdvisor && canApply(candidate, clazz)) {
                    eligibleAdvisors.add(candidate);
                }
            }
            boolean hasIntroductions = !eligibleAdvisors.isEmpty();
            for (Advisor candidate : candidateAdvisors) {
                if (candidate instanceof IntroductionAdvisor) {
                    continue;
                }
                if (canApply(candidate, clazz, hasIntroductions)) {
                    eligibleAdvisors.add(candidate);
                }

            }
            return eligibleAdvisors;
        }
    }

    /**
     * Invoke the given target via reflection, as part of an AOP method invocation.
     * @param target the target object
     * @param method the method to invoke
     * @param args the arguments for the method
     * @return the invocation result, if any
     * @throws Throwable if thrown by the target method
     * @throws AopInvocationException in case of a reflection error
     */
    @Nullable
    public static Object invokeJoinpointUsingReflection(@Nullable Object target, Method method, Object[] args)
            throws Throwable {

        // Use reflection to invoke the method.
        try {
            ReflectionUtils.makeAccessible(method);
            return method.invoke(target, args);
        }
        catch (InvocationTargetException ex) {
            // Invoked method threw a checked exception.
            // We must rethrow it. The client won't see the interceptor.
            throw ex.getTargetException();
        }
        catch (IllegalArgumentException ex) {
            throw new AopInvocationException("AOP configuration seems to be invalid: tried calling method [" +
                    method + "] on target [" + target + "]", ex);
        }
        catch (IllegalAccessException ex) {
            throw new AopInvocationException("Could not access method [" + method + "]", ex);
        }
    }

}
