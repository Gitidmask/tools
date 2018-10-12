package com.liuqi.util;

import com.liuqi.ui.common.entity.Entity;

import java.io.*;
import java.util.List;

/**
 * 对象辅助类
 *
 * @author LiuQI 2018/9/7 10:43
 * @version V1.0
 **/
public class ObjectUtil {
    public static <T> T copy(T t) {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(t);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            return (T) objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
