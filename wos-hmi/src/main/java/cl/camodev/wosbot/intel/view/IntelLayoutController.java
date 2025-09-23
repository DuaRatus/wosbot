package cl.camodev.wosbot.intel.view;

import cl.camodev.wosbot.common.view.AbstractProfileController;
import cl.camodev.wosbot.console.enumerable.EnumConfigCategory;
import cl.camodev.wosbot.console.enumerable.EnumConfigurationKey;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

public class IntelLayoutController extends AbstractProfileController {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private void initialize() {
        setupDynamicUI(scrollPane, EnumConfigCategory.INTEL);
    }
}
