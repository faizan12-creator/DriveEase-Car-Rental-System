package DriveEase;


import java.time.LocalDate;

public class PromoCode {
    public String  code, type;
    public double  value;
    public int     maxUses, usedCount;
    public LocalDate expiry;
    public boolean active;

    public PromoCode(String code, String type, double value, int maxUses, LocalDate expiry) {
        this.code = code; this.type = type; this.value = value;
        this.maxUses = maxUses; this.expiry = expiry;
        this.usedCount = 0; this.active = true;
    }

    public double apply(double total) {
        if (!isValid()) return 0;
        return type.equals("PERCENT") ? total * value / 100 : Math.min(value, total);
    }

    public boolean isValid() {
        return active && usedCount < maxUses && !LocalDate.now().isAfter(expiry);
    }
}