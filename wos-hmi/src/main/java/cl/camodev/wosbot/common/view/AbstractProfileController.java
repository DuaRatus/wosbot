package cl.camodev.wosbot.common.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cl.camodev.wosbot.console.enumerable.EnumConfigCategory;
import cl.camodev.wosbot.console.enumerable.EnumConfigurationKey;
import cl.camodev.wosbot.profile.model.IProfileChangeObserver;
import cl.camodev.wosbot.profile.model.IProfileLoadListener;
import cl.camodev.wosbot.profile.model.IProfileObserverInjectable;
import cl.camodev.wosbot.profile.model.ProfileAux;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public abstract class AbstractProfileController implements IProfileLoadListener, IProfileObserverInjectable {

	protected final Map<CheckBox, EnumConfigurationKey> checkBoxMappings = new HashMap<>();
	protected final Map<TextField, EnumConfigurationKey> textFieldMappings = new HashMap<>();
	protected final Map<RadioButton, EnumConfigurationKey> radioButtonMappings = new HashMap<>();
	protected final Map<ComboBox<?>, EnumConfigurationKey> comboBoxMappings = new HashMap<>();

	// Variables para soportar la creación dinámica de interfaz
	protected Map<EnumConfigurationKey, CheckBox> checkboxes = new HashMap<>();
	protected Map<EnumConfigurationKey, TextField> textFields = new HashMap<>();
	protected Map<EnumConfigurationKey, ComboBox<?>> comboBoxes = new HashMap<>();
	protected Map<EnumConfigurationKey, VBox> childContainers = new HashMap<>();

	protected IProfileChangeObserver profileObserver;
	protected boolean isLoadingProfile = false;

	// Variables para construcción de UI dinámica
	protected VBox mainContainer;

	@Override
	public void setProfileObserver(IProfileChangeObserver observer) {
		this.profileObserver = observer;
	}

	protected void initializeChangeEvents() {
		checkBoxMappings.forEach(this::setupCheckBoxListener);
		textFieldMappings.forEach(this::setupTextFieldUpdateOnFocusOrEnter);
		radioButtonMappings.forEach(this::setupRadioButtonListener);
		comboBoxMappings.forEach(this::setupComboBoxListener);
	}

	protected void createToggleGroup(RadioButton... radioButtons) {
		ToggleGroup toggleGroup = new ToggleGroup();
		for (RadioButton radioButton : radioButtons) {
			radioButton.setToggleGroup(toggleGroup);
		}
	}

	protected void setupRadioButtonListener(RadioButton radioButton, EnumConfigurationKey configKey) {
		radioButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
			if (!isLoadingProfile) {
				profileObserver.notifyProfileChange(configKey, newVal);
			}
		});
	}

	protected void setupCheckBoxListener(CheckBox checkBox, EnumConfigurationKey configKey) {
		checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
			if (!isLoadingProfile) {
				profileObserver.notifyProfileChange(configKey, newVal);
			}
		});
	}

	protected void setupTextFieldUpdateOnFocusOrEnter(TextField textField, EnumConfigurationKey configKey) {
		textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
			if (!isNowFocused && !isLoadingProfile) {
				updateProfile(textField, configKey);
			}
		});

		textField.setOnAction(event -> {
			if (!isLoadingProfile) {
				updateProfile(textField, configKey);
			}
		});
	}

	protected void setupComboBoxListener(ComboBox<?> comboBox, EnumConfigurationKey configKey) {
		comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (!isLoadingProfile && newVal != null) {
				profileObserver.notifyProfileChange(configKey, newVal);
			}
		});
	}

	private void updateProfile(TextField textField, EnumConfigurationKey configKey) {
		String newVal = textField.getText();
		if (isValidPositiveInteger(newVal)) {
			profileObserver.notifyProfileChange(configKey, Integer.valueOf(newVal));
		} else {
			textField.setText(configKey.getDefaultValue());
		}
	}

	private boolean isValidPositiveInteger(String value) {
		if (value == null || value.isEmpty()) {
			return false;
		}
		try {
			int number = Integer.parseInt(value);
			return number >= 0 && number <= 999;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public void onProfileLoad(ProfileAux profile) {
		isLoadingProfile = true;
		try {
			checkBoxMappings.forEach((checkBox, key) -> {
				Boolean value = profile.getConfiguration(key);
				checkBox.setSelected(value);
			});

			textFieldMappings.forEach((textField, key) -> {
				Integer value = profile.getConfiguration(key);
				textField.setText(String.valueOf(value));
			});

			radioButtonMappings.forEach((radioButton, key) -> {
				Boolean value = profile.getConfiguration(key);
				radioButton.setSelected(value);
			});

			comboBoxMappings.forEach((comboBox, key) -> {
				Object value = profile.getConfiguration(key);
				if (value != null) {
					@SuppressWarnings("unchecked")
					ComboBox<Object> uncheckedComboBox = (ComboBox<Object>) comboBox;
					uncheckedComboBox.setValue(value);
				}
			});

		} finally {
			isLoadingProfile = false;
		}
	}

	/**
	 * Inicializa el contenedor principal y configura la UI dinámica en un ScrollPane
	 */
	protected void setupDynamicUI(ScrollPane scrollPane, EnumConfigCategory category) {
		// Crear el contenedor principal
		mainContainer = new VBox();
		mainContainer.setSpacing(8.0);
		mainContainer.setPadding(new Insets(10.0));

		scrollPane.setContent(mainContainer);
		scrollPane.setFitToWidth(true);

		// Construir la UI dinámicamente basada en la categoría
		buildDynamicUI(category);

		// Inicializar eventos de cambio después de que se haya construido la UI
		initializeChangeEvents();
	}

	/**
	 * Construye la interfaz de usuario dinámicamente basada en las configuraciones
	 */
	protected void buildDynamicUI(EnumConfigCategory category) {
		// Obtener todos los elementos raíz (sin padre) de la categoría especificada
		List<EnumConfigurationKey> rootConfigs = Arrays.stream(EnumConfigurationKey.values())
				.filter(key -> key.getCategory() == category && key.isRoot())
				.collect(Collectors.toList());

		// Ordenar las categorías raíz (opcional)
		Collections.sort(rootConfigs, Comparator.comparing(EnumConfigurationKey::getTitle));

		// Para cada elemento raíz, crear un TitledPane con su checkbox padre y añadirlo directamente al contenedor principal
		for (EnumConfigurationKey rootConfig : rootConfigs) {
			TitledPane categoryPane = createCategoryPane(rootConfig);
			mainContainer.getChildren().add(categoryPane);
		}
	}

	/**
	 * Crea un panel de categoría (TitledPane) para un elemento raíz
	 */
	protected TitledPane createCategoryPane(EnumConfigurationKey rootConfig) {
		// Crear el contenedor para el checkbox y su descripción
		VBox headerContent = new VBox();
		headerContent.setSpacing(1.0);

		// Crear el checkbox padre para esta categoría
		CheckBox parentCheckbox = new CheckBox();
		parentCheckbox.setText(rootConfig.getTitle());
		parentCheckbox.setMnemonicParsing(false);
		parentCheckbox.getStyleClass().add("parent-checkbox");

		// Añadir el checkbox al contenedor
		headerContent.getChildren().add(parentCheckbox);

		// Añadir la descripción como un pequeño label si existe
		if (rootConfig.getDescription() != null && !rootConfig.getDescription().isEmpty()) {
			Label descriptionLabel = createDescriptionLabel(rootConfig.getDescription());
			headerContent.getChildren().add(descriptionLabel);
		}

		// Guardar referencia al checkbox
		checkboxes.put(rootConfig, parentCheckbox);
		checkBoxMappings.put(parentCheckbox, rootConfig);

		// Crear el TitledPane
		TitledPane titledPane = new TitledPane();
		titledPane.setGraphic(headerContent);
		titledPane.setText(null); // No text, just use the checkbox and description
		titledPane.setAnimated(true);
		titledPane.setCollapsible(true);
		titledPane.setMaxWidth(Double.MAX_VALUE);
		titledPane.getStyleClass().add("titled-pane");

		// Crear el contenedor para los elementos hijo
		VBox childBox = new VBox();
		childBox.setSpacing(6.0);
		childBox.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));

		// Guardar referencia al contenedor
		childContainers.put(rootConfig, childBox);

		// Configurar el contenido del TitledPane
		titledPane.setContent(childBox);

		// Buscar y crear los elementos hijo para este elemento raíz
		createChildElements(rootConfig, childBox);

		// Configurar la relación padre-hijo para este checkbox padre
		setupParentChildRelationship(parentCheckbox, childBox);

		return titledPane;
	}

	/**
	 * Crea los elementos hijo para un elemento padre
	 */
	protected void createChildElements(EnumConfigurationKey parentConfig, VBox parentContainer) {
		// Obtener todos los elementos hijo directos de este padre
		List<EnumConfigurationKey> directChildren = Arrays.stream(EnumConfigurationKey.values())
				.filter(key -> key.getCategory() == parentConfig.getCategory() &&
							   key.hasParent() &&
							   key.getParent() == parentConfig)
				.collect(Collectors.toList());

		// Si no hay hijos, mostrar un mensaje indicando que no hay configuraciones adicionales
		if (directChildren.isEmpty()) {
			Label noConfigLabel = new Label("No additional config");
			noConfigLabel.getStyleClass().add("no-config-label");
			parentContainer.getChildren().add(noConfigLabel);
			return;
		}

		// Ordenar los hijos (opcional)
		Collections.sort(directChildren, Comparator.comparing(EnumConfigurationKey::getTitle));

		// Crear un FlowPane para organizar las tarjetas horizontalmente cuando hay espacio
		FlowPane flowPane = new FlowPane();
		flowPane.setHgap(10); // Espacio horizontal entre tarjetas
		flowPane.setVgap(10); // Espacio vertical entre filas de tarjetas
		flowPane.setPrefWrapLength(1200); // Ajustar según sea necesario
		flowPane.setStyle("-fx-background-color: transparent;");
		parentContainer.getChildren().add(flowPane);

		// Para cada hijo directo, crear su representación en la UI
		for (EnumConfigurationKey childConfig : directChildren) {
			VBox childCard = createChildCard(childConfig);
			flowPane.getChildren().add(childCard);

			// Si este hijo también tiene hijos, procesarlos recursivamente
			List<EnumConfigurationKey> grandchildren = Arrays.stream(EnumConfigurationKey.values())
					.filter(key -> key.getCategory() == childConfig.getCategory() &&
								   key.hasParent() &&
								   key.getParent() == childConfig)
					.collect(Collectors.toList());

			if (!grandchildren.isEmpty()) {
				VBox grandchildContainer = new VBox();
				grandchildContainer.setSpacing(5.0);
				grandchildContainer.setPadding(new Insets(0, 0, 0, 20.0)); // Indentación para los nietos

				childCard.getChildren().add(grandchildContainer);
				childContainers.put(childConfig, grandchildContainer);

				// Ordenar los nietos (opcional)
				Collections.sort(grandchildren, Comparator.comparing(EnumConfigurationKey::getTitle));

				// Procesar recursivamente los nietos
				for (EnumConfigurationKey grandchild : grandchildren) {
					Node grandchildNode = createConfigControl(grandchild);
					if (grandchildNode != null) {
						grandchildContainer.getChildren().add(grandchildNode);
					}
				}
			}
		}
	}

	/**
	 * Crea una tarjeta para un elemento hijo
	 */
	protected VBox createChildCard(EnumConfigurationKey config) {
		// Crear el contenedor de la tarjeta
		VBox card = new VBox();
		card.setSpacing(3.0);
		card.setPadding(new Insets(8.0, 10.0, 8.0, 10.0));
		card.getStyleClass().add("child-card");

		// Establecer dimensiones fijas para todas las tarjetas
		card.setPrefWidth(300);  // Ancho fijo para todas las tarjetas
		card.setMinWidth(300);   // Ancho mínimo igual al preferido
		card.setPrefHeight(100); // Altura preferida
		card.setMinHeight(90);   // Altura mínima

		// Crear el control apropiado según el tipo de configuración
		Node control = createConfigControl(config);
		if (control != null) {
			card.getChildren().add(control);

			// Asegurar que el contenido se expande para llenar la tarjeta
			VBox.setVgrow(control, Priority.ALWAYS);
		}

		return card;
	}

	/**
	 * Crea el control apropiado para una configuración específica
	 */
	protected Node createConfigControl(EnumConfigurationKey config) {
		Class<?> type = config.getType();

		// Crear un VBox para contener el control y su descripción
		VBox controlContainer = new VBox();
		controlContainer.setSpacing(3.0);

		if (type == Boolean.class) {
			// Crear un checkbox para valores booleanos
			CheckBox checkbox = new CheckBox(config.getTitle());
			checkbox.setMnemonicParsing(false);
			checkbox.getStyleClass().add("child-checkbox");

			// Guardar referencia y mapear
			checkboxes.put(config, checkbox);
			checkBoxMappings.put(checkbox, config);

			// Añadir el checkbox al contenedor
			controlContainer.getChildren().add(checkbox);

			// Añadir la descripción como un pequeño label si existe
			if (config.getDescription() != null && !config.getDescription().isEmpty()) {
				Label descriptionLabel = createDescriptionLabel(config.getDescription());
				controlContainer.getChildren().add(descriptionLabel);
			}

			return controlContainer;

		} else if (type == Integer.class) {
			// Para valores enteros, verificar si tiene valores válidos (para combobox)
			String[] validValues = config.getValidValues();
			if (validValues != null && validValues.length > 0) {
				// Crear un combobox para valores enteros con opciones predefinidas
				ComboBox<Integer> comboBox = new ComboBox<>();
				comboBox.getStyleClass().add("combo-box");
				List<Integer> values = Stream.of(validValues)
						.map(Integer::parseInt)
						.collect(Collectors.toList());
				comboBox.setItems(FXCollections.observableArrayList(values));

				// Configurar etiqueta y layout para el control
				HBox controlLayout = new HBox();
				controlLayout.setSpacing(5.0);
				controlLayout.setAlignment(Pos.CENTER_LEFT);
				controlLayout.getStyleClass().add("control-container");
				controlLayout.getChildren().addAll(
					new Label(config.getTitle()),
					comboBox
				);

				// Añadir el layout del control al contenedor
				controlContainer.getChildren().add(controlLayout);

				// Añadir la descripción como un pequeño label si existe
				if (config.getDescription() != null && !config.getDescription().isEmpty()) {
					Label descriptionLabel = createDescriptionLabel(config.getDescription());
					controlContainer.getChildren().add(descriptionLabel);
				}

				// Guardar referencia y mapear
				comboBoxes.put(config, comboBox);
				comboBoxMappings.put(comboBox, config);

				return controlContainer;

			} else {
				// Crear un campo de texto para valores enteros sin opciones predefinidas
				TextField textField = new TextField();
				textField.getStyleClass().add("text-field");

				// Configurar etiqueta y layout para el control
				HBox controlLayout = new HBox();
				controlLayout.setSpacing(5.0);
				controlLayout.setAlignment(Pos.CENTER_LEFT);
				controlLayout.getStyleClass().add("control-container");
				controlLayout.getChildren().addAll(
					new Label(config.getTitle()),
					textField
				);

				// Añadir el layout del control al contenedor
				controlContainer.getChildren().add(controlLayout);

				// Añadir la descripción como un pequeño label si existe
				if (config.getDescription() != null && !config.getDescription().isEmpty()) {
					Label descriptionLabel = createDescriptionLabel(config.getDescription());
					controlContainer.getChildren().add(descriptionLabel);
				}

				// Guardar referencia y mapear
				textFields.put(config, textField);
				textFieldMappings.put(textField, config);

				return controlContainer;
			}

		} else if (type == String.class) {
			// Para valores string, verificar si tiene valores válidos (para combobox)
			String[] validValues = config.getValidValues();
			if (validValues != null && validValues.length > 0) {
				// Crear un combobox para strings con opciones predefinidas
				ComboBox<String> comboBox = new ComboBox<>();
				comboBox.getStyleClass().add("combo-box");
				comboBox.setItems(FXCollections.observableArrayList(validValues));

				// Configurar etiqueta y layout para el control
				HBox controlLayout = new HBox();
				controlLayout.setSpacing(5.0);
				controlLayout.setAlignment(Pos.CENTER_LEFT);
				controlLayout.getStyleClass().add("control-container");
				controlLayout.getChildren().addAll(
					new Label(config.getTitle()),
					comboBox
				);

				// Añadir el layout del control al contenedor
				controlContainer.getChildren().add(controlLayout);

				// Añadir la descripción como un pequeño label si existe
				if (config.getDescription() != null && !config.getDescription().isEmpty()) {
					Label descriptionLabel = createDescriptionLabel(config.getDescription());
					controlContainer.getChildren().add(descriptionLabel);
				}

				// Guardar referencia y mapear
				comboBoxes.put(config, comboBox);
				comboBoxMappings.put(comboBox, config);

				return controlContainer;

			} else {
				// Crear un campo de texto para strings sin opciones predefinidas
				TextField textField = new TextField();
				textField.getStyleClass().add("text-field");

				// Configurar etiqueta y layout para el control
				HBox controlLayout = new HBox();
				controlLayout.setSpacing(5.0);
				controlLayout.setAlignment(Pos.CENTER_LEFT);
				controlLayout.getStyleClass().add("control-container");
				controlLayout.getChildren().addAll(
					new Label(config.getTitle()),
					textField
				);

				// Añadir el layout del control al contenedor
				controlContainer.getChildren().add(controlLayout);

				// Añadir la descripción como un pequeño label si existe
				if (config.getDescription() != null && !config.getDescription().isEmpty()) {
					Label descriptionLabel = createDescriptionLabel(config.getDescription());
					controlContainer.getChildren().add(descriptionLabel);
				}

				// Guardar referencia y mapear
				textFields.put(config, textField);
				textFieldMappings.put(textField, config);

				return controlContainer;
			}
		}

		return null;
	}

	/**
	 * Crea una etiqueta de descripción con estilo adecuado
	 */
	protected Label createDescriptionLabel(String description) {
		Label label = new Label(description);
		label.getStyleClass().add("description-label");
		label.setWrapText(true);
		return label;
	}

	/**
	 * Configura la relación padre-hijo entre un checkbox padre y sus elementos hijo
	 */
	protected void setupParentChildRelationship(CheckBox parentCheckBox, VBox childrenContainer) {
		// Asegurar estado inicial consistente
		updateChildrenState(childrenContainer, parentCheckBox.isSelected(), parentCheckBox.isDisabled());

		// Agregar listener para cambios en la selección
		parentCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				updateChildrenState(childrenContainer, newValue, parentCheckBox.isDisabled());
			}
		});

		// También escuchar cambios en la propiedad disabled
		parentCheckBox.disableProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				updateChildrenState(childrenContainer, parentCheckBox.isSelected(), newValue);
			}
		});
	}

	/**
	 * Actualiza el estado de todos los hijos en el contenedor según el estado del padre
	 */
	protected void updateChildrenState(VBox container, boolean parentSelected, boolean parentDisabled) {
		// Deshabilitar el contenedor si el padre está deshabilitado o no seleccionado
		container.setDisable(parentDisabled || !parentSelected);

		// Procesar recursivamente todos los hijos
		for (Node node : container.getChildren()) {
			// Si este hijo es también un contenedor, procesar sus hijos
			if (node instanceof VBox) {
				processChildContainer((VBox) node, parentSelected, parentDisabled);
			}

			// Si este es un checkbox, actualizar su estado
			if (node instanceof CheckBox) {
				// Solo configurar el estado disabled, no cambiar el estado selected
				node.setDisable(parentDisabled || !parentSelected);
			}

			// Si este es un HBox (contenedor de control + label), procesar sus elementos
			if (node instanceof HBox) {
				for (Node child : ((HBox) node).getChildren()) {
					if (child instanceof CheckBox) {
						// Solo configurar el estado disabled, no cambiar el estado selected
						child.setDisable(parentDisabled || !parentSelected);
					}
				}
			}
		}
	}

	/**
	 * Procesa un contenedor hijo para actualizar sus estados
	 */
	protected void processChildContainer(VBox childContainer, boolean parentSelected, boolean parentDisabled) {
		// Buscar cualquier checkbox padre en este contenedor
		CheckBox containerParentCheckBox = null;

		for (Node node : childContainer.getChildren()) {
			if (node instanceof CheckBox) {
				containerParentCheckBox = (CheckBox) node;
				break; // Asumir que el primer checkbox es el padre
			}

			if (node instanceof HBox) {
				for (Node child : ((HBox) node).getChildren()) {
					if (child instanceof CheckBox) {
						containerParentCheckBox = (CheckBox) child;
						break;
					}
				}
				if (containerParentCheckBox != null) break;
			}
		}

		// Si encontramos un checkbox padre, usar su estado junto con el estado del padre original
		if (containerParentCheckBox != null) {
			boolean effectiveSelected = parentSelected && containerParentCheckBox.isSelected();
			boolean effectiveDisabled = parentDisabled || !parentSelected;

			// Actualizar los hijos de este contenedor
			for (Node node : childContainer.getChildren()) {
				if (node != containerParentCheckBox) { // Saltar el padre mismo
					node.setDisable(effectiveDisabled || !effectiveSelected);

					// Si este es otro contenedor, procesar recursivamente
					if (node instanceof VBox) {
						processChildContainer((VBox) node, effectiveSelected, effectiveDisabled);
					}
				}
			}
		}
	}
}
