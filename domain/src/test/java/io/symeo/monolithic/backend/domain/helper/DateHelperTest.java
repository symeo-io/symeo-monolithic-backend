package io.symeo.monolithic.backend.domain.helper;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.FAILED_TO_PARSE_DATE;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DateHelperTest {

    @Test
    void should_parse_date_to_string() {
        // Given
        final Date now = new Date();

        // When
        final String dateToString = dateToString(now);

        // Then
        assertThat(dateToString).isEqualTo(new SimpleDateFormat(DATE_PATTERN).format(now));
    }

    @Test
    void should_parse_string_to_date() throws SymeoException, ParseException {
        // Given
        final String dateString = "1992-08-12";

        // When
        final Date date = stringToDate(dateString);

        // Then
        assertThat(date).isEqualTo(new SimpleDateFormat(DATE_PATTERN).parse(dateString));
    }

    @Test
    void should_raise_an_exception_given_an_invalid_date_format() {
        // Given
        final String dateString = new Faker().ancient().god();

        // When
        SymeoException symeoException = null;
        try {
            stringToDate(dateString);
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNotNull();
        assertThat(symeoException.getCode()).isEqualTo(FAILED_TO_PARSE_DATE);
        assertThat(symeoException.getMessage()).isEqualTo(String.format("Failed to parse date %s for pattern %s",
                dateString, DATE_PATTERN));
    }

    @Test
    void should_return_date_ranges() {
        // Given
        final Date startDate =
                Date.from(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusDays(25).atStartOfDay(ZoneId.systemDefault()).toInstant());
        final Date endDate =
                Date.from(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());

        // When
        final List<Date> rangeDates =
                getRangeDatesBetweenStartDateAndEndDateForRange(startDate, endDate, 5,
                        TimeZone.getTimeZone(ZoneId.systemDefault()));

        // Then
        assertThat(rangeDates.get(0)).isEqualTo(startDate);
        assertThat(rangeDates.get(4)).isEqualTo(endDate);
        assertThat(ChronoUnit.DAYS.between(rangeDates.get(0).toInstant(), rangeDates.get(1).toInstant())).isEqualTo(5);
        assertThat(ChronoUnit.DAYS.between(rangeDates.get(1).toInstant(), rangeDates.get(2).toInstant())).isEqualTo(5);
        assertThat(ChronoUnit.DAYS.between(rangeDates.get(2).toInstant(), rangeDates.get(3).toInstant())).isEqualTo(5);
        assertThat(ChronoUnit.DAYS.between(rangeDates.get(3).toInstant(), rangeDates.get(4).toInstant())).isEqualTo(5);
    }
}
