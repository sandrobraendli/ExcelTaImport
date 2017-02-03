package io.braendli.importer;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.controlsfx.control.NotificationPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ImportForm extends Application {
    private static final Logger LOG = LoggerFactory.getLogger(ImportForm.class);

    private Stage stage;
    private NotificationPane notification;
    @FXML
    private GridPane mainPane;
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
        if (file != null && file.exists()) {
            field.setText(file.getAbsolutePath());
        }
    }

    @FXML
    public void initialize() {
        updateTextField(excelField, new File(System.getProperty("user.home"), "Desktop\\users.xlsx"));
        updateTextField(excelField, new File("users.xlsx"));
        updateTextField(databaseField, new File("C:\\Program Files\\SafeScan\\TA\\TADATA.FDB"));
        importButton.disableProperty().bind(excelField.textProperty().isEmpty().or(databaseField.textProperty().isEmpty()));
    }

    public static void main(String[] args) throws ClassNotFoundException {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/import_form.fxml"));
        Parent root = loader.load();
        NotificationPane notification = new NotificationPane(root);
        ((ImportForm) loader.getController()).setUp(stage, notification);
        Scene scene = new Scene(notification, 500, -1);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/app.png")));
        stage.setTitle("Excel TA Import");
        stage.setScene(scene);
        stage.show();
    }

    private void setUp(Stage stage, NotificationPane notification) {
        this.stage = stage;
        this.notification = notification;
    }

    public void importData() {
        LOG.info("Starting import");
        try {
            Importer.importToDatabase(
                    deleteOldDataBox.isSelected(),
                    new File(excelField.getText()),
                    new File(databaseField.getText())
            );
            notification.setText("Import erfolgreich abgeschlossen");
            LOG.info("Import successful");
        } catch (Exception e) {
            notification.setText("Der Import ist fehlgeschlagen");
            LOG.error("Import failed", e);
        } finally {
            notification.show();
        }
    }
}
