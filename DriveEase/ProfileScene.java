package DriveEase;


import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
        import javafx.scene.Node;
import javafx.scene.control.*;
        import javafx.scene.layout.*;
        import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;

        import java.util.*;

        import static DriveEase.AppColors.*;
        import static DriveEase.UIHelper.*;

/**
 * Builds the customer profile page with three tabs:
 *   1. Account Info
 *   2. Login History
 *   3. My Bookings
 */
public class ProfileScene {

    private final AppState        state;
    private final SceneController ctrl;

    public ProfileScene(AppState state, SceneController ctrl) {
        this.state = state; this.ctrl = ctrl;
    }

    public Node build() {
        BorderPane page = new BorderPane();
        page.setStyle("-fx-background-color:#050812;");

        // ─ Navigation bar ─
        HBox nav = new HBox(14);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(14, 26, 14, 26));
        nav.setStyle("-fx-background-color:#070b1a;-fx-border-color:#0f1e3c;-fx-border-width:0 0 1 0;");

        Label titleL = new Label("My Account");
        titleL.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        titleL.setTextFill(TEXT_BRIGHT);

        Button browseBtn = mkBtn("🚗  Browse Cars", NEON_BLUE, Color.rgb(12, 55, 200));
        browseBtn.setOnAction(e -> { ctrl.rebuild("CUST_FLEET"); ctrl.showScene("CUST_FLEET"); });

        Button signOutBtn = mkBtnRed("Sign Out  ↩");
        signOutBtn.setOnAction(e -> {
            state.activeCustomer = null;
            state.cart.clear();
            state.fleet.forEach(c -> c.inCart = false);
            ctrl.showScene("PORTAL");
        });

        nav.getChildren().addAll(titleL, hSpacer(), browseBtn, signOutBtn);

        // ─ Tab pane ─
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(buildInfoTab(), buildHistoryTab(), buildBookingsTab());

