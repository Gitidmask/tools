/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.components;/**
 * Created by icaru on 2017/8/23.
 */

import javafx.event.EventHandler;
import javafx.scene.control.Button;

/**
 * <p>
 *     按钮封装
 * </p>
 *
 * @Author icaru
 * @Date 2017/8/23 16:22
 * @Version V1.0
 * --------------Modify Logs------------------
 * @Version V1.*
 * @Comments <p></p>
 * @Author icaru
 * @Date 2017/8/23 16:22
 **/
public class LButton extends Button {
    public LButton(String title, EventHandler handler) {
        super(title);
        this.setOnAction(handler);
    }
}
