package fr.catlean.delivery.processor.domain.helper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DateHelper {
    public static Date getWeekStartDate(final TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, -1);
        }
        return calendar.getTime();
    }

    public static List<Date> getWeekStartDateForTheLastWeekNumber(int numberOfWeek, final TimeZone timeZone) {
        final List<Date> weekStartDates = new ArrayList<>();
        final Date currentWeekStartDate = getWeekStartDate(timeZone);
        weekStartDates.add(currentWeekStartDate);
        LocalDate localWeekStartDate = currentWeekStartDate.toInstant().atZone(timeZone.toZoneId()).toLocalDate();
        for (int weekIndex = 1; weekIndex < numberOfWeek; weekIndex++) {
            localWeekStartDate = localWeekStartDate.minus(1, ChronoUnit.WEEKS);
            weekStartDates.add(java.sql.Date.valueOf(localWeekStartDate));
        }
        return weekStartDates.stream().sorted(Comparator.comparing(Date::getTime)).toList();
    }
}
