
package com.meteormath.admin;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        Label mensagem = new Label(
            "MeteorMath Admin - JavaFX funcionando!"
        );

        StackPane layout = new StackPane(mensagem);

        Scene scene = new Scene(layout, 800, 500);

        stage.setTitle("MeteorMath Admin");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