        page.setTop(nav);
        page.setCenter(tabs);
        return page;
    }

    // ══════════════════ TAB 1 — Account Info ══════════════════════

    private Tab buildInfoTab() {
        Tab tab = new Tab("  Account Info  ");

        VBox content = new VBox(20);
        content.setPadding(new Insets(22, 26, 22, 26));

        if (state.activeCustomer != null) {
            Customer c = state.activeCustomer;
            content.getChildren().add(buildProfileCard(c));
            content.getChildren().add(buildInfoGrid(c));
        }

        tab.setContent(new ScrollPane(content) {{
            setFitToWidth(true);
            setStyle("-fx-background:#050812;-fx-background-color:#050812;");
        }});
        return tab;
    }

    private HBox buildProfileCard(Customer c) {
        HBox card = new HBox(22);
        card.setPadding(new Insets(22, 24, 22, 24));
        card.setStyle(String.format(
                "-fx-background-color:linear-gradient(to right,%s,%s);" +
                        "-fx-background-radius:20;-fx-border-color:%s;-fx-border-radius:20;",
                rgba(SURFACE_2, 0.9), rgba(SURFACE_1, 0.9), rgba(NEON_PURPLE, 0.38)));

        // Avatar circle
        StackPane avatar = new StackPane();
        Circle bg = new Circle(36, 36, 36);
        bg.setFill(Color.rgb(40, 50, 90));
        bg.setStroke(NEON_PURPLE); bg.setStrokeWidth(3);
        Label avatarTxt = new Label(c.name.substring(0, 1).toUpperCase());
        avatarTxt.setFont(Font.font("Georgia", FontWeight.BOLD, 32));
        avatarTxt.setTextFill(TEXT_BRIGHT);
        avatar.getChildren().addAll(bg, avatarTxt);

        // Info
        VBox profInfo = new VBox(8);
        profInfo.getChildren().add(mkLbl(c.name, 22, FontWeight.BOLD, TEXT_BRIGHT));
        profInfo.getChildren().add(mkBadge("⭐  Loyal Customer", NEON_YELLOW));
        profInfo.getChildren().add(mkLbl("NIC: " + c.nic, 13, FontWeight.NORMAL, TEXT_MUTED));
        if (!c.phone.isEmpty())
            profInfo.getChildren().add(mkLbl("📞 " + c.phone, 13, FontWeight.NORMAL, TEXT_MUTED));
        if (!c.email.isEmpty())
            profInfo.getChildren().add(mkLbl("📧 " + c.email, 13, FontWeight.NORMAL, TEXT_MUTED));

        // Stats box
        VBox statBox = new VBox(12);
        statBox.setPadding(new Insets(10, 18, 10, 18));
        statBox.setStyle(String.format(
                "-fx-background-color:%s;-fx-background-radius:14;-fx-border-color:%s;-fx-border-radius:14;",
                rgba(SURFACE_1, 0.8), rgba(BORDER_DIM, 0.45)));

        long activeBks = c.bookings.stream().filter(br -> br.status.equals("ACTIVE")).count();
        double totalSpent = c.bookings.stream().mapToDouble(br -> br.totalCost).sum();

        statBox.getChildren().addAll(
                statRow(String.valueOf(c.bookings.size()), "Total\nBookings", NEON_BLUE),
                new Separator(),
                statRow(String.valueOf(activeBks), "Active\nRentals", NEON_GREEN),
                new Separator(),
                statRow("$" + String.format("%.0f", totalSpent), "Total\nSpent", AMBER)
        );

        card.getChildren().addAll(avatar, profInfo, hSpacer(), statBox);
        return card;
    }

    private HBox statRow(String value, String label, Color color) {
        HBox row = new HBox(12); row.setAlignment(Pos.CENTER);
        Label val = mkLbl(value, 36, FontWeight.BOLD, color);
        val.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        Label lbl = mkLbl(label, 11, FontWeight.NORMAL, TEXT_DIM);
        row.getChildren().addAll(val, lbl);
        return row;
    }

    private GridPane buildInfoGrid(Customer c) {
        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(0);
        addRow(grid, 0, "Full Name",    c.name);
        addRow(grid, 1, "NIC Number",   c.nic);
        addRow(grid, 2, "Phone",        c.phone.isEmpty() ? "—" : c.phone);
        addRow(grid, 3, "Email",        c.email.isEmpty() ? "—" : c.email);
        addRow(grid, 4, "First Login",  c.loginHistory.isEmpty() ? "—" : c.loginHistory.get(0));
        addRow(grid, 5, "Last Login",   c.loginHistory.size() < 2 ? "—"
                : c.loginHistory.get(c.loginHistory.size() - 1));
        addRow(grid, 6, "Total Logins", String.valueOf(c.loginHistory.size()));
        return grid;
    }

    private void addRow(GridPane grid, int row, String key, String val) {
        Label kl = mkLbl(key, 13, FontWeight.NORMAL, TEXT_DIM);
        kl.setPadding(new Insets(8, 0, 8, 0));
        kl.setStyle("-fx-border-color:#0f1e3c;-fx-border-width:0 0 1 0;");
        Label vl = mkLbl(val, 14, FontWeight.BOLD, TEXT_PRIMARY);
        vl.setPadding(new Insets(8, 0, 8, 0));
        vl.setStyle("-fx-border-color:#0f1e3c;-fx-border-width:0 0 1 0;");
        grid.add(kl, 0, row); grid.add(vl, 1, row);
    }

    // ══════════════════ TAB 2 — Login History ═════════════════════

    private Tab buildHistoryTab() {
        Tab tab = new Tab("  Login History  ");

        VBox content = new VBox(10);
        content.setPadding(new Insets(18, 22, 18, 22));

        if (state.activeCustomer != null && !state.activeCustomer.loginHistory.isEmpty()) {
            TableView<Map.Entry<Integer, String>> table = new TableView<>();

            TableColumn<Map.Entry<Integer, String>, String> idxCol = new TableColumn<>("#");
            idxCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getKey())));
            idxCol.setPrefWidth(60);

            TableColumn<Map.Entry<Integer, String>, String> tsCol = new TableColumn<>("Session Date & Time");
            tsCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getValue()));
            tsCol.setPrefWidth(340);

            TableColumn<Map.Entry<Integer, String>, String> stCol = new TableColumn<>("Status");
            stCol.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getKey() == state.activeCustomer.loginHistory.size()
                            ? "Current Session" : "Completed"));
            stCol.setPrefWidth(160);

            table.getColumns().addAll(idxCol, tsCol, stCol);

            List<Map.Entry<Integer, String>> entries = new ArrayList<>();
            List<String> hist = state.activeCustomer.loginHistory;
            for (int i = hist.size() - 1; i >= 0; i--)
                entries.add(new AbstractMap.SimpleEntry<>(hist.size() - i, hist.get(i)));
            table.setItems(FXCollections.observableArrayList(entries));
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            content.getChildren().add(table);
        } else {
            content.getChildren().add(mkLbl("No login history found.", 16, FontWeight.NORMAL, TEXT_MUTED));
        }

        tab.setContent(content);
        return tab;
    }

    // ══════════════════ TAB 3 — My Bookings ═══════════════════════

    private Tab buildBookingsTab() {
        Tab tab = new Tab("  My Bookings  ");

        VBox content = new VBox(12);
        content.setPadding(new Insets(18, 22, 18, 22));

        if (state.activeCustomer != null && !state.activeCustomer.bookings.isEmpty()) {
            for (BookingRecord br : state.activeCustomer.bookings)
                content.getChildren().add(buildBookingCard(br));
        } else {
            content.getChildren().add(mkLbl("No bookings yet.", 16, FontWeight.NORMAL, TEXT_MUTED));
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#050812;-fx-background-color:#050812;");
        tab.setContent(scroll);
        return tab;
    }

    private VBox buildBookingCard(BookingRecord br) {
        boolean returned = br.status.equals("RETURNED");

        VBox card = new VBox(10);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(String.format(
                "-fx-background-color:%s;-fx-background-radius:16;" +
                        "-fx-border-color:%s;-fx-border-radius:16;-fx-border-width:1.5;",
                rgba(SURFACE_1, 0.9),
                rgba(returned ? NEON_GREEN : NEON_BLUE, 0.55)));

        // Top row — ID + status badge
        HBox topRow = new HBox(12); topRow.setAlignment(Pos.CENTER_LEFT);
        Label bidL    = mkLbl("📋 " + br.bookingId, 14, FontWeight.BOLD, TEXT_BRIGHT);
        Label statusL = mkBadge(returned ? "✅ Returned" : "🔵 Active",
                returned ? NEON_GREEN : NEON_BLUE);
        topRow.getChildren().addAll(bidL, hSpacer(), statusL);

        // Timestamp
        Label tsL = mkLbl("Booked: " + br.timestamp, 12, FontWeight.NORMAL, TEXT_DIM);

        // Cars list
        StringBuilder carsStr = new StringBuilder();
        for (CartItem ci : br.items)
            carsStr.append(ci.car.icon).append(" ").append(ci.car.name)
                    .append(" (").append(ci.days).append("d), ");
        Label carsL = mkLbl(carsStr.toString().replaceAll(",\\s*$", ""),
                13, FontWeight.NORMAL, TEXT_MUTED);

        // Financial row
        HBox finRow = new HBox(12); finRow.setAlignment(Pos.CENTER_LEFT);
        Label finL = mkLbl("💰 Total: $" + String.format("%.2f", br.totalCost)
                + "  ·  " + br.paymentMethod, 13, FontWeight.NORMAL, AMBER);
        finRow.getChildren().add(finL);
        if (br.discount > 0)
            finRow.getChildren().add(mkLbl("(Saved $" + String.format("%.2f", br.discount) + ")",
                    12, FontWeight.NORMAL, NEON_GREEN));

        // Return button
        HBox actionRow = new HBox(); actionRow.setAlignment(Pos.CENTER_RIGHT);
        if (!returned) {
            Button retBtn = mkBtn("↩  Return Vehicle", NEON_ORANGE, NEON_ORANGE.darker().darker());
            retBtn.setOnAction(e -> {
                if (ctrl.showConfirm("Confirm Return",
                        "Return all vehicles in Booking " + br.bookingId + "?")) {
                    br.status = "RETURNED";
                    for (CartItem ci : br.items) ci.car.isAvailable = true;
                    ctrl.showAlert("Return Successful",
                            "✅ Vehicles returned!\nThey are now available for rental.",
                            Alert.AlertType.INFORMATION);
                    ctrl.saveData();
                    ctrl.rebuild("PROFILE", "CUST_FLEET");
                    ctrl.showScene("PROFILE");
                }
            });
            actionRow.getChildren().add(retBtn);
        }

        card.getChildren().addAll(topRow, tsL, carsL, finRow, actionRow);
        return card;
    }
}