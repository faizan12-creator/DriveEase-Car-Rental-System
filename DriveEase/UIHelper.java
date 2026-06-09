package DriveEase;

import javafx.animation.FadeTransition;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;

import static DriveEase.AppColors.*;

/**
 * Stateless utility class — every method is public static.
 * Provides button builders, label factories, colour helpers, etc.
 */
public class UIHelper {

    // ── Colour helpers ────────────────────────────────────────────

    public static String hex(Color c) {
        return String.format("#%02x%02x%02x",
                (int)(c.getRed()*255),(int)(c.getGreen()*255),(int)(c.getBlue()*255));
    }

    public static String rgba(Color c, double a) {
        return String.format("rgba(%d,%d,%d,%.2f)",
                (int)(c.getRed()*255),(int)(c.getGreen()*255),(int)(c.getBlue()*255), a);
    }

    // ── Button factories ──────────────────────────────────────────

    public static Button mkBtn(String text, Color top, Color bot) {
        Button b = new Button(text);
        String base = String.format(
                "-fx-background-color:linear-gradient(to bottom right,%s,%s);" +
                        "-fx-background-radius:12;-fx-text-fill:white;-fx-font-size:13px;" +
                        "-fx-font-weight:bold;-fx-padding:11 22;-fx-cursor:hand;" +
                        "-fx-effect:dropshadow(gaussian,%s,12,0,0,3);",
                hex(top), hex(bot), rgba(top, 0.4));
        String hover = String.format(
                "-fx-background-color:linear-gradient(to bottom right,%s,%s);" +
                        "-fx-background-radius:12;-fx-text-fill:white;-fx-font-size:13px;" +
                        "-fx-font-weight:bold;-fx-padding:11 22;-fx-cursor:hand;" +
                        "-fx-effect:dropshadow(gaussian,%s,22,0,0,6);",
                hex(top.brighter()), hex(bot), rgba(top, 0.65));
        String press = String.format(
                "-fx-background-color:linear-gradient(to bottom right,%s,%s);" +
                        "-fx-background-radius:12;-fx-text-fill:white;-fx-font-size:13px;" +
                        "-fx-font-weight:bold;-fx-padding:13 22 9 22;-fx-cursor:hand;",
                hex(bot), hex(bot.darker()));
        b.setStyle(base);
        b.setOnMouseEntered(e  -> b.setStyle(hover));
        b.setOnMouseExited (e  -> b.setStyle(base));
        b.setOnMousePressed(e  -> b.setStyle(press));
        b.setOnMouseReleased(e -> b.setStyle(hover));
        return b;
    }

    public static Button mkBtn(String text, Color c)  { return mkBtn(text, c.brighter(), c.darker()); }
    public static Button mkBtnRed(String text)        { return mkBtn(text, CRIMSON, CRIMSON.darker().darker()); }
    public static Button mkBtnGhost(String text)      { return mkBtn(text, SURFACE_2, SURFACE_1); }

    // ── Label factories ───────────────────────────────────────────

    public static Label mkLbl(String t, double size, FontWeight w, Color c) {
        Label l = new Label(t);
        l.setFont(Font.font("Segoe UI", w, size));
        l.setTextFill(c);
        return l;
    }

    public static Label mkTitle(String t) {
        Label l = new Label(t);
        l.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        l.setTextFill(TEXT_BRIGHT);
        return l;
    }

    /** Coloured glowing badge chip */
    public static Label mkBadge(String text, Color c) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        l.setTextFill(c);
        l.setStyle(String.format(
                "-fx-background-color:%s;-fx-background-radius:20;" +
                        "-fx-border-color:%s;-fx-border-radius:20;-fx-padding:3 10;",
                rgba(c, 0.15), rgba(c, 0.55)));
        return l;
    }

    /** Small spec chip for car cards */
    public static Label mkChip(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(TEXT_MUTED);
        l.setStyle("-fx-background-color:#090e20;-fx-background-radius:6;" +
                "-fx-border-color:#182848;-fx-border-radius:6;-fx-padding:3 8;");
        return l;
    }

    // ── Field factories ───────────────────────────────────────────

    public static TextField    mkField(String p) { TextField f = new TextField();    f.setPromptText(p); return f; }
    public static PasswordField mkPass(String p) { PasswordField f = new PasswordField(); f.setPromptText(p); return f; }

    // ── Spacer / divider helpers ──────────────────────────────────

    public static HBox   hSpacer() { HBox   v = new HBox();   HBox.setHgrow(v, Priority.ALWAYS); return v; }
    public static VBox   vSpacer() { VBox   v = new VBox();   VBox.setVgrow(v, Priority.ALWAYS); return v; }
    public static Region hLine()   { Region r = new Region(); r.setPrefHeight(1); r.setStyle("-fx-background-color:#0f1e3c;"); return r; }

    // ── Animation ─────────────────────────────────────────────────

    public static void fadeIn(Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(380), node);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ── Category → colour mapping ─────────────────────────────────

    public static Color catColor(String cat) {
        return switch (cat) {
            case "Sedan"    -> NEON_BLUE;
            case "Sports"   -> NEON_PINK;
            case "Luxury"   -> NEON_YELLOW;
            case "Electric" -> NEON_GREEN;
            case "SUV"      -> NEON_ORANGE;
            default         -> NEON_TEAL;
        };
    }
}