package cl.camodev.wosbot.launcher.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import cl.camodev.utiles.ImageSearchUtil;
import cl.camodev.wosbot.alliance.view.AllianceLayoutController;
import cl.camodev.wosbot.alliancechampionship.view.AllianceChampionshipLayoutController;
import cl.camodev.wosbot.bear.view.BearTrapLayoutController;
import cl.camodev.wosbot.chieforder.view.ChiefOrderLayoutController;
import cl.camodev.wosbot.city.view.CityEventsLayoutController;
import cl.camodev.wosbot.city.view.CityUpgradesLayoutController;
import cl.camodev.wosbot.console.enumerable.EnumConfigurationKey;
import cl.camodev.wosbot.console.enumerable.EnumTpMessageSeverity;
import cl.camodev.wosbot.console.view.ConsoleLogLayoutController;
import cl.camodev.wosbot.emulator.EmulatorType;
import cl.camodev.wosbot.emulator.view.EmuConfigLayoutController;
import cl.camodev.wosbot.events.view.EventsLayoutController;
import cl.camodev.wosbot.experts.view.ExpertsLayoutController;
import cl.camodev.wosbot.gather.view.GatherLayoutController;
import cl.camodev.wosbot.intel.view.IntelLayoutController;
import cl.camodev.wosbot.mobilization.view.MobilizationLayoutController;
import cl.camodev.wosbot.ot.DTOBotState;
import cl.camodev.wosbot.ot.DTOLogMessage;
import cl.camodev.wosbot.ot.DTOQueueProfileState;
import cl.camodev.wosbot.pets.view.PetsLayoutController;
import cl.camodev.wosbot.polarterror.view.PolarTerrorLayoutController;
import cl.camodev.wosbot.profile.model.IProfileChangeObserver;
import cl.camodev.wosbot.profile.model.IProfileLoadListener;
import cl.camodev.wosbot.profile.model.IProfileObserverInjectable;
import cl.camodev.wosbot.profile.model.ProfileAux;
import cl.camodev.wosbot.profile.view.ProfileManagerLayoutController;
import cl.camodev.wosbot.ot.DTOQueueState;
import cl.camodev.wosbot.serv.IStaminaChangeListener;
import cl.camodev.wosbot.serv.impl.ServConfig;
import cl.camodev.wosbot.serv.impl.ServScheduler;
import cl.camodev.wosbot.serv.impl.StaminaService;
import cl.camodev.wosbot.shop.view.ShopLayoutController;
import cl.camodev.wosbot.taskmanager.view.TaskManagerLayoutController;
import cl.camodev.wosbot.training.view.TrainingLayoutController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import cl.camodev.wosbot.alliance.view.AllianceShopController;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class LauncherLayoutController implements IProfileLoadListener, IStaminaChangeListener {

    private final Map<String, Object> moduleControllers = new HashMap<>();
    @FXML
    private VBox buttonsContainer;
    @FXML
    private Button buttonStartStop;
    @FXML
    private SplitMenuButton buttonPauseResume;
    @FXML
    private MenuItem menuToggleAllQueues;
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
    private ProfileAux currentProfile = null; // Perfil actualmente cargado
    private boolean allQueuesPaused = false;
    private final Map<Long, DTOQueueProfileState> activeQueueStates = new HashMap<>();
    @FXML
    private MenuItem menuToggleCurrentQueue;
    @FXML
    private ScrollPane sidebarScroll;
    @FXML
    private GridPane bottomBar;
    @FXML
    private VBox sideBarContainer;

    public LauncherLayoutController(Stage stage) {
        this.stage = stage;
        StaminaService.getServices().addStaminaChangeListener(this);
    }

    @FXML
    private void initialize() {
        initializeDiscordBot();
        initializeEmulatorManager();
        initializeLogModule();
        initializeProfileModule();
        initializeProfileComboBox();
        initializeModules();
        initializeExternalLibraries();
        showVersion();
        buttonStartStop.setDisable(false);
        buttonPauseResume.setDisable(true);
        configurePauseMenu();

        // Wirear eventos por código (en lugar de FXML)
        if (buttonStartStop != null) {
            buttonStartStop.setOnAction(this::handleButtonStartStop);
        }
        if (buttonPauseResume != null) {
            buttonPauseResume.setOnAction(this::handleButtonPauseResume);
        }
        if (menuToggleCurrentQueue != null) {
            menuToggleCurrentQueue.setOnAction(this::handleToggleCurrentQueue);
        }
        if (menuToggleAllQueues != null) {
            menuToggleAllQueues.setOnAction(this::handleToggleAllQueues);
        }

        // Apply sidebar style class and normal orientation to the left menu container
        if (sideBarContainer != null) {
            sideBarContainer.getStyleClass().add("sidebar");
            sideBarContainer.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }
        if (buttonsContainer != null) {
            // Ensure inner buttons also keep normal orientation
            buttonsContainer.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }
        // Move vertical scrollbar to the left and add a specific style class
        if (sidebarScroll != null) {
            sidebarScroll.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            sidebarScroll.getStyleClass().add("sidebar-scroll");
        }
        // Apply bottom bar background style
        if (bottomBar != null) {
            bottomBar.getStyleClass().add("bottom-bar");
        }

        // Initialize action buttons' styles
        updateStartStopButtonStyle();
        updatePauseButtonStyle();

        // Ensure icon is placed to the left of the text with spacing
        if (buttonStartStop != null) {
            buttonStartStop.setContentDisplay(ContentDisplay.LEFT);
            buttonStartStop.setGraphicTextGap(8);
        }

    }

    private void showVersion() {
        String version = getVersion();
        labelVersion.setText("Whiteout Survival Bot v" + version);
    }

    private String getVersion() {
        // If running as JAR
        Package pkg = getClass().getPackage();
        if (pkg != null && pkg.getImplementationVersion() != null) {
            return pkg.getImplementationVersion();
        }
        // Read version from parent project pom.xml
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

        // Verificar si hay un emulador activo y validar su path
        String savedActiveEmulator = globalConfig.get(EnumConfigurationKey.CURRENT_EMULATOR_STRING.name());
        EmulatorType activeEmulator = savedActiveEmulator != null ? EmulatorType.valueOf(savedActiveEmulator) : null;
        boolean activeEmulatorValid = false;

        if (activeEmulator != null) {
            String activePath = globalConfig.get(activeEmulator.getConfigKey());
            if (activePath != null && new File(activePath).exists()) {
                activeEmulatorValid = true;
            } else {
                ServScheduler.getServices().saveEmulatorPath(activeEmulator.getConfigKey(), null); // Invalidar path no válido
            }
        }

        // Validar el otro emulador si el activo no es válido
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
                ServScheduler.getServices().saveEmulatorPath(EnumConfigurationKey.CURRENT_EMULATOR_STRING.name(), foundEmulators.get(0).name());
                return;
            } else if (foundEmulators.isEmpty()) {
                selectEmulatorManually();
            } else {
                EmulatorType selectedEmulator = askUserForPreferredEmulator(foundEmulators);
                ServScheduler.getServices().saveEmulatorPath(EnumConfigurationKey.CURRENT_EMULATOR_STRING.name(), selectedEmulator.name());
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
        return null; // Nunca debería llegar aquí porque el sistema se cerrará antes.
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
                    ServScheduler.getServices().saveEmulatorPath(EnumConfigurationKey.CURRENT_EMULATOR_STRING.name(), emulator.name());
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
//		ServDiscord.getServices();

    }

    private void initializeLogModule() {
        actionController = new LauncherActionController(this);
        consoleLogLayoutController = new ConsoleLogLayoutController();
        Objects.requireNonNull(addButton("ConsoleLogLayout", "Logs", consoleLogLayoutController)).fire();
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
            profileManagerLayoutController.addProfileLoadListener(profile -> Platform.runLater(() -> {
                actionController.updateProfileComboBox();
            }));
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

    private void initializeModules() {
        //@formatter:off
        List<ModuleDefinition> modules = Arrays.asList(
                new ModuleDefinition("TaskManagerLayout", "Task Manager", TaskManagerLayoutController::new),
                new ModuleDefinition("CityUpgradesLayout", "City Upgrades", CityUpgradesLayoutController::new),
                new ModuleDefinition("CityEventsLayout", "City Events", CityEventsLayoutController::new),
                new ModuleDefinition("PolarTerrorLayout", "Polar Terror", PolarTerrorLayoutController::new),
                new ModuleDefinition("ShopLayout", "Shop", ShopLayoutController::new),
                new ModuleDefinition("GatherLayout", "Gather", GatherLayoutController::new),
                new ModuleDefinition("IntelLayout", "Intel", IntelLayoutController::new),
                new ModuleDefinition("AllianceLayout", "Alliance", AllianceLayoutController::new),
                new ModuleDefinition("AllianceChampionshipLayout", "Alliance Championship", AllianceChampionshipLayoutController::new),
                new ModuleDefinition("AllianceShop", "Alliance Shop", AllianceShopController::new),
                new ModuleDefinition("AllianceMobilizationLayout", "Alliance Mobilization", MobilizationLayoutController::new),
                new ModuleDefinition("BearTrapLayout", "Bear Trap", BearTrapLayoutController::new),
                new ModuleDefinition("TrainingLayout", "Training", TrainingLayoutController::new),
                new ModuleDefinition("PetsLayout", "Pets", PetsLayoutController::new),
                new ModuleDefinition("EventsLayout", "Events", EventsLayoutController::new),
                new ModuleDefinition("ExpertsLayout", "Experts", ExpertsLayoutController::new),
                new ModuleDefinition("ChiefOrderLayout", "Chief Order", ChiefOrderLayoutController::new),
                new ModuleDefinition("EmuConfigLayout", "Config", EmuConfigLayoutController::new)
                );
        //@formatter:on

        for (ModuleDefinition module : modules) {
            consoleLogLayoutController.appendMessage(new DTOLogMessage(EnumTpMessageSeverity.INFO, "Loading module: " + module.buttonTitle(), "-", "-"));


            Object controller = module.createController(profileManagerLayoutController);
            moduleControllers.put(module.buttonTitle(), controller);
            addButton(module.fxmlName(), module.buttonTitle(), controller);

            if (controller instanceof IProfileLoadListener) {
                profileManagerLayoutController.addProfileLoadListener((IProfileLoadListener) controller);
            }
        }
        profileManagerLayoutController.addProfileLoadListener(this);

        // Se elimina la auto-selección del primer botón para respetar la selección de Logs hecha previamente.
    }


    @Override
    public void onProfileLoad(ProfileAux profile) {
        this.currentProfile = profile;
        updateWindowTitle();
        selectProfileInComboBox(profile);
        refreshPauseMenuItems();
    }

    @Override
    public void onStaminaChanged(Long profileId, int newStamina) {
        // Solo actualizar si el perfil que cambió es el perfil actual
        if (currentProfile != null && currentProfile.getId().equals(profileId)) {
            updateWindowTitle();
        }
    }

    /**
     * Actualiza el título de la ventana con la información del perfil y stamina actual
     */
    private void updateWindowTitle() {
        if (currentProfile == null) {
            return;
        }

        String version = getVersion();
        int stamina = StaminaService.getServices().getCurrentStamina(currentProfile.getId());
        String title = String.format("Whiteout Survival Bot v%s - %s [Stamina: %d]",
                                    version,
                                    currentProfile.getName(),
                                    stamina);

        Platform.runLater(() -> stage.setTitle(title));
    }

    public void onBotStateChange(DTOBotState botState) {
        if (botState != null) {
            if (botState.getRunning()) {
                if (botState.getPaused() != null && botState.getPaused()) {
                    // Bot is running but paused
                    buttonStartStop.setText("Stop");
                    buttonStartStop.setDisable(false);
                    allQueuesPaused = true;
                    buttonPauseResume.setDisable(false);
                    estado = true;
                    updatePauseButtonState();
                    refreshPauseMenuItems();
                } else {
                    // Bot is running and active
                    buttonStartStop.setText("Stop");
                    buttonStartStop.setDisable(false);
                    allQueuesPaused = false;
                    buttonPauseResume.setDisable(false);
                    estado = true;
                    updatePauseButtonState();
                    refreshPauseMenuItems();
                }
            } else {
                // Bot is stopped
                buttonStartStop.setText("Start Bot");
                buttonStartStop.setDisable(false);
                buttonPauseResume.setDisable(true);
                resetPauseStates();
                estado = false;
            }
            // Update styles after any state change
            updateStartStopButtonStyle();
            updatePauseButtonStyle();
        }
    }

    public void onQueueStateChange(DTOQueueState queueState) {
        if (queueState == null) {
            return;
        }

        if (queueState.getActiveQueues() != null) {
            updateActiveQueueStates(queueState.getActiveQueues());
        }

        if (queueState.getProfileId() == null) {
            activeQueueStates.values().forEach(state -> state.setPaused(queueState.isPaused()));
        } else {
            DTOQueueProfileState profileState = activeQueueStates.get(queueState.getProfileId());
            if (profileState != null) {
                profileState.setPaused(queueState.isPaused());
            }
        }

        updateAggregatedPauseStates();

        if (estado && (!activeQueueStates.isEmpty() || queueState.getProfileId() != null)) {
            buttonPauseResume.setDisable(false);
        }

        refreshPauseMenuItems();
        updatePauseButtonState();
    }

    @FXML
    public void handleButtonStartStop(ActionEvent event) {
        Thread startStopThread = Thread.ofVirtual().unstarted(() -> {
            if (!estado) {
                Platform.runLater(() -> {buttonStartStop.setText("Starting..."); buttonStartStop.setDisable(true);});
                actionController.startBot();
            } else {
                Platform.runLater(() -> {buttonStartStop.setText("Stopping..."); buttonStartStop.setDisable(true); buttonPauseResume.setDisable(true);});
                actionController.stopBot();
            }
        });
        startStopThread.setName( "Start-Stop-Thread");
        startStopThread.start();
    }

    @FXML
    public void handleButtonPauseResume(ActionEvent event) {
        toggleAllQueues();
    }

    @FXML
    private void handleToggleCurrentQueue(ActionEvent event) {
        toggleCurrentQueue();
    }

    @FXML
    private void handleToggleAllQueues(ActionEvent event) {
        toggleAllQueues();
    }

    private void handleToggleSpecificQueue(Long profileId) {
        toggleSpecificQueue(profileId, true);
    }

    private void toggleAllQueues() {
        if (!estado) {
            return;
        }

        if (!allQueuesPaused) {
            actionController.pauseAllQueues();
            setAllQueuesPausedLocally(true);
        } else {
            actionController.resumeAllQueues();
            setAllQueuesPausedLocally(false);
        }
    }

    private void toggleCurrentQueue() {
        if (!estado) {
            return;
        }

        ProfileAux selectedProfile = currentProfile != null ? currentProfile : getSelectedProfile();
        if (selectedProfile == null) {
            showProfileSelectionWarning();
            return;
        }

        toggleSpecificQueue(selectedProfile.getId(), true);
    }

    private void toggleSpecificQueue(Long profileId, boolean showWarnings) {
        if (!estado) {
            return;
        }

        if (profileId == null) {
            if (showWarnings) {
                showQueueUnavailableWarning();
            }
            return;
        }

        DTOQueueProfileState targetState = activeQueueStates.get(profileId);
        if (targetState == null) {
            if (showWarnings) {
                showQueueUnavailableWarning();
            }
            return;
        }

        ProfileAux targetProfile = findProfileById(profileId);
        if (targetProfile == null) {
            if (showWarnings) {
                showQueueUnavailableWarning();
            }
            return;
        }

        if (!targetState.isPaused()) {
            actionController.pauseQueue(targetProfile);
            setQueuePausedLocally(profileId, true);
        } else {
            actionController.resumeQueue(targetProfile);
            setQueuePausedLocally(profileId, false);
        }
    }

    private void configurePauseMenu() {
        refreshPauseMenuItems();
        updatePauseButtonState();
    }

    private void updateActiveQueueStates(List<DTOQueueProfileState> queueProfiles) {
        activeQueueStates.clear();
        if (queueProfiles == null) {
            return;
        }

        queueProfiles.stream()
            .filter(state -> state != null && state.getProfileId() != null)
            .forEach(state -> activeQueueStates.put(state.getProfileId(),
                    new DTOQueueProfileState(state.getProfileId(), state.getProfileName(), state.isPaused())));
    }

    private void updateAggregatedPauseStates() {
        if (activeQueueStates.isEmpty()) {
            allQueuesPaused = false;
        } else {
            allQueuesPaused = activeQueueStates.values().stream().allMatch(DTOQueueProfileState::isPaused);
        }
    }

    private void refreshPauseMenuItems() {
        if (buttonPauseResume == null) {
            return;
        }

        List<MenuItem> items = new ArrayList<>();

        if (menuToggleAllQueues != null) {
            menuToggleAllQueues.setText(allQueuesPaused ? "Resume" : "Pause");
            menuToggleAllQueues.setDisable(!estado);
            items.add(menuToggleAllQueues);
        }

        List<DTOQueueProfileState> queueStates = new ArrayList<>(activeQueueStates.values());
        queueStates.sort(Comparator.comparing(DTOQueueProfileState::getProfileName, String.CASE_INSENSITIVE_ORDER));

        if (!queueStates.isEmpty()) {
            items.add(new SeparatorMenuItem());
            for (DTOQueueProfileState state : queueStates) {
                MenuItem item = createQueueMenuItem(state);
                items.add(item);
            }
        }

        buttonPauseResume.getItems().setAll(items);
    }

    private MenuItem createQueueMenuItem(DTOQueueProfileState state) {
        MenuItem item = new MenuItem(formatQueueMenuItemLabel(state));
        item.setOnAction(evt -> handleToggleSpecificQueue(state.getProfileId()));
        item.setDisable(!estado);
        return item;
    }

    private String formatQueueMenuItemLabel(DTOQueueProfileState state) {
        if (state == null) {
            return "Toggle queue";
        }

        String profileName = state.getProfileName() != null ? state.getProfileName() : String.valueOf(state.getProfileId());
        return (state.isPaused() ? "Resume " : "Pause ") + profileName;
    }

    private void updatePauseButtonState() {
        if (buttonPauseResume == null) {
            return;
        }

        String text = allQueuesPaused ? "Resume All" : "Pause All";
        buttonPauseResume.setText(text);
        // Also refresh its style to reflect pause/resume color
        updatePauseButtonStyle();
    }


    private void resetPauseStates() {
        allQueuesPaused = false;
        activeQueueStates.clear();
        refreshPauseMenuItems();
        updatePauseButtonState();
        updatePauseButtonStyle();
    }

    private void showProfileSelectionWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Profile selection required");
        alert.setHeaderText(null);
        alert.setContentText("Please select a profile to control its queue.");
        alert.showAndWait();
    }

    private void showQueueUnavailableWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Queue not available");
        alert.setHeaderText(null);
        alert.setContentText("The selected queue is not currently running.");
        alert.showAndWait();
    }

    private ProfileAux findProfileById(Long profileId) {
        if (profileId == null || profileComboBox == null) {
            return null;
        }

        for (ProfileAux profile : profileComboBox.getItems()) {
            if (profile != null && profileId.equals(profile.getId())) {
                return profile;
            }
        }
        return null;
    }

    private void setAllQueuesPausedLocally(boolean paused) {
        activeQueueStates.values().forEach(state -> state.setPaused(paused));
        updateAggregatedPauseStates();
        refreshPauseMenuItems();
        updatePauseButtonState();
    }

    private void setQueuePausedLocally(Long profileId, boolean paused) {
        if (profileId == null) {
            return;
        }

        DTOQueueProfileState state = activeQueueStates.get(profileId);
        if (state != null) {
            state.setPaused(paused);
        }

        updateAggregatedPauseStates();
        refreshPauseMenuItems();
        updatePauseButtonState();
    }

    private Button addButton(String fxmlName, String title, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(controller.getClass().getResource(fxmlName + ".fxml"));
            loader.setController(controller);
            Parent root = loader.load();

            Button button = new Button(title);
            button.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(button, Priority.ALWAYS);

            // Asigna clases de estilo para el menú lateral
            button.getStyleClass().add("square-button");
            button.getStyleClass().add("side-menu-button");

            button.setOnAction(e -> {
                // Limpia el contenido actual y agrega el nuevo panel
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

            // Add horizontal margin (left/right = 10px) so every lateral menu button keeps consistent spacing
            VBox.setMargin(button, new Insets(10, 10, 0, 10));
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

    private record ModuleDefinition(String fxmlName, String buttonTitle, Supplier<Object> controllerSupplier) {

        public Object createController(IProfileChangeObserver profileObserver) {
            Object controller = controllerSupplier.get();
            if (controller instanceof IProfileObserverInjectable) {
                ((IProfileObserverInjectable) controller).setProfileObserver(profileObserver);
            }
            return controller;
        }


    }

    // Styling helpers for bottom action buttons
    private void updateStartStopButtonStyle() {
        if (buttonStartStop == null) return;
        // Remove any existing start/stop classes first
        buttonStartStop.getStyleClass().removeAll("btn-start", "btn-stop");
        // Also ensure legacy 'start-button' class does not conflict when in STOP state
        if (estado) {
            // Running -> show Stop (red)
            buttonStartStop.getStyleClass().remove("start-button");
            if (!buttonStartStop.getStyleClass().contains("btn-stop")) {
                buttonStartStop.getStyleClass().add("btn-stop");
            }
            buttonStartStop.setGraphic(buildStopIcon(Color.WHITE));
        } else {
            // Stopped -> show Start (yellow)
            // Ensure 'start-button' class present so start style applies
            if (!buttonStartStop.getStyleClass().contains("start-button")) {
                buttonStartStop.getStyleClass().add("start-button");
            }
            if (!buttonStartStop.getStyleClass().contains("btn-start")) {
                buttonStartStop.getStyleClass().add("btn-start");
            }
            buttonStartStop.setGraphic(buildPlayIcon(Color.BLACK));
        }
    }

    // Build a simple right-pointing play triangle (hollow)
    private Node buildPlayIcon(Color color) {
        Polygon triangle = new Polygon(
                0.0, 0.0,
                0.0, 12.0,
                10.0, 6.0
        );
        triangle.setFill(Color.TRANSPARENT);
        triangle.setStroke(color);
        triangle.setStrokeWidth(2.0);
        return triangle;
    }

    // Build a simple stop square (hollow)
    private Node buildStopIcon(Color color) {
        Rectangle square = new Rectangle(10, 10);
        square.setArcWidth(2);
        square.setArcHeight(2);
        square.setFill(Color.TRANSPARENT);
        square.setStroke(color);
        square.setStrokeWidth(2.0);
        return square;
    }

    private void updatePauseButtonStyle() {
        if (buttonPauseResume == null) return;
        // Ensure pause/resume classes are kept up-to-date even when the control is disabled.
        buttonPauseResume.getStyleClass().removeAll("btn-pause", "btn-resume");
        if (allQueuesPaused) {
            buttonPauseResume.getStyleClass().add("btn-resume");
        } else {
            buttonPauseResume.getStyleClass().add("btn-pause");
        }
    }

}
