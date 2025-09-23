package cl.camodev.wosbot.shop.view;

import cl.camodev.wosbot.common.view.AbstractProfileController;
import cl.camodev.wosbot.console.enumerable.EnumConfigCategory;
import cl.camodev.wosbot.console.enumerable.EnumConfigurationKey;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

public class ShopLayoutController extends AbstractProfileController {


    @FXML
    private ScrollPane scrollPane;
    @FXML
    private void initialize() {
        setupDynamicUI(scrollPane, EnumConfigCategory.SHOP);
    }
}
