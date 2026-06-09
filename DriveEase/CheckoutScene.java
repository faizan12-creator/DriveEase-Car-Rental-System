package DriveEase;

import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.util.ArrayList;

import static DriveEase.AppColors.*;
import static DriveEase.UIHelper.*;

/**
 * Builds the secure checkout page — promo codes, add-ons, payment method.
 */
public class CheckoutScene {

    private final AppState        state;
    private final SceneController ctrl;

    public CheckoutScene(AppState state, SceneController ctrl) {
        this.state = state; this.ctrl = ctrl;
    }

    public Node build() {
        StackPane outer = new StackPane();
        outer.setStyle("-fx-background-color:#030510;");

        VBox card = new VBox(16);
        card.setMaxWidth(590);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(34, 46, 34, 46));
        card.setStyle(String.format(
                "-fx-background-color:linear-gradient(to bottom,%s,%s);" +
                        "-fx-background-radius:26;-fx-border-radius:26;" +
                        "-fx-border-color:%s;-fx-border-width:1.5;" +
                        "-fx-effect:dropshadow(gaussian,%s,32,0,0,10);",
                rgba(SURFACE_1, 0.98), rgba(BG_DARK, 0.98),
                rgba(NEON_GREEN, 0.55), rgba(NEON_GREEN, 0.18)));

        Label emoji  = new Label("💳"); emoji.setFont(Font.font(50)); emoji.setTextFill(Color.WHITE);
        Label titleL = mkTitle("Secure Checkout");
        Label subL   = mkLbl("Review order, apply promo, and complete payment",
                13, FontWeight.NORMAL, TEXT_MUTED);

        // ─ Order items box ─
        VBox itemsBox = new VBox(8);
        itemsBox.setPadding(new Insets(14, 16, 14, 16));
        itemsBox.setStyle(String.format(
                "-fx-background-color:%s;-fx-background-radius:12;-fx-border-color:%s;-fx-border-radius:12;",
                rgba(BG_DARK, 0.92), rgba(BORDER_DIM, 0.55)));

        double gross = 0;
        for (CartItem ci : state.cart) {
            gross += ci.subtotal();
            HBox r = new HBox(); r.setAlignment(Pos.CENTER_LEFT);
            Label il = mkLbl(ci.car.icon + " " + ci.car.name + " × " + ci.days + "d",
                    13, FontWeight.NORMAL, TEXT_PRIMARY);
            Label iv = mkLbl("$" + String.format("%.2f", ci.subtotal()),
                    13, FontWeight.BOLD, NEON_GREEN);
            r.getChildren().addAll(il, hSpacer(), iv);
            itemsBox.getChildren().add(r);
        }
        itemsBox.getChildren().add(hLine());
        final double grossFinal = gross;
        Label totalDisplayL = mkLbl("Total: $" + String.format("%.2f", grossFinal),
                15, FontWeight.BOLD, AMBER);
        itemsBox.getChildren().add(totalDisplayL);

        // ─ Promo code ─
        Label promoLabel = mkLbl("Promo Code:", 13, FontWeight.BOLD, TEXT_MUTED);
        HBox promoRow = new HBox(10); promoRow.setAlignment(Pos.CENTER_LEFT);
        TextField promoF  = mkField("Enter promo code"); promoF.setPrefWidth(200);
        Button    applyBtn = mkBtn("Apply", NEON_TEAL, NEON_TEAL.darker().darker());
        Label     promoMsgL = mkLbl("", 12, FontWeight.BOLD, NEON_GREEN);

        final double[] discount    = {0};
        final String[] appliedCode = {""};

