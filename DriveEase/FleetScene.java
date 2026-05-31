package DriveEase;


import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static DriveEase.AppColors.*;
import static DriveEase.UIHelper.*;

/**
 * Builds the fleet browser page (with search, filter, sort)
 * and individual car cards.
 */
public class FleetScene {

    private final AppState        state;
    private final SceneController ctrl;

    public FleetScene(AppState state, SceneController ctrl) {
        this.state = state; this.ctrl = ctrl;
    }

    // ── Main fleet page ───────────────────────────────────────────

    public Node build() {
        BorderPane page = new BorderPane();
        page.setStyle("-fx-background-color:#050812;");

        // ─ Nav bar ─
        HBox nav = new HBox(12);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(13, 22, 13, 22));
        nav.setStyle("-fx-background-color:#070b1a;-fx-border-color:#0f1e3c;-fx-border-width:0 0 1 0;");

        Label logoL   = new Label("DriveEase");
        logoL.setFont(Font.font("Georgia", FontWeight.BOLD, 20)); logoL.setTextFill(TEXT_BRIGHT);
        Label logoSub = mkLbl("Fleet", 13, FontWeight.NORMAL, TEXT_DIM);

        TextField searchF = mkField("🔍  Search vehicle, category or plate...");
        searchF.setPrefWidth(270);

        ComboBox<String> catBox = new ComboBox<>(FXCollections.observableArrayList(
                "All", "Sedan", "Sports", "Luxury", "Electric", "SUV", "Economy"));
        catBox.setValue("All"); catBox.setStyle("-fx-pref-width:145px;");

        ComboBox<String> sortBox = new ComboBox<>(FXCollections.observableArrayList(
                "Default", "Price ↑", "Price ↓", "Name A-Z"));
        sortBox.setValue("Default"); sortBox.setStyle("-fx-pref-width:130px;");

        if (state.activeCustomer != null)
            nav.getChildren().add(mkBadge("👤  " + state.activeCustomer.name, NEON_BLUE));

        Button profileBtn = mkBtn("My Profile", NEON_PURPLE, Color.rgb(90, 18, 200));
        profileBtn.setOnAction(e -> { ctrl.rebuild("PROFILE"); ctrl.showScene("PROFILE"); });

        Button cartBtn = mkBtn("🛒  Cart  [" + state.cart.size() + "]",
                NEON_ORANGE, NEON_ORANGE.darker().darker());
        cartBtn.setOnAction(e -> { ctrl.rebuild("CART"); ctrl.showScene("CART"); });

        nav.getChildren().addAll(logoL, logoSub, hSpacer(), searchF, catBox, sortBox,
                hSpacer(), profileBtn, cartBtn);

        // ─ Fleet content ─
        VBox fleetWrap = new VBox(18);
        fleetWrap.setPadding(new Insets(18, 22, 18, 22));
        VBox.setVgrow(fleetWrap, Priority.ALWAYS);

        int avail = (int) state.fleet.stream().filter(c -> c.isAvailable).count();
        HBox pageHdr = new HBox(12); pageHdr.setAlignment(Pos.CENTER_LEFT);
        Label pageTitleL = new Label("Available Fleet");
        pageTitleL.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
        pageTitleL.setTextFill(TEXT_BRIGHT);
        Label pageCountL = mkBadge(avail + " vehicles ready to rent", NEON_GREEN);
        pageHdr.getChildren().addAll(pageTitleL, pageCountL);

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(16);

