package io.symeo.monolithic.backend.domain.helper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.lang.Math.round;

@Slf4j
public class DateHelper {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static String dateToString(final Date date) {
        return new SimpleDateFormat(DATE_PATTERN).format(date);
    }

    public static String dateTimeToString(final Date date) {
        return new SimpleDateFormat(DATE_TIME_PATTERN).format(date);
    }

    public static Date stringToDateTime(final String dateTimeString) {
        try {
            return new SimpleDateFormat(DATE_TIME_PATTERN).parse(dateTimeString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date stringToDate(String dateString) throws SymeoException {
        try {
            return new SimpleDateFormat(DATE_PATTERN).parse(dateString);
        } catch (ParseException e) {

            final String message = String.format("Failed to parse date %s for pattern %s", dateString, DATE_PATTERN);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.FAILED_TO_PARSE_DATE)
                    .message(message)
                    .rootException(e)
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

    public static List<Date> getPreviousRangeDateFromStartDateAndEndDate(final Date startDate, final Date endDate,
                                                                         final int range, final TimeZone timeZone) {
        final Date previousStartDate = getPreviousStartDateFromStartDateAndEndDate(startDate, endDate, timeZone);
        return getRangeDatesBetweenStartDateAndEndDateForRange(previousStartDate, startDate, range, timeZone);
    }

    public static Date getPreviousStartDateFromStartDateAndEndDate(final Date startDate, final Date endDate,
                                                                   final TimeZone timeZone) {
        final long rangeDays = ChronoUnit.DAYS.between(startDate.toInstant(), endDate.toInstant());
        return Date.from((startDate.toInstant().atZone(timeZone.toZoneId()).toLocalDate().minusDays(rangeDays)).atStartOfDay(timeZone.toZoneId()).toInstant());
    }

    public static float hoursToDays(long hours) {
        return hours < 2f ? 0.1f : round(10f * hours / 24) / 10f;
    }

    public static Long getNumberOfMinutesBetweenDates(final Date date1, final Date date2) {
        return ChronoUnit.MINUTES.between(date1.toInstant(), date2.toInstant());
    }
}
