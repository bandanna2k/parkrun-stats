package dnt.parkrun.stats;

import dnt.parkrun.common.DateConverter;
import org.junit.Test;

import java.util.Date;

import static dnt.parkrun.stats.MostEventStats.getParkrunDay;

public class DateAdjusterTest
{
    @Test
    public void shouldAdjustDate()
    {
        System.out.println(getParkrunDay(new Date()));
        System.out.println(getParkrunDay(DateConverter.parseWebsiteDate("08/03/2024")));
    }
}
