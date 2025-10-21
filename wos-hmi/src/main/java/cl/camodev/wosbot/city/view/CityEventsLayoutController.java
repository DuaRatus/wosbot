package cl.camodev.wosbot.city.view;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

import cl.camodev.wosbot.common.view.AbstractProfileController;
import cl.camodev.wosbot.console.enumerable.EnumConfigurationKey;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;

public class CityEventsLayoutController extends AbstractProfileController {

	// Original CityEvents elements
	@FXML
	private CheckBox checkBoxCrystalLabFC, checkBoxDailyDiscountedRFC, checkBoxExplorationChest, checkBoxMailRewards,
			checkBoxLifeEssence, checkBoxWeeklyScroll, checkBoxDailyMission, checkBoxAutoScheduleDailyMission,
			checkBoxWarAcademyShards;

	@FXML
	private TextField textfieldExplorationOffset, textfieldMailOffset, textfieldLifeEssenceOffset,
			textfieldDailyMissionOffset;

	@FXML
	private ComboBox<Integer> comboBoxMondayRefinements;

	// Extra CityEvents elements
	@FXML
	private CheckBox checkBoxDailyVipRewards, checkBoxBuyMonthlyVip, checkBoxStorehouseChest, checkBoxDailyLabyrinth,
			checkBoxHeroRecruitment, checkBoxTrekSupplies, checkBoxTrekAutomation, checkBoxArena,
			checkBoxArenaRefreshWithGems;

	@FXML
	private TextField textFieldArenaActivationHour;

	@FXML
	private ComboBox<Integer> comboBoxArenaExtraAttempts;

	@FXML
	private Label labelDateTimeError;

	@FXML
	private void initialize() {
		// Original CityEvents mappings
		checkBoxMappings.put(checkBoxCrystalLabFC, EnumConfigurationKey.BOOL_CRYSTAL_LAB_FC);
		checkBoxMappings.put(checkBoxDailyDiscountedRFC, EnumConfigurationKey.BOOL_CRYSTAL_LAB_DAILY_DISCOUNTED_RFC);
		checkBoxMappings.put(checkBoxExplorationChest, EnumConfigurationKey.BOOL_EXPLORATION_CHEST);
		checkBoxMappings.put(checkBoxWarAcademyShards, EnumConfigurationKey.BOOL_WAR_ACADEMY_SHARDS);
		checkBoxMappings.put(checkBoxMailRewards, EnumConfigurationKey.MAIL_REWARDS_BOOL);
		checkBoxMappings.put(checkBoxLifeEssence, EnumConfigurationKey.LIFE_ESSENCE_BOOL);
		checkBoxMappings.put(checkBoxWeeklyScroll, EnumConfigurationKey.LIFE_ESSENCE_BUY_WEEKLY_SCROLL_BOOL);
		checkBoxMappings.put(checkBoxDailyMission, EnumConfigurationKey.DAILY_MISSION_BOOL);
		checkBoxMappings.put(checkBoxAutoScheduleDailyMission, EnumConfigurationKey.DAILY_MISSION_AUTO_SCHEDULE_BOOL);

		textFieldMappings.put(textfieldExplorationOffset, EnumConfigurationKey.INT_EXPLORATION_CHEST_OFFSET);
		textFieldMappings.put(textfieldMailOffset, EnumConfigurationKey.MAIL_REWARDS_OFFSET_INT);
		textFieldMappings.put(textfieldLifeEssenceOffset, EnumConfigurationKey.LIFE_ESSENCE_OFFSET_INT);
		textFieldMappings.put(textfieldDailyMissionOffset, EnumConfigurationKey.DAILY_MISSION_OFFSET_INT);

		comboBoxMondayRefinements.getItems().addAll(0, 20, 40, 60);
		comboBoxMappings.put(comboBoxMondayRefinements, EnumConfigurationKey.INT_WEEKLY_RFC);

		// Extra CityEvents mappings
		checkBoxMappings.put(checkBoxDailyVipRewards, EnumConfigurationKey.BOOL_VIP_POINTS);
		checkBoxMappings.put(checkBoxBuyMonthlyVip, EnumConfigurationKey.VIP_MONTHLY_BUY_BOOL);
		checkBoxMappings.put(checkBoxStorehouseChest, EnumConfigurationKey.STOREHOUSE_CHEST_BOOL);
		checkBoxMappings.put(checkBoxDailyLabyrinth, EnumConfigurationKey.DAILY_LABYRINTH_BOOL);
		checkBoxMappings.put(checkBoxHeroRecruitment, EnumConfigurationKey.BOOL_HERO_RECRUITMENT);
		checkBoxMappings.put(checkBoxTrekSupplies, EnumConfigurationKey.TUNDRA_TREK_SUPPLIES_BOOL);
		checkBoxMappings.put(checkBoxTrekAutomation, EnumConfigurationKey.TUNDRA_TREK_AUTOMATION_BOOL);
		checkBoxMappings.put(checkBoxArena, EnumConfigurationKey.ARENA_TASK_BOOL);
		checkBoxMappings.put(checkBoxArenaRefreshWithGems, EnumConfigurationKey.ARENA_TASK_REFRESH_WITH_GEMS_BOOL);

		textFieldMappings.put(textFieldArenaActivationHour, EnumConfigurationKey.ARENA_TASK_ACTIVATION_TIME_STRING);

		// Set up date/time validation for arena activation time
		textFieldArenaActivationHour.textProperty().addListener((obs, oldVal, newVal) -> {
			validateTime(newVal);
		});

		// Initialize combo box values (0 to 5 extra attempts)
		comboBoxArenaExtraAttempts.getItems().addAll(0, 1, 2, 3, 4, 5);
		comboBoxMappings.put(comboBoxArenaExtraAttempts, EnumConfigurationKey.ARENA_TASK_EXTRA_ATTEMPTS_INT);

		setupTimeFieldHelpers();
		initializeChangeEvents();
	}

