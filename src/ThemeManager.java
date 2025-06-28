import javafx.scene.Scene;

public class ThemeManager {

    private static final ThemeManager instance = new ThemeManager();
    private boolean darkMode = true; // Start in dark mode by default

    private ThemeManager() {
        // Private constructor for singleton pattern
    }

    public static ThemeManager getInstance() {
        return instance;
    }

    public void applyTheme(Scene scene) {
        scene.getStylesheets().clear();

        String cssFile = darkMode ? "/dark-theme.css" : "/light-theme.css";
        try {
            var resource = getClass().getResource(cssFile);
            if (resource == null) {
                throw new IllegalArgumentException("CSS file not found: " + cssFile);
            }
            scene.getStylesheets().add(resource.toExternalForm());
        } catch (Exception e) {
            System.err.println("Failed to apply theme: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void toggleTheme(Scene scene) {
        darkMode = !darkMode;
        applyTheme(scene);
    }

    public boolean isDarkMode() {
        return darkMode;
    }
}
