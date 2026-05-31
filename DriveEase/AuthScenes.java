package DriveEase;


import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;

import static DriveEase.AppColors.*;
import static DriveEase.UIHelper.*;

/**
 * Builds the three auth screens:
 *   buildCustLogin()  — returning customer sign-in
 *   buildCustReg()    — new customer registration
 *   buildAdminLogin() — administrator access
 *
 * Shared auth-card template is private to this class.
 */
public class AuthScenes {

    private final AppState        state;
    private final SceneController ctrl;

    public AuthScenes(AppState state, SceneController ctrl) {
        this.state = state; this.ctrl = ctrl;
    }

    // ── Customer Login ────────────────────────────────────────────

    public Node buildCustLogin() {
        TextField     nicF = mkField("NIC Number  e.g. 3830263261151");
        PasswordField pF   = mkPass("Password");
        Label errL = mkLbl(" ", 12, FontWeight.BOLD, CRIMSON);

        Button loginBtn = mkBtn("  Sign In to My Account  →", NEON_PURPLE, Color.rgb(90, 18, 200));
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        Button backBtn = mkBtnGhost("← Back to Portal");
        backBtn.setMaxWidth(Double.MAX_VALUE);

        loginBtn.setOnAction(e -> {
            Customer c = state.customers.get(nicF.getText().trim());
            if (c == null || !c.password.equals(pF.getText().trim())) {
                errL.setText("❌  Invalid NIC or password. Please try again."); pF.clear(); return;
            }
            if (c.isBlocked) { errL.setText("🚫  Account blocked. Contact support."); return; }
            state.activeCustomer = c; c.recordLogin(); ctrl.saveData();
            errL.setText(" "); nicF.clear(); pF.clear();
            state.cart.clear(); state.fleet.forEach(car -> car.inCart = false);
            ctrl.rebuild("CUST_FLEET"); ctrl.showScene("CUST_FLEET");
        });
        pF.setOnAction(e -> loginBtn.fire());
        backBtn.setOnAction(e -> { errL.setText(" "); ctrl.showScene("PORTAL"); });

        Label regLink = mkLbl("Don't have an account?  Register here →", 12, FontWeight.NORMAL, NEON_BLUE);
        regLink.setStyle("-fx-cursor:hand;");
        regLink.setOnMouseClicked(e -> ctrl.showScene("CUST_REG"));

        return buildAuthCard(NEON_PURPLE, "👤", "Welcome Back",
                "Sign in to access your DriveEase account",
                nicF, pF, errL, loginBtn, backBtn, regLink);
    }

    // ── Customer Registration ─────────────────────────────────────

    public Node buildCustReg() {
        TextField     nameF  = mkField("Full Name");
        TextField     nicF   = mkField("NIC Number (13 digits)");
        TextField     phoneF = mkField("Phone Number (optional)");
        TextField     emailF = mkField("Email Address (optional)");
        PasswordField pF     = mkPass("Password (min 4 characters)");
        PasswordField cF     = mkPass("Confirm Password");
        Label errL = mkLbl(" ", 12, FontWeight.BOLD, CRIMSON);

        Button regBtn  = mkBtn("✅  Create Account & Start Renting  →", NEON_BLUE, Color.rgb(16, 70, 210));
        regBtn.setMaxWidth(Double.MAX_VALUE);
        Button backBtn = mkBtnGhost("← Back to Portal");
        backBtn.setMaxWidth(Double.MAX_VALUE);

        regBtn.setOnAction(e -> {
            String name = nameF.getText().trim(), nic = nicF.getText().trim();
            String pass = pF.getText().trim(),    conf = cF.getText().trim();
            if (name.isEmpty() || nic.isEmpty() || pass.isEmpty()) {
                errL.setText("❌  Name, NIC and Password required."); return; }
            if (nic.length() != 13 || !nic.matches("\\d+")) {
                errL.setText("❌  NIC must be exactly 13 digits."); return; }
            if (pass.length() < 4) {
                errL.setText("❌  Password must be at least 4 characters."); return; }
            if (!pass.equals(conf)) {
                errL.setText("❌  Passwords do not match."); return; }
            if (state.customers.containsKey(nic)) {
                errL.setText("❌  NIC already registered — please sign in."); return; }
            Customer nc = new Customer(name, nic, pass);
            nc.phone = phoneF.getText().trim(); nc.email = emailF.getText().trim();
            nc.recordLogin(); state.customers.put(nic, nc); state.activeCustomer = nc;
            state.cart.clear(); state.fleet.forEach(car -> car.inCart = false);
            ctrl.saveData(); ctrl.rebuild("CUST_FLEET"); ctrl.showScene("CUST_FLEET");
        });
        cF.setOnAction(e -> regBtn.fire());
        backBtn.setOnAction(e -> ctrl.showScene("PORTAL"));

        return buildAuthCard(NEON_BLUE, "🚗", "Create Your Account",
                "Join DriveEase and drive the extraordinary",
                nameF, nicF, phoneF, emailF, pF, cF, errL, regBtn, backBtn);
    }

