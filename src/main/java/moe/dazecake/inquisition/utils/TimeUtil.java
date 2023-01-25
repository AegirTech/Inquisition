package moe.dazecake.inquisition.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    public static boolean isInTime(String startTime, String endTime) {
        startTime = LocalDateTime.now().toLocalDate().toString() + "T" + startTime + ":00.000Z";
        endTime = LocalDateTime.now().toLocalDate().toString() + "T" + endTime + ":00.000Z";

        LocalDateTime start = LocalDateTime.parse(startTime, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ISO_DATE_TIME);
        return LocalDateTime.now().isAfter(start) && LocalDateTime.now().isBefore(end);
    }

}
