package DriveEase;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Application entry point.
 * Implements SceneController — all scene builders navigate via this interface.
 *
 * CORRECT LOADING ORDER (prevents duplicate-data bug):
 *   1. seedFleet()       — Car objects needed before loadBookings()
 *   2. loadCars()        — restore car availability from cars.txt
 *   3. loadCustomers()   — restore customers from customers.txt
 *   4. loadBookings()    — restore bookings, link to customers & cars
 *   5. loadPromos()      — restore promos (clears list then reloads)
 *   6. seedDemoCustomer()— only if customers list is STILL empty
 *   7. seedPromos()      — only if promos list is STILL empty
 */
public class Drive extends Application implements SceneController {

    // ── Infrastructure ────────────────────────────────────────────
    private AppState    state;
    private FileManager files;
    private DataStore   store;

    // ── JavaFX ───────────────────────────────────────────────────
    private Stage     primaryStage;
    private StackPane root;
    private final Map<String, Node> sceneMap = new HashMap<>();

    // ══════════════════ START ═════════════════════════════════════

    @Override

    public void start(Stage stage) {
        primaryStage = stage;

        state = new AppState();
        files = new FileManager(state);

        store = new DataStore(state);


        // ── Load data in correct order ────────────────────────────
        store.seedFleet();          // 1. car objects (always needed)
        files.loadCars();           // 2. restore availability
        files.loadCustomers();      // 3. restore customers
        files.loadBookings();       // 4. restore bookings
        files.loadPromos();         // 5. restore promos (clears list first)
        store.seedDemoCustomer();   // 6. only if no customers loaded from file
        store.seedPromos();         // 7. only if no promos loaded from file

        // ── UI setup ──────────────────────────────────────────────
        root = new StackPane();
        root.setBackground(new Background(
                new BackgroundFill(Color.rgb(3, 5, 16), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(root, 1400, 840);
        scene.getStylesheets().add("data:text/css," + StyleSheet.get());

        buildAllScenes();
        showScene("PORTAL");

        stage.setTitle("DriveEase  ·  Premium Car Rental Platform  v2.0");
        stage.setMinWidth(1100);
        stage.setMinHeight(680);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    // ── Build all scenes once ─────────────────────────────────────

    private void buildAllScenes() {
        AuthScenes   auth    = new AuthScenes(state, this);
        ReceiptScene receipt = new ReceiptScene(state, this);

        sceneMap.put("PORTAL",      new PortalScene(state, this).build());
        sceneMap.put("CUST_LOGIN",  auth.buildCustLogin());
        sceneMap.put("CUST_REG",    auth.buildCustReg());
        sceneMap.put("ADMIN_LOGIN", auth.buildAdminLogin());
        sceneMap.put("CUST_FLEET",  new FleetScene(state, this).build());
        sceneMap.put("CART",        new CartScene(state, this).build());
        sceneMap.put("CHECKOUT",    new CheckoutScene(state, this).build());
        sceneMap.put("RECEIPT",     receipt.buildEmpty());
        sceneMap.put("PROFILE",     new ProfileScene(state, this).build());
        sceneMap.put("ADMIN_DASH",  new AdminDashScene(state, this).build());
    }

    // ══════════════════ SceneController METHODS ═══════════════════

    @Override
    public void showScene(String key) {
        Node n = sceneMap.get(key);
        if (n == null) return;
        root.getChildren().setAll(n);
        UIHelper.fadeIn(n);
    }

    @Override
    public void rebuild(String... keys) {
        AuthScenes   auth    = new AuthScenes(state, this);
        ReceiptScene receipt = new ReceiptScene(state, this);

        for (String k : keys) {
            Node built = switch (k) {
                case "PORTAL"      -> new PortalScene(state, this).build();
                case "CUST_LOGIN"  -> auth.buildCustLogin();
                case "CUST_REG"    -> auth.buildCustReg();
                case "ADMIN_LOGIN" -> auth.buildAdminLogin();
                case "CUST_FLEET"  -> new FleetScene(state, this).build();
                case "CART"        -> new CartScene(state, this).build();
                case "CHECKOUT"    -> new CheckoutScene(state, this).build();
                case "RECEIPT"     -> receipt.buildEmpty();
                case "PROFILE"     -> new ProfileScene(state, this).build();
                case "ADMIN_DASH"  -> new AdminDashScene(state, this).build();
                default            -> null;
            };
            if (built != null) sceneMap.put(k, built);
        }
    }

    @Override
    public void putScene(String key, Node node) {
        sceneMap.put(key, node);
    }

    @Override
    public void saveData() {
        files.saveAll();
    }

    @Override
    public void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type, msg);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    @Override
    public boolean showConfirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setTitle(title);
        a.setHeaderText(null);
        Optional<ButtonType> res = a.showAndWait();
        return res.isPresent() && res.get() == ButtonType.YES;
    }

    @Override
    public Stage getStage() {
        return primaryStage;
    }

    // ══════════════════ MAIN ══════════════════════════════════════

    public static void main(String[] args) {
        launch(args);
    }
}