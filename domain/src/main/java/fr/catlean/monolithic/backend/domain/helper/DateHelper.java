package fr.catlean.monolithic.backend.domain.helper;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
public class DateHelper {

    public static final String DATE_PATTERN = "yyyy-MM-dd";

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

    public static List<Date> getWeekStartDateForTheLastWeekNumber(final int numberOfWeek, final TimeZone timeZone) {
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

    public static String dateToString(final Date now) {
        return new SimpleDateFormat(DATE_PATTERN).format(now);
    }

    public static Date stringToDate(String dateString) throws CatleanException {
        try {
            return new SimpleDateFormat(DATE_PATTERN).parse(dateString);
        } catch (ParseException e) {

            final String message = String.format("Failed to parse date %s for pattern %s", dateString, DATE_PATTERN);
            LOGGER.error(message, e);
            throw CatleanException.builder()
                    .code(CatleanExceptionCode.FAILED_TO_PARSE_DATE)
                    .message(message)
                    .build();
        }
    }

    public static List<Date> getRangeDatesBetweenStartDateAndEndDateForRange(final Date startDate, final Date endDate
            , final int range, final TimeZone timeZone) {
        final List<Date> rangeDates = new ArrayList<>();
        rangeDates.add(startDate);
        Date rangeDate = startDate;
        while (ChronoUnit.DAYS.between(rangeDate.toInstant(), endDate.toInstant()) > range) {
            rangeDate =
                    Date.from((rangeDate.toInstant().atZone(timeZone.toZoneId()).toLocalDate().plusDays(range)).atStartOfDay(timeZone.toZoneId()).toInstant());
            rangeDates.add(rangeDate);

        }
        rangeDates.add(endDate);
        return rangeDates;
    }
}
