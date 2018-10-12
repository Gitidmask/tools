/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.util;/**
 * Created by icaru on 2017/7/21.
 */

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * <p>
 * </p>
 *
 * @Author icaru
 * @Date 2017/7/21 11:17
 * @Version V1.0
 * --------------Modify Logs------------------
 * @Version V1.*
 * @Comments <p></p>
 * @Author icaru
 * @Date 2017/7/21 11:17
 **/
public class ReflectUtil {
    public static <T, P> P getBeanValue(T t, String property) {
        if (t instanceof Map) {
            Map<String, P> map = (Map<String, P>) t;
            return map.get(property);
        }

        try {

            BeanInfo beanInfo = Introspector.getBeanInfo(t.getClass());
            PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor prop : props) {
                if (!prop.getName().equals(property)) {
                    continue;
                }

                Method method = prop.getReadMethod();
                Object obj = method.invoke(t);

                return (P) obj;
            }
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T, P> void setBeanValue(T t, String property, P value) {
        if (t instanceof Map) {
            Map<String, P> map = (Map<String, P>) t;
            map.put(property, value);
            return;
        }

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(t.getClass());
            PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor prop : props) {
                if (!prop.getName().equals(property)) {
                    continue;
                }

                Method method = prop.getWriteMethod();
                method.invoke(t, value);
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
