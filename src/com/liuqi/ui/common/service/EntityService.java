/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.service;

import com.liuqi.ui.common.entity.Entity;

import java.util.List;

/**
 * 实体服务接口
 *
 * @author LiuQI 2018/7/10 19:20
 * @version V1.0
 **/
public interface EntityService {
    void save(Entity entity);
    List<Entity> list();
    Entity getParent(Entity entity);
}
