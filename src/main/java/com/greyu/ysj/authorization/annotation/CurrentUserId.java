package com.greyu.ysj.authorization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description:  Use this annotation in the method parameters of the Controller, which will inject the currently logged in userId when mapping.
 * @See: com.greyu.ysj.authorization.resolvers.CurrentUserIdMethodArgumentResolver
 * @Author: gre_yu@163.com
 * @Date: Created in 0:57 2018/2/1
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}
