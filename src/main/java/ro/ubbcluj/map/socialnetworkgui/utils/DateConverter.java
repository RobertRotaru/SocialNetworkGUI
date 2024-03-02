package ro.ubbcluj.map.socialnetworkgui.utils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;

public class DateConverter {
    public static String convert(OffsetDateTime date) {

        date = date.withOffsetSameLocal(ZoneId.systemDefault().getRules().getOffset(
                LocalDateTime.now()
        ));

        long yearsDiff = Math.abs(ChronoUnit.YEARS.between(date, OffsetDateTime.now(ZoneId.systemDefault())));
        long monthsDiff = Math.abs(ChronoUnit.MONTHS.between(date, OffsetDateTime.now(ZoneId.systemDefault())));
        long daysDiff = Math.abs(ChronoUnit.DAYS.between(date, OffsetDateTime.now(ZoneId.systemDefault())));
        long hoursDiff = Math.abs(ChronoUnit.HOURS.between(date, OffsetDateTime.now(ZoneId.systemDefault())));
        long minsDiff = Math.abs(ChronoUnit.MINUTES.between(date, OffsetDateTime.now(ZoneId.systemDefault())));
        long secsDiff = Math.abs(ChronoUnit.SECONDS.between(date, OffsetDateTime.now(ZoneId.systemDefault())));

        if(yearsDiff > 0) {
            if(yearsDiff == 1) {
                return  "(a year ago)";
            }
            else return "(" + yearsDiff + " years ago)";
        }
        if(monthsDiff > 0) {
            if(monthsDiff == 1) {
                return "(a month ago)";
            }
            else return "(" + monthsDiff + " months ago)";
        }
        if(daysDiff > 0) {
            if(daysDiff == 1) {
                return "(a day ago)";
            }
            else return "(" + daysDiff + " days ago)";
        }
        if(hoursDiff > 0) {
            if(hoursDiff == 1) {
                return "(a hour ago)";
            }
            else return "(" + hoursDiff + " hours ago)";
        }
        if(minsDiff > 0) {
            if(minsDiff == 1) {
                return "(a minute ago)";
            }
            else return "(" + minsDiff + " minutes ago)";
        }
        if(secsDiff > 0) {
            if(secsDiff == 1) {
                return "(a second ago)";
            }
            else return "(" + secsDiff + " seconds ago)";
        }
        return "";
    }
}
