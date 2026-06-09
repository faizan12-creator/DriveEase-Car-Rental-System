package DriveEase;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Handles all persistence (text files).
 * No JavaFX dependency — pure I/O.
 */
public class FileManager {

    private static final String F_CUSTOMERS = "customers.txt";
    private static final String F_BOOKINGS  = "bookings.txt";
    private static final String F_CARS      = "cars.txt";
    private static final String F_PROMOS    = "promos.txt";

    private final AppState state;

    public FileManager(AppState state) { this.state = state; }

    // ── Save all ──────────────────────────────────────────────────

    public void saveAll() {
        saveCustomers();
        saveBookings();
        saveCars();
        savePromos();
    }

    // ── Save ──────────────────────────────────────────────────────

    public void saveCustomers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(F_CUSTOMERS))) {
            for (Customer c : state.customers.values()) {
                String logins = String.join(",", c.loginHistory);
                pw.printf("%s|%s|%s|%s|%s|%s|%b%n",
                        c.nic, c.name, c.password,
                        c.phone.isEmpty() ? "-" : c.phone,
                        c.email.isEmpty() ? "-" : c.email,
                        logins, c.isBlocked);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void saveBookings() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(F_BOOKINGS))) {
            for (BookingRecord br : state.allBookings) {
                StringBuilder sb = new StringBuilder();
                for (CartItem ci : br.items) {
                    String sd = ci.startDate != null ? ci.startDate.toString() : "null";
                    String ed = ci.endDate   != null ? ci.endDate.toString()   : "null";
                    sb.append(ci.car.id).append(":").append(ci.days).append(":")
                            .append(sd).append(":").append(ed).append(";");
                }
                String owner = findOwner(br);
                pw.printf("%s|%s|%s|%s|%.2f|%.2f|%s|%s|%s%n",
                        br.bookingId, br.timestamp, br.paymentMethod, br.status,
                        br.totalCost, br.discount,
                        br.promoUsed == null ? "" : br.promoUsed, sb, owner);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void saveCars() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(F_CARS))) {
            for (Car c : state.fleet) pw.printf("%s|%b%n", c.id, c.isAvailable);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void savePromos() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(F_PROMOS))) {
            for (PromoCode p : state.promoCodes)
                pw.printf("%s|%s|%.2f|%d|%d|%s|%b%n",
                        p.code, p.type, p.value, p.maxUses, p.usedCount, p.expiry, p.active);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Load ──────────────────────────────────────────────────────

    public void loadCars() {
        File f = new File(F_CARS); if (!f.exists()) return;
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String[] p = sc.nextLine().trim().split("\\|");
                if (p.length < 2) continue;
                for (Car c : state.fleet)
                    if (c.id.equals(p[0])) { c.isAvailable = Boolean.parseBoolean(p[1]); break; }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadCustomers() {
        File f = new File(F_CUSTOMERS); if (!f.exists()) return;
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String[] p = sc.nextLine().trim().split("\\|", -1);
                if (p.length < 3) continue;
                Customer c = new Customer(p[1], p[0], p[2]);
                if (p.length > 3) c.phone = p[3].equals("-") ? "" : p[3];
                if (p.length > 4) c.email = p[4].equals("-") ? "" : p[4];
                if (p.length > 5 && !p[5].isEmpty())
                    for (String l : p[5].split(",")) if (!l.isEmpty()) c.loginHistory.add(l);
                if (p.length > 6) c.isBlocked = Boolean.parseBoolean(p[6]);
                state.customers.put(c.nic, c);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadBookings() {
        File f = new File(F_BOOKINGS); if (!f.exists()) return;
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String[] p = sc.nextLine().trim().split("\\|", -1);
                if (p.length < 9) continue;
                List<CartItem> items = new ArrayList<>();
                for (String entry : p[7].split(";")) {
                    if (entry.isEmpty()) continue;
                    String[] kv = entry.split(":");
                    if (kv.length < 2) continue;
                    int days = Integer.parseInt(kv[1]);
                    LocalDate sd = kv.length > 2 && !kv[2].equals("null") ? LocalDate.parse(kv[2]) : null;
                    LocalDate ed = kv.length > 3 && !kv[3].equals("null") ? LocalDate.parse(kv[3]) : null;
                    for (Car car : state.fleet)
                        if (car.id.equals(kv[0])) { items.add(new CartItem(car, days, sd, ed)); break; }
                }
                if (items.isEmpty()) continue;
                BookingRecord br = new BookingRecord(items,
                        Double.parseDouble(p[4]), p[2],
                        p.length > 5 ? Double.parseDouble(p[5]) : 0,
                        p.length > 6 ? p[6] : "");
                br.bookingId = p[0]; br.timestamp = p[1]; br.status = p[3];
                state.allBookings.add(br);
                Customer cu = state.customers.get(p[8]);
                if (cu != null) cu.bookings.add(br);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadPromos() {
        File f = new File(F_PROMOS); if (!f.exists()) return;
        state.promoCodes.clear();
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String[] p = sc.nextLine().trim().split("\\|");
                if (p.length < 7) continue;
                PromoCode pr = new PromoCode(p[0], p[1], Double.parseDouble(p[2]),
                        Integer.parseInt(p[3]), LocalDate.parse(p[5]));
                pr.usedCount = Integer.parseInt(p[4]);
                pr.active    = Boolean.parseBoolean(p[6]);
                state.promoCodes.add(pr);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String findOwner(BookingRecord br) {
        for (Customer c : state.customers.values())
            if (c.bookings.contains(br)) return c.nic;
        return "";
    }
}