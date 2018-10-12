/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.entity;

import java.io.Serializable;
import java.util.HashMap;

/**
 * .
 *
 * @author LiuQI 2018/7/10 19:33
 * @version V1.0
 **/
public class Entity extends HashMap<String, Object> implements Comparable<Entity> {
    @Override
    public int compareTo(Entity o) {
        Object status1 = this.getOrDefault("status", "false");
        Object status2 = o.getOrDefault("status", "false");

        boolean b1 = Boolean.parseBoolean(status1.toString());
        boolean b2 = Boolean.parseBoolean(status2.toString());

        if (b1 == b2) {
            String planDate1 = this.getOrDefault("planDate", "").toString();
            String planDate2 = o.getOrDefault("planDate", "").toString();

            int result = planDate1.compareTo(planDate2);

            if (0 != result) {
                return result;
            }

            // 计划时间一样时，根据添加时间排序
            String addDate1 = this.getOrDefault("addDate", "").toString();
            String addDate2 = o.getOrDefault("addDate", "").toString();

            return addDate1.compareTo(addDate2);
        } else if (b1) {
            return 1;
        } else {
            return -1;
        }
    }
}
