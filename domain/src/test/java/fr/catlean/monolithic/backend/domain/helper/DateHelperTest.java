package fr.catlean.monolithic.backend.domain.helper;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.FAILED_TO_PARSE_DATE;
import static fr.catlean.monolithic.backend.domain.helper.DateHelper.*;
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
    void should_parse_string_to_date() throws CatleanException, ParseException {
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
        CatleanException catleanException = null;
        try {
            stringToDate(dateString);
        } catch (CatleanException e) {
            catleanException = e;
        }

        // Then
        assertThat(catleanException).isNotNull();
        assertThat(catleanException.getCode()).isEqualTo(FAILED_TO_PARSE_DATE);
        assertThat(catleanException.getMessage()).isEqualTo(String.format("Failed to parse date %s for pattern %s",
                dateString, DATE_PATTERN));
    }
}
