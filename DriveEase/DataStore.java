package DriveEase;



import java.time.LocalDate;
import java.util.List;

/**
 * Populates AppState with initial seed data (fleet, promos, demo customer).
 * Called once at startup if files are empty / missing.
 */
public class DataStore {

    private final AppState state;

    public DataStore(AppState state) {
        this.state = state;
    }

    /** Seeds fleet, promo codes and demo customer — skips if already populated. */
    public void seed() {
        seedFleet();
        seedPromos();
        seedDemoCustomer();
    }

    // ── Fleet ──────────────────────────────────────────────────────

    public void seedFleet() {
        if (!state.fleet.isEmpty()) return;

        state.fleet.add(new Car("C01", "Honda Civic",      "XYZ-123",  50, true,  "Sedan",   "🚗",
                "Smooth daily driver with great fuel economy",       5, "Petrol",   "Automatic"));
        state.fleet.add(new Car("C02", "Toyota Corolla",   "ABC-456",  45, true,  "Sedan",   "🚗",
                "Reliable and comfortable mid-size sedan",           5, "Petrol",   "Automatic"));
        state.fleet.add(new Car("C03", "Ford Mustang",     "MUS-001", 120, true,  "Sports",  "🏎",
                "Iconic American muscle — thrilling performance",    4, "Petrol",   "Manual"));
        state.fleet.add(new Car("C04", "BMW M3",           "BMW-999", 150, true,  "Luxury",  "🏎",
                "Ultimate driving machine — luxury and power",       4, "Petrol",   "Automatic"));
        state.fleet.add(new Car("C05", "Audi A4",          "AUD-111",  90, true,  "Luxury",  "🚙",
                "Sophisticated German engineering at its finest",    5, "Petrol",   "Automatic"));
        state.fleet.add(new Car("C06", "Tesla Model 3",    "TSL-404",  80, true,  "Electric","⚡",
                "Zero-emission — fast, silent, and smart",           5, "Electric", "Automatic"));
        state.fleet.add(new Car("C07", "Hyundai Elantra",  "HYN-777",  40, true,  "Economy", "🚗",
                "Budget-friendly, efficient, and comfortable",       5, "Petrol",   "Automatic"));
        state.fleet.add(new Car("C08", "Range Rover",      "RR-555",  200, false, "SUV",     "🛻",
                "Premium off-road luxury SUV — seats 7",            7, "Diesel",   "Automatic"));
        state.fleet.add(new Car("C09", "Mercedes C-Class", "MER-222", 110, false, "Luxury",  "🚙",
                "Pure elegance — a statement of class and style",   5, "Petrol",   "Automatic"));
        state.fleet.add(new Car("C10", "Kia Sportage",     "KIA-333",  60, true,  "SUV",     "🛻",
                "Modern SUV with advanced safety features",          5, "Petrol",   "Automatic"));
        state.fleet.add(new Car("C11", "Porsche 911",      "POR-911", 280, true,  "Sports",  "🏎",
                "Legendary sports car — raw, precise, breathtaking",2, "Petrol",   "Manual"));
        state.fleet.add(new Car("C12", "Toyota Fortuner",  "FOR-400",  85, true,  "SUV",     "🛻",
                "Rugged 7-seater SUV perfect for long trips",        7, "Diesel",   "Automatic"));
    }

    // ── Promo codes ────────────────────────────────────────────────

    public void seedPromos() {
        if (!state.promoCodes.isEmpty()) return;

        state.promoCodes.add(new PromoCode("WELCOME20", "PERCENT", 20, 100,
                LocalDate.now().plusMonths(3)));
        state.promoCodes.add(new PromoCode("FLAT500",   "FLAT",   500,  50,
                LocalDate.now().plusMonths(1)));
        state.promoCodes.add(new PromoCode("DRIVE10",   "PERCENT", 10, 200,
                LocalDate.now().plusMonths(6)));
    }

    // ── Demo customer ──────────────────────────────────────────────

    public void seedDemoCustomer() {
        if (state.customers.containsKey("3830263261151")) return;

        Customer demo = new Customer("Ali Hassan", "3830263261151", "123");
        demo.phone = "0301-1234567";
        demo.email = "ali@driveease.pk";
        demo.loginHistory.add("2026/04/10 09:15:00");
        demo.loginHistory.add("2026/05/14 14:30:22");
        demo.recordLogin();

        // One existing booking (Mercedes, 5 days)
        Car mercedes = state.fleet.stream()
                .filter(c -> c.id.equals("C09"))
                .findFirst().orElse(null);
        if (mercedes != null) {
            CartItem ci = new CartItem(mercedes, 5,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 6));
            BookingRecord br = new BookingRecord(List.of(ci), 550, "Bank Transfer", 0, "");
            mercedes.isAvailable = false;
            demo.bookings.add(br);
            state.allBookings.add(br);
        }

        state.customers.put(demo.nic, demo);
    }
}