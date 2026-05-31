package DriveEase;


import java.util.ArrayList;
import java.util.List;

public class Customer {
    public String name, nic, password, phone, email;
    public boolean isBlocked = false;
    public List<String>        loginHistory = new ArrayList<>();
    public List<BookingRecord> bookings     = new ArrayList<>();

    public Customer(String name, String nic, String password) {
        this.name = name; this.nic = nic; this.password = password;
        this.phone = ""; this.email = "";
    }

    public void recordLogin() { loginHistory.add(Utils.now()); }
}
