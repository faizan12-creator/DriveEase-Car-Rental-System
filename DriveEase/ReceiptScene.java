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
 * Builds the booking confirmation / receipt page.
 */
public class ReceiptScene {

    private final AppState        state;
    private final SceneController ctrl;

    public ReceiptScene(AppState state, SceneController ctrl) {
        this.state = state; this.ctrl = ctrl;
    }

    /** Builds the receipt for a null booking (placeholder) */
    public Node buildEmpty() {
        StackPane page = new StackPane();
        page.setStyle("-fx-background-color:#030510;");
        page.getChildren().add(mkLbl("No booking data.", 16, FontWeight.NORMAL, TEXT_MUTED));
        return page;
    }

    /** Builds the animated receipt for a completed booking */
    public Node build(BookingRecord b) {
        if (b == null) return buildEmpty();

        StackPane page = new StackPane();
        page.setStyle("-fx-background-color:#030510;");

        VBox outer = new VBox(20);
        outer.setAlignment(Pos.CENTER);
        outer.setPadding(new Insets(30));

        // ─ Receipt card ─
        VBox receipt = new VBox(0);
        receipt.setMaxWidth(620);
        receipt.setStyle(String.format(
                "-fx-background-color:linear-gradient(to bottom,%s,%s);" +
                        "-fx-background-radius:24;-fx-border-radius:24;" +
                        "-fx-border-color:%s;-fx-border-width:2;" +
                        "-fx-effect:dropshadow(gaussian,%s,44,0,0,12);",
                rgba(SURFACE_1, 0.99), rgba(BG_DARK, 0.99),
                rgba(NEON_GREEN, 0.75), rgba(NEON_GREEN, 0.28)));

        // Header
        VBox rHdr = new VBox(8);
        rHdr.setAlignment(Pos.CENTER);
        rHdr.setPadding(new Insets(30, 34, 36, 34));
        rHdr.setStyle(String.format(
                "-fx-background-color:linear-gradient(to bottom right,%s,%s);-fx-background-radius:24 24 0 0;",
                rgba(Color.rgb(8, 56, 38), 1.0), rgba(Color.rgb(5, 28, 18), 1.0)));

        Label checkL = new Label("✅"); checkL.setFont(Font.font(52)); checkL.setTextFill(Color.WHITE);
        ScaleTransition pulse = new ScaleTransition(Duration.millis(350), checkL);
        pulse.setFromX(0.4); pulse.setFromY(0.4); pulse.setToX(1); pulse.setToY(1);
        pulse.setInterpolator(Interpolator.EASE_OUT); pulse.play();

        Label confL   = mkLbl("Booking Confirmed!", 28, FontWeight.BOLD, Color.rgb(167, 243, 208));
        confL.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        Label bookIdL = mkLbl("Booking ID: " + b.bookingId, 14, FontWeight.BOLD, Color.rgb(110, 231, 183));
        Label tsL     = mkLbl("Confirmed on  " + b.timestamp, 12, FontWeight.NORMAL,
                Color.rgb(52, 211, 153, 0.75));
        rHdr.getChildren().addAll(checkL, confL, bookIdL, tsL);

        // Body
        VBox body = new VBox(10);
        body.setPadding(new Insets(24, 34, 28, 34));

        if (state.activeCustomer != null) {
            body.getChildren().add(rctRow("Customer", state.activeCustomer.name, TEXT_BRIGHT));
            body.getChildren().add(rctRow("NIC",      state.activeCustomer.nic,  TEXT_MUTED));
        }
        body.getChildren().add(rctRow("Payment", b.paymentMethod, TEXT_BRIGHT));
        if (b.promoUsed != null && !b.promoUsed.isEmpty())
            body.getChildren().add(rctRow("Promo Code", b.promoUsed, NEON_GREEN));
        body.getChildren().add(hLine());

        // Column headers
        HBox colH = new HBox(); colH.setPadding(new Insets(5, 0, 5, 0));
        Label vh = mkLbl("Vehicle",  11, FontWeight.BOLD, TEXT_DIM);
        Label dh = mkLbl("Days",     11, FontWeight.BOLD, TEXT_DIM);
        Label sh = mkLbl("Subtotal", 11, FontWeight.BOLD, TEXT_DIM);
        colH.getChildren().addAll(vh, hSpacer(), new HBox(24, dh, sh));
        body.getChildren().add(colH);
        body.getChildren().add(hLine());

        for (CartItem ci : b.items) {
            HBox row = new HBox(); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(8, 0, 8, 0));
            HBox left = new HBox(6);
            Label ico = new Label(ci.car.icon); ico.setTextFill(Color.WHITE); ico.setFont(Font.font(16));
            Label nm  = mkLbl(ci.car.name, 13, FontWeight.BOLD, TEXT_PRIMARY);
            left.getChildren().addAll(ico, nm);
            Label dl = mkLbl(ci.days + "d",                           13, FontWeight.NORMAL, TEXT_MUTED);
            Label sl = mkLbl("$" + String.format("%.2f", ci.subtotal()), 13, FontWeight.BOLD,  NEON_GREEN);
            row.getChildren().addAll(left, hSpacer(), new HBox(24, dl, sl));
            body.getChildren().add(row);
        }
        body.getChildren().add(hLine());

