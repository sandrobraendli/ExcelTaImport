package io.braendli.importer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class ImportForm extends Application {
    private Stage stage;
    @FXML
    private CheckBox deleteOldDataBox;
    @FXML
    private Button importButton;
    @FXML
    private TextField excelField;
    private File excelFile;
    @FXML
    private TextField databaseField;
    private File databaseFile;

    @FXML
    public void handleChooseExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Excel Datei wählen");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel Dateien", "*.xlsx"));
        excelFile = fileChooser.showOpenDialog(stage);
        updateTextField(excelField, excelFile);
    }

    @FXML
    public void handleChooseDatabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Datenbank wählen");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Datenbanken", "*.FDB"));
        databaseFile = fileChooser.showOpenDialog(stage);
        updateTextField(databaseField, databaseFile);
    }

    private void updateTextField(TextField field, File file) {
        field.setText(file != null ? file.getAbsolutePath() : null);
    }

    @FXML
    public boolean filesChosen() {
        return true;
    }

    @FXML
    public void initialize() {
        importButton.disableProperty().bind(excelField.textProperty().isEmpty().or(databaseField.textProperty().isEmpty()));
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/import_form.fxml"));

        Scene scene = new Scene(root, 500, -1);

        stage.setTitle("Excel TA Import");
        stage.setScene(scene);
        stage.show();
    }

    public void importData() {
        System.out.println("Blub");
    }
}
