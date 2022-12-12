package org.springframework.aop.support;

public interface AopAspectjConstant {

    String USE_AOP_ASPECTJ = System.getProperty("isUseAopAspectj");

    boolean IS_USE_AOP_ASPECTJ = USE_AOP_ASPECTJ != null && USE_AOP_ASPECTJ.equals("true");

}
