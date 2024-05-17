package dnt.parkrun.common;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static dnt.parkrun.common.DateConverter.parseWebsiteDate;
import static org.assertj.core.api.Assertions.assertThat;

public class DateConverterTest
{
    @Test
    public void testParseWebsiteDate()
    {
        assertMatch(parseWebsiteDate("25/12/2024"), 2024, Calendar.DECEMBER, 25);
    }

    @Test
    public void testParseWebsiteDatePostMay17()
    {
        assertMatch(parseWebsiteDate("2024-05-11"), 2024, Calendar.MAY, 11);
    }

    private void assertMatch(Date actualDate, int expectedYear, int expectedMonth, int expectedDayOfMonth)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(actualDate);
        assertThat(cal.get(Calendar.YEAR)).isEqualTo(expectedYear);
        assertThat(cal.get(Calendar.MONTH)).isEqualTo(expectedMonth);
        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(expectedDayOfMonth);
    }

}