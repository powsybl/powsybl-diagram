package com.powsybl.sld.view.app;

import java.util.List;

import com.powsybl.sld.util.RGBColor;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TestColor extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Test colors");

        FlowPane flow = new FlowPane(Orientation.VERTICAL);
        flow.setPrefWrapLength(200);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        flow.getChildren().add(grid);
        Scene scene = new Scene(flow, 600, 800);

        primaryStage.setScene(scene);
        Label l1 = new Label();
        l1.setText("Base Color");
        TextField basecolor = new TextField();

        Label l2 = new Label();
        l2.setText("Steps");
        TextField num = new TextField();

        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);

        Button btn = new Button();
        btn.setText("Show gradient");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                RGBColor color = RGBColor.parse(basecolor.getText());
                String colNum = num.getText();
                int cn = Integer.valueOf(colNum);

                List<RGBColor> colors0 = color.getColorGradient(cn);
                List<RGBColor> colors1 = color.getColorGradient1(cn);
                List<RGBColor> colors2 = color.getColorGradient2(cn);
                List<RGBColor> colors3 = color.getColorGradient3(cn);

                pane.getChildren().clear();

                for (int i = 0; i < cn; i++) {
                    Label l0 = new Label();
                    l0.setMinWidth(100);
                    l0.setTextFill(Color.web("#FFFFFF"));
                    l0.setBackground(new Background(new BackgroundFill(Color.web(colors0.get(i).toString()), CornerRadii.EMPTY, Insets.EMPTY)));
                    l0.setText(colors0.get(i).toString());
                    pane.add(l0, 0, i);

                    Label l1 = new Label();
                    l1.setMinWidth(100);
                    l1.setTextFill(Color.web("#FFFFFF"));
                    l1.setBackground(new Background(new BackgroundFill(Color.web(colors1.get(i).toString()), CornerRadii.EMPTY, Insets.EMPTY)));
                    l1.setText(colors1.get(i).toString());
                    pane.add(l1, 1, i);
                    Label l2 = new Label();
                    l2.setMinWidth(100);
                    l2.setTextFill(Color.web("#FFFFFF"));
                    l2.setBackground(new Background(new BackgroundFill(Color.web(colors2.get(i).toString()), CornerRadii.EMPTY, Insets.EMPTY)));
                    l2.setText(colors2.get(i).toString());
                    pane.add(l2, 2, i);
                    Label l3 = new Label();
                    l3.setMinWidth(100);
                    l3.setTextFill(Color.web("#FFFFFF"));
                    l3.setBackground(new Background(new BackgroundFill(Color.web(colors3.get(i).toString()), CornerRadii.EMPTY, Insets.EMPTY)));
                    l3.setText(colors3.get(i).toString());
                    pane.add(l3, 3, i);
                }
            }
        });
        grid.add(l1, 0, 1);
        grid.add(basecolor, 1, 1);
        grid.add(l2, 0, 2);

        grid.add(num, 1, 2);
        grid.add(btn, 2, 2);
        flow.getChildren().add(pane);

        primaryStage.show();
    }
}
