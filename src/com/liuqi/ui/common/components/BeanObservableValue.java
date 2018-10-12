/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.components;

import javafx.beans.value.ObservableValueBase;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 可监控实体类
 *
 * @author qi.liu
 * @param <T>
 * @param <P>
 */
public class BeanObservableValue <T, P> extends ObservableValueBase<T> {
    private P bean;
    private String property;

    public BeanObservableValue(String property, P value) {
        this.bean = value;
        this.property = property;
    }

    @Override
    public T getValue() {
        if (null == bean) {
            return null;
        }

        if (bean instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, T> map = (Map<String, T>) bean;
            return map.get(property);
        }

        //从BEAN中读取指定属性的值并返回
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) bean.getClass();

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

            for (PropertyDescriptor pd: pds) {
                String name = pd.getName();

                if (!name.equals(property)) {
                    continue;
                }

                Method method = pd.getReadMethod();
                Object value = method.invoke(bean);

                if (method.getReturnType().equals(String.class)  && null == value) {
                    //字符串时，如果返回值为空，则替换成空字符串
                    value = "";
                }

                //noinspection unchecked
                return (T) value;
            }
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }
}