    // ── Admin Login ───────────────────────────────────────────────

    public Node buildAdminLogin() {
        TextField     userF = mkField("Administrator Username");
        PasswordField pF    = mkPass("Secure Password");
        Label errL = mkLbl(" ", 12, FontWeight.BOLD, CRIMSON);

        Button loginBtn = mkBtn("🔓  Access Admin Control Panel", CRIMSON, CRIMSON.darker().darker());
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        Button backBtn = mkBtnGhost("← Back to Portal");
        backBtn.setMaxWidth(Double.MAX_VALUE);

        loginBtn.setOnAction(e -> {
            if (userF.getText().trim().equals(AppState.ADMIN_USER)
                    && pF.getText().trim().equals(AppState.ADMIN_PASS)) {
                errL.setText(" "); userF.clear(); pF.clear();
                ctrl.rebuild("ADMIN_DASH"); ctrl.showScene("ADMIN_DASH");
            } else {
                errL.setText("❌  Invalid credentials. Access denied."); pF.clear();
            }
        });
        pF.setOnAction(e -> loginBtn.fire());
        backBtn.setOnAction(e -> ctrl.showScene("PORTAL"));

        return buildAuthCard(CRIMSON, "🔐", "Admin Login",
                "Restricted area — authorised personnel only",
                userF, pF, errL, loginBtn, backBtn);
    }

    // ── Shared auth-card template ──────────────────────────────────

    private StackPane buildAuthCard(Color borderColor, String emoji,
                                    String title, String subtitle, Node... children) {
        StackPane outer = new StackPane();
        outer.setStyle("-fx-background-color:#030510;");

        Region blob = new Region();
        blob.setPrefSize(600, 600);
        blob.setStyle(String.format(
                "-fx-background-color:radial-gradient(center 50%% 50%%,radius 50%%,%s 0%%,transparent 70%%);",
                rgba(borderColor, 0.06)));
        blob.setMouseTransparent(true);

        VBox card = new VBox(16);
        card.setMaxWidth(490);
        card.setAlignment(Pos.CENTER);
        card.setStyle(String.format(
                "-fx-background-color:linear-gradient(to bottom,%s,%s);" +
                        "-fx-background-radius:26;-fx-border-radius:26;" +
                        "-fx-border-color:%s;-fx-border-width:1.5;" +
                        "-fx-padding:40 50 36 50;" +
                        "-fx-effect:dropshadow(gaussian,%s,36,0,0,12);",
                rgba(SURFACE_1, 0.98), rgba(BG_DARK, 0.98),
                rgba(borderColor, 0.65), rgba(borderColor, 0.28)));

        Label emojiL = new Label(emoji); emojiL.setFont(Font.font(56)); emojiL.setTextFill(Color.WHITE);
        Label titleL = mkTitle(title);
        Label subL   = mkLbl(subtitle, 13, FontWeight.NORMAL, TEXT_MUTED);
        subL.setAlignment(Pos.CENTER);
        Region divLine = new Region(); divLine.setPrefHeight(1);
        divLine.setStyle(String.format("-fx-background-color:%s;", rgba(borderColor, 0.25)));

        card.getChildren().addAll(emojiL, titleL, subL, divLine);
        card.getChildren().addAll(children);

        // Slide-up + fade animation
        card.setTranslateY(50); card.setOpacity(0);
        new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(card.translateYProperty(), 50),
                        new KeyValue(card.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(480),
                        new KeyValue(card.translateYProperty(), 0, Interpolator.EASE_OUT),
                        new KeyValue(card.opacityProperty(), 1, Interpolator.EASE_OUT))
        ).play();

        outer.getChildren().addAll(blob, card);
        return outer;
    }
}