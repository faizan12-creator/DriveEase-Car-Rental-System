package DriveEase;


import java.util.*;

/**
 * Central store for all shared mutable state.
 * Every scene builder reads / writes through this object.
 */
public class AppState {

    public final List<Car>            fleet       = new ArrayList<>();
    public final Map<String,Customer> customers   = new LinkedHashMap<>();
    public final List<BookingRecord>  allBookings = new ArrayList<>();
    public final List<CartItem>       cart        = new ArrayList<>();
    public final List<PromoCode>      promoCodes  = new ArrayList<>();

    public Customer activeCustomer;

    // ── Admin credentials ─────────────────────────────────────────
    public static final String ADMIN_USER = "admin";
    public static final String ADMIN_PASS = "admin123";
}