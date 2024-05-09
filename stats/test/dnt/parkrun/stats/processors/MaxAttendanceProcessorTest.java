package dnt.parkrun.stats.processors;

import dnt.parkrun.datastructures.*;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class MaxAttendanceProcessorTest
{
    public static final Date EPOCH = Date.from(Instant.EPOCH);
    public static final Date EPOCH_PLUS_7 = Date.from(Instant.EPOCH.plus(7, ChronoUnit.DAYS));
    public static final Date EPOCH_PLUS_14 = Date.from(Instant.EPOCH.plus(14, ChronoUnit.DAYS));

    @Test
    public void shouldGetHighestAttendance()
    {
        Date now = Date.from(Instant.now());

        MaxAttendanceProcessor processor = new MaxAttendanceProcessor();
        for (int week = 1; week <= 10; week++)
        {
            Date date = Date.from(Instant.EPOCH.plus(week * 7, ChronoUnit.DAYS));
            for (int result = 1; result <= week; result++)
            {
//                System.out.printf("Loop %d %d%n", week, result);
                processor.visitInOrder(new Result(
                        1, date, result, Athlete.NO_ATHLETE, Time.NO_TIME, AgeCategory.UNKNOWN, AgeGrade.newInstanceNoAgeGrade()));
            }
        }
        for (int week = 1; week <= 10; week++)
        {
            Date date = Date.from(Instant.EPOCH.plus(week * 7, ChronoUnit.DAYS));

            for (int result = week; result >= week; --result)
            {
//                System.out.printf("Loop %d %d%n", week, result);
                processor.visitInOrder(new Result(
                        2, date, result, Athlete.NO_ATHLETE, Time.NO_TIME, AgeCategory.UNKNOWN, AgeGrade.newInstanceNoAgeGrade()));
            }
        }

        Assertions.assertThat(processor.getMaxAttendancesOverAllEvents(1).getFirst().count).isEqualTo(10);
        Assertions.assertThat(processor.getMaxAttendancesOverAllEvents(1).getFirst().date)
                .isEqualTo(Date.from(Instant.EPOCH.plus(70, ChronoUnit.DAYS)));

        Assertions.assertThat(processor.getMaxAttendancesOverAllEvents(2).getFirst().count).isEqualTo(1);
        Assertions.assertThat(processor.getMaxAttendancesOverAllEvents(2).getFirst().date)
                .isEqualTo(Date.from(Instant.EPOCH.plus(7, ChronoUnit.DAYS)));
    }
}