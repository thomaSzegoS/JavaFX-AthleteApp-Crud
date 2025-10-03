package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class Mainapp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Mainapp.class.getResource("CrudAthletes.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("Athlete CRUD App");
        stage.show();

        // Τύπωσε το περιεχόμενο του Structure CSS
        /*
        try (InputStream cssStream = Mainapp.class.getResourceAsStream("/com/example/demo/Structure.css")) {
            if (cssStream != null) {
                String cssContent = new String(cssStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                System.out.println(cssContent);
            } else {
                System.out.println("Structure CSS file not found!");
            }
        }
        */
    }
}
