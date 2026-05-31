package DriveEase;


import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
        import javafx.scene.Node;
import javafx.scene.control.*;
        import javafx.scene.layout.*;
        import javafx.scene.paint.Color;
import javafx.scene.text.*;

        import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static DriveEase.AppColors.*;
        import static DriveEase.UIHelper.*;

/**
 * Builds the full admin dashboard:
 *   - Sidebar navigation
 *   - Overview (metrics + charts)
 *   - Fleet Management
 *   - All Bookings
 *   - Customers
 *   - Promo Codes
 *   - Settings
 */
public class AdminDashScene {

    private final AppState        state;
    private final SceneController ctrl;

    public AdminDashScene(AppState state, SceneController ctrl) {
        this.state = state; this.ctrl = ctrl;
    }

    // ══════════════════ MAIN LAYOUT ═══════════════════════════════

    public Node build() {
        BorderPane page = new BorderPane();
        page.setStyle("-fx-background-color:#030510;");

        StackPane contentArea = new StackPane();
        contentArea.getChildren().add(buildOverview());   // default view

        page.setLeft(buildSidebar(contentArea));
        page.setCenter(contentArea);
        return page;
    }

    // ── Sidebar ───────────────────────────────────────────────────

    private VBox buildSidebar(StackPane contentArea) {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color:#050a1a;-fx-border-color:#0f1e3c;-fx-border-width:0 1 0 0;");

        // Brand
        VBox brand = new VBox(6);
        brand.setPadding(new Insets(22, 18, 22, 18));
        brand.getChildren().add(mkLbl("DriveEase", 22, FontWeight.BOLD, TEXT_BRIGHT));
        brand.getChildren().add(mkLbl("Admin Panel", 11, FontWeight.NORMAL, TEXT_DIM));
        sidebar.getChildren().addAll(brand, hLine());

        // Nav buttons
        VBox nav = new VBox(6);
        nav.setPadding(new Insets(12, 10, 12, 10));

        String[] labels = {
                "📊  Dashboard Overview",
                "🚗  Fleet Management",
                "📋  All Bookings",
                "👥  Customers",
                "🏷️  Promo Codes",
                "⚙️  Settings"
        };
        Runnable[] actions = {
                () -> contentArea.getChildren().setAll(buildOverview()),
                () -> contentArea.getChildren().setAll(buildFleetMgmt()),
                () -> contentArea.getChildren().setAll(buildAllBookings()),
                () -> contentArea.getChildren().setAll(buildCustomers()),
                () -> contentArea.getChildren().setAll(buildPromoCodes()),
                () -> contentArea.getChildren().setAll(buildSettings())
        };

        for (int i = 0; i < labels.length; i++) {
            nav.getChildren().add(navBtn(labels[i], actions[i]));
        }

        // Logout
        Button signOut = mkBtnRed("🚪  Sign Out");
        signOut.setMaxWidth(Double.MAX_VALUE);
        signOut.setOnAction(e -> ctrl.showScene("PORTAL"));
        VBox logoutBox = new VBox(10);
        logoutBox.setPadding(new Insets(12, 10, 18, 10));
        logoutBox.getChildren().addAll(hLine(), signOut);

        VBox spacer = new VBox(); VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(nav, spacer, logoutBox);
        return sidebar;
    }

    private Button navBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        String normal = "-fx-background-color:transparent;-fx-text-fill:#64748b;" +
                "-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        String hover  = "-fx-background-color:#0f1a38;-fx-text-fill:#e2e8f0;" +
                "-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited (e -> btn.setStyle(normal));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    // ══════════════════ OVERVIEW ══════════════════════════════════