	/**
	 * Validates and formats the time input field.
	 * Expected format: HH:mm (24-hour format)
	 */
	private void setupTimeFieldHelpers() {
		// Visual hints
		textFieldArenaActivationHour.setPromptText("HH:mm");
		textFieldArenaActivationHour.setTooltip(new Tooltip("Use format: HH:mm (e.g., 19:30)"));

		// TextFormatter that auto-inserts ':' and restricts to mask "##:##"
		TextFormatter<String> formatter = getTimeTextFormatter();
		textFieldArenaActivationHour.setTextFormatter(formatter);

		// Optional: normalize/pad on focus lost (e.g., "9:5" → "09:05")
		textFieldArenaActivationHour.focusedProperty().addListener((obs, had, has) -> {
			if (!has) {
				String t = textFieldArenaActivationHour.getText();
				if (t == null || t.isBlank())
					return;

				// Keep only digits
				String digits = t.replaceAll("\\D", "");
				if (digits.length() == 4) {
					String HH = digits.substring(0, 2);
					String mm = digits.substring(2, 4);
					textFieldArenaActivationHour.setText(HH + ":" + mm);
				}
			}
		});
	}

	/**
	 * Validates time format (HH:mm) and shows error messages if invalid.
	 */
	private void validateTime(String timeText) {
		labelDateTimeError.setText("");
		textFieldArenaActivationHour.setStyle("");

		if (timeText == null || timeText.trim().isEmpty())
			return;

		final String regex = "\\d{2}:\\d{2}";
		if (!timeText.matches(regex)) {
			labelDateTimeError.setText("Invalid format. Use: HH:mm (e.g., 19:30)");
			textFieldArenaActivationHour.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
			return;
		}

		try {
			DateTimeFormatter formatter = new DateTimeFormatterBuilder()
					.parseStrict()
					.appendPattern("HH:mm")
					.toFormatter(Locale.ROOT);

			LocalTime time = LocalTime.parse(timeText, formatter);

			if (time.getHour() == 23 && time.getMinute() >= 56) {
				labelDateTimeError.setText("Max Arena time for correct execution is 23:55 UTC");
				textFieldArenaActivationHour.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
				return;
			}

			// Passed validation
			labelDateTimeError.setText("");
			textFieldArenaActivationHour.setStyle("");

		} catch (java.time.DateTimeException ex) {
			labelDateTimeError.setText("Invalid time values (check hour/minute).");
			textFieldArenaActivationHour.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
		}
	}
}
