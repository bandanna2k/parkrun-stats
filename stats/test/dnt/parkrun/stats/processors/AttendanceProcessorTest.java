package dnt.parkrun.stats.processors;

import dnt.parkrun.datastructures.*;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class AttendanceProcessorTest
{
    @Test
    public void shouldGetHighestAttendance()
    {
        AttendanceProcessor processor = new AttendanceProcessor();
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

        processor.onFinishCourse();

        Assertions.assertThat(processor.getMaxAttendance(1).getFirst().count).isEqualTo(10);
        Assertions.assertThat(processor.getMaxAttendance(1).getFirst().date)
                .isEqualTo(Date.from(Instant.EPOCH.plus(70, ChronoUnit.DAYS)));
        Assertions.assertThat(processor.getLastAttendance(1).count).isEqualTo(10);
        Assertions.assertThat(processor.getLastAttendance(1).date)
                .isEqualTo(Date.from(Instant.EPOCH.plus(70, ChronoUnit.DAYS)));

        Assertions.assertThat(processor.getMaxAttendance(2).getFirst().count).isEqualTo(1);
        Assertions.assertThat(processor.getMaxAttendance(2).getFirst().date)
                .isEqualTo(Date.from(Instant.EPOCH.plus(7, ChronoUnit.DAYS)));
        Assertions.assertThat(processor.getLastAttendance(2).count).isEqualTo(1);
        Assertions.assertThat(processor.getLastAttendance(2).date)
                .isEqualTo(Date.from(Instant.EPOCH.plus(70, ChronoUnit.DAYS)));
    }
}