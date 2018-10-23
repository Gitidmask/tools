package com.liuqi.tool.other;

import com.liuqi.util.CronSequenceGenerator;
import com.liuqi.util.DateUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

/**
 * 其它工具面板
 *
 * @author LiuQI 2018/10/19 17:05
 * @version V1.0
 **/
public class OtherToolPanel extends VBox {

    public OtherToolPanel() {
        addCronTool();

        addLongToDate();
    }

    private void addNormalTool(
            String title, String inputTitle,
            Function<String, String> function) {
        HBox hBox = createHBox();
        hBox.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        Label titleLabel = new Label(title);
        Label inputTitleLabel = new Label(inputTitle);
        Label resultTitleLabel = new Label("结果：");
        TextField inputField = new TextField();
        Label resultLabel = new Label();

        titleLabel.setPrefWidth(200);
        inputTitleLabel.setPrefWidth(100);
        resultTitleLabel.setPrefWidth(100);
        inputField.setPrefWidth(200);

        resultLabel.setStyle("-fx-text-fill: blue; ");

        hBox.getChildren().addAll(titleLabel, inputTitleLabel, inputField, resultTitleLabel, resultLabel);

        inputField.setOnAction(e -> {
            String str = inputField.getText().trim();
            if ("".equals(str)) {
                return;
            }

           resultLabel.setText(function.apply(str));
        });
    }

    /**
     * 添加Long类型的数字转换成日期的工具
     */
    private void addLongToDate() {
        this.addNormalTool("Long转日期工具", "数字：", str -> {
            try {
                long value = Long.parseLong(str);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(value);
                return DateUtils.formatTime(calendar.getTime());
            } catch (Exception ex) {
                return "不是有效的数字";
            }
        });
    }

    /**
     * 添加Cron表格式校验工具
     */
    private void addCronTool() {
        this.addNormalTool("Cron表达式校验工具", "Cron表格式：", str -> {
            StringBuilder resultStr = new StringBuilder();
            if (!CronSequenceGenerator.isValidExpression(str)) {
                resultStr.append("表达式格式不正确！");
            } else {
                resultStr.append("下次执行时间：");
                CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(str);
                Date date = new Date();
                resultStr.append(DateUtils.formatTime(cronSequenceGenerator.next(date)));
            }

            return resultStr.toString();
        });
    }

    /**
     * 增加HBox到界面中
     */
    private HBox createHBox() {
        HBox hBox = new HBox();
        this.getChildren().addAll(hBox, new Separator());

        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10));
        hBox.setAlignment(Pos.CENTER_LEFT);
        return hBox;
    }
}
