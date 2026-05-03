package CarRentalSystem;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Main extends Application {

    // ──────────────────── Color Constants ──────────────────────
    private static final Color BG_DARKEST    = Color.rgb(6,   10,  24);
    private static final Color BG_DARK       = Color.rgb(10,  16,  34);
    private static final Color SURFACE_1     = Color.rgb(16,  24,  52);
    private static final Color SURFACE_2     = Color.rgb(22,  34,  68);
    private static final Color SURFACE_3     = Color.rgb(30,  46,  88);
    private static final Color BORDER_SUBTLE = Color.rgb(40,  60, 110);
    private static final Color BORDER_MED    = Color.rgb(60,  90, 150);

    private static final Color ELECTRIC_BLUE = Color.rgb(56,  182, 255);
    private static final Color ROYAL_PURPLE  = Color.rgb(124,  77, 255);
    private static final Color MINT_GREEN    = Color.rgb(52,  211, 153);
    private static final Color CRIMSON       = Color.rgb(239,  68,  68);
    private static final Color AMBER         = Color.rgb(251, 191,  36);
    private static final Color CORAL         = Color.rgb(249, 115,  22);
    private static final Color TEAL          = Color.rgb(20,  184, 166);

    private static final Color TEXT_PRIMARY   = Color.rgb(241, 245, 249);
    private static final Color TEXT_SECONDARY = Color.rgb(148, 163, 184);
    private static final Color TEXT_TERTIARY  = Color.rgb(71,  85, 105);

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    // ═══════════════ Models (unchanged) ═════════════════════════
    class Car {
        String id, name, plate, category;
        double pricePerDay;
        boolean isAvailable, inCart;
        String icon;
        Car(String id, String name, String plate, double price, boolean avail, String category, String icon) {
            this.id = id; this.name = name; this.plate = plate;
            this.pricePerDay = price; this.isAvailable = avail;
            this.inCart = false; this.category = category; this.icon = icon;
        }
    }

    class CartItem {
        Car car; int days;
        CartItem(Car car, int days) { this.car = car; this.days = days; }
        double subtotal() { return car.pricePerDay * days; }
    }

    class BookingRecord {
        String bookingId, paymentMethod, timestamp;
        String status = "ACTIVE";
        List<CartItem> items;
        double totalCost;
        BookingRecord(List<CartItem> items, double total, String method) {
            this.bookingId = "BKG-" + (int)(Math.random() * 90000 + 10000);
            this.items = new ArrayList<>(items);
            this.totalCost = total;
            this.paymentMethod = method;
            this.timestamp = now();
        }
    }

    class Customer {
        String name, nic, password;
        List<String>        loginHistory = new ArrayList<>();
        List<BookingRecord> bookings     = new ArrayList<>();
        Customer(String name, String nic, String password) {
            this.name = name; this.nic = nic; this.password = password;
        }
        void recordLogin() { loginHistory.add(now()); }
    }

    // ═══════════════ State ══════════════════════════════════════
    private final List<Car>            fleet       = new ArrayList<>();
    private final Map<String,Customer> customers   = new LinkedHashMap<>();
    private final List<BookingRecord>  allBookings = new ArrayList<>();
    private final List<CartItem>       cart        = new ArrayList<>();
    private Customer activeCustomer;
    private static final String CUSTOMERS_FILE = "customers.txt";
    private static final String BOOKINGS_FILE  = "bookings.txt";
    private static final String CARS_FILE      = "cars.txt";
    private Stage          primaryStage;
    private StackPane      root;               // holds all scenes
    private Map<String, Node> sceneMap = new HashMap<>();

    // ═══════════════ CSS Stylesheet (programmatic) ═════════════
    private String getGlobalCSS() {
        return """
            .root {
                -fx-background-color: #060a18;
            }
            .glass-card {
                -fx-background-color: rgba(16,24,52,0.95);
                -fx-background-radius: 20;
                -fx-border-radius: 20;
                -fx-border-width: 1.5;
                -fx-padding: 36 48 36 48;
            }
            .premium-button {
                -fx-background-radius: 14;
                -fx-cursor: hand;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-padding: 0 20 0 20;
            }
            .text-field {
                -fx-background-color: #1e2e58;
                -fx-text-fill: #f1f5f9;
                -fx-prompt-text-fill: #475569;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-border-color: #3c5a96;
                -fx-border-width: 1;
                -fx-padding: 8 12;
            }
            .table-view {
                -fx-background-color: #162234;
                -fx-table-cell-border-color: transparent;
            }
            .table-view .column-header-background {
                -fx-background-color: #1e2e58;
            }
            .table-view .column-header, .table-view .filler {
                -fx-background-color: transparent;
                -fx-border-color: #3c5a96;
                -fx-border-width: 0 0 1 0;
            }
           .table-row-cell {
                       -fx-background-color: #162234;
                       -fx-text-fill: #f1f5f9;
                   }
                   .table-row-cell:odd {
                       -fx-background-color: #101834;
                   }
                   .table-row-cell .text {
                       -fx-fill: #f1f5f9;
                   }
                   .table-cell {
                       -fx-text-fill: #f1f5f9;
                       -fx-font-size: 13px;
                   }
                   .column-header .label {
                       -fx-text-fill: #38b6ff;
                       -fx-font-weight: bold;
                       -fx-font-size: 12px;
                   }
            .table-row-cell:selected {
                -fx-background-color: rgba(56,182,255,0.3);
            }
            .tab-pane .tab-header-background {
                -fx-background-color: #101834;
            }
            .tab-pane .tab {
                -fx-background-color: #162234;
                -fx-text-base-color: #94a3b8;
            }
            .tab-pane .tab:selected {
                -fx-background-color: #1e2e58;
                -fx-text-base-color: #f1f5f9;
            }
            .sidebar {
                -fx-background-color: #080c1c;
            }
            .metric-card {
                -fx-background-color: #162234;
                -fx-background-radius: 12;
                -fx-border-radius: 12;
                -fx-padding: 18 20;
            }
        """;
    }

    // ═══════════════ Custom Nodes (just styling helpers) ═══════
    private Region createGlassCard(Color borderColor) {
        Region card = new Region();
        card.getStyleClass().add("glass-card");
        card.setStyle("-fx-border-color: " + toRGBCode(borderColor) + ";");
        return card;
    }

    private Button createPremiumButton(String text, Color baseColor) {
        Button btn = new Button(text);
        btn.getStyleClass().add("premium-button");
        // Dynamic gradient via inline style for base color
        String lighter = toRGBCode(baseColor.brighter());
        String darker  = toRGBCode(baseColor.darker().darker());
        btn.setStyle("-fx-background-color: linear-gradient(to bottom, " + lighter + ", " + darker + ");" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 4, 0, 2, 2);");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: linear-gradient(to bottom, " + toRGBCode(baseColor.brighter().brighter()) + ", " + darker + "); -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.2), 8, 0, 0, 4);"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: linear-gradient(to bottom, " + lighter + ", " + darker + "); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 4, 0, 2, 2);"));
        btn.setOnMousePressed(e -> btn.setStyle("-fx-background-color: " + darker + "; -fx-translate-y: 2;"));
        btn.setOnMouseReleased(e -> btn.setStyle("-fx-background-color: linear-gradient(to bottom, " + lighter + ", " + darker + "); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 4, 0, 2, 2);"));
        return btn;
    }

    private String toRGBCode(Color c) {
        return String.format("rgba(%d,%d,%d,%f)", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255), c.getOpacity());
    }

    // ═══════════════ App Entry ═════════════════════════════════
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        loadData();
        if (customers.isEmpty()) seedData();
        root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(BG_DARKEST, CornerRadii.EMPTY, Insets.EMPTY)));
        Scene scene = new Scene(root, 1280, 780);
        scene.getStylesheets().add("data:text/css," + getGlobalCSS());  // inject CSS

        // Build all pages upfront
        sceneMap.put("PORTAL",      buildPortal());
        sceneMap.put("CUST_LOGIN",  buildCustLogin());
        sceneMap.put("CUST_REG",    buildCustReg());
        sceneMap.put("ADMIN_LOGIN", buildAdminLogin());
        sceneMap.put("CUST_FLEET",  buildFleet());
        sceneMap.put("CART",        buildCart());
        sceneMap.put("CHECKOUT",    buildCheckout());
        sceneMap.put("RECEIPT",     buildReceipt(null));
        sceneMap.put("PROFILE",     buildProfile());
        sceneMap.put("ADMIN_DASH",  buildAdminDash());

        showScene("PORTAL");

        primaryStage.setTitle("DriveEase — Premium Car Rental Platform");
        primaryStage.setMinWidth(1040);
        primaryStage.setMinHeight(640);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void showScene(String name) {
        root.getChildren().setAll(sceneMap.get(name));
    }

    private void refreshFleet() {
        sceneMap.put("CUST_FLEET", buildFleet());
    }

    // ═══════════════ Seed Data (identical) ═════════════════════
    private void seedData() {
        fleet.add(new Car("C01","Honda Civic",       "XYZ-123",  50, true,  "Sedan",   "🚗"));
        fleet.add(new Car("C02","Toyota Corolla",    "ABC-456",  45, true,  "Sedan",   "🚗"));
        fleet.add(new Car("C03","Ford Mustang",      "MUS-001", 120, true,  "Sports",  "🏎️"));
        fleet.add(new Car("C04","BMW M3",            "BMW-999", 150, true,  "Luxury",  "🏎️"));
        fleet.add(new Car("C05","Audi A4",           "AUD-111",  90, true,  "Luxury",  "🚙"));
        fleet.add(new Car("C06","Tesla Model 3",     "TSL-404",  80, true,  "Electric","⚡"));
        fleet.add(new Car("C07","Hyundai Elantra",   "HYN-777",  40, true,  "Economy", "🚗"));
        fleet.add(new Car("C08","Range Rover",       "RR-555",  200, false, "SUV",     "🛻"));
        fleet.add(new Car("C09","Mercedes C-Class",  "MER-222", 110, false, "Luxury",  "🚙"));
        fleet.add(new Car("C10","Kia Sportage",      "KIA-333",  60, true,  "SUV",     "🛻"));

        Customer demo = new Customer("Faizan Gul", "3830263261151", "123");
        demo.loginHistory.add("2025/06/10 09:15:00");
        demo.loginHistory.add("2025/06/14 14:30:22");
        demo.recordLogin();
        BookingRecord br = new BookingRecord(List.of(new CartItem(fleet.get(8), 5)), 550, "Bank Transfer");
        fleet.get(8).isAvailable = false;
        demo.bookings.add(br);
        allBookings.add(br);
        customers.put(demo.nic, demo);
    }
    // ═══════════════ File I/O ═══════════════════════════════════
    private void saveData() {
        saveCustomers();
        saveBookings();
        saveCars();
    }

    private void saveCustomers() {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(CUSTOMERS_FILE))) {
            for (Customer c : customers.values()) {
                String logins = String.join(",", c.loginHistory);
                pw.println(c.nic + "|" + c.name + "|" + c.password + "|" + logins);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveBookings() {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(BOOKINGS_FILE))) {
            for (BookingRecord br : allBookings) {
                StringBuilder items = new StringBuilder();
                for (CartItem ci : br.items) {
                    items.append(ci.car.id).append(":").append(ci.days).append(";");
                }
                String custNic = findCustomerOfBooking(br);
                pw.println(br.bookingId + "|" + br.timestamp + "|" + br.paymentMethod + "|" +
                        br.status + "|" + br.totalCost + "|" + items + "|" + custNic);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String findCustomerOfBooking(BookingRecord br) {
        for (Customer c : customers.values()) {
            if (c.bookings.contains(br)) return c.nic;
        }
        return "";
    }

    private void saveCars() {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(CARS_FILE))) {
            for (Car car : fleet) {
                pw.println(car.id + "|" + car.isAvailable);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadData() {
        seedData();
        loadCars();
        loadCustomersAndBookings();
    }

    private void loadCars() {
        java.io.File file = new java.io.File(CARS_FILE);
        if (!file.exists()) return;
        try (java.util.Scanner sc = new java.util.Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 2) continue;
                String id = parts[0];
                boolean avail = Boolean.parseBoolean(parts[1]);
                for (Car car : fleet) {
                    if (car.id.equals(id)) { car.isAvailable = avail; break; }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadCustomersAndBookings() {
        java.io.File custFile = new java.io.File(CUSTOMERS_FILE);
        if (custFile.exists()) {
            try (java.util.Scanner sc = new java.util.Scanner(custFile)) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine().trim();
                    if (line.isEmpty()) continue;
                    String[] parts = line.split("\\|", -1);
                    if (parts.length < 3) continue;
                    String nic = parts[0], name = parts[1], pass = parts[2];
                    Customer c = new Customer(name, nic, pass);
                    if (parts.length > 3 && !parts[3].isEmpty()) {
                        String[] logins = parts[3].split(",");
                        for (String l : logins) if (!l.isEmpty()) c.loginHistory.add(l);
                    }
                    customers.put(nic, c);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        java.io.File bkgFile = new java.io.File(BOOKINGS_FILE);
        if (bkgFile.exists()) {
            try (java.util.Scanner sc = new java.util.Scanner(bkgFile)) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine().trim();
                    if (line.isEmpty()) continue;
                    String[] parts = line.split("\\|", -1);
                    if (parts.length < 7) continue;
                    String bookingId = parts[0];
                    String timestamp = parts[1];
                    String method    = parts[2];
                    String status    = parts[3];
                    double total     = Double.parseDouble(parts[4]);
                    String itemsStr  = parts[5];
                    String custNic   = parts[6];

                    List<CartItem> items = new ArrayList<>();
                    if (!itemsStr.isEmpty()) {
                        for (String entry : itemsStr.split(";")) {
                            if (entry.isEmpty()) continue;
                            String[] kv = entry.split(":");
                            if (kv.length < 2) continue;
                            String carId = kv[0];
                            int days = Integer.parseInt(kv[1]);
                            for (Car car : fleet) {
                                if (car.id.equals(carId)) { items.add(new CartItem(car, days)); break; }
                            }
                        }
                    }
                    if (items.isEmpty()) continue;

                    BookingRecord br = new BookingRecord(items, total, method);
                    br.bookingId = bookingId;
                    br.timestamp = timestamp;
                    br.status    = status;
                    allBookings.add(br);

                    Customer c = customers.get(custNic);
                    if (c != null) c.bookings.add(br);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
    // ═══════════════ Portal ════════════════════════════════════
    private Node buildPortal() {
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: transparent; -fx-padding: 50;");

        // Badge
        HBox badge = new HBox();
        badge.setAlignment(Pos.CENTER);
        badge.setStyle("-fx-background-color: rgba(56,182,255,0.2); -fx-background-radius: 20; -fx-border-color: rgba(56,182,255,0.5); -fx-border-radius: 20; -fx-padding: 5 18;");
        Label badgeText = new Label("🏆  ENTERPRISE EDITION  ·  PREMIUM FLEET");
        badgeText.setTextFill(ELECTRIC_BLUE); badgeText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        badge.getChildren().add(badgeText);

        Label title = new Label("DriveEase");
        title.setTextFill(TEXT_PRIMARY); title.setFont(Font.font("Georgia", FontWeight.BOLD, 82));
        Label subtitle = new Label("Drive the extraordinary — Rent premium cars on demand");
        subtitle.setTextFill(TEXT_SECONDARY); subtitle.setFont(Font.font("Segoe UI", 19));

        // Divider
        HBox divider = new HBox();
        divider.setMaxWidth(540); divider.setPrefHeight(4);
        divider.setStyle("-fx-background-color: linear-gradient(to right, transparent, #38b6ff, transparent);");

        // Stats row
        HBox stats = new HBox(48);
        stats.setAlignment(Pos.CENTER);
        stats.getChildren().addAll(
                buildStatBox(String.valueOf(fleet.size()), "Vehicles"),
                buildStatBox(String.valueOf(fleet.stream().filter(c->c.isAvailable).count()), "Available Now"),
                buildStatBox("24/7", "Support"),
                buildStatBox("100%", "Insured")
        );

        // Buttons
        VBox buttons = new VBox(16);
        buttons.setAlignment(Pos.CENTER);
        Button btnNew    = createPremiumButton("🚗   New Customer  —  Register Now",    Color.rgb(25,75,220));
        Button btnReturn = createPremiumButton("👤   Returning Customer  —  Sign In",    Color.rgb(100,30,230));
        Button btnAdmin  = createPremiumButton("🔐   Administrator  —  Secure Login",    Color.rgb(160,20,20));
        btnNew.setOnAction(e -> showScene("CUST_REG"));
        btnReturn.setOnAction(e -> showScene("CUST_LOGIN"));
        btnAdmin.setOnAction(e -> showScene("ADMIN_LOGIN"));
        buttons.getChildren().addAll(btnNew, btnReturn, btnAdmin);

        content.getChildren().addAll(badge, title, subtitle, divider, stats, buttons);
        return content;
    }

    private VBox buildStatBox(String value, String label) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        Label valLbl = new Label(value);
        valLbl.setTextFill(ELECTRIC_BLUE); valLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        Label lbl = new Label(label);
        lbl.setTextFill(TEXT_TERTIARY); lbl.setFont(Font.font("Segoe UI", 12));
        box.getChildren().addAll(valLbl, lbl);
        return box;
    }

    // ═══════════════ Background Wrapper ════════════════════════
    private StackPane bgWrapper() {
        StackPane sp = new StackPane();
        sp.setStyle("-fx-background-color: #060a18;");
        return sp;
    }

    // ═══════════════ Customer Login ════════════════════════════
    private Node buildCustLogin() {
        StackPane outer = bgWrapper();
        VBox card = new VBox(20);
        card.setStyle("-fx-background-color: rgba(16,24,52,0.95); -fx-background-radius: 20; -fx-border-color: " + toRGBCode(ROYAL_PURPLE) + "; -fx-border-radius: 20; -fx-border-width: 1.5; -fx-padding: 36 48;");
        card.setMaxWidth(500); card.setAlignment(Pos.CENTER);

        Label emoji = new Label("👤"); emoji.setFont(Font.font(52)); emoji.setTextFill(Color.WHITE);
        Label title = new Label("Welcome Back"); title.setFont(Font.font(26)); title.setTextFill(TEXT_PRIMARY);
        Label sub = new Label("Sign in to your account"); sub.setFont(Font.font(13)); sub.setTextFill(TEXT_SECONDARY);

        TextField nicF = new TextField(); nicF.setPromptText("NIC Number"); nicF.getStyleClass().add("text-field");
        PasswordField passF = new PasswordField(); passF.setPromptText("Password"); passF.getStyleClass().add("text-field");
        Label errL = new Label(" "); errL.setTextFill(CRIMSON); errL.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Button loginBtn = createPremiumButton("  Sign In to My Account  →", Color.rgb(100,30,230));
        loginBtn.setMaxWidth(340);
        Button backBtn = createPremiumButton("← Back to Portal", Color.rgb(30,42,62));
        backBtn.setMaxWidth(340);

        passF.setOnAction(e -> loginBtn.fire());
        loginBtn.setOnAction(e -> {
            String nic = nicF.getText().trim(), pass = passF.getText().trim();
            Customer c = customers.get(nic);
            if (c != null && c.password.equals(pass)) {
                activeCustomer = c; c.recordLogin();
                saveData();
                errL.setText(" "); nicF.clear(); passF.clear();
                cart.clear(); fleet.forEach(car -> car.inCart = false);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Login Successful");
                alert.setHeaderText("Welcome back, " + c.name + "!");
                alert.setContentText("What would you like to do?");
                ButtonType browseBtn = new ButtonType("Browse Cars 🚗", ButtonBar.ButtonData.YES);
                ButtonType profileBtn = new ButtonType("My Profile 👤", ButtonBar.ButtonData.NO);
                alert.getButtonTypes().setAll(browseBtn, profileBtn);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == browseBtn) {
                    refreshFleet(); showScene("CUST_FLEET");
                } else {
                    sceneMap.put("PROFILE", buildProfile()); showScene("PROFILE");
                }
            } else {
                errL.setText("❌  Invalid NIC or Password. Please try again."); passF.clear();
            }
        });

        backBtn.setOnAction(e -> { errL.setText(" "); showScene("PORTAL"); });

        card.getChildren().addAll(emoji, title, sub, nicF, passF, errL, loginBtn, backBtn);
        outer.getChildren().add(card);
        return outer;
    }

    // ═══════════════ Registration ═════════════════════════════ ═
    private Node buildCustReg() {
        StackPane outer = bgWrapper();
        VBox card = new VBox(20);
        card.setStyle("-fx-background-color: rgba(16,24,52,0.95); -fx-background-radius: 20; -fx-border-color: " + toRGBCode(ELECTRIC_BLUE) + "; -fx-border-radius: 20; -fx-border-width: 1.5; -fx-padding: 36 48;");
        card.setMaxWidth(520); card.setAlignment(Pos.CENTER);

        Label emoji = new Label("🚗"); emoji.setFont(Font.font(52)); emoji.setTextFill(Color.WHITE);
        Label title = new Label("Create Your Account"); title.setFont(Font.font(26)); title.setTextFill(TEXT_PRIMARY);
        Label sub = new Label("Join DriveEase and start renting today"); sub.setFont(Font.font(13)); sub.setTextFill(TEXT_SECONDARY);

        TextField nameF = new TextField(); nameF.setPromptText("Full Name"); nameF.getStyleClass().add("text-field");
        TextField nicF  = new TextField(); nicF.setPromptText("NIC Number"); nicF.getStyleClass().add("text-field");
        PasswordField passF = new PasswordField(); passF.setPromptText("Password"); passF.getStyleClass().add("text-field");
        PasswordField confF = new PasswordField(); confF.setPromptText("Confirm Password"); confF.getStyleClass().add("text-field");
        Label errL = new Label(" "); errL.setTextFill(CRIMSON); errL.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Button regBtn = createPremiumButton("✅  Create Account & Browse Cars  →", Color.rgb(25,75,220));
        regBtn.setMaxWidth(360);
        Button backBtn = createPremiumButton("← Back to Portal", Color.rgb(30,42,62));
        backBtn.setMaxWidth(360);

        confF.setOnAction(e -> regBtn.fire());
        regBtn.setOnAction(e -> {
            String name = nameF.getText().trim(), nic = nicF.getText().trim();
            String pass = passF.getText().trim(), conf = confF.getText().trim();
            if(name.isEmpty() || nic.isEmpty() || pass.isEmpty()) {
                errL.setText("❌  All fields are required."); return;
            }
            if(!pass.equals(conf)) {
                errL.setText("❌  Passwords do not match."); return;
            }
            if(customers.containsKey(nic)) {
                errL.setText("❌  NIC already registered — please sign in."); return;
            }
            Customer nc = new Customer(name, nic, pass);
            nc.recordLogin(); customers.put(nic, nc); activeCustomer = nc;
            cart.clear(); fleet.forEach(car -> car.inCart = false);
            errL.setText(" "); nameF.clear(); nicF.clear(); passF.clear(); confF.clear();
            saveData();  // ← YE ADD KARO YAHAN
            refreshFleet(); showScene("CUST_FLEET");
        });

        backBtn.setOnAction(e -> { errL.setText(" "); showScene("PORTAL"); });

        card.getChildren().addAll(emoji, title, sub, nameF, nicF, passF, confF, errL, regBtn, backBtn);
        outer.getChildren().add(card);
        return outer;
    }

    // ═══════════════ Admin Login ═══════════════════════════════
    private Node buildAdminLogin() {
        StackPane outer = bgWrapper();
        VBox card = new VBox(20);
        card.setStyle("-fx-background-color: rgba(16,24,52,0.95); -fx-background-radius: 20; -fx-border-color: " + toRGBCode(CRIMSON) + "; -fx-border-radius: 20; -fx-border-width: 1.5; -fx-padding: 36 48;");
        card.setMaxWidth(480); card.setAlignment(Pos.CENTER);

        Label emoji = new Label("🔐"); emoji.setFont(Font.font(52)); emoji.setTextFill(Color.WHITE);
        Label title = new Label("Administrator Login"); title.setFont(Font.font(26)); title.setTextFill(TEXT_PRIMARY);
        Label sub = new Label("Restricted area — authorised personnel only"); sub.setFont(Font.font(13)); sub.setTextFill(TEXT_SECONDARY);

        TextField userF = new TextField(); userF.setPromptText("Username"); userF.getStyleClass().add("text-field");
        PasswordField passF = new PasswordField(); passF.setPromptText("Password"); passF.getStyleClass().add("text-field");
        Label errL = new Label(" "); errL.setTextFill(CRIMSON); errL.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Button loginBtn = createPremiumButton("🔓  Access Admin Panel", Color.rgb(160,20,20));
        loginBtn.setMaxWidth(340);
        Button backBtn = createPremiumButton("← Back to Portal", Color.rgb(30,42,62));
        backBtn.setMaxWidth(340);

        passF.setOnAction(e -> loginBtn.fire());
        loginBtn.setOnAction(e -> {
            if(userF.getText().trim().equals(ADMIN_USER) && passF.getText().trim().equals(ADMIN_PASS)) {
                errL.setText(" "); userF.clear(); passF.clear();
                sceneMap.put("ADMIN_DASH", buildAdminDash());
                showScene("ADMIN_DASH");
            } else {
                errL.setText("❌  Invalid credentials. Access denied."); passF.clear();
            }
        });
        backBtn.setOnAction(e -> { errL.setText(" "); showScene("PORTAL"); });

        card.getChildren().addAll(emoji, title, sub, userF, passF, errL, loginBtn, backBtn);
        outer.getChildren().add(card);
        return outer;
    }

    // ═══════════════ Fleet ═════════════════════════════════════
    private Node buildFleet() {
        BorderPane rootPane = new BorderPane();
        rootPane.setStyle("-fx-background-color: #0a1022; -fx-padding: 24 30 24 30;");

        // Top bar
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(topBar, Priority.ALWAYS);

        VBox titleArea = new VBox(2);
        Label pageTitle = new Label("Browse Fleet"); pageTitle.setTextFill(TEXT_PRIMARY); pageTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
        int avail = (int) fleet.stream().filter(c -> c.isAvailable).count();
        Label pageSubtitle = new Label(avail + " vehicles available for rental"); pageSubtitle.setTextFill(TEXT_SECONDARY); pageSubtitle.setFont(Font.font(13));
        titleArea.getChildren().addAll(pageTitle, pageSubtitle);

        HBox topRight = new HBox(10); topRight.setAlignment(Pos.CENTER_RIGHT);
        if (activeCustomer != null) {
            Label userBadge = new Label("👤  " + activeCustomer.name);
            userBadge.setTextFill(TEXT_SECONDARY); userBadge.setFont(Font.font(13));
            topRight.getChildren().add(userBadge);
        }
        Button profileBtn = createPremiumButton("My Profile", Color.rgb(100,30,230));
        profileBtn.setOnAction(e -> { sceneMap.put("PROFILE", buildProfile()); showScene("PROFILE"); });
        Button cartBtn = createPremiumButton("🛒  Cart  [" + cart.size() + "]", Color.rgb(160,60,10));
        cartBtn.setOnAction(e -> { sceneMap.put("CART", buildCart()); showScene("CART"); });
        topRight.getChildren().addAll(profileBtn, cartBtn);

        topBar.getChildren().add(titleArea);
        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().add(spacer);
        topBar.getChildren().add(topRight);

        // Fleet grid
        GridPane grid = new GridPane();
        grid.setHgap(18); grid.setVgap(18); grid.setPadding(new Insets(16,0,0,0));
        int col = 0, row = 0;
        boolean any = false;
        for (Car car : fleet) {
            if (!car.isAvailable) continue;
            any = true;
            grid.add(buildCarCard(car), col, row);
            col++;
            if (col == 3) { col = 0; row++; }
        }
        if (!any) {
            Label noc = new Label("No vehicles available at the moment.");
            noc.setTextFill(TEXT_SECONDARY); noc.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 18));
            grid.add(noc, 0, 0);
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0a1022; -fx-background-color: #0a1022; -fx-border-color: transparent;");
        scroll.getStyleClass().add("edge-to-edge");

        // Bottom bar
        HBox south = new HBox();
        south.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(south, Priority.ALWAYS);
        south.setStyle("-fx-background-color: #101834; -fx-border-color: #283c6e; -fx-border-width: 1 0 0 0; -fx-padding: 12 0;");

        Button backBtn = createPremiumButton("← Back to Portal", Color.rgb(30,42,62));
        backBtn.setOnAction(e -> { cart.clear(); fleet.forEach(c -> c.inCart = false); showScene("PORTAL"); });
        HBox southLeft = new HBox(12, backBtn); southLeft.setAlignment(Pos.CENTER_LEFT);

        HBox southRight = new HBox(12); southRight.setAlignment(Pos.CENTER_RIGHT);
        double cartTotal = cart.stream().mapToDouble(CartItem::subtotal).sum();
        if (!cart.isEmpty()) {
            Label cartInfo = new Label(cart.size() + " vehicle" + (cart.size()>1?"s":"") + "  ·  Total: $" + String.format("%.2f", cartTotal));
            cartInfo.setTextFill(AMBER); cartInfo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            southRight.getChildren().add(cartInfo);
        }
        Button procBtn = createPremiumButton("Proceed to Checkout  →", Color.rgb(15,110,50));
        procBtn.setOnAction(e -> {
            if (cart.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Your cart is empty. Please add a vehicle first.");
                alert.show();
                return;
            }
            sceneMap.put("CHECKOUT", buildCheckout()); showScene("CHECKOUT");
        });
        southRight.getChildren().add(procBtn);

        // Spacer to push southRight to right
        HBox spacerSouth = new HBox(); HBox.setHgrow(spacerSouth, Priority.ALWAYS);
        south.getChildren().addAll(southLeft, spacerSouth, southRight);

        rootPane.setTop(topBar);
        rootPane.setCenter(scroll);
        rootPane.setBottom(south);
        return rootPane;
    }

    private Node buildCarCard(Car car) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #162234; -fx-background-radius: 12; -fx-border-color: " + (car.inCart ? toRGBCode(MINT_GREEN) : toRGBCode(BORDER_SUBTLE)) + "; -fx-border-width: " + (car.inCart ? "2" : "1") + "; -fx-border-radius: 12;");
        card.setPadding(new Insets(0));

        // Header
        HBox header = new HBox();
        header.setStyle("-fx-background-color: " + (car.inCart ? "#143c28" : "#1e2e58") + "; -fx-background-radius: 12 12 0 0; -fx-padding: 14 18;");
        header.setAlignment(Pos.CENTER_LEFT);
        VBox nameSec = new VBox(3);
        Label nameLbl = new Label(car.icon + "  " + car.name);
        nameLbl.setTextFill(TEXT_PRIMARY); nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        Label plateLbl = new Label("Plate: " + car.plate);
        plateLbl.setTextFill(TEXT_TERTIARY); plateLbl.setFont(Font.font(12));
        nameSec.getChildren().addAll(nameLbl, plateLbl);
        Label catBadge = new Label(car.category);
        catBadge.setTextFill(ELECTRIC_BLUE); catBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        catBadge.setStyle("-fx-background-color: rgba(56,182,255,0.2); -fx-background-radius: 10; -fx-border-color: rgba(56,182,255,0.6); -fx-border-radius: 10; -fx-padding: 3 8;");
        header.getChildren().add(nameSec);
        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().add(spacer);
        header.getChildren().add(catBadge);

        // Pricing
        HBox pricing = new HBox(); pricing.setAlignment(Pos.CENTER_LEFT);
        pricing.setStyle("-fx-padding: 12 18 10 18;");
        Label price = new Label("$" + (int)car.pricePerDay + " / day");
        price.setTextFill(MINT_GREEN); price.setFont(Font.font(18));
        pricing.getChildren().add(price);

        // Controls
        VBox controls = new VBox(8);
        controls.setStyle("-fx-padding: 0 18 14 18;");
        HBox daysRow = new HBox(8); daysRow.setAlignment(Pos.CENTER_LEFT);
        Label daysLbl = new Label("Rental days:"); daysLbl.setTextFill(TEXT_SECONDARY); daysLbl.setFont(Font.font(13));
        Spinner<Integer> daysSpinner = new Spinner<>(1, 365, 1);
        daysSpinner.setEditable(true);
        daysSpinner.getStyleClass().add("spinner");
        daysRow.getChildren().addAll(daysLbl, daysSpinner);

        Button addBtn = createPremiumButton(car.inCart ? "✓  Added to Cart" : "+  Add to Cart",
                car.inCart ? Color.rgb(15,100,45) : Color.rgb(25,75,220));
        addBtn.setOnAction(e -> {
            if (car.inCart) {
                cart.removeIf(ci -> ci.car == car);
                car.inCart = false;
            } else {
                cart.add(new CartItem(car, daysSpinner.getValue()));
                car.inCart = true;
            }
            refreshFleet(); showScene("CUST_FLEET");
        });
        controls.getChildren().addAll(daysRow, addBtn);

        card.getChildren().addAll(header, pricing, controls);
        return card;
    }

    // ═══════════════ Cart ══════════════════════════════════════
    private Node buildCart() {
        BorderPane rootPane = new BorderPane();
        rootPane.setStyle("-fx-background-color: #0a1022; -fx-padding: 28 36;");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header, Priority.ALWAYS);
        Label title = new Label("🛒  Shopping Cart"); title.setFont(Font.font("Georgia", FontWeight.BOLD, 26)); title.setTextFill(TEXT_PRIMARY);
        Label subTitle = new Label(cart.isEmpty() ? "No items" : cart.size() + " vehicle(s) selected");
        subTitle.setTextFill(TEXT_SECONDARY); subTitle.setFont(Font.font(14));
        HBox subBox = new HBox(); subBox.setAlignment(Pos.CENTER_RIGHT); subBox.getChildren().add(subTitle);
        header.getChildren().addAll(title, new HBox(){{setHgrow(this,Priority.ALWAYS);}}, subBox);
        rootPane.setTop(header);

        if (cart.isEmpty()) {
            rootPane.setCenter(emptyState("Your cart is empty", "Browse the fleet and add vehicles to get started"));
        } else {
            VBox items = new VBox(12);
            double grand = 0;
            for (CartItem ci : cart) {
                grand += ci.subtotal();

                HBox row = new HBox(16);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #162234; -fx-background-radius: 12; -fx-border-color: #283c6e; -fx-border-radius: 12; -fx-padding: 16 20;");
                VBox info = new VBox(4);
                info.setAlignment(Pos.CENTER_LEFT);
                Label carInfo = new Label(ci.car.icon + "  " + ci.car.name + "  ·  " + ci.days + " day" + (ci.days>1?"s":"") + "  ·  $" + (int)ci.car.pricePerDay + "/day");
                carInfo.setTextFill(TEXT_PRIMARY); carInfo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
                Label plateCat = new Label("Plate: " + ci.car.plate + "  ·  " + ci.car.category);
                plateCat.setTextFill(TEXT_TERTIARY); plateCat.setFont(Font.font(12));
                info.getChildren().addAll(carInfo, plateCat);

                HBox rightBox = new HBox(12);
                rightBox.setAlignment(Pos.CENTER_RIGHT);
                Label sub = new Label("$" + String.format("%.2f", ci.subtotal()));
                sub.setTextFill(MINT_GREEN); sub.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
                Button rmBtn = createPremiumButton("Remove", CRIMSON);
                rmBtn.setOnAction(e -> {
                    ci.car.inCart = false;
                    cart.remove(ci);
                    sceneMap.put("CART", buildCart());
                    showScene("CART");
                });
                rightBox.getChildren().addAll(sub, rmBtn);
                row.getChildren().addAll(info, new HBox(){{setHgrow(this,Priority.ALWAYS);}}, rightBox);
                items.getChildren().add(row);
            }

            // Summary box
            HBox summary = new HBox();
            summary.setAlignment(Pos.CENTER_LEFT);
            summary.setStyle("-fx-background-color: #1e2e58; -fx-background-radius: 12; -fx-border-color: #3c5a96; -fx-border-radius: 12; -fx-padding: 18 22;");
            Label totalLabel = new Label("Order Total:  $" + String.format("%.2f", grand));
            totalLabel.setTextFill(AMBER); totalLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
            Label vat = new Label("Includes applicable taxes and fees");
            vat.setTextFill(TEXT_TERTIARY); vat.setFont(Font.font(12));
            VBox totalPanel = new VBox(3, totalLabel, vat);
            summary.getChildren().add(totalPanel);
            items.getChildren().add(summary);

            ScrollPane scroll = new ScrollPane(items);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background: #0a1022; -fx-background-color: #0a1022;");
            rootPane.setCenter(scroll);
        }

        // Bottom bar
        HBox south = new HBox();
        south.setAlignment(Pos.CENTER_LEFT);
        south.setStyle("-fx-background-color: #101834; -fx-border-color: #283c6e; -fx-border-width: 1 0 0 0; -fx-padding: 12 0;");
        Button contBtn = createPremiumButton("← Continue Shopping", Color.rgb(30,42,62));
        contBtn.setOnAction(e -> { refreshFleet(); showScene("CUST_FLEET"); });
        HBox sLeft = new HBox(12, contBtn);
        HBox sRight = new HBox(12);
        Button checkoutBtn = createPremiumButton("Proceed to Checkout  →", Color.rgb(15,110,50));
        checkoutBtn.setOnAction(e -> {
            if (cart.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Your cart is empty.").show();
                return;
            }
            sceneMap.put("CHECKOUT", buildCheckout()); showScene("CHECKOUT");
        });
        sRight.getChildren().add(checkoutBtn);
        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        south.getChildren().addAll(sLeft, spacer, sRight);
        rootPane.setBottom(south);
        return rootPane;
    }

    // ═══════════════ Checkout ══════════════════════════════════
    private Node buildCheckout() {
        StackPane outer = bgWrapper();
        VBox card = new VBox(20);
        card.setStyle("-fx-background-color: rgba(16,24,52,0.95); -fx-background-radius: 20; -fx-border-color: " + toRGBCode(MINT_GREEN) + "; -fx-border-radius: 20; -fx-border-width: 1.5; -fx-padding: 36 48;");
        card.setMaxWidth(560); card.setAlignment(Pos.CENTER);

        Label emoji = new Label("💳"); emoji.setFont(Font.font(52)); emoji.setTextFill(Color.WHITE);
        Label title = new Label("Secure Checkout"); title.setFont(Font.font(26)); title.setTextFill(TEXT_PRIMARY);
        Label sub = new Label("Review your order and choose a payment method"); sub.setFont(Font.font(13)); sub.setTextFill(TEXT_SECONDARY);

        double grand = cart.stream().mapToDouble(CartItem::subtotal).sum();
        VBox orderSummary = new VBox(6);
        orderSummary.setStyle("-fx-background-color: #1e2e58; -fx-padding: 12 14; -fx-background-radius: 8;");
        for (CartItem ci : cart) {
            Label itemLine = new Label(ci.car.icon + " " + ci.car.name + " × " + ci.days + "d  ——  $" + String.format("%.2f", ci.subtotal()));
            itemLine.setTextFill(TEXT_SECONDARY); itemLine.setFont(Font.font(13));
            orderSummary.getChildren().add(itemLine);
        }
        orderSummary.getChildren().add(new Separator());
        Label totalLine = new Label("Total:  $" + String.format("%.2f", grand));
        totalLine.setTextFill(AMBER); totalLine.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        orderSummary.getChildren().add(totalLine);

        ComboBox<String> payMethod = new ComboBox<>(FXCollections.observableArrayList("Bank Transfer","Cash on Pickup","Credit Card","Debit Card"));
        payMethod.setValue("Bank Transfer");
        payMethod.setStyle("-fx-background-color: #1e2e58; -fx-text-fill: #f1f5f9; -fx-font-size: 13px;");

        HBox payRow = new HBox(8); payRow.setAlignment(Pos.CENTER_LEFT);
        Label payLbl = new Label("Payment Method:"); payLbl.setTextFill(TEXT_SECONDARY); payLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        payRow.getChildren().addAll(payLbl, payMethod);

        Button okBtn = createPremiumButton("✅  Confirm & Pay  $" + String.format("%.2f", grand), Color.rgb(15,110,50));
        okBtn.setMaxWidth(380);
        Button cancelBtn = createPremiumButton("← Modify Cart", Color.rgb(30,42,62));
        cancelBtn.setMaxWidth(380);

        okBtn.setOnAction(e -> {
            String method = payMethod.getValue();
            for (CartItem ci : cart) { ci.car.isAvailable = false; ci.car.inCart = false; }
            BookingRecord br = new BookingRecord(new ArrayList<>(cart), grand, method);
            allBookings.add(br);
            if (activeCustomer != null) activeCustomer.bookings.add(br);
            sceneMap.put("RECEIPT", buildReceipt(br));
            saveData();
            showScene("RECEIPT");
            cart.clear();
        });

        cancelBtn.setOnAction(e -> { sceneMap.put("CART", buildCart()); showScene("CART"); });

        card.getChildren().addAll(emoji, title, sub, orderSummary, payRow, okBtn, cancelBtn);
        outer.getChildren().add(card);
        return outer;
    }

    // ═══════════════ Receipt with slide-up animation ════════════
    private Node buildReceipt(BookingRecord b) {
        StackPane page = new StackPane();
        page.setStyle("-fx-background-color: #060a18;");

        if (b == null) {
            Label none = new Label("No booking data available.");
            none.setTextFill(TEXT_SECONDARY); page.getChildren().add(none);
            return page;
        }

        VBox receiptCard = new VBox();
        receiptCard.setMaxWidth(580);
        receiptCard.setStyle("-fx-background-color: #121c34; -fx-background-radius: 18; -fx-border-color: rgba(52,211,153,0.9); -fx-border-radius: 18; -fx-border-width: 1.5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 8);");
        receiptCard.setPadding(new Insets(0));

        // Header
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: linear-gradient(to right, #0f5037, #0a3228); -fx-background-radius: 18 18 0 0; -fx-padding: 28 32 36 32;");
        Label checkIcon = new Label("✅"); checkIcon.setFont(Font.font(32)); checkIcon.setTextFill(Color.WHITE);
        Label confirmed = new Label("Booking Confirmed!"); confirmed.setFont(Font.font(24)); confirmed.setTextFill(Color.rgb(167,243,208));
        Label bookId = new Label("Booking ID: " + b.bookingId); bookId.setFont(Font.font(13)); bookId.setTextFill(Color.rgb(110,231,183));
        Label timestamp = new Label(b.timestamp); timestamp.setFont(Font.font(12)); timestamp.setTextFill(Color.rgb(52,211,153,0.8));
        header.getChildren().addAll(checkIcon, confirmed, bookId, timestamp);
        receiptCard.getChildren().add(header);

        // Body
        VBox body = new VBox(8);
        body.setPadding(new Insets(24,32,24,32));
        if (activeCustomer != null) {
            body.getChildren().add(receiptInfoRow("Customer", activeCustomer.name, TEXT_PRIMARY));
            body.getChildren().add(receiptInfoRow("NIC Number", activeCustomer.nic, TEXT_SECONDARY));
        }
        body.getChildren().add(receiptInfoRow("Payment Method", b.paymentMethod, TEXT_PRIMARY));
        body.getChildren().add(new Separator());

        // Column headers
        HBox colHeader = new HBox();
        colHeader.setAlignment(Pos.CENTER_LEFT);
        colHeader.setStyle("-fx-border-color: rgba(52,211,153,0.4); -fx-border-width: 1 0 1 0; -fx-padding: 7 0;");
        Label vehLbl = new Label("Vehicle"); vehLbl.setTextFill(TEXT_TERTIARY); vehLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        HBox rightCols = new HBox(); rightCols.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightCols, Priority.ALWAYS);
        Label daysLbl = new Label("Days"); daysLbl.setTextFill(TEXT_TERTIARY); daysLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        Label subLbl = new Label("Subtotal"); subLbl.setTextFill(TEXT_TERTIARY); subLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        HBox daysSubBox = new HBox(20, daysLbl, subLbl);
        colHeader.getChildren().add(vehLbl);
        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        colHeader.getChildren().add(spacer);
        colHeader.getChildren().add(daysSubBox);
        body.getChildren().add(colHeader);

        for (CartItem ci : b.items) {
            HBox itemRow = new HBox();
            itemRow.setAlignment(Pos.CENTER_LEFT);
            itemRow.setStyle("-fx-padding: 8 0;");
            HBox nameBox = new HBox(4);
            nameBox.getChildren().add(new Label(ci.car.icon) {{ setTextFill(Color.WHITE); }});
            nameBox.getChildren().add(new Label(ci.car.name) {{ setTextFill(TEXT_PRIMARY); setFont(Font.font("Segoe UI", FontWeight.BOLD, 13)); }});
            HBox right = new HBox(20);
            right.setAlignment(Pos.CENTER_RIGHT);
            Label days = new Label(ci.days + "d"); days.setTextFill(TEXT_SECONDARY); days.setFont(Font.font(13));
            Label sub = new Label("$" + String.format("%.2f", ci.subtotal())); sub.setTextFill(MINT_GREEN); sub.setFont(Font.font(13));
            right.getChildren().addAll(days, sub);
            itemRow.getChildren().add(nameBox);
            HBox spacerItem = new HBox(); HBox.setHgrow(spacerItem, Priority.ALWAYS);
            itemRow.getChildren().add(spacerItem);
            itemRow.getChildren().add(right);
            body.getChildren().add(itemRow);
            body.getChildren().add(new Separator());
        }

        // Grand total
        HBox totalBar = new HBox();
        totalBar.setAlignment(Pos.CENTER_LEFT);
        totalBar.setStyle("-fx-background-color: linear-gradient(to right, #3c2800, #281e00); -fx-background-radius: 10; -fx-border-color: rgba(251,191,36,0.6); -fx-border-radius: 10; -fx-padding: 14 18;");
        Label grandLabel = new Label("GRAND TOTAL"); grandLabel.setTextFill(Color.rgb(251,191,36,0.8)); grandLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        Label totalAmt = new Label("$" + String.format("%.2f", b.totalCost)); totalAmt.setTextFill(AMBER); totalAmt.setFont(Font.font(26));
        totalBar.getChildren().add(grandLabel);
        HBox totalSpacer = new HBox(); HBox.setHgrow(totalSpacer, Priority.ALWAYS);
        totalBar.getChildren().add(totalSpacer);
        totalBar.getChildren().add(totalAmt);
        body.getChildren().add(totalBar);

        body.getChildren().add(new Label("Thank you for choosing DriveEase!") {{ setTextFill(TEXT_SECONDARY); setFont(Font.font(15)); setAlignment(Pos.CENTER); setMaxWidth(Double.MAX_VALUE); }});
        body.getChildren().add(new Label("Safe travels and enjoy the ride  🚗") {{ setTextFill(TEXT_TERTIARY); setFont(Font.font(12)); setAlignment(Pos.CENTER); setMaxWidth(Double.MAX_VALUE); }});

        ScrollPane scrollBody = new ScrollPane(body);
        scrollBody.setFitToWidth(true);
        scrollBody.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        receiptCard.getChildren().add(scrollBody);

        // Slide-up animation
        receiptCard.setTranslateY(300);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(receiptCard.translateYProperty(), 300)),
                new KeyFrame(Duration.millis(600), new KeyValue(receiptCard.translateYProperty(), 0, Interpolator.EASE_OUT))
        );
        timeline.play();

        // Bottom buttons
        HBox bottomBtns = new HBox(18);
        bottomBtns.setAlignment(Pos.CENTER);
        bottomBtns.setPadding(new Insets(20));
        Button profileBtn = createPremiumButton("View My Profile  👤", Color.rgb(100,30,230));
        profileBtn.setOnAction(e -> { sceneMap.put("PROFILE", buildProfile()); showScene("PROFILE"); });
        Button portalBtn = createPremiumButton("Return to Portal  🏠", Color.rgb(25,75,220));
        portalBtn.setOnAction(e -> showScene("PORTAL"));
        bottomBtns.getChildren().addAll(profileBtn, portalBtn);

        VBox wrapper = new VBox(10);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.getChildren().addAll(receiptCard, bottomBtns);
        page.getChildren().add(wrapper);
        return page;
    }

    private HBox receiptInfoRow(String label, String value, Color valueColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 5 0;");
        Label lbl = new Label(label); lbl.setTextFill(TEXT_TERTIARY); lbl.setFont(Font.font(13));
        Label val = new Label(value); val.setTextFill(valueColor); val.setFont(Font.font(13));
        row.getChildren().add(lbl);
        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().add(spacer);
        row.getChildren().add(val);
        return row;
    }

    // ═══════════════ Profile ═══════════════════════════════════
    private Node buildProfile() {
        BorderPane rootPane = new BorderPane();
        rootPane.setStyle("-fx-background-color: #0a1022; -fx-padding: 24 30;");

        // Header
        HBox hdr = new HBox();
        hdr.setAlignment(Pos.CENTER_LEFT);
        VBox titleSec = new VBox(3);
        Label title = new Label("My Account"); title.setFont(Font.font("Georgia", FontWeight.BOLD, 26)); title.setTextFill(TEXT_PRIMARY);
        String subText = activeCustomer != null ? "Welcome back, " + activeCustomer.name + " · " + activeCustomer.bookings.size() + " booking(s)" : "Not logged in";
        Label sub = new Label(subText); sub.setTextFill(TEXT_SECONDARY); sub.setFont(Font.font(13));
        titleSec.getChildren().addAll(title, sub);

        HBox hBtns = new HBox(10);
        hBtns.setAlignment(Pos.CENTER_RIGHT);
        Button browseBtn = createPremiumButton("🚗  Browse Cars", Color.rgb(25,75,220));
        browseBtn.setOnAction(e -> { refreshFleet(); showScene("CUST_FLEET"); });
        Button signOutBtn = createPremiumButton("Sign Out  ↩", CRIMSON);
        signOutBtn.setOnAction(e -> { activeCustomer = null; cart.clear(); fleet.forEach(c -> c.inCart = false); showScene("PORTAL"); });
        hBtns.getChildren().addAll(browseBtn, signOutBtn);
        hdr.getChildren().add(titleSec);
        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        hdr.getChildren().add(spacer);
        hdr.getChildren().add(hBtns);

        // Tabs
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getStyleClass().add("tab-pane");

        // Account Info Tab
        Tab infoTab = new Tab("  Account Info  ");
        infoTab.setClosable(false);
        VBox infoContent = new VBox();
        infoContent.setAlignment(Pos.TOP_CENTER);
        if (activeCustomer != null) {
            GridPane grid = new GridPane();
            grid.setHgap(0); grid.setVgap(0);
            grid.setStyle("-fx-background-color: #162234; -fx-background-radius: 12; -fx-border-color: " + toRGBCode(ROYAL_PURPLE) + "; -fx-border-radius: 12; -fx-padding: 28 36;");
            addProfileRow(grid, 0, "Full Name", activeCustomer.name);
            addProfileRow(grid, 1, "NIC Number", activeCustomer.nic);
            addProfileRow(grid, 2, "Total Bookings", String.valueOf(activeCustomer.bookings.size()));
            addProfileRow(grid, 3, "First Login", activeCustomer.loginHistory.isEmpty() ? "—" : activeCustomer.loginHistory.get(0));
            addProfileRow(grid, 4, "Last Login", activeCustomer.loginHistory.size()<2 ? "—" : activeCustomer.loginHistory.get(activeCustomer.loginHistory.size()-1));
            addProfileRow(grid, 5, "Total Logins", String.valueOf(activeCustomer.loginHistory.size()));
            infoContent.getChildren().add(grid);
        }
        infoTab.setContent(infoContent);

        // Login History Tab
        Tab histTab = new Tab("  Login History  ");
        histTab.setClosable(false);
        VBox histContent = new VBox(12);
        histContent.setPadding(new Insets(16));
        if (activeCustomer != null && !activeCustomer.loginHistory.isEmpty()) {
            TableView<LoginEntry> table = new TableView<>();
            TableColumn<LoginEntry, String> numCol = new TableColumn<>("#"); numCol.setCellValueFactory(new PropertyValueFactory<>("number"));
            TableColumn<LoginEntry, String> dateCol = new TableColumn<>("Session Date & Time"); dateCol.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
            TableColumn<LoginEntry, String> statusCol = new TableColumn<>("Status"); statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
            table.getColumns().addAll(numCol, dateCol, statusCol);
            List<LoginEntry> entries = new ArrayList<>();
            List<String> hist = activeCustomer.loginHistory;
            for (int i = hist.size()-1; i >= 0; i--) {
                entries.add(new LoginEntry(String.valueOf(hist.size()-i), hist.get(i), i == 0 ? "Current Session" : "Completed"));
            }
            table.setItems(FXCollections.observableArrayList(entries));
            histContent.getChildren().add(table);
        } else {
            histContent.getChildren().add(emptyState("No login history found.", ""));
        }
        histTab.setContent(histContent);

        // Bookings Tab
        Tab bookTab = new Tab("  Bookings & Returns  ");
        bookTab.setClosable(false);
        VBox bookContent = new VBox(10);
        bookContent.setPadding(new Insets(16));
        if (activeCustomer != null && !activeCustomer.bookings.isEmpty()) {
            VBox list = new VBox(14);
            for (BookingRecord br : activeCustomer.bookings) {
                boolean returned = br.status.equals("RETURNED");
                HBox bc = new HBox(14);
                bc.setAlignment(Pos.CENTER_LEFT);
                bc.setStyle("-fx-background-color: #162234; -fx-background-radius: 12; -fx-border-color: " + (returned ? toRGBCode(MINT_GREEN) : toRGBCode(ELECTRIC_BLUE)) + "; -fx-border-radius: 12; -fx-padding: 16 20;");
                VBox info = new VBox(5);
                info.getChildren().add(new Label("📋  " + br.bookingId + "   ·   " + br.timestamp) {{ setTextFill(TEXT_PRIMARY); setFont(Font.font("Segoe UI", FontWeight.BOLD, 14)); }});
                StringBuilder carsStr = new StringBuilder();
                for (CartItem ci : br.items) carsStr.append(ci.car.icon).append(" ").append(ci.car.name).append(" (").append(ci.days).append("d), ");
                info.getChildren().add(new Label(carsStr.toString().replaceAll(",\\s*$", "")) {{ setTextFill(TEXT_SECONDARY); setFont(Font.font(13)); }});
                info.getChildren().add(new Label("💰  Total: $" + String.format("%.2f", br.totalCost) + "   ·   Payment: " + br.paymentMethod) {{ setTextFill(AMBER); setFont(Font.font(13)); }});

                HBox right = new HBox(8);
                right.setAlignment(Pos.CENTER_RIGHT);
                Label statusLbl = new Label("  " + (returned ? "✅  Returned" : "🔵  Active Rental"));
                statusLbl.setStyle("-fx-background-color: " + (returned ? "rgba(52,211,153,0.15)" : "rgba(56,182,255,0.15)") + "; -fx-background-radius: 8; -fx-border-color: " + (returned ? "rgba(52,211,153,0.6)" : "rgba(56,182,255,0.6)") + "; -fx-border-radius: 8; -fx-padding: 3 10;");
                statusLbl.setTextFill(returned ? MINT_GREEN : ELECTRIC_BLUE); statusLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                if (!returned) {
                    Button retBtn = createPremiumButton("↩  Return Vehicle", CORAL);
                    retBtn.setOnAction(e -> {
                        if (new Alert(Alert.AlertType.CONFIRMATION, "Confirm return of all vehicles in Booking " + br.bookingId + "?").showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                            br.status = "RETURNED";
                            for (CartItem ci : br.items) ci.car.isAvailable = true;
                            new Alert(Alert.AlertType.INFORMATION, "✅  Vehicle(s) returned successfully!").show();
                            saveData();
                            sceneMap.put("PROFILE", buildProfile()); showScene("PROFILE");
                        }
                    });
                    right.getChildren().add(retBtn);
                }
                right.getChildren().add(statusLbl);
                bc.getChildren().add(info);
                HBox spacerBox = new HBox(); HBox.setHgrow(spacerBox, Priority.ALWAYS);
                bc.getChildren().add(spacerBox);
                bc.getChildren().add(right);
                list.getChildren().add(bc);
            }
            ScrollPane sc = new ScrollPane(list); sc.setFitToWidth(true);
            bookContent.getChildren().add(sc);
        } else {
            bookContent.getChildren().add(emptyState("No bookings yet.", ""));
        }
        bookTab.setContent(bookContent);

        tabs.getTabs().addAll(infoTab, histTab, bookTab);

        rootPane.setTop(hdr);
        rootPane.setCenter(tabs);
        return rootPane;
    }

    private void addProfileRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label); lbl.setTextFill(TEXT_SECONDARY); lbl.setFont(Font.font(13));
        lbl.setStyle("-fx-border-color: #283c6e; -fx-border-width: 0 0 1 0; -fx-padding: 10 0;");
        Label val = new Label(value); val.setTextFill(TEXT_PRIMARY); val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        val.setStyle("-fx-border-color: #283c6e; -fx-border-width: 0 0 1 0; -fx-padding: 10 0;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    public static class LoginEntry {
        private final String number, dateTime, status;
        public LoginEntry(String number, String dateTime, String status) {
            this.number = number; this.dateTime = dateTime; this.status = status;
        }
        public String getNumber() { return number; }
        public String getDateTime() { return dateTime; }
        public String getStatus() { return status; }
    }

    // ═══════════════ Admin Dashboard ═══════════════════════════
    private Node buildAdminDash() {
        BorderPane rootPane = new BorderPane();
        rootPane.setStyle("-fx-background-color: #060a18;");

        // Sidebar
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(230);
        sidebar.setStyle("-fx-background-color: #080c1c; -fx-border-color: #283c6e; -fx-border-width: 0 1 0 0;");
        VBox brand = new VBox(3);
        brand.setPadding(new Insets(24,18,24,18));
        brand.getChildren().add(new Label("DriveEase") {{ setTextFill(TEXT_PRIMARY); setFont(Font.font("Georgia", FontWeight.BOLD, 20)); }});
        brand.getChildren().add(new Label("Administrator Panel") {{ setTextFill(TEXT_TERTIARY); setFont(Font.font(11)); }});
        sidebar.getChildren().add(brand);
        sidebar.getChildren().add(new Separator());

        // Navigation
        StackPane contentArea = new StackPane();
        VBox nav = new VBox(12);
        nav.setPadding(new Insets(12,10,12,10));
        Button ovBtn = createNavButton("📊  Overview", SURFACE_3, () -> contentArea.getChildren().setAll(buildOverview()));
        Button carBtn = createNavButton("🚗  Fleet Mgmt", SURFACE_3, () -> contentArea.getChildren().setAll(buildFleetMgmt()));
        Button bkgBtn = createNavButton("📋  All Bookings", SURFACE_3, () -> contentArea.getChildren().setAll(buildAllBookings()));
        Button custBtn = createNavButton("👥  Customers", SURFACE_3, () -> contentArea.getChildren().setAll(buildCustomers()));
        nav.getChildren().addAll(ovBtn, carBtn, bkgBtn, custBtn);

        // Logout
        VBox logoutBox = new VBox(8);
        logoutBox.setPadding(new Insets(8,10,16,10));
        Button logoutBtn = createPremiumButton("🚪  Sign Out", Color.rgb(100,20,20));
        logoutBtn.setOnAction(e -> showScene("PORTAL"));
        logoutBox.getChildren().add(logoutBtn);

        VBox spacer = new VBox(); VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(nav);
        sidebar.getChildren().add(spacer);
        sidebar.getChildren().add(new Separator());
        sidebar.getChildren().add(logoutBox);

        contentArea.getChildren().add(buildOverview()); // default view

        rootPane.setLeft(sidebar);
        rootPane.setCenter(contentArea);
        return rootPane;
    }

    private Button createNavButton(String text, Color bg, Runnable action) {
        Button btn = new Button(text);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: linear-gradient(to right, #1e2e58, #162040);" +
                "-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-background-radius: 10; -fx-padding: 12 16;" +
                "-fx-border-color: transparent; -fx-border-radius: 10;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: linear-gradient(to right, #2a3e70, #1e2e58);" +
                "-fx-text-fill: #f1f5f9; -fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-background-radius: 10; -fx-padding: 12 16;" +
                "-fx-border-color: rgba(56,182,255,0.4); -fx-border-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(56,182,255,0.2), 8, 0, 0, 2);"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: linear-gradient(to right, #1e2e58, #162040);" +
                "-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-background-radius: 10; -fx-padding: 12 16;" +
                "-fx-border-color: transparent; -fx-border-radius: 10;"));
        btn.setOnAction(e -> action.run());
        return btn;
    }
    private Node buildOverview() {
        VBox root = new VBox(28);
        root.setPadding(new Insets(36, 40, 36, 40));
        root.setStyle("-fx-background-color: #060a18;");

        VBox header = new VBox(6);
        Label titleLbl = new Label("Dashboard Overview");
        titleLbl.setTextFill(TEXT_PRIMARY);
        titleLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        Label subLbl = new Label("Real-time metrics for your fleet and customers");
        subLbl.setTextFill(TEXT_SECONDARY);
        subLbl.setFont(Font.font("Segoe UI", 14));
        Region underline = new Region();
        underline.setPrefHeight(3); underline.setMaxWidth(320);
        underline.setStyle("-fx-background-color: linear-gradient(to right, #38b6ff, #7c4dff, transparent); -fx-background-radius: 2;");
        header.getChildren().addAll(titleLbl, subLbl, underline);

        int tot = fleet.size();
        int avl = (int) fleet.stream().filter(c -> c.isAvailable).count();
        int rent = tot - avl;
        double revenue = allBookings.stream().mapToDouble(b -> b.totalCost).sum();
        long active = allBookings.stream().filter(b -> b.status.equals("ACTIVE")).count();
        long returned = allBookings.stream().filter(b -> b.status.equals("RETURNED")).count();

        GridPane metrics = new GridPane();
        metrics.setHgap(18); metrics.setVgap(18);
        metrics.add(metricCard("Total Fleet",    String.valueOf(tot),              ELECTRIC_BLUE, "vehicles registered"), 0, 0);
        metrics.add(metricCard("Available",      String.valueOf(avl),              MINT_GREEN,    "ready to rent"),       1, 0);
        metrics.add(metricCard("Rented Out",     String.valueOf(rent),             CORAL,         "currently rented"),    2, 0);
        metrics.add(metricCard("Total Bookings", String.valueOf(allBookings.size()),AMBER,         "all time"),            3, 0);
        metrics.add(metricCard("Customers",      String.valueOf(customers.size()), ROYAL_PURPLE,  "registered"),          0, 1);
        metrics.add(metricCard("Active Rentals", String.valueOf(active),           TEAL,          "in progress"),         1, 1);
        metrics.add(metricCard("Returns",        String.valueOf(returned),         MINT_GREEN,    "completed"),           2, 1);
        metrics.add(metricCard("Revenue",        "$" + String.format("%.0f", revenue), Color.rgb(250,204,21), "total earned"), 3, 1);

        VBox chartBox = new VBox(12);
        chartBox.setStyle("-fx-background-color: #0f1830; -fx-background-radius: 16; -fx-border-color: #283c6e; -fx-border-radius: 16; -fx-padding: 22 26;");
        Label chartTitle = new Label("Fleet Status Breakdown");
        chartTitle.setTextFill(TEXT_PRIMARY); chartTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        chartBox.getChildren().add(chartTitle);
        chartBox.getChildren().add(buildBar("Available",      avl,          tot,                          MINT_GREEN));
        chartBox.getChildren().add(buildBar("Rented Out",     rent,         tot,                          CORAL));
        chartBox.getChildren().add(buildBar("Active Rentals", (int)active,  Math.max(1,allBookings.size()), TEAL));
        chartBox.getChildren().add(buildBar("Returns",        (int)returned,Math.max(1,allBookings.size()), AMBER));

        VBox recentBox = new VBox(10);
        recentBox.setStyle("-fx-background-color: #0f1830; -fx-background-radius: 16; -fx-border-color: #283c6e; -fx-border-radius: 16; -fx-padding: 22 26;");
        Label recentTitle = new Label("Recent Bookings");
        recentTitle.setTextFill(TEXT_PRIMARY); recentTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        recentBox.getChildren().add(recentTitle);
        List<BookingRecord> recent = allBookings.stream()
                .sorted((a, b) -> b.timestamp.compareTo(a.timestamp))
                .limit(3).collect(Collectors.toList());
        if (recent.isEmpty()) {
            Label noData = new Label("No bookings yet"); noData.setTextFill(TEXT_TERTIARY);
            recentBox.getChildren().add(noData);
        } else {
            for (BookingRecord br : recent) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #162234; -fx-background-radius: 10; -fx-padding: 10 14;");
                Label idLbl = new Label("📋  " + br.bookingId); idLbl.setTextFill(ELECTRIC_BLUE); idLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                Label timeLbl = new Label(br.timestamp); timeLbl.setTextFill(TEXT_TERTIARY); timeLbl.setFont(Font.font(11));
                HBox sp = new HBox(); HBox.setHgrow(sp, Priority.ALWAYS);
                Label amtLbl = new Label("$" + String.format("%.2f", br.totalCost)); amtLbl.setTextFill(AMBER); amtLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                Label statusLbl = new Label(br.status.equals("ACTIVE") ? "🔵 Active" : "✅ Returned");
                statusLbl.setTextFill(br.status.equals("ACTIVE") ? ELECTRIC_BLUE : MINT_GREEN);
                statusLbl.setFont(Font.font(11));
                row.getChildren().addAll(idLbl, timeLbl, sp, amtLbl, statusLbl);
                recentBox.getChildren().add(row);
            }
        }

        HBox bottomRow = new HBox(18, chartBox, recentBox);
        HBox.setHgrow(chartBox, Priority.ALWAYS);
        HBox.setHgrow(recentBox, Priority.ALWAYS);

        root.getChildren().addAll(header, metrics, bottomRow);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #060a18; -fx-background-color: #060a18;");
        return scroll;
    }

    private VBox metricCard(String title, String value, Color color, String subtext) {
        VBox box = new VBox(8);
        box.setMinWidth(180);
        box.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a2744, #0f1a36);" +
                "-fx-background-radius: 16; -fx-border-radius: 16;" +
                "-fx-border-color: " + toRGBCode(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.7)) + ";" +
                "-fx-border-width: 1.5; -fx-padding: 22 24;" +
                "-fx-effect: dropshadow(gaussian, " + toRGBCode(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.3)) + ", 12, 0, 0, 4);");

        // Top accent line
        Region accent = new Region();
        accent.setPrefHeight(3);
        accent.setStyle("-fx-background-color: linear-gradient(to right, " + toRGBCode(color) + ", transparent); -fx-background-radius: 2;");

        Label valueLbl = new Label(value);
        valueLbl.setTextFill(color);
        valueLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        valueLbl.setEffect(new DropShadow(8, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.6)));

        Label titleLbl = new Label(title);
        titleLbl.setTextFill(TEXT_PRIMARY);
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        Region spacer = new Region(); spacer.setPrefHeight(4);

        Label subLbl = new Label("▲  " + subtext);
        subLbl.setTextFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.7));
        subLbl.setFont(Font.font("Segoe UI", 11));

        box.getChildren().addAll(accent, valueLbl, titleLbl, spacer, subLbl);

        // Hover effect
        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: linear-gradient(to bottom right, #223060, #162040);" +
                "-fx-background-radius: 16; -fx-border-radius: 16;" +
                "-fx-border-color: " + toRGBCode(color) + ";" +
                "-fx-border-width: 2; -fx-padding: 22 24;" +
                "-fx-effect: dropshadow(gaussian, " + toRGBCode(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5)) + ", 20, 0, 0, 6);"));
        box.setOnMouseExited(e -> box.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a2744, #0f1a36);" +
                "-fx-background-radius: 16; -fx-border-radius: 16;" +
                "-fx-border-color: " + toRGBCode(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.7)) + ";" +
                "-fx-border-width: 1.5; -fx-padding: 22 24;" +
                "-fx-effect: dropshadow(gaussian, " + toRGBCode(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.3)) + ", 12, 0, 0, 4);"));
        return box;
    }
    private VBox buildBar(String label, int value, int max, Color color) {
        VBox barBox = new VBox(4);
        HBox labelRow = new HBox();
        labelRow.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label); lbl.setTextFill(TEXT_SECONDARY); lbl.setFont(Font.font(12));
        HBox sp = new HBox(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label valLbl = new Label(value + " / " + max); valLbl.setTextFill(color); valLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        labelRow.getChildren().addAll(lbl, sp, valLbl);
        StackPane trackPane = new StackPane();
        trackPane.setAlignment(Pos.CENTER_LEFT);
        Region track = new Region(); track.setPrefHeight(8);
        track.setStyle("-fx-background-color: #1e2e58; -fx-background-radius: 4;");
        double pct = max == 0 ? 0 : (double) value / max;
        Region fill = new Region(); fill.setPrefHeight(8);
        fill.setStyle("-fx-background-color: linear-gradient(to right, " + toRGBCode(color) + ", " + toRGBCode(color.brighter()) + "); -fx-background-radius: 4;");
        fill.prefWidthProperty().bind(track.widthProperty().multiply(pct));
        trackPane.getChildren().addAll(track, fill);
        barBox.getChildren().addAll(labelRow, trackPane);
        return barBox;
    }
    private Node buildFleetMgmt() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(24,28,24,28));

        HBox carHdr = new HBox();
        carHdr.setAlignment(Pos.CENTER_LEFT);
        carHdr.getChildren().add(new Label("Fleet Management") {{ setTextFill(TEXT_PRIMARY); setFont(Font.font(20)); }});
        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        carHdr.getChildren().add(spacer);
        Button toggleBtn = createPremiumButton("Toggle Availability", Color.rgb(25,75,220));
        carHdr.getChildren().add(toggleBtn);

        TableView<Car> table = new TableView<>();
        TableColumn<Car, String> idCol = new TableColumn<>("ID"); idCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().id));
        TableColumn<Car, String> nameCol = new TableColumn<>("Vehicle"); nameCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().name));
        TableColumn<Car, String> plateCol = new TableColumn<>("Plate"); plateCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().plate));
        TableColumn<Car, String> catCol = new TableColumn<>("Category"); catCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().category));
        TableColumn<Car, String> priceCol = new TableColumn<>("Price/Day"); priceCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty("$" + (int)cell.getValue().pricePerDay));
        TableColumn<Car, String> statusCol = new TableColumn<>("Status"); statusCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().isAvailable ? "Available ✅" : "Rented ❌"));
        table.getColumns().addAll(idCol, nameCol, plateCol, catCol, priceCol, statusCol);
        table.setItems(FXCollections.observableArrayList(fleet));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        toggleBtn.setOnAction(e -> {
            Car selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                new Alert(Alert.AlertType.WARNING, "Please select a vehicle first.").show();
                return;
            }
            selected.isAvailable = !selected.isAvailable;
            new Alert(Alert.AlertType.INFORMATION, selected.name + " is now " + (selected.isAvailable ? "Available ✅" : "Unavailable ❌")).show();
            sceneMap.put("ADMIN_DASH", buildAdminDash());
            showScene("ADMIN_DASH");
        });

        root.getChildren().addAll(carHdr, table);
        return root;
    }

    private Node buildAllBookings() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(24,28,24,28));
        root.getChildren().add(new Label("All Bookings") {{ setTextFill(TEXT_PRIMARY); setFont(Font.font(20)); }});
        TableView<BookingRecord> table = new TableView<>();
        TableColumn<BookingRecord, String> idCol = new TableColumn<>("Booking ID"); idCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().bookingId));
        TableColumn<BookingRecord, String> dateCol = new TableColumn<>("Date & Time"); dateCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().timestamp));
        TableColumn<BookingRecord, String> payCol = new TableColumn<>("Payment"); payCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().paymentMethod));
        TableColumn<BookingRecord, String> statusCol = new TableColumn<>("Status"); statusCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().status));
        TableColumn<BookingRecord, String> vehCol = new TableColumn<>("Vehicles"); vehCol.setCellValueFactory(cell -> {
            String str = cell.getValue().items.stream().map(ci -> ci.car.name + " (" + ci.days + "d)").collect(Collectors.joining(", "));
            return new javafx.beans.property.SimpleStringProperty(str);
        });
        TableColumn<BookingRecord, String> totalCol = new TableColumn<>("Total"); totalCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty("$" + String.format("%.2f", cell.getValue().totalCost)));

        table.getColumns().addAll(idCol, dateCol, vehCol, totalCol, payCol, statusCol);
        table.setItems(FXCollections.observableArrayList(allBookings));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        root.getChildren().add(table);
        return root;
    }

    private Node buildCustomers() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(24,28,24,28));
        root.getChildren().add(new Label("Registered Customers") {{ setTextFill(TEXT_PRIMARY); setFont(Font.font(20)); }});
        TableView<Customer> table = new TableView<>();
        TableColumn<Customer, String> nameCol = new TableColumn<>("Full Name"); nameCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().name));
        TableColumn<Customer, String> nicCol = new TableColumn<>("NIC"); nicCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().nic));
        TableColumn<Customer, String> bokCol = new TableColumn<>("Bookings"); bokCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().bookings.size())));
        TableColumn<Customer, String> lastLogin = new TableColumn<>("Last Login"); lastLogin.setCellValueFactory(cell -> {
            List<String> hist = cell.getValue().loginHistory;
            return new javafx.beans.property.SimpleStringProperty(hist.isEmpty() ? "—" : hist.get(hist.size()-1));
        });
        TableColumn<Customer, String> loginsCol = new TableColumn<>("Total Logins"); loginsCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().loginHistory.size())));
        table.getColumns().addAll(nameCol, nicCol, bokCol, lastLogin, loginsCol);
        table.setItems(FXCollections.observableArrayList(customers.values()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        root.getChildren().add(table);
        return root;
    }

    // ═══════════════ Helper Components ══════════════════════════
    private Node emptyState(String title, String subtitle) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #101834; -fx-background-radius: 12; -fx-border-color: #283c6e; -fx-border-radius: 12; -fx-padding: 30;");
        box.getChildren().add(new Label("📭") {{ setFont(Font.font(40)); setTextFill(TEXT_PRIMARY); }});
        box.getChildren().add(new Label(title) {{ setFont(Font.font(16)); setTextFill(TEXT_PRIMARY); }});
        if (!subtitle.isEmpty())
            box.getChildren().add(new Label(subtitle) {{ setFont(Font.font(13)); setTextFill(TEXT_SECONDARY); }});
        return box;
    }

    private static String now() {
        return DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now());
    }

    public static void main(String[] args) {
        launch(args);
    }
}