    private Node buildOverview() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(32, 36, 32, 36));
        root.setStyle("-fx-background-color:#030510;");

        Label titleL = new Label("Dashboard Overview");
        titleL.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        titleL.setTextFill(TEXT_BRIGHT);
        Label subL = mkLbl("Real-time analytics for your fleet and customers",
                14, FontWeight.NORMAL, TEXT_MUTED);

        // ─ Metric cards ─
        int    tot      = state.fleet.size();
        int    avl      = (int)  state.fleet.stream().filter(c -> c.isAvailable).count();
        int    rent     = tot - avl;
        long   active   = state.allBookings.stream().filter(b -> b.status.equals("ACTIVE")).count();
        long   returned = state.allBookings.stream().filter(b -> b.status.equals("RETURNED")).count();
        double revenue  = state.allBookings.stream().mapToDouble(b -> b.totalCost).sum();

        GridPane metrics = new GridPane();
        metrics.setHgap(16); metrics.setVgap(16);
        metrics.add(metricCard("Total Fleet",    String.valueOf(tot),              NEON_BLUE,   "vehicles registered"), 0, 0);
        metrics.add(metricCard("Available",      String.valueOf(avl),              NEON_GREEN,  "ready to rent"),       1, 0);
        metrics.add(metricCard("Rented Out",     String.valueOf(rent),             NEON_ORANGE, "currently rented"),    2, 0);
        metrics.add(metricCard("Total Bookings", String.valueOf(state.allBookings.size()), AMBER,"all time"),           3, 0);
        metrics.add(metricCard("Customers",      String.valueOf(state.customers.size()), NEON_PURPLE,"registered"),     0, 1);
        metrics.add(metricCard("Active Rentals", String.valueOf(active),           NEON_TEAL,   "in progress"),         1, 1);
        metrics.add(metricCard("Returns",        String.valueOf(returned),         NEON_GREEN,  "completed"),           2, 1);
        metrics.add(metricCard("Revenue",        "$" + String.format("%.0f", revenue), NEON_YELLOW,"total earned"),    3, 1);

        // ─ Chart + recent bookings ─
        HBox bottomRow = new HBox(16);

        VBox fleetChart = buildFleetChart(avl, rent, tot, (int) active, (int) returned);
        VBox recentBox  = buildRecentBox();

        HBox.setHgrow(fleetChart, Priority.ALWAYS);
        HBox.setHgrow(recentBox,  Priority.ALWAYS);
        bottomRow.getChildren().addAll(fleetChart, recentBox);

        root.getChildren().addAll(titleL, subL, metrics, bottomRow);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#030510;-fx-background-color:#030510;");
        return scroll;
    }

    private VBox metricCard(String title, String value, Color color, String subtext) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(20, 22, 20, 22));
        box.setStyle(String.format(
                "-fx-background-color:linear-gradient(to bottom right,%s,%s);" +
                        "-fx-background-radius:18;-fx-border-radius:18;" +
                        "-fx-border-color:%s;-fx-border-width:1.5;" +
                        "-fx-effect:dropshadow(gaussian,%s,14,0,0,4);",
                rgba(SURFACE_2, 0.92), rgba(SURFACE_1, 0.92),
                rgba(color, 0.55), rgba(color, 0.18)));

        Region accent = new Region(); accent.setPrefHeight(3);
        accent.setStyle("-fx-background-color:" + hex(color) + ";-fx-background-radius:2;");

        Label valL = mkLbl(value, 40, FontWeight.BOLD, color);
        valL.setFont(Font.font("Georgia", FontWeight.BOLD, 40));
        Label titleL = mkLbl(title,   14, FontWeight.BOLD, TEXT_PRIMARY);
        Label subL   = mkLbl("▲  " + subtext, 11, FontWeight.NORMAL, TEXT_DIM);

        // Hover
        String base = box.getStyle();
        box.setOnMouseEntered(e -> box.setStyle(base
                .replace(rgba(color, 0.18), rgba(color, 0.35))
                .replace(rgba(SURFACE_2, 0.92), rgba(SURFACE_2.brighter(), 0.95))));
        box.setOnMouseExited(e  -> box.setStyle(base));

        box.getChildren().addAll(accent, valL, titleL, subL);
        return box;
    }

    private VBox buildFleetChart(int avl, int rent, int tot, int active, int returned) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16, 20, 16, 20));
        box.setStyle("-fx-background-color:#070b1a;-fx-background-radius:16;" +
                "-fx-border-color:#0f1e3c;-fx-border-radius:16;");
        box.getChildren().add(mkLbl("Fleet Status", 14, FontWeight.BOLD, TEXT_BRIGHT));
        box.getChildren().add(progressBar("Available",     avl,      tot,                             NEON_GREEN));
        box.getChildren().add(progressBar("Rented Out",    rent,     tot,                             NEON_ORANGE));
        box.getChildren().add(progressBar("Active",        active,   Math.max(1, state.allBookings.size()), NEON_TEAL));
        box.getChildren().add(progressBar("Returned",      returned, Math.max(1, state.allBookings.size()), NEON_GREEN));
        return box;
    }

    private HBox progressBar(String label, int value, int max, Color color) {
        HBox box = new HBox(8); box.setAlignment(Pos.CENTER_LEFT);
        Label ll = mkLbl(label, 12, FontWeight.NORMAL, TEXT_MUTED); ll.setPrefWidth(85);

        StackPane track = new StackPane(); track.setPrefHeight(8);
        HBox.setHgrow(track, Priority.ALWAYS);
        Region bg  = new Region(); bg.setStyle("-fx-background-color:#0f1a38;-fx-background-radius:4;");
        double pct = max == 0 ? 0 : (double) value / max;
        Region fill = new Region();
        fill.setStyle("-fx-background-color:" + hex(color) + ";-fx-background-radius:4;");
        fill.prefWidthProperty().bind(track.widthProperty().multiply(pct));
        track.getChildren().addAll(bg, fill);

        Label vl = mkLbl(value + "/" + max, 12, FontWeight.BOLD, color); vl.setPrefWidth(60);
        box.getChildren().addAll(ll, track, vl);
        return box;
    }

    private VBox buildRecentBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16, 20, 16, 20));
        box.setStyle("-fx-background-color:#070b1a;-fx-background-radius:16;" +
                "-fx-border-color:#0f1e3c;-fx-border-radius:16;");
        box.getChildren().add(mkLbl("Recent Bookings", 14, FontWeight.BOLD, TEXT_BRIGHT));

        List<BookingRecord> recent = state.allBookings.stream()
                .sorted((a, b) -> b.timestamp.compareTo(a.timestamp))
                .limit(4).collect(Collectors.toList());

        if (recent.isEmpty()) {
            box.getChildren().add(mkLbl("No bookings yet", 13, FontWeight.NORMAL, TEXT_DIM));
        } else {
            for (BookingRecord br : recent) {
                HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8, 0, 8, 0));
                row.getChildren().add(mkLbl("📋 " + br.bookingId, 12, FontWeight.BOLD, NEON_BLUE));
                row.getChildren().add(mkLbl(br.timestamp, 11, FontWeight.NORMAL, TEXT_DIM));
                row.getChildren().add(hSpacer());
                row.getChildren().add(mkLbl("$" + String.format("%.2f", br.totalCost),
                        13, FontWeight.BOLD, AMBER));
                row.getChildren().add(mkBadge(br.status,
                        br.status.equals("ACTIVE") ? NEON_TEAL : NEON_GREEN));
                box.getChildren().add(row);
                if (recent.indexOf(br) < recent.size() - 1) {
                    Region divider = new Region(); divider.setPrefHeight(1);
                    divider.setStyle("-fx-background-color:#0f1e3c;");
                    box.getChildren().add(divider);
                }
            }
        }
        return box;
    }

    // ══════════════════ FLEET MANAGEMENT ══════════════════════════

    private Node buildFleetMgmt() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(24, 28, 24, 28));
        root.setStyle("-fx-background-color:#030510;");

        Label titleL = new Label("Fleet Management");
        titleL.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        titleL.setTextFill(TEXT_BRIGHT);

        Button toggleBtn = mkBtn("Toggle Availability", NEON_PURPLE, Color.rgb(90, 18, 200));

        TableView<Car> table = new TableView<>();
        TableColumn<Car, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id));
        TableColumn<Car, String> nameCol = new TableColumn<>("Vehicle");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        TableColumn<Car, String> plateCol = new TableColumn<>("Plate");
        plateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().plate));
        TableColumn<Car, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().category));
        TableColumn<Car, String> priceCol = new TableColumn<>("Price/Day");
        priceCol.setCellValueFactory(d -> new SimpleStringProperty("$" + (int) d.getValue().pricePerDay));
        TableColumn<Car, String> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().seats)));
        TableColumn<Car, String> fuelCol = new TableColumn<>("Fuel");
        fuelCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().fuel));
        TableColumn<Car, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().isAvailable ? "Available ✅" : "Rented ❌"));

        table.getColumns().addAll(idCol, nameCol, plateCol, catCol, priceCol, seatsCol, fuelCol, statusCol);
        table.setItems(FXCollections.observableArrayList(state.fleet));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        toggleBtn.setOnAction(e -> {
            Car sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                ctrl.showAlert("Selection Required", "Please select a vehicle first.", Alert.AlertType.WARNING);
                return;
            }
            sel.isAvailable = !sel.isAvailable;
            ctrl.showAlert("Status Updated", sel.name + " is now " +
                    (sel.isAvailable ? "Available ✅" : "Unavailable ❌"), Alert.AlertType.INFORMATION);
            ctrl.saveData();
            table.refresh();
        });

        HBox controls = new HBox(10, toggleBtn);
        root.getChildren().addAll(titleL, controls, table);
        return root;
    }

    // ══════════════════ ALL BOOKINGS ══════════════════════════════

    private Node buildAllBookings() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(24, 28, 24, 28));
        root.setStyle("-fx-background-color:#030510;");

        Label titleL = new Label("All Bookings");
        titleL.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        titleL.setTextFill(TEXT_BRIGHT);

        TableView<BookingRecord> table = new TableView<>();
        TableColumn<BookingRecord, String> idCol = new TableColumn<>("Booking ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().bookingId));
        TableColumn<BookingRecord, String> tsCol = new TableColumn<>("Date & Time");
        tsCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().timestamp));
        TableColumn<BookingRecord, String> payCol = new TableColumn<>("Payment");
        payCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().paymentMethod));
        TableColumn<BookingRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status));
        TableColumn<BookingRecord, String> vehCol = new TableColumn<>("Vehicles");
        vehCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().items.stream()
                        .map(ci -> ci.car.name + " (" + ci.days + "d)")
                        .collect(Collectors.joining(", "))));
        TableColumn<BookingRecord, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(d ->
                new SimpleStringProperty("$" + String.format("%.2f", d.getValue().totalCost)));
        TableColumn<BookingRecord, String> discCol = new TableColumn<>("Discount");
        discCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().discount > 0
                        ? "-$" + String.format("%.2f", d.getValue().discount) : "—"));

        table.getColumns().addAll(idCol, tsCol, vehCol, totalCol, discCol, payCol, statusCol);
        table.setItems(FXCollections.observableArrayList(state.allBookings));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        root.getChildren().addAll(titleL, table);
        return root;
    }

    // ══════════════════ CUSTOMERS ═════════════════════════════════

    private Node buildCustomers() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(24, 28, 24, 28));
        root.setStyle("-fx-background-color:#030510;");

        Label titleL = new Label("Registered Customers");
        titleL.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        titleL.setTextFill(TEXT_BRIGHT);

        TableView<Customer> table = new TableView<>();
        TableColumn<Customer, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        TableColumn<Customer, String> nicCol = new TableColumn<>("NIC");
        nicCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nic));
        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().phone.isEmpty() ? "—" : d.getValue().phone));
        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().email.isEmpty() ? "—" : d.getValue().email));
        TableColumn<Customer, String> bookCol = new TableColumn<>("Bookings");
        bookCol.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().bookings.size())));
        TableColumn<Customer, String> loginCol = new TableColumn<>("Last Login");
        loginCol.setCellValueFactory(d -> {
            List<String> h = d.getValue().loginHistory;
            return new SimpleStringProperty(h.isEmpty() ? "—" : h.get(h.size() - 1));
        });
        TableColumn<Customer, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().isBlocked ? "🚫 Blocked" : "✅ Active"));

        table.getColumns().addAll(nameCol, nicCol, phoneCol, emailCol, bookCol, loginCol, statusCol);
        table.setItems(FXCollections.observableArrayList(state.customers.values()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Block / unblock button
        Button blockBtn = mkBtn("Block / Unblock Customer", CRIMSON, CRIMSON.darker().darker());
        blockBtn.setOnAction(e -> {
            Customer sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                ctrl.showAlert("Select Customer", "Please select a customer first.", Alert.AlertType.WARNING);
                return;
            }
            sel.isBlocked = !sel.isBlocked;
            ctrl.saveData();
            table.refresh();
            ctrl.showAlert("Status Updated",
                    sel.name + " is now " + (sel.isBlocked ? "🚫 Blocked" : "✅ Active"),
                    Alert.AlertType.INFORMATION);
        });

        root.getChildren().addAll(titleL, new HBox(10, blockBtn), table);
        return root;
    }

    // ══════════════════ PROMO CODES ═══════════════════════════════

    private Node buildPromoCodes() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(24, 28, 24, 28));
        root.setStyle("-fx-background-color:#030510;");

        Label titleL = new Label("Promo Codes");
        titleL.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        titleL.setTextFill(TEXT_BRIGHT);

        // ─ Add form ─
        HBox addRow = new HBox(10); addRow.setAlignment(Pos.CENTER_LEFT);
        TextField     codeF    = mkField("Code (e.g. SUMMER20)"); codeF.setPrefWidth(140);
        ComboBox<String> typeBox = new ComboBox<>(
                FXCollections.observableArrayList("PERCENT", "FLAT"));
        typeBox.setValue("PERCENT"); typeBox.setPrefWidth(100);
        TextField valF  = mkField("Value");    valF.setPrefWidth(80);
        TextField usesF = mkField("Max Uses"); usesF.setPrefWidth(80);
        DatePicker expiry = new DatePicker(LocalDate.now().plusMonths(1));
        expiry.setPrefWidth(130);
        Button addBtn = mkBtn("+  Add", NEON_GREEN, NEON_GREEN.darker().darker());
        addRow.getChildren().addAll(codeF, typeBox, valF, usesF, expiry, addBtn);

        // ─ Table ─
        TableView<PromoCode> table = new TableView<>();
        TableColumn<PromoCode, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().code));
        TableColumn<PromoCode, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().type));
        TableColumn<PromoCode, String> valCol = new TableColumn<>("Value");
        valCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().type.equals("PERCENT") ?
                        d.getValue().value + "%" : "$" + d.getValue().value));
        TableColumn<PromoCode, String> usedCol = new TableColumn<>("Used / Max");
        usedCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().usedCount + " / " + d.getValue().maxUses));
        TableColumn<PromoCode, String> expiryCol = new TableColumn<>("Expiry");
        expiryCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().expiry.toString()));
        TableColumn<PromoCode, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().isValid() ? "✅ Active" : "❌ Inactive"));

        table.getColumns().addAll(codeCol, typeCol, valCol, usedCol, expiryCol, statusCol);
        table.setItems(FXCollections.observableArrayList(state.promoCodes));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        addBtn.setOnAction(e -> {
            String code = codeF.getText().trim().toUpperCase();
            if (code.isEmpty() || valF.getText().trim().isEmpty() || usesF.getText().trim().isEmpty()) {
                ctrl.showAlert("Invalid Input", "Please fill all fields.", Alert.AlertType.WARNING);
                return;
            }
            try {
                double val  = Double.parseDouble(valF.getText().trim());
                int    uses = Integer.parseInt(usesF.getText().trim());
                if (val <= 0 || uses <= 0) throw new NumberFormatException();
                state.promoCodes.add(new PromoCode(code, typeBox.getValue(), val, uses, expiry.getValue()));
                ctrl.saveData();
                table.setItems(FXCollections.observableArrayList(state.promoCodes));
                codeF.clear(); valF.clear(); usesF.clear();
            } catch (NumberFormatException ex) {
                ctrl.showAlert("Invalid Input", "Value and Max Uses must be positive numbers.", Alert.AlertType.WARNING);
            }
        });

        root.getChildren().addAll(titleL, addRow, table);
        return root;
    }

    // ══════════════════ SETTINGS ══════════════════════════════════

    private Node buildSettings() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24, 28, 24, 28));
        root.setStyle("-fx-background-color:#030510;");

        Label titleL = new Label("Admin Settings");
        titleL.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        titleL.setTextFill(TEXT_BRIGHT);

        // ─ Change password box ─
        VBox pwdBox = new VBox(12);
        pwdBox.setPadding(new Insets(16, 20, 16, 20));
        pwdBox.setStyle("-fx-background-color:#070b1a;-fx-background-radius:16;" +
                "-fx-border-color:#0f1e3c;-fx-border-radius:16;");

        Label pwdTitle  = mkLbl("Change Admin Password", 16, FontWeight.BOLD, TEXT_PRIMARY);
        PasswordField oldPwd  = mkPass("Current Password");
        PasswordField newPwd  = mkPass("New Password");
        PasswordField confPwd = mkPass("Confirm New Password");
        Button changeBtn = mkBtn("Update Password", NEON_PURPLE, Color.rgb(90, 18, 200));
        Label  pwdMsg    = mkLbl("", 12, FontWeight.BOLD, NEON_GREEN);

        changeBtn.setOnAction(e -> {
            if (!oldPwd.getText().trim().equals(AppState.ADMIN_PASS)) {
                pwdMsg.setTextFill(CRIMSON); pwdMsg.setText("❌ Current password is incorrect."); return;
            }
            String np = newPwd.getText().trim(), cp = confPwd.getText().trim();
            if (np.length() < 4) {
                pwdMsg.setTextFill(CRIMSON); pwdMsg.setText("❌ New password must be at least 4 characters."); return;
            }
            if (!np.equals(cp)) {
                pwdMsg.setTextFill(CRIMSON); pwdMsg.setText("❌ Passwords do not match."); return;
            }
            pwdMsg.setTextFill(NEON_GREEN);
            pwdMsg.setText("✅ Password updated successfully!");
            oldPwd.clear(); newPwd.clear(); confPwd.clear();
        });

        pwdBox.getChildren().addAll(pwdTitle, oldPwd, newPwd, confPwd, changeBtn, pwdMsg);

        // ─ Data management box ─
        VBox dataBox = new VBox(12);
        dataBox.setPadding(new Insets(16, 20, 16, 20));
        dataBox.setStyle("-fx-background-color:#070b1a;-fx-background-radius:16;" +
                "-fx-border-color:#0f1e3c;-fx-border-radius:16;");

        Label dataTitle  = mkLbl("Data Management", 16, FontWeight.BOLD, TEXT_PRIMARY);
        Button saveAllBtn = mkBtn("💾  Save All Data Now", NEON_GREEN, NEON_GREEN.darker().darker());
        Label  saveMsg    = mkLbl("", 12, FontWeight.BOLD, NEON_GREEN);
        saveAllBtn.setOnAction(e -> {
            ctrl.saveData();
            saveMsg.setText("✅ All data saved at " + Utils.now());
        });

        Button reloadBtn = mkBtn("🔄  Reload Data from Disk", NEON_BLUE, Color.rgb(12, 55, 200));
        Label  reloadMsg = mkLbl("", 12, FontWeight.BOLD, NEON_BLUE);
        reloadBtn.setOnAction(e -> {
            ctrl.rebuild("ADMIN_DASH", "CUST_FLEET");
            reloadMsg.setText("✅ Data reloaded at " + Utils.now());
        });

        dataBox.getChildren().addAll(dataTitle, saveAllBtn, saveMsg, reloadBtn, reloadMsg);

        root.getChildren().addAll(titleL, pwdBox, dataBox);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#030510;-fx-background-color:#030510;");
        return scroll;
    }
}