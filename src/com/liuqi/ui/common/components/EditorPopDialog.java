package com.liuqi.ui.common.components;

import javafx.collections.ObservableList;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.util.List;

/**
 * 逻辑时的提示下拉框
 *
 * @author LiuQI 2018/8/28 15:41
 * @version V1.0
 **/
public class EditorPopDialog extends Dialog {
    public EditorPopDialog() {
        this.setOnCloseRequest(value -> close());
        this.initModality(Modality.NONE);
        this.initStyle(StageStyle.UNDECORATED);
    }
}
