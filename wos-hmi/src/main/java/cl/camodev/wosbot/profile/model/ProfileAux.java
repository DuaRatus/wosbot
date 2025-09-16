package cl.camodev.wosbot.profile.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cl.camodev.wosbot.console.enumerable.EnumConfigurationKey;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProfileAux {

	private final LongProperty id;
	private final StringProperty name;
	private final StringProperty emulatorNumber;
	private final BooleanProperty enabled;
	private final LongProperty priority;
	private final StringProperty status;
	private final LongProperty reconnectionTime;

	private final List<ConfigAux> configs = new ArrayList<>();

	public ProfileAux(Long id, String name, String emulatorNumber, boolean enabled, Long priority, String status, Long reconnectionTime) {
		this.id = new SimpleLongProperty(id);
		this.name = new SimpleStringProperty(name);
		this.emulatorNumber = new SimpleStringProperty(emulatorNumber);
		this.enabled = new SimpleBooleanProperty(enabled);
		this.priority = new SimpleLongProperty(priority);
		this.status = new SimpleStringProperty(status);
		this.reconnectionTime = new SimpleLongProperty(reconnectionTime);
	}

	// Métodos para la propiedad 'id'
	public Long getId() {
		return id.get();
	}

	public void setId(Long id) {
		this.id.set(id);
	}

	public LongProperty idProperty() {
		return id;
	}

	// Métodos para la propiedad 'name'
	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public StringProperty nameProperty() {
		return name;
	}

	// Métodos para la propiedad 'emulatorNumber'
	public String getEmulatorNumber() {
		return emulatorNumber.get();
	}

	public void setEmulatorNumber(String emulatorNumber) {
		this.emulatorNumber.set(emulatorNumber);
	}

	public StringProperty emulatorNumberProperty() {
		return emulatorNumber;
	}

	// Métodos para la propiedad 'enabled'
	public Boolean isEnabled() {
		return enabled.get();
	}

	public void setEnabled(boolean enabled) {
		this.enabled.set(enabled);
	}

	public BooleanProperty enabledProperty() {
		return enabled;
	}

	// Métodos para la propiedad 'status'
	public String getStatus() {
		return status.get();
	}

	public void setStatus(String status) {
		this.status.set(status);
	}

	public StringProperty statusProperty() {
		return status;
	}

	// Métodos para la propiedad 'priority'
	public Long getPriority() {
		return priority.get();
	}

	public void setPriority(Long priority) {
		this.priority.set(priority != null ? priority : 50L);
	}

	public LongProperty priorityProperty() {
		return priority;
	}

	public Long getReconnectionTime() {
		return reconnectionTime.get();
	}

	public void setReconnectionTime(Long reconnectionTime) {
		this.reconnectionTime.set(reconnectionTime);
	}

	public LongProperty reconnectionTimeProperty() {
		return reconnectionTime;
	}

	@SuppressWarnings("unchecked")
	public <T> T getConfiguration(EnumConfigurationKey key) {
		Optional<ConfigAux> config = configs.stream()
				.filter(c -> c.getName().equals(key.name()))
				.findFirst();

		if (config.isPresent()) {
			return (T) key.parseValue(config.get().getValue(), key.getType());
		} else {
			return (T) key.parseValue(key.getDefaultValue(), key.getType());
		}
	}

	public List<ConfigAux> getConfigs() {
		return configs;
	}

	/**
	 * Obtiene el valor de una configuración específica utilizando EnumConfigurationKey. Es un método genérico que devuelve el tipo correcto
	 * basado en la clave.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getConfig(EnumConfigurationKey key) {
		Optional<ConfigAux> configOptional = configs.stream().filter(config -> config.getName().equalsIgnoreCase(key.name())).findFirst();

		if (!configOptional.isPresent()) {
			ConfigAux defaultConfig = new ConfigAux(key.name(), key.getDefaultValue());
			configs.add(defaultConfig);
		}
		String valor = configOptional.map(ConfigAux::getValue).orElse(key.getDefaultValue());

		return (T) key.parseValue(valor, key.getType());
	}

	public <T> void setConfig(EnumConfigurationKey key, T value) {
		String valorAAlmacenar = value.toString();
		Optional<ConfigAux> configOptional = configs.stream().filter(config -> config.getName().equalsIgnoreCase(key.name())).findFirst();

		if (configOptional.isPresent()) {
			configOptional.get().setValue(valorAAlmacenar);
		} else {
			ConfigAux nuevaConfig = new ConfigAux(key.name(), valorAAlmacenar);
			configs.add(nuevaConfig);
		}
	}
}
