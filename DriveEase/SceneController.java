package DriveEase;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Implemented by Drive.java.
 * All scene builders use this interface for navigation,
 * alerts, and saving — without depending on Drive directly.
 */
public interface SceneController {

    /** Show a pre-built scene by key */
    void showScene(String key);

    /** Re-build and update one or more scenes in the scene map */
    void rebuild(String... keys);

    /** Inject a pre-built Node directly into the scene map */
    void putScene(String key, Node node);

    /** Persist all data to disk */
    void saveData();

    /** Show a JavaFX Alert dialog */
    void showAlert(String title, String msg, Alert.AlertType type);

    /** Show a YES/NO confirmation dialog; returns true if YES chosen */
    boolean showConfirm(String title, String msg);

    /** Expose the primary Stage (for close button etc.) */
    Stage getStage();
}