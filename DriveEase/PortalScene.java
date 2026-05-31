package DriveEase;

import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;

import static DriveEase.AppColors.*;
import static DriveEase.UIHelper.*;

/**
 * Builds the main landing / portal page.
 */
public class PortalScene {

    private final AppState        state;
    private final SceneController ctrl;

    public PortalScene(AppState state, SceneController ctrl) {
        this.state = state; this.ctrl = ctrl;
    }

    public Node build() {
        StackPane page = new StackPane();
        page.setStyle("-fx-background-color: #030510;");

        // Deep radial glow background
        Region glow = new Region();
        glow.setStyle("-fx-background-color:radial-gradient(center 50% 35%,radius 65%," +
                "#0a1e5a 0%,#030510 70%);");
        glow.setMouseTransparent(true);

        // Close button (top-right)
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#2a3a5a;" +
                "-fx-font-size:20px;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:4 10;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color:transparent;" +
                "-fx-text-fill:#64748b;-fx-font-size:20px;-fx-cursor:hand;" +
                "-fx-border-color:transparent;-fx-padding:4 10;"));
        closeBtn.setOnMouseExited(e  -> closeBtn.setStyle("-fx-background-color:transparent;" +
                "-fx-text-fill:#2a3a5a;-fx-font-size:20px;-fx-cursor:hand;" +
                "-fx-border-color:transparent;-fx-padding:4 10;"));
        closeBtn.setOnAction(e -> ctrl.getStage().close());
        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new Insets(12));

        // Main content
        VBox main = new VBox(0);
        main.setAlignment(Pos.CENTER);
        main.setPadding(new Insets(30, 60, 30, 60));

        // ─ Enterprise badge ─
        HBox topBadge = new HBox(8);
        topBadge.setAlignment(Pos.CENTER);
        topBadge.setStyle(String.format(
                "-fx-background-color:%s;-fx-background-radius:30;" +
                        "-fx-border-color:%s;-fx-border-radius:30;-fx-padding:6 22;",
                rgba(NEON_BLUE, 0.08), rgba(NEON_BLUE, 0.35)));
        Label star1 = new Label("✦"); star1.setTextFill(NEON_YELLOW); star1.setFont(Font.font(11));
        Label badgeTxt = mkLbl("ENTERPRISE EDITION  ·  PREMIUM FLEET  ·  SINCE 2024",
                11, FontWeight.BOLD, NEON_BLUE);
        Label star2 = new Label("✦"); star2.setTextFill(NEON_YELLOW); star2.setFont(Font.font(11));
        topBadge.getChildren().addAll(star1, badgeTxt, star2);
        VBox.setMargin(topBadge, new Insets(0, 0, 22, 0));

        // ─ Title ─
        Label titleL = new Label("DriveEase");
        titleL.setFont(Font.font("Georgia", FontWeight.BOLD, 100));
        titleL.setTextFill(TEXT_BRIGHT);
        javafx.scene.effect.DropShadow gEffect =
                new javafx.scene.effect.DropShadow(40, NEON_BLUE);
        gEffect.setSpread(0.05);
        titleL.setEffect(gEffect);

        Label taglineL = mkLbl("Drive the Extraordinary — Rent Premium Cars on Demand",
                20, FontWeight.NORMAL, TEXT_MUTED);
        taglineL.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 20));

        Region ul = new Region(); ul.setPrefHeight(3); ul.setMaxWidth(520);
        ul.setStyle(String.format(
                "-fx-background-color:linear-gradient(to right,transparent,%s,%s,transparent);" +
                        "-fx-background-radius:2;", hex(NEON_BLUE), hex(NEON_PURPLE)));
        VBox.setMargin(ul, new Insets(4, 0, 0, 0));

        VBox titleBox = new VBox(8, titleL, taglineL, ul);
        titleBox.setAlignment(Pos.CENTER);
        VBox.setMargin(titleBox, new Insets(0, 0, 26, 0));

        // ─ Stats strip ─
        int avail = (int) state.fleet.stream().filter(c -> c.isAvailable).count();
        long validPromos = state.promoCodes.stream().filter(PromoCode::isValid).count();

        HBox statsStrip = new HBox(0);
        statsStrip.setAlignment(Pos.CENTER);
        statsStrip.setStyle(String.format(
                "-fx-background-color:%s;-fx-background-radius:22;" +
                        "-fx-border-color:%s;-fx-border-radius:22;-fx-padding:14 30;",
                rgba(SURFACE_1, 0.75), rgba(BORDER_DIM, 0.7)));
        statsStrip.getChildren().addAll(
                statCell("🚗", String.valueOf(state.fleet.size()), "Fleet Size"),
                statDiv(),
                statCell("✅", String.valueOf(avail), "Available Now"),
                statDiv(),
                statCell("🏷️", String.valueOf(validPromos), "Active Promos"),
                statDiv(),
                statCell("🛡️", "100%", "Fully Insured"),
                statDiv(),
                statCell("📞", "24/7", "Support")
        );
        VBox.setMargin(statsStrip, new Insets(0, 0, 30, 0));

        // ─ Action buttons ─
        VBox actions = new VBox(14);
        actions.setAlignment(Pos.CENTER);
        actions.setMaxWidth(430);

        Button btnNew    = mkBtn("🚗   New Customer  —  Register & Explore",
                NEON_BLUE,   Color.rgb(18, 70, 220));
        Button btnReturn = mkBtn("👤   Returning Customer  —  Sign In",
                NEON_PURPLE, Color.rgb(95, 18, 200));
        Button btnAdmin  = mkBtn("🔐   Administrator  —  Secure Access",
                CRIMSON,     CRIMSON.darker().darker());
        for (Button b : new Button[]{btnNew, btnReturn, btnAdmin}) b.setMaxWidth(Double.MAX_VALUE);

        btnNew.setOnAction(e    -> ctrl.showScene("CUST_REG"));
        btnReturn.setOnAction(e -> ctrl.showScene("CUST_LOGIN"));
        btnAdmin.setOnAction(e  -> ctrl.showScene("ADMIN_LOGIN"));
        actions.getChildren().addAll(btnNew, btnReturn, btnAdmin);
        VBox.setMargin(actions, new Insets(0, 0, 26, 0));

        // ─ Category pill row ─
        HBox cats = new HBox(10);
        cats.setAlignment(Pos.CENTER);
        String[] catNames  = {"🚗 Sedan", "🏎 Sports", "🚙 Luxury", "⚡ Electric", "🛻 SUV", "💰 Economy"};
        Color[]  catColors = {NEON_BLUE, NEON_PINK, NEON_YELLOW, NEON_GREEN, NEON_ORANGE, NEON_TEAL};
        for (int i = 0; i < catNames.length; i++) cats.getChildren().add(mkBadge(catNames[i], catColors[i]));

        main.getChildren().addAll(topBadge, titleBox, statsStrip, actions, cats);

        // Entry animation
        main.setOpacity(0); main.setTranslateY(20);
        new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(main.opacityProperty(), 0),
                        new KeyValue(main.translateYProperty(), 20)),
                new KeyFrame(Duration.millis(550),
                        new KeyValue(main.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(main.translateYProperty(), 0, Interpolator.EASE_OUT))
        ).play();

        page.getChildren().addAll(glow, main, closeBtn);
        return page;
    }

    // ── Internal helpers ──────────────────────────────────────────

    private VBox statCell(String icon, String val, String lbl) {
        VBox v = new VBox(2); v.setAlignment(Pos.CENTER); v.setPadding(new Insets(0, 22, 0, 22));
        Label il = new Label(icon); il.setFont(Font.font(17)); il.setTextFill(Color.WHITE);
        Label vl = mkLbl(val, 22, FontWeight.BOLD, NEON_BLUE);
        vl.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        Label ll = mkLbl(lbl, 11, FontWeight.NORMAL, TEXT_DIM);
        v.getChildren().addAll(il, vl, ll); return v;
    }

    private Region statDiv() {
        Region r = new Region(); r.setPrefWidth(1); r.setPrefHeight(52);
        r.setStyle("-fx-background-color:#0f1e3c;"); return r;
    }
}
