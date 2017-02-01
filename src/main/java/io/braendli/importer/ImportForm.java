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
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import javax.swing.filechooser.FileSystemView;
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
    @FXML
    private TextField databaseField;

    @FXML
    public void handleChooseExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Excel Datei wählen");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel Dateien", "*.xlsx"));
        updateTextField(excelField, fileChooser.showOpenDialog(stage));
    }

    @FXML
    public void handleChooseDatabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Datenbank wählen");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Datenbanken", "*.FDB"));
        updateTextField(databaseField, fileChooser.showOpenDialog(stage));
    }

    private void updateTextField(TextField field, File file) {
        field.setText(file != null && file.exists() ? file.getAbsolutePath() : null);
    }

    @FXML
    public boolean filesChosen() {
        return true;
    }

    @FXML
    public void initialize() {
        File homeDirectory = FileSystemView.getFileSystemView().getHomeDirectory();
        setIfExists(excelField, new File(System.getProperty("user.home"), "Desktop\\users.xlsx"));
        setIfExists(databaseField, new File("C:\\Program Files\\SafeScan\\TA\\TADATA.FDB"));
        importButton.disableProperty().bind(excelField.textProperty().isEmpty().or(databaseField.textProperty().isEmpty()));
    }

    private void setIfExists(TextField textField, File file) {
        if (file.exists()) {
            textField.setText(file.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/import_form.fxml"));

        Scene scene = new Scene(root, 500, -1);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/app.png")));
        stage.setTitle("Excel TA Import");
        stage.setScene(scene);
        stage.show();
    }

    public void importData() {
        Importer.importToDatabase(
            deleteOldDataBox.isSelected(),
            new File(excelField.getText()),
            new File(databaseField.getText())
        );
    }
}
