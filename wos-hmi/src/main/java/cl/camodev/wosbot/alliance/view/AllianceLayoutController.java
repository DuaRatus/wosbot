package cl.camodev.wosbot.alliance.view;

import cl.camodev.wosbot.common.view.AbstractProfileController;
import cl.camodev.wosbot.console.enumerable.EnumConfigCategory;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AllianceLayoutController extends AbstractProfileController {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private void initialize() {
        setupDynamicUI(scrollPane, EnumConfigCategory.ALLIANCE);
    }
}
