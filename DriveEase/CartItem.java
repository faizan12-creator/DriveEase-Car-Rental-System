package DriveEase;



import java.time.LocalDate;

public class CartItem {
    public Car car;
    public int days;
    public LocalDate startDate, endDate;

    public CartItem(Car car, int days, LocalDate start, LocalDate end) {
        this.car = car; this.days = days;
        this.startDate = start; this.endDate = end;
    }

    public double subtotal() { return car.pricePerDay * days; }
}