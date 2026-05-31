# 🚗 DriveEase — Premium Car Rental Platform

<div align="center">

![Java](https://img.shields.io/badge/Java-26-orange?style=for-the-badge&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge&logo=java)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**A fully-featured desktop Car Rental Management System built with JavaFX.**  
Dark-themed, animated UI with customer portal, admin dashboard, promo codes, and file-based persistence.

</div>

---

## ✨ Features

### 👤 Customer Side
| Feature | Details |
|---|---|
| 🔐 Register / Login | NIC-based auth with validation (13-digit NIC) |
| 🚗 Browse Fleet | Search, filter by category, sort by price or name |
| 🛒 Shopping Cart | Add/remove vehicles, see live total estimate |
| 📅 Rental Days | Spinner-based day selector with instant price estimate |
| 💳 Checkout | Choose payment method, apply promo codes, select add-ons |
| 🧾 Receipt | Animated booking confirmation with full breakdown |
| 👤 My Profile | Account info, login history, booking history |
| ↩️ Return Vehicle | Mark active rentals as returned |

### 🔐 Admin Side
| Feature | Details |
|---|---|
| 📊 Dashboard | 8 live metric cards + fleet status bars + recent bookings |
| 🚗 Fleet Management | View all cars, toggle availability |
| 📋 All Bookings | Complete booking history with discount info |
| 👥 Customers | View all customers, block/unblock accounts |
| 🏷️ Promo Codes | Create & manage discount codes (% or flat) |
| ⚙️ Settings | Password change, save/reload data |

### 🎨 UI / UX
- Deep space dark theme with neon accents
- Smooth fade-in and slide-up animations
- Hover & press effects on all buttons
- Responsive grid layout for fleet cards
- Undecorated full-screen window

---

## 📸 Screenshots

| Portal | Admin Dashboard |
|--------|----------------|
| ![Portal](screenshots/portal.png) | ![Dashboard](screenshots/dashboard.png) |

> Add your own screenshots in a `/screenshots` folder.

---

## 🏗️ Project Structure

```
DriveEase/
│
├── 📦 Models
│   ├── Car.java              — Vehicle data model
│   ├── CartItem.java         — Cart item (car + days + dates)
│   ├── BookingRecord.java    — Booking with payment & discount info
│   ├── Customer.java         — Customer with login history & bookings
│   └── PromoCode.java        — Discount code (% or flat, with expiry)
│
├── 🏛️ Infrastructure
│   ├── AppState.java         — Central shared state (fleet, cart, customers)
│   ├── AppColors.java        — All color constants
│   ├── StyleSheet.java       — Complete JavaFX CSS
│   ├── UIHelper.java         — Static UI factory methods (buttons, labels)
│   ├── Utils.java            — Utility helpers (timestamp)
│   ├── FileManager.java      — Save & load all data (text files)
│   ├── DataStore.java        — Seed data (fleet, promos, demo customer)
│   └── SceneController.java  — Interface for cross-scene navigation
│
├── 🖼️ Scenes
│   ├── PortalScene.java      — Landing / home page
│   ├── AuthScenes.java       — Login, Register, Admin Login
│   ├── FleetScene.java       — Fleet browser + car cards
│   ├── CartScene.java        — Shopping cart
│   ├── CheckoutScene.java    — Payment + promo + add-ons
│   ├── ReceiptScene.java     — Booking confirmation receipt
│   ├── ProfileScene.java     — Customer account & bookings
│   └── AdminDashScene.java   — Full admin panel
│
└── 🚀 Entry Point
    └── Drive.java            — Main Application class
```

---

## 💾 Data Persistence

Data is saved to plain text files in the project root — **no database required**.

| File | Contents |
|------|----------|
| `customers.txt` | NIC, name, password, phone, email, login history, blocked status |
| `bookings.txt` | Booking ID, timestamp, payment, status, items, total, discount |
| `cars.txt` | Car ID and availability status |
| `promos.txt` | Promo code, type, value, usage count, expiry |

> Files are created automatically on first run.

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+** (tested on Java 26)
- **JavaFX 21** SDK — [Download here](https://gluonhq.com/products/javafx/)
- IntelliJ IDEA (recommended) or any Java IDE

### Run in IntelliJ IDEA

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/DriveEase.git
   cd DriveEase
   ```

2. **Add JavaFX SDK to project**
   - File → Project Structure → Libraries → Add JavaFX SDK lib folder

3. **Set VM Options**
   - Run → Edit Configurations → VM Options:
   ```
   --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
   ```

4. **Run `Drive.java`**
   - Right-click `Drive.java` → Run

### Default Login Credentials

| Role | Username / NIC | Password |
|------|---------------|----------|
| 👤 Customer (demo) | `3830263261151` | `123` |
| 🔐 Admin | `admin` | `admin123` |

---

## 🏷️ Default Promo Codes

| Code | Type | Discount | Valid For |
|------|------|----------|-----------|
| `WELCOME20` | Percentage | 20% off | 3 months |
| `FLAT500` | Flat | $500 off | 1 month |
| `DRIVE10` | Percentage | 10% off | 6 months |

---

## 🔧 Tech Stack

| Technology | Usage |
|-----------|-------|
| **Java 26** | Core language |
| **JavaFX 21** | UI framework |
| **Java I/O** | File-based data persistence |
| **Java Streams** | Data filtering & sorting |
| **JavaFX Animations** | FadeTransition, Timeline, ScaleTransition |
| **JavaFX CSS** | Inline + stylesheet dark theme |

---

## 🏛️ Architecture

```
Drive.java (Application + SceneController)
    │
    ├── AppState          ← all shared data
    ├── FileManager       ← persistence
    ├── DataStore         ← seeding
    │
    └── Scene Builders (each gets AppState + SceneController)
            ├── PortalScene
            ├── AuthScenes
            ├── FleetScene
            ├── CartScene
            ├── CheckoutScene
            ├── ReceiptScene
            ├── ProfileScene
            └── AdminDashScene
```

The `SceneController` interface decouples scene builders from `Drive.java` — no scene builder imports `Drive` directly. Navigation, alerts, saving, and stage access all go through the interface.

---

## 👨‍💻 Developer

**Faizan Gul**  
## BS Software Engineering  

---

## 📄 License

This project is licensed under the **MIT License** — feel free to use, modify and distribute.

```
MIT License — Copyright (c) 2026 Faizan Gul
```
