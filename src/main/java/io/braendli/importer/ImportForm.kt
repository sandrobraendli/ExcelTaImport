package io.braendli.importer

import javafx.application.Application
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Stage
import org.controlsfx.control.NotificationPane
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

class ImportForm : Application() {
    private lateinit var stage: Stage
    private lateinit var notification: NotificationPane
    @FXML
    private lateinit var deleteOldDataBox: CheckBox
    @FXML
    private lateinit var importButton: Button
    @FXML
    private lateinit var excelField: TextField
    @FXML
    private lateinit var databaseField: TextField

    companion object {
        private val LOG = LoggerFactory.getLogger(ImportForm::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(ImportForm::class.java)
        }
    }

    @FXML
    fun handleChooseExcel() {
        val fileChooser = FileChooser()
        fileChooser.title = "Excel Datei wählen"
        fileChooser.extensionFilters.add(ExtensionFilter("Excel Dateien", "*.xlsx"))
        updateTextField(excelField, fileChooser.showOpenDialog(stage))
    }

    @FXML
    fun handleChooseDatabase() {
        val fileChooser = FileChooser()
        fileChooser.title = "Datenbank wählen"
        fileChooser.extensionFilters.add(ExtensionFilter("Datenbanken", "*.FDB"))
        updateTextField(databaseField, fileChooser.showOpenDialog(stage))
    }

    private fun updateTextField(field: TextField, file: File?) {
        if (file != null && file.exists()) {
            field.text = file.absolutePath
        }
    }

    @FXML
    fun initialize() {
        updateTextField(excelField, File(System.getProperty("user.home"), "Desktop\\users.xlsx"))
        updateTextField(excelField, File("users.xlsx"))
        updateTextField(databaseField, File("C:\\Program Files\\SafeScan\\TA\\TADATA.FDB"))
        importButton.disableProperty().bind(excelField.textProperty().isEmpty.or(databaseField.textProperty().isEmpty))
    }

    @Throws(IOException::class)
    override fun start(stage: Stage) {
        val loader = FXMLLoader(javaClass.getResource("/import_form.fxml"))
        val root = loader.load<Parent>()
        val notification = NotificationPane(root)
        (loader.getController<Any>() as ImportForm).setUp(stage, notification)
        val scene = Scene(notification, 500.0, -1.0)
        stage.icons.add(Image(javaClass.getResourceAsStream("/icons/app.png")))
        stage.title = "Excel TA Import"
        stage.scene = scene
        stage.show()
    }

    private fun setUp(stage: Stage, notification: NotificationPane) {
        this.stage = stage
        this.notification = notification
    }

    fun importData() {
        LOG.info("Starting import")
        try {
            Importer.importToDatabase(
                    deleteOldDataBox.isSelected,
                    File(excelField.text),
                    File(databaseField.text)
            )
            notification.text = "Import erfolgreich abgeschlossen"
            LOG.info("Import successful")
        } catch (e: Exception) {
            notification.text = "Der Import ist fehlgeschlagen"
            LOG.error("Import failed", e)
        } finally {
            notification.show()
        }
    }
}
