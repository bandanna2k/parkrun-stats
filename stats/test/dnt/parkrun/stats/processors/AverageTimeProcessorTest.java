package dnt.parkrun.stats.processors;

import dnt.parkrun.datastructures.*;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class AverageTimeProcessorTest
{
    @Test
    public void shouldGetAverageTime()
    {
        AverageTimeProcessor processor = new AverageTimeProcessor();
        for (int week = 1; week <= 10; week++)
        {
            Date date = Date.from(Instant.EPOCH.plus(week * 7, ChronoUnit.DAYS));
            for (int result = 1; result <= week; result++)
            {
//                System.out.printf("Loop %d %d%n", week, result);
                processor.visitInOrder(new Result(
                        1, date, week, result, Athlete.from("Name" + result, result), Time.from(result),
                        AgeCategory.UNKNOWN, AgeGrade.newInstanceNoAgeGrade()));
            }
        }
        for (int week = 1; week <= 10; week++)
        {
            Date date = Date.from(Instant.EPOCH.plus(week * 7, ChronoUnit.DAYS));

            for (int result = week; result >= week; --result)
            {
//                System.out.printf("Loop %d %d%n", week, result);
                processor.visitInOrder(new Result(
                        2, date, week, result, Athlete.from("Name" + result, result), Time.from(result),
                        AgeCategory.UNKNOWN, AgeGrade.newInstanceNoAgeGrade()));
            }
        }

        processor.onFinish();

        Assertions.assertThat(processor.getAverageTime(1).getTotalSeconds()).isEqualTo(4);
        Assertions.assertThat(processor.getRecentAverageTime(1).getTotalSeconds()).isEqualTo(4);
        Assertions.assertThat(processor.getAverageTime(2).getTotalSeconds()).isEqualTo(5);
        Assertions.assertThat(processor.getRecentAverageTime(2).getTotalSeconds()).isEqualTo(5);
    }

    @Test
    public void testMovingAverage()
    {
        AverageTimeProcessor processor = new AverageTimeProcessor();
        for (int week = 1; week <= 20; week++)
        {
            Date date = Date.from(Instant.EPOCH.plus(week * 7, ChronoUnit.DAYS));

            processor.visitInOrder(new Result(
                    2, date, week, 1, Athlete.from("Name", 1), Time.from(week),
                    AgeCategory.UNKNOWN, AgeGrade.newInstanceNoAgeGrade()));
        }

        processor.onFinish();

        Assertions.assertThat(processor.getAverageTime(2).getTotalSeconds()).isEqualTo(10);
        Assertions.assertThat(processor.getRecentAverageTime(2).getTotalSeconds()).isEqualTo(15);
    }
}