        if (b.discount > 0) {
            HBox discRow = new HBox();
            Label dl = mkLbl("Discount Applied", 13, FontWeight.NORMAL, NEON_GREEN);
            Label dv = mkLbl("-$" + String.format("%.2f", b.discount), 13, FontWeight.BOLD, NEON_GREEN);
            discRow.getChildren().addAll(dl, hSpacer(), dv);
            body.getChildren().add(discRow);
        }

        // Grand total bar
        HBox totBar = new HBox();
        totBar.setAlignment(Pos.CENTER_LEFT);
        totBar.setPadding(new Insets(14, 18, 14, 18));
        totBar.setStyle(String.format(
                "-fx-background-color:linear-gradient(to right,%s,%s);" +
                        "-fx-background-radius:12;-fx-border-color:%s;-fx-border-radius:12;",
                rgba(Color.rgb(56, 36, 0), 0.9), rgba(Color.rgb(28, 18, 0), 0.9), rgba(AMBER, 0.45)));
        Label gtL = mkLbl("GRAND TOTAL", 12, FontWeight.BOLD, AMBER);
        Label gtV = mkLbl("$" + String.format("%.2f", b.totalCost), 32, FontWeight.BOLD, AMBER);
        gtV.setFont(Font.font("Georgia", FontWeight.BOLD, 32));
        totBar.getChildren().addAll(gtL, hSpacer(), gtV);
        body.getChildren().add(totBar);

        Label thanksL = mkLbl("🎉  Thank you for choosing DriveEase!", 14, FontWeight.NORMAL, TEXT_MUTED);
        thanksL.setMaxWidth(Double.MAX_VALUE); thanksL.setAlignment(Pos.CENTER);
        Label safeL = mkLbl("Safe travels and enjoy your ride  🚗", 12, FontWeight.NORMAL, TEXT_DIM);
        safeL.setMaxWidth(Double.MAX_VALUE); safeL.setAlignment(Pos.CENTER);
        body.getChildren().addAll(thanksL, safeL);

        receipt.getChildren().addAll(rHdr, body);

        // Slide-up animation
        receipt.setTranslateY(320); receipt.setOpacity(0);
        new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(receipt.translateYProperty(), 320),
                        new KeyValue(receipt.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(650),
                        new KeyValue(receipt.translateYProperty(), 0, Interpolator.EASE_OUT),
                        new KeyValue(receipt.opacityProperty(), 1, Interpolator.EASE_OUT))
        ).play();

        // Bottom buttons
        HBox btns = new HBox(16); btns.setAlignment(Pos.CENTER);
        Button profBtn = mkBtn("View My Profile  👤", NEON_PURPLE, Color.rgb(90, 18, 200));
        profBtn.setOnAction(e -> { ctrl.rebuild("PROFILE"); ctrl.showScene("PROFILE"); });
        Button homeBtn = mkBtn("Return to Portal  🏠", NEON_BLUE, Color.rgb(12, 55, 200));
        homeBtn.setOnAction(e -> ctrl.showScene("PORTAL"));
        btns.getChildren().addAll(profBtn, homeBtn);

        outer.getChildren().addAll(receipt, btns);

        ScrollPane sp = new ScrollPane(outer);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:#030510;-fx-background-color:#030510;");
        page.getChildren().add(sp);
        return page;
    }

    // ── Helper ────────────────────────────────────────────────────

    private HBox rctRow(String key, String value, Color c) {
        HBox row = new HBox(); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(4, 0, 4, 0));
        Label ll = mkLbl(key, 13, FontWeight.NORMAL, TEXT_DIM);
        Label vl = mkLbl(value, 13, FontWeight.BOLD, c);
        row.getChildren().addAll(ll, hSpacer(), vl); return row;
    }
}