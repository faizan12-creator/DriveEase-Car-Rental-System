package DriveEase;

public class Car {
    public String id, name, plate, category, icon, description, fuel, transmission;
    public double pricePerDay;
    public boolean isAvailable, inCart;
    public int seats;

    public Car(String id, String name, String plate, double price, boolean avail,
               String category, String icon, String desc, int seats, String fuel, String trans) {
        this.id = id; this.name = name; this.plate = plate;
        this.pricePerDay = price; this.isAvailable = avail;
        this.category = category; this.icon = icon;
        this.description = desc; this.seats = seats;
        this.fuel = fuel; this.transmission = trans;
        this.inCart = false;
    }
}