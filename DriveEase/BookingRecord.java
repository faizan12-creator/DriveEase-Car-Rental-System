package DriveEase;


import java.util.ArrayList;
import java.util.List;

public class BookingRecord {
    public String bookingId, paymentMethod, timestamp, status, promoUsed;
    public List<CartItem> items;
    public double totalCost, discount;

    public BookingRecord(List<CartItem> items, double total, String method,
                         double discount, String promo) {
        this.bookingId     = "BKG-" + (int)(Math.random() * 90000 + 10000);
        this.items         = new ArrayList<>(items);
        this.totalCost     = total;
        this.paymentMethod = method;
        this.discount      = discount;
        this.promoUsed     = promo;
        this.status        = "ACTIVE";
        this.timestamp     = Utils.now();
    }
}
