package io.braendli.importer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ImportForm extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/import_form.fxml"));

        Scene scene = new Scene(root, 350, -1);

        stage.setTitle("Excel TA Import");
        stage.setScene(scene);
        stage.show();
    }
}
