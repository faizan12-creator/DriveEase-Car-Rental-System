package DriveEase;


import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import static DriveEase.AppColors.*;
import static DriveEase.UIHelper.*;

/**
 * Builds the shopping cart page and individual cart-row cards.
 */
public class CartScene {

    private final AppState        state;
    private final SceneController ctrl;

    public CartScene(AppState state, SceneController ctrl) {
        this.state = state; this.ctrl = ctrl;
    }

    public Node build() {
        BorderPane page = new BorderPane();
        page.setStyle("-fx-background-color:#050812;");

        // ─ Header ─
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 26, 18, 26));
        header.setStyle("-fx-background-color:#070b1a;-fx-border-color:#0f1e3c;-fx-border-width:0 0 1 0;");
        Label titleL = new Label("🛒  Shopping Cart");
        titleL.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
        titleL.setTextFill(TEXT_BRIGHT);
        Label cntL = mkBadge(state.cart.isEmpty() ? "Empty" : state.cart.size() + " item(s)",
                state.cart.isEmpty() ? TEXT_DIM : NEON_GREEN);
        header.getChildren().addAll(titleL, cntL);

        // ─ Body ─
        VBox body = new VBox(14);
        body.setPadding(new Insets(20, 26, 20, 26));

        if (state.cart.isEmpty()) {
            VBox empty = new VBox(16);
            empty.setAlignment(Pos.CENTER); empty.setPadding(new Insets(90));
            empty.getChildren().add(mkLbl("🛒", 60, FontWeight.NORMAL, TEXT_DIM));
            empty.getChildren().add(mkLbl("Your cart is empty", 22, FontWeight.BOLD, TEXT_MUTED));
            empty.getChildren().add(mkLbl("Browse our fleet and add vehicles to get started",
                    14, FontWeight.NORMAL, TEXT_DIM));
            Button browseBtn = mkBtn("🚗  Browse Fleet", NEON_BLUE, javafx.scene.paint.Color.rgb(12, 55, 200));
            browseBtn.setOnAction(e -> { ctrl.rebuild("CUST_FLEET"); ctrl.showScene("CUST_FLEET"); });
            empty.getChildren().add(browseBtn);
            body.getChildren().add(empty);
        } else {
            double grand = 0;
            for (CartItem ci : state.cart) {
                grand += ci.subtotal();
                body.getChildren().add(buildCartRow(ci));
            }

            // Summary box
            VBox summary = new VBox(10);
            summary.setPadding(new Insets(18, 22, 18, 22));
            summary.setStyle(String.format(
                    "-fx-background-color:linear-gradient(to right,%s,%s);" +
                            "-fx-background-radius:16;-fx-border-color:%s;-fx-border-radius:16;",
                    rgba(SURFACE_2, 0.88), rgba(SURFACE_1, 0.88), rgba(AMBER, 0.35)));

            summary.getChildren().add(mkLbl("Order Summary", 13, FontWeight.BOLD, TEXT_MUTED));
            for (CartItem ci : state.cart) {
                HBox r = new HBox(); r.setAlignment(Pos.CENTER_LEFT);
                Label nl = mkLbl(ci.car.icon + " " + ci.car.name + " × " + ci.days + "d  @  $" +
                        (int) ci.car.pricePerDay + "/day", 13, FontWeight.NORMAL, TEXT_PRIMARY);
                Label sl = mkLbl("$" + String.format("%.2f", ci.subtotal()),
                        13, FontWeight.BOLD, NEON_GREEN);
                r.getChildren().addAll(nl, hSpacer(), sl);
                summary.getChildren().add(r);
            }
            summary.getChildren().add(hLine());

            HBox totRow = new HBox(); totRow.setAlignment(Pos.CENTER_LEFT);
            Label totL = mkLbl("ORDER TOTAL", 14, FontWeight.BOLD, AMBER);
            Label totV = mkLbl("$" + String.format("%.2f", grand), 26, FontWeight.BOLD, AMBER);
            totV.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
            totRow.getChildren().addAll(totL, hSpacer(), totV);
            summary.getChildren().add(totRow);
            body.getChildren().add(summary);
        }

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#050812;-fx-background-color:#050812;");

        // ─ Bottom bar ─
        HBox south = new HBox(14);
        south.setAlignment(Pos.CENTER_LEFT);
        south.setPadding(new Insets(13, 26, 13, 26));
        south.setStyle("-fx-background-color:#070b1a;-fx-border-color:#0f1e3c;-fx-border-width:1 0 0 0;");

        Button contBtn = mkBtn("← Continue Shopping", NEON_BLUE, javafx.scene.paint.Color.rgb(12, 55, 200));
        contBtn.setOnAction(e -> { ctrl.rebuild("CUST_FLEET"); ctrl.showScene("CUST_FLEET"); });

        Button checkBtn = mkBtn("Proceed to Checkout  →", NEON_GREEN, NEON_GREEN.darker().darker());
        checkBtn.setOnAction(e -> {
            if (state.cart.isEmpty()) {
                ctrl.showAlert("Cart Empty", "Add vehicles first.", Alert.AlertType.WARNING);
                return;
            }
            ctrl.rebuild("CHECKOUT"); ctrl.showScene("CHECKOUT");
        });

        south.getChildren().addAll(contBtn, hSpacer(), checkBtn);

        page.setTop(header); page.setCenter(scroll); page.setBottom(south);
        return page;
    }

    // ── Cart row card ─────────────────────────────────────────────

    public HBox buildCartRow(CartItem ci) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 20, 16, 20));
        String rowBase = String.format(
                "-fx-background-color:linear-gradient(to right,%s,%s);" +
                        "-fx-background-radius:14;-fx-border-color:%s;-fx-border-radius:14;",
                rgba(SURFACE_1, 0.82), rgba(BG_DARK, 0.82), rgba(BORDER_DIM, 0.55));
        row.setStyle(rowBase);
        row.setOnMouseEntered(e -> row.setStyle(String.format(
                "-fx-background-color:linear-gradient(to right,%s,%s);" +
                        "-fx-background-radius:14;-fx-border-color:%s;-fx-border-radius:14;",
                rgba(SURFACE_2, 0.92), rgba(SURFACE_1, 0.92), rgba(NEON_BLUE, 0.38))));
        row.setOnMouseExited(e -> row.setStyle(rowBase));

        Label ico = new Label(ci.car.icon);
        ico.setFont(Font.font(34)); ico.setTextFill(javafx.scene.paint.Color.WHITE);

        VBox info = new VBox(4);
        info.getChildren().add(mkLbl(ci.car.name, 15, FontWeight.BOLD, TEXT_BRIGHT));
        info.getChildren().add(mkLbl(ci.car.plate + "  ·  " + ci.car.category +
                "  ·  " + ci.days + " day(s)", 12, FontWeight.NORMAL, TEXT_DIM));
        info.getChildren().add(mkLbl("$" + (int) ci.car.pricePerDay + "/day",
                12, FontWeight.NORMAL, TEXT_MUTED));

        VBox right = new VBox(8); right.setAlignment(Pos.CENTER_RIGHT);
        Label subL = mkLbl("$" + String.format("%.2f", ci.subtotal()),
                22, FontWeight.BOLD, NEON_GREEN);
        subL.setFont(Font.font("Georgia", FontWeight.BOLD, 22));

        Button rmBtn = mkBtnRed("🗑  Remove");
        rmBtn.setOnAction(e -> {
            ci.car.inCart = false; state.cart.remove(ci);
            ctrl.rebuild("CART"); ctrl.showScene("CART");
        });
        right.getChildren().addAll(subL, rmBtn);

        row.getChildren().addAll(ico, info, hSpacer(), right);
        return row;
    }
}