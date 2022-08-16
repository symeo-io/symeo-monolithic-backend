package fr.catlean.monolithic.backend.domain.helper;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Slf4j
public class DateHelper {

    public static final String DATE_PATTERN = "yyyy-MM-dd";

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
