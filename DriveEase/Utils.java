package DriveEase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {

    /** Returns current date-time as formatted string */
    public static String now() {
        return DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                .format(LocalDateTime.now());
    }
}