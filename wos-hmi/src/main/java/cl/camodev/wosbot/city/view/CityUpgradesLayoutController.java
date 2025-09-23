package cl.camodev.wosbot.city.view;

import cl.camodev.wosbot.common.view.AbstractProfileController;
import cl.camodev.wosbot.console.enumerable.EnumConfigCategory;
import cl.camodev.wosbot.console.enumerable.EnumConfigurationKey;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;

public class CityUpgradesLayoutController extends AbstractProfileController {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private void initialize() {
        setupDynamicUI(scrollPane, EnumConfigCategory.CITY_UPGRADES);
    }
}