        Runnable refreshGrid = () -> {
            grid.getChildren().clear();
            String search = searchF.getText().toLowerCase().trim();
            String cat    = catBox.getValue();
            String sort   = sortBox.getValue();

            List<Car> filtered = state.fleet.stream()
                    .filter(c -> c.isAvailable)
                    .filter(c -> search.isEmpty() ||
                            c.name.toLowerCase().contains(search) ||
                            c.category.toLowerCase().contains(search) ||
                            c.plate.toLowerCase().contains(search) ||
                            c.fuel.toLowerCase().contains(search))
                    .filter(c -> cat.equals("All") || c.category.equals(cat))
                    .collect(Collectors.toList());

            switch (sort) {
                case "Price ↑"  -> filtered.sort(Comparator.comparingDouble(c -> c.pricePerDay));
                case "Price ↓"  -> filtered.sort((a, b) -> Double.compare(b.pricePerDay, a.pricePerDay));
                case "Name A-Z" -> filtered.sort(Comparator.comparing(c -> c.name));
            }

            int col = 0, row = 0;
            for (Car car : filtered) {
                grid.add(buildCarCard(car), col, row);
                if (++col == 3) { col = 0; row++; }
            }
            if (filtered.isEmpty()) {
                VBox empty = new VBox(10);
                empty.setAlignment(Pos.CENTER); empty.setPadding(new Insets(60));
                empty.getChildren().add(mkLbl("🔍  No vehicles matched your search",
                        18, FontWeight.BOLD, TEXT_MUTED));
                empty.getChildren().add(mkLbl("Adjust your filters or search term",
                        13, FontWeight.NORMAL, TEXT_DIM));
                grid.add(empty, 0, 0, 3, 1);
            }
        };

        searchF.textProperty().addListener((o, old, nv) -> refreshGrid.run());
        catBox.setOnAction(e -> refreshGrid.run());
        sortBox.setOnAction(e -> refreshGrid.run());
        refreshGrid.run();

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#050812;-fx-background-color:#050812;-fx-border-color:transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        fleetWrap.getChildren().addAll(pageHdr, scroll);

        // ─ Bottom status bar ─
        HBox south = new HBox(14);
        south.setAlignment(Pos.CENTER_LEFT);
        south.setPadding(new Insets(12, 22, 12, 22));
        south.setStyle("-fx-background-color:#070b1a;-fx-border-color:#0f1e3c;-fx-border-width:1 0 0 0;");

        Button backBtn = mkBtnGhost("← Back to Portal");
        backBtn.setOnAction(e -> {
            state.cart.clear(); state.fleet.forEach(c -> c.inCart = false);
            ctrl.showScene("PORTAL");
        });

        double cartTotal = state.cart.stream().mapToDouble(CartItem::subtotal).sum();
        Label cartInfoL = mkLbl(state.cart.isEmpty() ? "" :
                        state.cart.size() + " vehicle" + (state.cart.size() > 1 ? "s" : "") +
                        "  ·  Total: $" + String.format("%.2f", cartTotal),
                14, FontWeight.BOLD, AMBER);

        Button checkBtn = mkBtn("Proceed to Checkout  →", NEON_GREEN, NEON_GREEN.darker().darker());
        checkBtn.setOnAction(e -> {
            if (state.cart.isEmpty()) {
                ctrl.showAlert("Cart Empty", "Please add at least one vehicle.", javafx.scene.control.Alert.AlertType.WARNING);
                return;
            }
            ctrl.rebuild("CHECKOUT"); ctrl.showScene("CHECKOUT");
        });

        south.getChildren().addAll(backBtn, hSpacer(), cartInfoL, checkBtn);

