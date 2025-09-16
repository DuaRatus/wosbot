package cl.camodev.wosbot.launcher.view;

import cl.camodev.utiles.ImageSearchUtil;
import cl.camodev.wosbot.console.enumerable.EnumConfigCategory;
import cl.camodev.wosbot.console.enumerable.EnumConfigurationKey;
import cl.camodev.wosbot.console.enumerable.EnumGlobalConfig;
import cl.camodev.wosbot.console.enumerable.EnumTpMessageSeverity;
import cl.camodev.wosbot.console.view.ConsoleLogLayoutController;
import cl.camodev.wosbot.emulator.EmulatorType;
import cl.camodev.wosbot.emulator.view.EmuConfigLayoutController;
import cl.camodev.wosbot.events.view.EventsLayoutController;
import cl.camodev.wosbot.experts.view.ExpertsLayoutController;
import cl.camodev.wosbot.ot.DTOBotState;
import cl.camodev.wosbot.ot.DTOLogMessage;
import cl.camodev.wosbot.profile.model.IProfileChangeObserver;
import cl.camodev.wosbot.profile.model.IProfileLoadListener;
import cl.camodev.wosbot.profile.model.IProfileObserverInjectable;
import cl.camodev.wosbot.profile.model.ProfileAux;
import cl.camodev.wosbot.profile.view.ProfileManagerLayoutController;
import cl.camodev.wosbot.serv.impl.ServConfig;
import cl.camodev.wosbot.serv.impl.ServScheduler;
import cl.camodev.wosbot.taskmanager.view.TaskManagerLayoutController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LauncherLayoutController implements IProfileLoadListener {

    private final Map<String, Object> moduleControllers = new HashMap<>();
    @FXML
    private VBox buttonsContainer;
    @FXML
    private Button buttonStartStop;
    @FXML
    private Button buttonPauseResume;
    @FXML
    private AnchorPane mainContentPane;
    @FXML
    private Label labelRunTime;
    @FXML
    private Label labelVersion;
    @FXML
    private ComboBox<ProfileAux> profileComboBox;
    private Stage stage;
    private LauncherActionController actionController;
    private ConsoleLogLayoutController consoleLogLayoutController;
    private ProfileManagerLayoutController profileManagerLayoutController;
    private boolean estado = false;
    private boolean updatingComboBox = false;

    public LauncherLayoutController(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        // Cargar tema oscuro
        loadDarkTheme();

        initializeDiscordBot();
        initializeEmulatorManager();
        initializeLogModule();
        initializeProfileModule();
        initializeProfileComboBox();
        initializeModules();
        initializeExternalLibraries();
        initializeEmulatorManager();
        showVersion();
    }

    private void loadDarkTheme() {
        try {
            String cssFile = getClass().getResource("/styles/dark-theme.css").toExternalForm();
            if (stage != null && stage.getScene() != null) {
                stage.getScene().getStylesheets().add(cssFile);
            }
            System.out.println("Dark theme loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading dark theme: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showVersion() {
        String version = getVersion();
        labelVersion.setText("Version: " + version);
    }

    private String getVersion() {
        Package pkg = getClass().getPackage();
        if (pkg != null && pkg.getImplementationVersion() != null) {
            return pkg.getImplementationVersion();
        }
        try {
            Path parentPomPath = Paths.get("..", "pom.xml");
            if (!Files.exists(parentPomPath)) {
                parentPomPath = Paths.get("pom.xml");
            }
            List<String> lines = Files.readAllLines(parentPomPath);
            String revision = null;
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("<revision>") && line.endsWith("</revision>")) {
                    revision = line.replace("<revision>", "").replace("</revision>", "").trim();
                    break;
                }
            }
            if (revision != null) {
                return revision;
            }
        } catch (Exception e) {
            // Ignore error
        }
        return "Unknown";
    }

    private void initializeEmulatorManager() {
        HashMap<String, String> globalConfig = ServConfig.getServices().getGlobalConfig();

        if (globalConfig == null || globalConfig.isEmpty()) {
            globalConfig = new HashMap<>();
        }

        String savedActiveEmulator = globalConfig.get(EnumGlobalConfig.CURRENT_EMULATOR_STRING.name());
        EmulatorType activeEmulator = savedActiveEmulator != null ? EmulatorType.valueOf(savedActiveEmulator) : null;
        boolean activeEmulatorValid = false;

        if (activeEmulator != null) {
            String activePath = globalConfig.get(activeEmulator.getConfigKey());
            if (activePath != null && new File(activePath).exists()) {
                activeEmulatorValid = true;
            } else {
                ServScheduler.getServices().saveEmulatorPath(activeEmulator.getConfigKey(), null);
            }
        }

        List<EmulatorType> foundEmulators = new ArrayList<>();
        for (EmulatorType emulator : EmulatorType.values()) {
            if (activeEmulator == emulator)
                continue;

            String emulatorPath = globalConfig.get(emulator.getConfigKey());
            if (emulatorPath != null && new File(emulatorPath).exists()) {
                foundEmulators.add(emulator);
            } else {
                File emulatorFile = new File(emulator.getDefaultPath());
                if (emulatorFile.exists()) {
                    ServScheduler.getServices().saveEmulatorPath(emulator.getConfigKey(), emulatorFile.getParent());
                    foundEmulators.add(emulator);
                }
            }
        }

        if (!activeEmulatorValid) {
            if (foundEmulators.size() == 1) {
                ServScheduler.getServices().saveEmulatorPath(EnumGlobalConfig.CURRENT_EMULATOR_STRING.name(), foundEmulators.get(0).name());
                return;
            } else if (foundEmulators.isEmpty()) {
                selectEmulatorManually();
            } else {
                EmulatorType selectedEmulator = askUserForPreferredEmulator(foundEmulators);
                ServScheduler.getServices().saveEmulatorPath(EnumGlobalConfig.CURRENT_EMULATOR_STRING.name(), selectedEmulator.name());
            }
        }
    }

    private EmulatorType askUserForPreferredEmulator(List<EmulatorType> emulators) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Select Emulator");
        alert.setHeaderText("Multiple emulators found. Please select which one to use.");

        List<ButtonType> buttons = new ArrayList<>();
        for (EmulatorType emulator : emulators) {
            buttons.add(new ButtonType(emulator.getDisplayName()));
        }
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        buttons.add(cancelButton);

        alert.getButtonTypes().setAll(buttons);
        Optional<ButtonType> result = alert.showAndWait();

        for (EmulatorType emulator : emulators) {
            if (result.isPresent() && result.get().getText().equals(emulator.getDisplayName())) {
                return emulator;
            }
        }

        showErrorAndExit("No emulator selected. The application will close.");
        return null;
    }

    private void selectEmulatorManually() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Emulator Executable");

        FileChooser.ExtensionFilter exeFilter = new FileChooser.ExtensionFilter("Emulator Executable", "*.exe");
        fileChooser.getExtensionFilters().add(exeFilter);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            for (EmulatorType emulator : EmulatorType.values()) {
                if (selectedFile.getName().equals(new File(emulator.getDefaultPath()).getName())) {
                    ServScheduler.getServices().saveEmulatorPath(emulator.getConfigKey(), selectedFile.getParent());
                    ServScheduler.getServices().saveEmulatorPath(EnumGlobalConfig.CURRENT_EMULATOR_STRING.name(), emulator.name());
                    return;
                }
            }
            showErrorAndExit("Invalid emulator file selected. Please select a valid emulator executable.");
        } else {
            showErrorAndExit("No emulator selected. The application will close.");
        }
    }

    private void showErrorAndExit(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(0);
    }

    private void initializeDiscordBot() {
        // ServDiscord.getServices();
    }

    private void initializeLogModule() {
        actionController = new LauncherActionController(this);
        consoleLogLayoutController = new ConsoleLogLayoutController();
        addButton("ConsoleLogLayout", "Logs", consoleLogLayoutController).fire();
    }

    private void initializeProfileModule() {
        profileManagerLayoutController = new ProfileManagerLayoutController();
        actionController.setProfileManagerController(profileManagerLayoutController);
        addButton("ProfileManagerLayout", "Profiles", profileManagerLayoutController);
    }

    private void initializeProfileComboBox() {
        configureComboCells();

        profileComboBox.setOnAction(event -> {
            if (!updatingComboBox) {
                ProfileAux selectedProfile = profileComboBox.getSelectionModel().getSelectedItem();
                if (selectedProfile != null) {
                    actionController.selectProfile(selectedProfile);
                }
            }
        });

        if (profileManagerLayoutController != null) {
            profileManagerLayoutController.addProfileLoadListener(new IProfileLoadListener() {
                @Override
                public void onProfileLoad(ProfileAux profile) {
                    Platform.runLater(() -> {
                        actionController.updateProfileComboBox();
                    });
                }
            });
        }

        Platform.runLater(() -> {
            actionController.loadProfilesIntoComboBox();
        });
    }

    public void updateComboBoxItems(javafx.collections.ObservableList<ProfileAux> profiles) {
        updatingComboBox = true;
        profileComboBox.getItems().clear();
        profileComboBox.getItems().addAll(profiles);
        configureComboCells();
        updatingComboBox = false;
    }

    private void configureComboCells() {
        profileComboBox.setCellFactory(listView -> new ListCell<ProfileAux>() {
            @Override
            protected void updateItem(ProfileAux profile, boolean empty) {
                super.updateItem(profile, empty);
                if (empty || profile == null) {
                    setText(null);
                } else {
                    setText(profile.getName() + " (Emulator: " + profile.getEmulatorNumber() + ")");
                }
            }
        });

        profileComboBox.setButtonCell(new ListCell<ProfileAux>() {
            @Override
            protected void updateItem(ProfileAux profile, boolean empty) {
                super.updateItem(profile, empty);
                if (empty || profile == null) {
                    setText(null);
                } else {
                    setText(profile.getName() + " (Emulator: " + profile.getEmulatorNumber() + ")");
                }
            }
        });
    }

    public ProfileAux getSelectedProfile() {
        return profileComboBox.getSelectionModel().getSelectedItem();
    }

    public void selectProfileInComboBox(ProfileAux profile) {
        updatingComboBox = true;
        profileComboBox.getSelectionModel().select(profile);
        updatingComboBox = false;
    }

    public void refreshProfileComboBox() {
        actionController.refreshProfileComboBox();
    }

    private void initializeExternalLibraries() {
        try {
            ImageSearchUtil.loadNativeLibrary("/native/opencv/opencv_java4110.dll");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============= MÉTODO INITIALIZEMODULES ACTUALIZADO =============
    private void initializeModules() {
        // Módulos estáticos que no dependen de las categorías del enum
        List<ModuleDefinition> staticModules = Arrays.asList(
                new ModuleDefinition("TaskManagerLayout", "Task Manager", TaskManagerLayoutController::new),
                new ModuleDefinition("EventsLayout", "Events", EventsLayoutController::new),
                new ModuleDefinition("ExpertsLayout", "Experts", ExpertsLayoutController::new),
                new ModuleDefinition("EmuConfigLayout", "Config", EmuConfigLayoutController::new)
        );

        // Crear módulos dinámicos para cada categoría del enum
        List<ModuleDefinition> dynamicModules = createDynamicModules();

        // Combinar y procesar todos los módulos
        List<ModuleDefinition> allModules = new ArrayList<>();
        allModules.addAll(staticModules);
        allModules.addAll(dynamicModules);

        for (ModuleDefinition module : allModules) {
            consoleLogLayoutController.appendMessage(
                    new DTOLogMessage(EnumTpMessageSeverity.INFO,
                            "Loading module: " + module.buttonTitle(), "-", "-"));

            Object controller = module.createController(profileManagerLayoutController);
            moduleControllers.put(module.buttonTitle(), controller);

            // Manejar controladores dinámicos vs estáticos
            if (controller instanceof GenericCategoryController) {
                addDynamicButton(module.buttonTitle(), (GenericCategoryController) controller);
            } else {
                addButton(module.fxmlName(), module.buttonTitle(), controller);
            }

            if (controller instanceof IProfileLoadListener) {
                profileManagerLayoutController.addProfileLoadListener((IProfileLoadListener) controller);
            }
        }

        profileManagerLayoutController.addProfileLoadListener(this);
    }

    // ============= CREAR MÓDULOS DINÁMICOS =============
    private List<ModuleDefinition> createDynamicModules() {
        List<ModuleDefinition> dynamicModules = new ArrayList<>();

        for (EnumConfigCategory category : EnumConfigCategory.values()) {
            String layoutName = category.getName().replaceAll("\\s+", "") + "Layout";
            String buttonTitle = category.getName();

            Supplier<Object> controllerSupplier = () ->
                    new GenericCategoryController(category, profileManagerLayoutController);

            dynamicModules.add(new ModuleDefinition(layoutName, buttonTitle, controllerSupplier));
        }

        return dynamicModules;
    }

    // ============= MÉTODO PARA AGREGAR BOTONES DINÁMICOS =============
    private Button addDynamicButton(String title, GenericCategoryController controller) {
        Button button = new Button(title);
        button.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(button, Priority.ALWAYS);
        button.getStyleClass().add("square-button");

        button.setOnAction(e -> {
            // Limpiar contenido actual y agregar el nuevo panel
            mainContentPane.getChildren().clear();

            ScrollPane scrollPane = new ScrollPane(controller.getMainContainer());
            scrollPane.setFitToWidth(true);
            scrollPane.getStyleClass().add("config-scroll-pane");

            AnchorPane.setTopAnchor(scrollPane, 0.0);
            AnchorPane.setBottomAnchor(scrollPane, 0.0);
            AnchorPane.setLeftAnchor(scrollPane, 0.0);
            AnchorPane.setRightAnchor(scrollPane, 0.0);
            mainContentPane.getChildren().add(scrollPane);

            // Actualizar estado del botón activo
            for (Node node : buttonsContainer.getChildren()) {
                if (node instanceof Button) {
                    node.getStyleClass().remove("active");
                }
            }
            button.getStyleClass().add("active");
        });

        buttonsContainer.getChildren().add(button);
        return button;
    }

    @Override
    public void onProfileLoad(ProfileAux profile) {
        String version = getVersion();
        stage.setTitle("Whiteout Survival Bot v" + version + " - " + profile.getName());
        buttonStartStop.setDisable(false);
        buttonPauseResume.setDisable(true);
        selectProfileInComboBox(profile);
    }

    public void onBotStateChange(DTOBotState botState) {
        if (botState != null) {
            if (botState.getRunning()) {
                if (botState.getPaused() != null && botState.getPaused()) {
                    buttonStartStop.setText("Stop");
                    buttonStartStop.setDisable(false);
                    buttonPauseResume.setText("Resume Bot");
                    buttonPauseResume.setDisable(false);
                    estado = true;
                } else {
                    buttonStartStop.setText("Stop");
                    buttonStartStop.setDisable(false);
                    buttonPauseResume.setText("Pause Bot");
                    buttonPauseResume.setDisable(false);
                    estado = true;
                }
            } else {
                buttonStartStop.setText("Start Bot");
                buttonStartStop.setDisable(false);
                buttonPauseResume.setText("Pause Bot");
                buttonPauseResume.setDisable(true);
                estado = false;
            }
        }
    }

    @FXML
    public void handleButtonStartStop(ActionEvent event) {
        if (!estado) {
            actionController.startBot();
        } else {
            actionController.stopBot();
        }
    }

    @FXML
    public void handleButtonPauseResume(ActionEvent event) {
        if (buttonPauseResume.getText().equals("Pause Bot")) {
            actionController.pauseBot();
        } else {
            actionController.resumeBot();
        }
    }

    private Button addButton(String fxmlName, String title, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(controller.getClass().getResource(fxmlName + ".fxml"));
            loader.setController(controller);
            Parent root = loader.load();

            Button button = new Button(title);
            button.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(button, Priority.ALWAYS);
            button.getStyleClass().add("square-button");

            button.setOnAction(e -> {
                mainContentPane.getChildren().clear();
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
                mainContentPane.getChildren().add(root);

                for (Node node : buttonsContainer.getChildren()) {
                    if (node instanceof Button) {
                        node.getStyleClass().remove("active");
                    }
                }

                button.getStyleClass().add("active");
            });

            buttonsContainer.getChildren().add(button);
            return button;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T getModuleController(String key, Class<T> type) {
        Object controller = moduleControllers.get(key);
        if (controller == null) {
            return null;
        }
        return type.cast(controller);
    }

    // ============= CONTROLADOR GENÉRICO DINÁMICO MEJORADO =============
    public static class GenericCategoryController implements IProfileLoadListener, IProfileObserverInjectable {
        private final EnumConfigCategory category;
        private final Map<EnumConfigurationKey, Control> configControls = new HashMap<>();
        private IProfileChangeObserver profileObserver;
        private ScrollPane mainContainer;
        private VBox contentContainer;

        public GenericCategoryController(EnumConfigCategory category, IProfileChangeObserver profileObserver) {
            this.category = category;
            this.profileObserver = profileObserver;
            initializeUI();
        }

        private void initializeUI() {
            // Crear el container principal con scroll
            mainContainer = new ScrollPane();
            mainContainer.setFitToWidth(true);
            mainContainer.setFitToHeight(true);
            mainContainer.getStyleClass().add("config-scroll-pane");

            contentContainer = new VBox(20);
            contentContainer.setPadding(new Insets(30));
            contentContainer.getStyleClass().add("category-main-container");

            // Header de la categoría
            createCategoryHeader();

            // Crear grupos de configuración organizados
            createConfigurationGroups();

            mainContainer.setContent(contentContainer);
        }

        private void createCategoryHeader() {
            VBox headerContainer = new VBox(10);
            headerContainer.getStyleClass().add("category-header");

            // Título principal
            Label categoryTitle = new Label(category.getName());
            categoryTitle.getStyleClass().addAll("category-main-title", "h1");

            // Descripción
            if (category.getDescription() != null && !category.getDescription().isEmpty()) {
                Label categoryDesc = new Label(category.getDescription());
                categoryDesc.getStyleClass().add("category-main-description");
                categoryDesc.setWrapText(true);
                headerContainer.getChildren().add(categoryDesc);
            }

            headerContainer.getChildren().add(0, categoryTitle);
            contentContainer.getChildren().add(headerContainer);
        }

        private void createConfigurationGroups() {
            List<EnumConfigurationKey> categoryConfigs = getCategoryConfigs();

            // Agrupar por configuraciones principales (root configs)
            List<EnumConfigurationKey> rootConfigs = categoryConfigs.stream()
                    .filter(EnumConfigurationKey::isRoot)
                    .sorted((a, b) -> {
                        String titleA = a.getTitle() != null ? a.getTitle() : formatConfigName(a.name());
                        String titleB = b.getTitle() != null ? b.getTitle() : formatConfigName(b.name());
                        return titleA.compareTo(titleB);
                    })
                    .collect(Collectors.toList());

            for (EnumConfigurationKey rootConfig : rootConfigs) {
                VBox configCard = createConfigurationCard(rootConfig, categoryConfigs);
                contentContainer.getChildren().add(configCard);
            }
        }

        private VBox createConfigurationCard(EnumConfigurationKey rootConfig, List<EnumConfigurationKey> allConfigs) {
            VBox card = new VBox(15);
            card.getStyleClass().add("config-card");
            card.setPadding(new Insets(25));

            // Header del grupo con el control principal
            HBox groupHeader = createGroupHeader(rootConfig);
            card.getChildren().add(groupHeader);

            // Container para los controles secundarios
            VBox childrenContainer = new VBox(15);
            childrenContainer.getStyleClass().add("config-children-container");

            // Encontrar controles hijos DIRECTOS
            List<EnumConfigurationKey> directChildren = findDirectChildren(rootConfig, allConfigs);

            if (!directChildren.isEmpty()) {
                // Crear grid layout responsive para los controles hijos
                createResponsiveChildrenGrid(directChildren, childrenContainer);
                card.getChildren().add(childrenContainer);

                // Configurar visibilidad basada en el control principal
                setupVisibilityBinding(rootConfig, childrenContainer);
            }

            return card;
        }

        private HBox createGroupHeader(EnumConfigurationKey config) {
            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);
            header.getStyleClass().add("config-group-header");

            // Control principal (toggle switch para boolean)
            Control mainControl = createMainControl(config);
            configControls.put(config, mainControl);

            // Información del grupo
            VBox infoContainer = new VBox(5);

            Label titleLabel = new Label(config.getTitle() != null ? config.getTitle() : formatConfigName(config.name()));
            titleLabel.getStyleClass().add("config-group-title");

            if (config.getDescription() != null && !config.getDescription().isEmpty()) {
                Label descLabel = new Label(config.getDescription());
                descLabel.getStyleClass().add("config-group-description");
                descLabel.setWrapText(true);
                infoContainer.getChildren().add(descLabel);
            }

            infoContainer.getChildren().add(0, titleLabel);

            // Spacer para empujar el control hacia la derecha
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            header.getChildren().addAll(infoContainer, spacer, mainControl);

            return header;
        }

        private void createResponsiveChildrenGrid(List<EnumConfigurationKey> children, VBox container) {
            // Crear un FlowPane responsive que se ajuste al ancho disponible
            final double CARD_MIN_WIDTH = 280;
            final double CARD_PREF_WIDTH = 320;
            final double CARD_SPACING = 20;

            // Agrupar en filas basado en el ancho disponible
            container.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                if (newWidth.doubleValue() > 0) {
                    updateChildrenLayout(children, container, newWidth.doubleValue());
                }
            });

            // Layout inicial
            updateChildrenLayout(children, container, 1000); // Ancho por defecto
        }

        private void updateChildrenLayout(List<EnumConfigurationKey> children, VBox container, double containerWidth) {
            // Limpiar container
            container.getChildren().clear();

            // Calcular cuántas columnas caben
            final double CARD_WIDTH = 320;
            final double SPACING = 20;
            final double PADDING = 50; // Padding lateral

            int maxColumns = Math.max(1, (int) ((containerWidth - PADDING) / (CARD_WIDTH + SPACING)));

            // Crear filas con el número de columnas calculado
            for (int i = 0; i < children.size(); i += maxColumns) {
                HBox row = new HBox(SPACING);
                row.setAlignment(Pos.TOP_LEFT);
                row.getStyleClass().add("config-grid-row");

                for (int j = 0; j < maxColumns && (i + j) < children.size(); j++) {
                    EnumConfigurationKey childConfig = children.get(i + j);
                    VBox childCard = createChildConfigCard(childConfig);

                    // Hacer que cada card tenga ancho flexible
                    childCard.setPrefWidth(CARD_WIDTH);
                    childCard.setMinWidth(280);
                    HBox.setHgrow(childCard, Priority.SOMETIMES);

                    row.getChildren().add(childCard);
                }

                container.getChildren().add(row);
            }
        }

        private VBox createChildConfigCard(EnumConfigurationKey config) {
            VBox card = new VBox(12);
            card.getStyleClass().add("config-child-card");
            card.setPadding(new Insets(20));

            // Header del hijo (título y descripción)
            VBox headerContainer = new VBox(6);
            headerContainer.getStyleClass().add("config-child-header");

            Label titleLabel = new Label(config.getTitle() != null ? config.getTitle() : formatConfigName(config.name()));
            titleLabel.getStyleClass().add("config-child-title");
            titleLabel.setWrapText(true);

            if (config.getDescription() != null && !config.getDescription().isEmpty()) {
                Label descLabel = new Label(config.getDescription());
                descLabel.getStyleClass().add("config-child-description");
                descLabel.setWrapText(true);
                headerContainer.getChildren().add(descLabel);
            }

            headerContainer.getChildren().add(0, titleLabel);
            card.getChildren().add(headerContainer);

            // Control container
            HBox controlContainer = new HBox(10);
            controlContainer.setAlignment(Pos.CENTER_LEFT);

            // Control
            Control control = createSpecificControl(config);
            configControls.put(config, control);

            // Layout según el tipo de control
            if (control instanceof CheckBox) {
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                controlContainer.getChildren().addAll(spacer, control);
            } else {
                Label controlLabel = new Label("Value:");
                controlLabel.getStyleClass().add("config-control-label");
                controlContainer.getChildren().addAll(controlLabel, control);

                // Añadir unidad si existe
                if (config.getUnit() != null && !config.getUnit().isEmpty()) {
                    Label unitLabel = new Label(config.getUnit());
                    unitLabel.getStyleClass().add("config-unit-label");
                    controlContainer.getChildren().add(unitLabel);
                }
            }

            card.getChildren().add(controlContainer);

            // ========== RENDER RECURSIVO DE SUB-HIJOS ==========
            List<EnumConfigurationKey> subChildren = findDirectChildren(config);
            if (!subChildren.isEmpty()) {
                VBox nestedContainer = new VBox(15);
                nestedContainer.getStyleClass().add("config-children-container");

                createResponsiveChildrenGrid(subChildren, nestedContainer);
                card.getChildren().add(nestedContainer);

                // Visibilidad basada en el control de este hijo
                setupVisibilityBinding(config, nestedContainer);
            }

            return card;
        }

        private Control createMainControl(EnumConfigurationKey config) {
            // Para controles principales, usar toggle switches más prominentes
            if (config.getType() == Boolean.class) {
                CheckBox toggleSwitch = new CheckBox();
                toggleSwitch.setSelected(Boolean.parseBoolean(config.getDefaultValue()));
                toggleSwitch.getStyleClass().addAll("config-main-toggle", "toggle-switch");

                toggleSwitch.selectedProperty().addListener((obs, oldVal, newVal) ->
                        onConfigValueChanged(config, newVal.toString()));

                return toggleSwitch;
            }

            return createSpecificControl(config);
        }

        private Control createSpecificControl(EnumConfigurationKey config) {
            Class<?> type = config.getType();

            // Verificar primero si tiene validValues para generar ComboBox
            if (config.getValidValues() != null && config.getValidValues().length > 0) {
                ComboBox<String> comboBox = new ComboBox<>();
                comboBox.getItems().addAll(config.getValidValues());
                comboBox.setValue(config.getDefaultValue());
                comboBox.getStyleClass().add("config-combobox-modern");
                comboBox.setPrefWidth(150);

                comboBox.valueProperty().addListener((obs, oldVal, newVal) ->
                        onConfigValueChanged(config, newVal));

                return comboBox;
            }

            // Si no tiene validValues, usar controles específicos por tipo
            if (type == Boolean.class) {
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(Boolean.parseBoolean(config.getDefaultValue()));
                checkBox.getStyleClass().addAll("config-checkbox", "toggle-switch-small");

                checkBox.selectedProperty().addListener((obs, oldVal, newVal) ->
                        onConfigValueChanged(config, newVal.toString()));

                return checkBox;

            } else if (type == Integer.class) {
                Spinner<Integer> spinner = new Spinner<>(0, 999999,
                        Integer.parseInt(config.getDefaultValue()));
                spinner.setEditable(true);
                spinner.getStyleClass().add("config-spinner-modern");
                spinner.setPrefWidth(100);

                spinner.valueProperty().addListener((obs, oldVal, newVal) ->
                        onConfigValueChanged(config, newVal.toString()));

                return spinner;

            } else {
                TextField textField = new TextField(config.getDefaultValue());
                textField.getStyleClass().add("config-textfield-modern");
                textField.setPrefWidth(150);

                textField.textProperty().addListener((obs, oldVal, newVal) ->
                        onConfigValueChanged(config, newVal));

                return textField;
            }
        }

        private void setupVisibilityBinding(EnumConfigurationKey parent, VBox childrenContainer) {
            Control parentControl = configControls.get(parent);
            if (parentControl instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) parentControl;

                // Binding de visibilidad - NO establecer valores después del binding
                childrenContainer.visibleProperty().bind(checkBox.selectedProperty());
                childrenContainer.managedProperty().bind(checkBox.selectedProperty());

                // El estado inicial se establece automáticamente por el binding
                // No necesitamos setVisible() o setManaged() después del bind
            }
        }

        private List<EnumConfigurationKey> findDirectChildren(EnumConfigurationKey parent, List<EnumConfigurationKey> allConfigs) {
            return allConfigs.stream()
                    .filter(config -> config.hasParent() && config.getParent() == parent)
                    .sorted((a, b) -> {
                        String titleA = a.getTitle() != null ? a.getTitle() : formatConfigName(a.name());
                        String titleB = b.getTitle() != null ? b.getTitle() : formatConfigName(b.name());
                        return titleA.compareTo(titleB);
                    })
                    .collect(Collectors.toList());
        }

        // Overload para obtener hijos directos usando las configs de la categoría
        private List<EnumConfigurationKey> findDirectChildren(EnumConfigurationKey parent) {
            return getCategoryConfigs().stream()
                    .filter(config -> config.hasParent() && config.getParent() == parent)
                    .sorted((a, b) -> {
                        String titleA = a.getTitle() != null ? a.getTitle() : formatConfigName(a.name());
                        String titleB = b.getTitle() != null ? b.getTitle() : formatConfigName(b.name());
                        return titleA.compareTo(titleB);
                    })
                    .collect(Collectors.toList());
        }

        @Override
        public void onProfileLoad(ProfileAux profile) {
            loadProfileValues(profile);
        }

        private void loadProfileValues(ProfileAux profile) {
            for (Map.Entry<EnumConfigurationKey, Control> entry : configControls.entrySet()) {
                EnumConfigurationKey config = entry.getKey();
                Control control = entry.getValue();

                try {
                    // Obtener valor del perfil usando el método corregido
                    Object configValue = profile.getConfiguration(config);
                    if (configValue != null) {
                        setControlValue(control, configValue.toString(), config.getType());
                    }
                } catch (Exception e) {
                    // Si hay error, usar valor por defecto
                    setControlValue(control, config.getDefaultValue(), config.getType());
                }
            }
        }

        private void setControlValue(Control control, String value, Class<?> type) {
            if (control instanceof CheckBox) {
                ((CheckBox) control).setSelected(Boolean.parseBoolean(value));
            } else if (control instanceof ComboBox) {
                ((ComboBox<String>) control).setValue(value);
            } else if (control instanceof Spinner) {
                try {
                    ((Spinner<Integer>) control).getValueFactory().setValue(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    // Mantener valor actual si hay error de parseo
                }
            } else if (control instanceof TextField) {
                ((TextField) control).setText(value);
            }
        }

        private void onConfigValueChanged(EnumConfigurationKey config, String newValue) {
            if (profileObserver != null && profileObserver instanceof ProfileManagerLayoutController) {
                try {
                    ProfileManagerLayoutController profileManager = (ProfileManagerLayoutController) profileObserver;
                    Long loadedProfileId = profileManager.getLoadedProfileId();

                    if (loadedProfileId != null) {
                        // Find the current profile by ID
                        ProfileAux currentProfile = profileManager.getProfiles().stream()
                                .filter(profile -> profile.getId().equals(loadedProfileId))
                                .findFirst()
                                .orElse(null);

                        if (currentProfile != null) {
                            // Guardar el nuevo valor en el perfil
                            currentProfile.setConfig(config, newValue);
                            // Notificar cambio
                            System.out.println("Config saved: " + config.name() + " = " + newValue);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void setProfileObserver(IProfileChangeObserver profileObserver) {
            this.profileObserver = profileObserver;
        }

        public ScrollPane getMainContainer() {
            return mainContainer;
        }

        public EnumConfigCategory getCategory() {
            return category;
        }

        private List<EnumConfigurationKey> getCategoryConfigs() {
            return Arrays.stream(EnumConfigurationKey.values())
                    .filter(config -> config.getCategory() == category)
                    .collect(Collectors.toList());
        }

        private String formatConfigName(String configName) {
            return Arrays.stream(configName.split("_"))
                    .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
        }
    }

    private record ModuleDefinition(String fxmlName, String buttonTitle, Supplier<Object> controllerSupplier) {

        public Object createController(IProfileChangeObserver profileObserver) {
            Object controller = controllerSupplier.get();
            if (controller instanceof IProfileObserverInjectable) {
                ((IProfileObserverInjectable) controller).setProfileObserver(profileObserver);
            }
            return controller;
        }
    }
}

