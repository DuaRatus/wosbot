package cl.camodev.wosbot.main;

import cl.camodev.wosbot.launcher.view.LauncherLayoutController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.prefs.Preferences;

public class FXApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(FXApp.class);
    private static final String KEY_X      = "windowX";
    private static final String KEY_Y      = "windowY";
    private static final String KEY_W      = "windowWidth";
    private static final String KEY_H      = "windowHeight";
    private static final double DEFAULT_W  = 900;
    private static final double DEFAULT_H  = 500;
    private static final int SNAP_THRESHOLD = 20;
    private static final int RESIZE_BORDER = 5;

    private Preferences prefs;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean maximized = false;
    private boolean snapped = false;
    private SnapPosition snapPosition = SnapPosition.NONE;
    private double prevX, prevY, prevWidth, prevHeight;

    // Variables para resize
    private boolean isResizing = false;
    private ResizeDirection resizeDirection = ResizeDirection.NONE;
    private double resizeStartX, resizeStartY, resizeStartWidth, resizeStartHeight;

    private enum SnapPosition {
        NONE, MAXIMIZE, LEFT, RIGHT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    private enum ResizeDirection {
        NONE, N, S, E, W, NE, NW, SE, SW
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Inicializar Preferences
        Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/appIcon.png")));
        prefs = Preferences.userRoot().node(FXApp.class.getName());

        // Usar ventana sin decoraciones
        stage.initStyle(StageStyle.UNDECORATED);

        // Cargar FXML y controlador
        FXMLLoader fxmlLoader = new FXMLLoader(
                LauncherLayoutController.class.getResource("LauncherLayout.fxml")
        );
        LauncherLayoutController controller = new LauncherLayoutController(stage);
        fxmlLoader.setController(controller);
        Parent contentRoot = fxmlLoader.load();

        // Crear la barra de título personalizada
        HBox titleBar = createTitleBar(stage);

        // Crear el layout principal con la barra de título
        BorderPane mainContent = new BorderPane();
        mainContent.setTop(titleBar);
        mainContent.setCenter(contentRoot);

        VBox.setVgrow(contentRoot, Priority.ALWAYS);
        BorderPane.setMargin(contentRoot, Insets.EMPTY);

        // Envolver en un StackPane para manejar el resize
        StackPane root = new StackPane(mainContent);

        // Agregar manejadores de resize
        addResizeHandlers(root, stage);

        // Crear escena con tamaño por defecto primero
        Scene scene = new Scene(root, DEFAULT_W, DEFAULT_H);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm());
        stage.setScene(scene);
        stage.getIcons().add(appIcon);
        stage.setTitle("Launcher");

        // set minimum height and width so the "run" button is always visible
        stage.setMinHeight(500);
        stage.setMinWidth(700);

        // Mostrar la ventana primero para que JavaFX calcule los tamaños correctamente
        stage.show();

        // Ahora restaurar el tamaño y posición guardados
        double savedWidth = prefs.getDouble(KEY_W, DEFAULT_W);
        double savedHeight = prefs.getDouble(KEY_H, DEFAULT_H);
        double savedX = prefs.getDouble(KEY_X, Double.NaN);
        double savedY = prefs.getDouble(KEY_Y, Double.NaN);

        // Establecer tamaño
        stage.setWidth(savedWidth);
        stage.setHeight(savedHeight);

        // Restaurar posición si existe y es válida
        if (!Double.isNaN(savedX) && !Double.isNaN(savedY)) {
            if (isPositionValidOnAnyScreen(savedX, savedY, savedWidth, savedHeight)) {
                stage.setX(savedX);
                stage.setY(savedY);
            } else {
                // La posición guardada no es válida (monitor desconectado), usar monitor principal
                positionOnPrimaryScreen(stage, savedWidth, savedHeight);
            }
        }

        // Antes de cerrar, guardar posición y tamaño
        stage.setOnCloseRequest(event -> {
            prefs.putDouble(KEY_X, stage.getX());
            prefs.putDouble(KEY_Y, stage.getY());
            prefs.putDouble(KEY_W, stage.getWidth());
            prefs.putDouble(KEY_H, stage.getHeight());

            // Kill adb.exe process
            try {
                new ProcessBuilder("taskkill", "/F", "/IM", "adb.exe").start();
                logger.info("adb.exe process terminated.");
            } catch (IOException e) {
                logger.error("Failed to terminate adb.exe process: " + e.getMessage(), e);
            }

            System.exit(0);
        });
    }

    /**
     * Agrega manejadores para permitir resize desde los bordes
     */
    private void addResizeHandlers(StackPane root, Stage stage) {
        root.setOnMouseMoved(event -> {
            if (maximized || isResizing) return;

            double mouseX = event.getX();
            double mouseY = event.getY();
            double width = root.getWidth();
            double height = root.getHeight();

            ResizeDirection direction = getResizeDirection(mouseX, mouseY, width, height);

            switch (direction) {
                case N:
                case S:
                    root.setCursor(Cursor.N_RESIZE);
                    break;
                case E:
                case W:
                    root.setCursor(Cursor.E_RESIZE);
                    break;
                case NE:
                case SW:
                    root.setCursor(Cursor.NE_RESIZE);
                    break;
                case NW:
                case SE:
                    root.setCursor(Cursor.NW_RESIZE);
                    break;
                default:
                    root.setCursor(Cursor.DEFAULT);
                    break;
            }
        });

        root.setOnMousePressed(event -> {
            if (maximized) return;

            double mouseX = event.getX();
            double mouseY = event.getY();
            double width = root.getWidth();
            double height = root.getHeight();

            resizeDirection = getResizeDirection(mouseX, mouseY, width, height);

            if (resizeDirection != ResizeDirection.NONE) {
                isResizing = true;
                resizeStartX = event.getScreenX();
                resizeStartY = event.getScreenY();
                resizeStartWidth = stage.getWidth();
                resizeStartHeight = stage.getHeight();
                prevX = stage.getX();
                prevY = stage.getY();
            }
        });

        root.setOnMouseDragged(event -> {
            if (!isResizing || maximized) return;

            double deltaX = event.getScreenX() - resizeStartX;
            double deltaY = event.getScreenY() - resizeStartY;

            double newWidth = resizeStartWidth;
            double newHeight = resizeStartHeight;
            double newX = prevX;
            double newY = prevY;

            switch (resizeDirection) {
                case E:
                    newWidth = Math.max(stage.getMinWidth(), resizeStartWidth + deltaX);
                    break;
                case W:
                    newWidth = Math.max(stage.getMinWidth(), resizeStartWidth - deltaX);
                    if (newWidth > stage.getMinWidth()) {
                        newX = prevX + deltaX;
                    }
                    break;
                case S:
                    newHeight = Math.max(stage.getMinHeight(), resizeStartHeight + deltaY);
                    break;
                case N:
                    newHeight = Math.max(stage.getMinHeight(), resizeStartHeight - deltaY);
                    if (newHeight > stage.getMinHeight()) {
                        newY = prevY + deltaY;
                    }
                    break;
                case SE:
                    newWidth = Math.max(stage.getMinWidth(), resizeStartWidth + deltaX);
                    newHeight = Math.max(stage.getMinHeight(), resizeStartHeight + deltaY);
                    break;
                case SW:
                    newWidth = Math.max(stage.getMinWidth(), resizeStartWidth - deltaX);
                    newHeight = Math.max(stage.getMinHeight(), resizeStartHeight + deltaY);
                    if (newWidth > stage.getMinWidth()) {
                        newX = prevX + deltaX;
                    }
                    break;
                case NE:
                    newWidth = Math.max(stage.getMinWidth(), resizeStartWidth + deltaX);
                    newHeight = Math.max(stage.getMinHeight(), resizeStartHeight - deltaY);
                    if (newHeight > stage.getMinHeight()) {
                        newY = prevY + deltaY;
                    }
                    break;
                case NW:
                    newWidth = Math.max(stage.getMinWidth(), resizeStartWidth - deltaX);
                    newHeight = Math.max(stage.getMinHeight(), resizeStartHeight - deltaY);
                    if (newWidth > stage.getMinWidth()) {
                        newX = prevX + deltaX;
                    }
                    if (newHeight > stage.getMinHeight()) {
                        newY = prevY + deltaY;
                    }
                    break;
            }

            stage.setWidth(newWidth);
            stage.setHeight(newHeight);
            stage.setX(newX);
            stage.setY(newY);
        });

        root.setOnMouseReleased(event -> {
            isResizing = false;
            resizeDirection = ResizeDirection.NONE;
            root.setCursor(Cursor.DEFAULT);
        });
    }

    /**
     * Determina la dirección de resize basado en la posición del mouse
     */
    private ResizeDirection getResizeDirection(double mouseX, double mouseY, double width, double height) {
        boolean left = mouseX < RESIZE_BORDER;
        boolean right = mouseX > width - RESIZE_BORDER;
        boolean top = mouseY < RESIZE_BORDER;
        boolean bottom = mouseY > height - RESIZE_BORDER;

        if (top && left) return ResizeDirection.NW;
        if (top && right) return ResizeDirection.NE;
        if (bottom && left) return ResizeDirection.SW;
        if (bottom && right) return ResizeDirection.SE;
        if (top) return ResizeDirection.N;
        if (bottom) return ResizeDirection.S;
        if (left) return ResizeDirection.W;
        if (right) return ResizeDirection.E;

        return ResizeDirection.NONE;
    }

    /**
     * Crea la barra de título personalizada con estilo oscuro
     */
    private HBox createTitleBar(Stage stage) {
        HBox titleBar = new HBox();
        titleBar.setPrefHeight(32);
        titleBar.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: transparent; -fx-border-width: 0 0 0 0;");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(3, 10, 3, 15));

        // SVG paths para los iconos (definidos aquí para acceso en los listeners)
        String maximizeSVG = "M 1.5,1.5 H 10.5 V 10.5 H 1.5 Z";
        String restoreSVG = "M 2,2 H 10 V 10 H 2 Z M 3,3 V 9 M 9,3 V 9";

        // Botón de maximizar (necesita ser final para actualizar su ícono)
        final Button maximizeBtn = createWindowButton(maximizeSVG, "#1e1e1e", "#3c3c3c");

        // Hacer la barra arrastrable con snap
        titleBar.setOnMousePressed(event -> {
            if (isResizing) return;

            xOffset = event.getSceneX();
            yOffset = event.getSceneY();

            // Si estaba maximizada o snapped, restaurar al empezar a arrastrar
            if (maximized || snapped) {
                // Calcular nueva posición proporcional
                double mouseXRatio = event.getScreenX() / stage.getWidth();
                prevWidth = DEFAULT_W;
                prevHeight = DEFAULT_H;
                stage.setWidth(prevWidth);
                stage.setHeight(prevHeight);
                xOffset = prevWidth * mouseXRatio;
                maximized = false;
                snapped = false;
                snapPosition = SnapPosition.NONE;
                maximizeBtn.setGraphic(createSVGIcon(maximizeSVG)); // Actualizar ícono
            }
        });

        titleBar.setOnMouseDragged(event -> {
            if (isResizing) return;

            if (!maximized && !snapped) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);

                // Detectar posición para snap
                checkSnapPosition(stage, event.getScreenX(), event.getScreenY());
            }
        });

        titleBar.setOnMouseReleased(event -> {
            if (isResizing) return;

            // Aplicar snap al soltar
            if (snapPosition != SnapPosition.NONE) {
                applySnap(stage, snapPosition);
                // Actualizar ícono si se maximizó
                if (maximized) {
                    maximizeBtn.setGraphic(createSVGIcon(restoreSVG));
                } else {
                    maximizeBtn.setGraphic(createSVGIcon(maximizeSVG));
                }
            }
            snapPosition = SnapPosition.NONE;
        });

        // Doble clic para maximizar
        titleBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                toggleMaximize(stage);
                // Actualizar ícono
                maximizeBtn.setGraphic(createSVGIcon(maximized ? restoreSVG : maximizeSVG));
            }
        });

        // Título
        Label titleLabel = new Label(stage.getTitle());
        titleLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 13px; -fx-font-weight: 500;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        // Actualizar el título cuando cambie
        stage.titleProperty().addListener((obs, oldTitle, newTitle) -> titleLabel.setText(newTitle));

        // Botones de control (estilo Windows)
        HBox windowButtons = new HBox(1);
        windowButtons.setAlignment(Pos.CENTER_RIGHT);

        // SVG paths para los iconos (usando rectángulos en lugar de líneas para mejor visibilidad)
        String minimizeSVG = "M 1,6 H 11";  // Identificador para minimize
        String closeSVG = "M 1.576,1 L 6,5.417 L 10.424,1 L 11,1.576 L 6.583,6 L 11,10.424 L 10.424,11 L 6,6.583 L 1.576,11 L 1,10.424 L 5.417,6 L 1,1.576 Z";

        Button minimizeBtn = createWindowButton(minimizeSVG, "#1e1e1e", "#3c3c3c");
        minimizeBtn.setOnAction(e -> stage.setIconified(true));

        maximizeBtn.setOnAction(e -> {
            toggleMaximize(stage);
            // Actualizar ícono entre maximizar y restaurar
            Region newIcon = createSVGIcon(maximized ? restoreSVG : maximizeSVG);
            maximizeBtn.setGraphic(newIcon);
        });

        Button closeBtn = createWindowButton(closeSVG, "#1e1e1e", "#e81123");
        closeBtn.setOnAction(e -> stage.close());

        windowButtons.getChildren().addAll(minimizeBtn, maximizeBtn, closeBtn);

        titleBar.getChildren().addAll(titleLabel, windowButtons);

        return titleBar;
    }

    /**
     * Detecta si el mouse está cerca de los bordes de la pantalla para snap
     */
    private void checkSnapPosition(Stage stage, double mouseX, double mouseY) {
        Screen currentScreen = getScreenAtPosition(mouseX, mouseY);
        Rectangle2D bounds = currentScreen.getVisualBounds();

        boolean nearTop = mouseY - bounds.getMinY() < SNAP_THRESHOLD;
        boolean nearBottom = bounds.getMaxY() - mouseY < SNAP_THRESHOLD;
        boolean nearLeft = mouseX - bounds.getMinX() < SNAP_THRESHOLD;
        boolean nearRight = bounds.getMaxX() - mouseX < SNAP_THRESHOLD;

        SnapPosition newPosition = SnapPosition.NONE;

        // Prioridad a las esquinas
        if (nearTop && nearLeft) {
            newPosition = SnapPosition.TOP_LEFT;
        } else if (nearTop && nearRight) {
            newPosition = SnapPosition.TOP_RIGHT;
        } else if (nearBottom && nearLeft) {
            newPosition = SnapPosition.BOTTOM_LEFT;
        } else if (nearBottom && nearRight) {
            newPosition = SnapPosition.BOTTOM_RIGHT;
        } else if (nearTop) {
            newPosition = SnapPosition.MAXIMIZE;
        } else if (nearLeft) {
            newPosition = SnapPosition.LEFT;
        } else if (nearRight) {
            newPosition = SnapPosition.RIGHT;
        }

        snapPosition = newPosition;
    }

    /**
     * Aplica el snap según la posición detectada
     */
    private void applySnap(Stage stage, SnapPosition position) {
        if (position == SnapPosition.NONE) return;

        Screen screen = getCurrentScreen(stage);
        Rectangle2D bounds = screen.getVisualBounds();

        // Guardar estado previo si no estaba snapped
        if (!snapped && !maximized) {
            prevX = stage.getX();
            prevY = stage.getY();
            prevWidth = stage.getWidth();
            prevHeight = stage.getHeight();
        }

        snapped = true;
        maximized = false;

        switch (position) {
            case MAXIMIZE:
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth());
                stage.setHeight(bounds.getHeight());
                maximized = true;
                snapped = false;
                break;
            case LEFT:
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth() / 2);
                stage.setHeight(bounds.getHeight());
                break;
            case RIGHT:
                stage.setX(bounds.getMinX() + bounds.getWidth() / 2);
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth() / 2);
                stage.setHeight(bounds.getHeight());
                break;
            case TOP_LEFT:
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth() / 2);
                stage.setHeight(bounds.getHeight() / 2);
                break;
            case TOP_RIGHT:
                stage.setX(bounds.getMinX() + bounds.getWidth() / 2);
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth() / 2);
                stage.setHeight(bounds.getHeight() / 2);
                break;
            case BOTTOM_LEFT:
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY() + bounds.getHeight() / 2);
                stage.setWidth(bounds.getWidth() / 2);
                stage.setHeight(bounds.getHeight() / 2);
                break;
            case BOTTOM_RIGHT:
                stage.setX(bounds.getMinX() + bounds.getWidth() / 2);
                stage.setY(bounds.getMinY() + bounds.getHeight() / 2);
                stage.setWidth(bounds.getWidth() / 2);
                stage.setHeight(bounds.getHeight() / 2);
                break;
        }
    }

    /**
     * Obtiene el monitor en una posición específica
     */
    private Screen getScreenAtPosition(double x, double y) {
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getVisualBounds();
            if (bounds.contains(x, y)) {
                return screen;
            }
        }
        return Screen.getPrimary();
    }

    /**
     * Crea un botón para la barra de título con estilos personalizados usando SVG
     */
    private Button createWindowButton(String svgPath, String normalColor, String hoverColor) {
        Button button = new Button();
        button.setPrefSize(46, 32);

        // Crear el gráfico SVG
        Region icon = createSVGIcon(svgPath);
        button.setGraphic(icon);

        button.setStyle(
                "-fx-background-color: " + normalColor + ";" +
                        "-fx-border-width: 0;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 0;" +
                        "-fx-padding: 0;" +
                        "-fx-alignment: center;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle().replace(normalColor, hoverColor))
        );

        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle().replace(hoverColor, normalColor))
        );

        return button;
    }

    /**
     * Crea un ícono SVG a partir de un path SVG
     */
    private Region createSVGIcon(String svgContent) {
        Region icon = new Region();
        // Para el ícono de minimize, usar un rectángulo delgado
        if (svgContent.contains("M 1,6")) {
            icon.setStyle(
                    "-fx-background-color: #e0e0e0;" +
                            "-fx-min-width: 10px;" +
                            "-fx-min-height: 1px;" +
                            "-fx-max-width: 10px;" +
                            "-fx-max-height: 1px;"
            );
        }
        // Para maximize/restore, usar border en lugar de fill
        else if (svgContent.contains("M 1.5,1.5") || svgContent.contains("M 2,2")) {
            icon.setStyle(
                    "-fx-border-color: #e0e0e0;" +
                            "-fx-border-width: 1px;" +
                            "-fx-min-width: 9px;" +
                            "-fx-min-height: 9px;" +
                            "-fx-max-width: 9px;" +
                            "-fx-max-height: 9px;" +
                            "-fx-background-color: transparent;"
            );
        }
        // Para close y otros, usar shape
        else {
            icon.setStyle(
                    "-fx-shape: \"" + svgContent + "\";" +
                            "-fx-background-color: #e0e0e0;" +
                            "-fx-min-width: 12px;" +
                            "-fx-min-height: 12px;" +
                            "-fx-max-width: 12px;" +
                            "-fx-max-height: 12px;"
            );
        }
        return icon;
    }

    /**
     * Alterna entre maximizar y restaurar la ventana
     */
    private void toggleMaximize(Stage stage) {
        if (!maximized) {
            // Guardar posición y tamaño actuales
            prevX = stage.getX();
            prevY = stage.getY();
            prevWidth = stage.getWidth();
            prevHeight = stage.getHeight();

            // Obtener el monitor donde está la ventana actualmente
            Screen currentScreen = getCurrentScreen(stage);
            Rectangle2D bounds = currentScreen.getVisualBounds();

            // Maximizar en el monitor actual
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            maximized = true;
            snapped = false;
        } else {
            // Restaurar
            stage.setX(prevX);
            stage.setY(prevY);
            stage.setWidth(prevWidth);
            stage.setHeight(prevHeight);
            maximized = false;
            snapped = false;
        }
    }

    /**
     * Obtiene el monitor donde está ubicada actualmente la ventana
     */
    private Screen getCurrentScreen(Stage stage) {
        double centerX = stage.getX() + stage.getWidth() / 2;
        double centerY = stage.getY() + stage.getHeight() / 2;

        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getVisualBounds();
            if (bounds.contains(centerX, centerY)) {
                return screen;
            }
        }
        return Screen.getPrimary();
    }

    /**
     * Verifica si la posición guardada es válida en alguno de los monitores disponibles.
     */
    private boolean isPositionValidOnAnyScreen(double x, double y, double width, double height) {
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getVisualBounds();

            // Verificar si al menos una parte significativa de la ventana está visible en este monitor
            // La ventana debe tener al menos 100x100 píxeles visibles en el monitor
            double visibleLeft = Math.max(x, bounds.getMinX());
            double visibleTop = Math.max(y, bounds.getMinY());
            double visibleRight = Math.min(x + width, bounds.getMaxX());
            double visibleBottom = Math.min(y + height, bounds.getMaxY());

            double visibleWidth = Math.max(0, visibleRight - visibleLeft);
            double visibleHeight = Math.max(0, visibleBottom - visibleTop);

            // Si hay al menos 100x100 píxeles visibles, consideramos la posición válida
            if (visibleWidth >= 100 && visibleHeight >= 100) {
                return true;
            }
        }
        return false;
    }

    /**
     * Posiciona la ventana en el monitor principal manteniendo el tamaño guardado.
     */
    private void positionOnPrimaryScreen(Stage stage, double savedWidth, double savedHeight) {
        Screen primaryScreen = Screen.getPrimary();
        Rectangle2D bounds = primaryScreen.getVisualBounds();

        // Centrar la ventana en el monitor principal
        double centerX = bounds.getMinX() + (bounds.getWidth() - savedWidth) / 2;
        double centerY = bounds.getMinY() + (bounds.getHeight() - savedHeight) / 2;

        // Asegurar que la ventana no se salga de los límites del monitor
        double finalX = Math.max(bounds.getMinX(), Math.min(centerX, bounds.getMaxX() - savedWidth));
        double finalY = Math.max(bounds.getMinY(), Math.min(centerY, bounds.getMaxY() - savedHeight));

        stage.setX(finalX);
        stage.setY(finalY);

        logger.info("Window positioned on primary screen due to invalid saved position");
    }
}