        page.setTop(nav);
        page.setCenter(fleetWrap);
        page.setBottom(south);
        return page;
    }

    // ── Car card ──────────────────────────────────────────────────

    public Node buildCarCard(Car car) {
        boolean inCart = car.inCart;
        Color borderC  = inCart ? NEON_GREEN : BORDER_DIM;
        Color accentC  = catColor(car.category);

        VBox card = new VBox(0);
        String baseStyle = String.format(
                "-fx-background-color:linear-gradient(to bottom,%s,%s);" +
                        "-fx-background-radius:16;-fx-border-radius:16;" +
                        "-fx-border-color:%s;-fx-border-width:%s;" +
                        "-fx-effect:dropshadow(gaussian,%s,%d,0,0,%d);",
                rgba(SURFACE_1, 0.92), rgba(BG_DARK, 0.92),
                rgba(borderC, inCart ? 0.95 : 0.45), inCart ? "2" : "1",
                rgba(borderC, inCart ? 0.35 : 0.08), inCart ? 20 : 6, inCart ? 4 : 1);
        card.setStyle(baseStyle);

        String hoverStyle = String.format(
                "-fx-background-color:linear-gradient(to bottom,%s,%s);" +
                        "-fx-background-radius:16;-fx-border-radius:16;" +
                        "-fx-border-color:%s;-fx-border-width:1.5;" +
                        "-fx-effect:dropshadow(gaussian,%s,18,0,0,5);",
                rgba(SURFACE_2, 0.96), rgba(SURFACE_1, 0.96),
                rgba(NEON_BLUE, 0.55), rgba(NEON_BLUE, 0.22));
        if (!inCart) {
            card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
            card.setOnMouseExited (e -> card.setStyle(baseStyle));
        }

        // Header strip
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 16, 14, 16));
        header.setStyle(String.format(
                "-fx-background-color:linear-gradient(to right,%s,%s);-fx-background-radius:16 16 0 0;",
                rgba(accentC, inCart ? 0.28 : 0.14), rgba(accentC, 0.03)));
        Label nameL    = mkLbl(car.icon + "  " + car.name, 15, FontWeight.BOLD, TEXT_BRIGHT);
        Label catBadge = mkBadge(car.category, accentC);
        header.getChildren().addAll(nameL, hSpacer(), catBadge);

        // Spec chips
        HBox specs = new HBox(8);
        specs.setPadding(new Insets(10, 16, 6, 16)); specs.setAlignment(Pos.CENTER_LEFT);
        specs.getChildren().addAll(
                mkChip("🪑 " + car.seats + " seats"),
                mkChip("⛽ " + car.fuel),
                mkChip("⚙️ " + car.transmission),
                mkChip("📋 " + car.plate));

        // Description
        Label descL = mkLbl(car.description, 12, FontWeight.NORMAL, TEXT_DIM);
        descL.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 12));
        descL.setWrapText(true);
        descL.setPadding(new Insets(2, 16, 10, 16));

        // Price + controls
        VBox bottom = new VBox(10);
        bottom.setPadding(new Insets(12, 16, 16, 16));

        HBox priceRow = new HBox(4); priceRow.setAlignment(Pos.BASELINE_LEFT);
        Label priceL  = mkLbl("$" + (int) car.pricePerDay, 28, FontWeight.BOLD,
                inCart ? NEON_GREEN : NEON_BLUE);
        priceL.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        Label perDayL = mkLbl("/day", 13, FontWeight.NORMAL, TEXT_DIM);
        priceRow.getChildren().addAll(priceL, perDayL);

        HBox daysRow = new HBox(8); daysRow.setAlignment(Pos.CENTER_LEFT);
        Label daysL   = mkLbl("Days:", 12, FontWeight.NORMAL, TEXT_MUTED);
        Spinner<Integer> spinner = new Spinner<>(1, 365, 1);
        spinner.setEditable(true); spinner.setPrefWidth(90);
        Label estL = mkLbl("Est: $" + (int) car.pricePerDay, 12, FontWeight.BOLD, AMBER);
        spinner.valueProperty().addListener((o, old, nv) ->
                estL.setText("Est: $" + String.format("%.0f", car.pricePerDay * nv)));
        daysRow.getChildren().addAll(daysL, spinner, hSpacer(), estL);

        Button addBtn = mkBtn(inCart ? "✓  In Cart  —  Remove" : "+  Add to Cart",
                inCart ? NEON_GREEN : NEON_BLUE,
                inCart ? NEON_GREEN.darker().darker() : Color.rgb(12, 55, 190));
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            if (car.inCart) {
                state.cart.removeIf(ci -> ci.car == car); car.inCart = false;
            } else {
                LocalDate start = LocalDate.now().plusDays(1);
                LocalDate end   = start.plusDays(spinner.getValue() - 1);
                state.cart.add(new CartItem(car, spinner.getValue(), start, end));
                car.inCart = true;
            }
            ctrl.rebuild("CUST_FLEET"); ctrl.showScene("CUST_FLEET");
        });

        bottom.getChildren().addAll(priceRow, daysRow, addBtn);
        card.getChildren().addAll(header, specs, descL, hLine(), bottom);
        return card;
    }
}