        applyBtn.setOnAction(e -> {
            String code = promoF.getText().trim().toUpperCase();
            PromoCode pc = state.promoCodes.stream()
                    .filter(p -> p.code.equals(code)).findFirst().orElse(null);
            if (pc == null || !pc.isValid()) {
                promoMsgL.setTextFill(CRIMSON); promoMsgL.setText("❌  Invalid or expired code");
                discount[0] = 0; appliedCode[0] = "";
                totalDisplayL.setTextFill(AMBER);
                totalDisplayL.setText("Total: $" + String.format("%.2f", grossFinal));
            } else {
                discount[0] = pc.apply(grossFinal); appliedCode[0] = pc.code;
                promoMsgL.setTextFill(NEON_GREEN);
                promoMsgL.setText("✅  " + (pc.type.equals("PERCENT") ?
                        pc.value + "% off" : "$" + pc.value + " off") + " applied!");
                totalDisplayL.setTextFill(NEON_GREEN);
                totalDisplayL.setText("Total: $" + String.format("%.2f", grossFinal - discount[0])
                        + "  (saved $" + String.format("%.2f", discount[0]) + ")");
            }
        });
        promoRow.getChildren().addAll(promoF, applyBtn, promoMsgL);

        // ─ Payment method ─
        Label payLbl = mkLbl("Payment Method:", 13, FontWeight.BOLD, TEXT_MUTED);
        ComboBox<String> payBox = new ComboBox<>(FXCollections.observableArrayList(
                "Bank Transfer", "Cash on Pickup", "Credit Card", "Debit Card", "JazzCash", "EasyPaisa"));
        payBox.setValue("Bank Transfer"); payBox.setMaxWidth(Double.MAX_VALUE);

        // ─ Add-ons ─
        Label addOnLbl = mkLbl("Optional Add-ons:", 13, FontWeight.BOLD, TEXT_MUTED);
        CheckBox insCheck = new CheckBox("🛡️  Basic Insurance (+$10/day per vehicle)");
        CheckBox gpsCheck = new CheckBox("🗺️  GPS Navigation (+$5/day per vehicle)");
        CheckBox drvCheck = new CheckBox("👨‍✈️  Driver Service (+$30/day per vehicle)");

        Button confirmBtn = mkBtn("✅  Confirm Booking  →", NEON_GREEN, NEON_GREEN.darker().darker());
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        Button cancelBtn = mkBtnGhost("← Modify Cart");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);

        confirmBtn.setOnAction(e -> {
            int totalDays = state.cart.stream().mapToInt(ci -> ci.days).sum();
            double addons = 0;
            if (insCheck.isSelected()) addons += 10.0 * totalDays;
            if (gpsCheck.isSelected()) addons +=  5.0 * totalDays;
            if (drvCheck.isSelected()) addons += 30.0 * totalDays;

            double finalTotal = grossFinal + addons - discount[0];

            // Mark promo as used
            if (!appliedCode[0].isEmpty()) {
                state.promoCodes.stream()
                        .filter(p -> p.code.equals(appliedCode[0]))
                        .findFirst().ifPresent(p -> p.usedCount++);
            }

            // Mark cars as rented, create booking
            for (CartItem ci : state.cart) { ci.car.isAvailable = false; ci.car.inCart = false; }
            BookingRecord br = new BookingRecord(new ArrayList<>(state.cart),
                    finalTotal, payBox.getValue(), discount[0], appliedCode[0]);
            state.allBookings.add(br);
            if (state.activeCustomer != null) state.activeCustomer.bookings.add(br);
            ctrl.saveData();

            // Navigate to receipt
            state.cart.clear();
            ReceiptScene rs = new ReceiptScene(state, ctrl);
            ctrl.putScene("RECEIPT", rs.build(br));
            ctrl.showScene("RECEIPT");
        });
        cancelBtn.setOnAction(e -> { ctrl.rebuild("CART"); ctrl.showScene("CART"); });

        card.getChildren().addAll(emoji, titleL, subL, itemsBox,
                promoLabel, promoRow, payLbl, payBox,
                addOnLbl, insCheck, gpsCheck, drvCheck,
                confirmBtn, cancelBtn);

        ScrollPane sp = new ScrollPane(card);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        outer.getChildren().add(sp);
        return outer;
    }
}