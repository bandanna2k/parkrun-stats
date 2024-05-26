package dnt.parkrun.stats.processors.mostevents;

import dnt.parkrun.database.ResultTestBuilder;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Result;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static dnt.parkrun.stats.processors.mostevents.MostRunsAtCourseProcessor.MAX_RESULT_SIZE;

public class MostEventAsCourseProcessorTest
{
    private int courseId = 49;
    private ResultTestBuilder builder = new ResultTestBuilder().courseId(courseId);
    private MostRunsAtCourseProcessor processor = new MostRunsAtCourseProcessor();

    @Test
    public void shouldSortCount()
    {
        for (int athleteId = 0; athleteId < (MAX_RESULT_SIZE + 5); athleteId++)
        {
            builder.athlete(Athlete.from("Most EVENTS", athleteId));

            for (int runs = 0; runs < 5; runs++)
            {
                Date date = Date.from(Instant.EPOCH.plus(runs, ChronoUnit.DAYS));
                Result result = builder.date(date).build();
                processor.visitInOrder(result);
            }
            for (int runs = 0; runs < athleteId; runs++)
            {
                Date date = Date.from(Instant.now().plus(runs, ChronoUnit.DAYS));
                Result result = builder.date(date).build();
                processor.visitInOrder(result);
            }
        }
        processor.onFinishCourse();

        List<Object[]> mostEventsForCourse = processor.getMostRunsForCourse(courseId);
        Object[] topAthleteCount = mostEventsForCourse.getFirst();
        Assertions.assertThat(topAthleteCount[0]).isEqualTo(MAX_RESULT_SIZE + 5 - 1);   // athleteId
        Assertions.assertThat(topAthleteCount[1]).isEqualTo(MAX_RESULT_SIZE + 5 + 5 - 1);   // courseCount
    }

    @Test
    public void shouldSortAthleteId()
    {
        for (int athleteId = 0; athleteId < (MAX_RESULT_SIZE + 5); athleteId++)
        {
            builder.athlete(Athlete.from("Most EVENTS", athleteId));

            Date date = Date.from(Instant.EPOCH);
            Result result = builder.date(date).build();
            processor.visitInOrder(result);
        }
        processor.onFinishCourse();

        List<Object[]> mostEventsForCourse = processor.getMostRunsForCourse(courseId);
        Object[] topAthleteCount = mostEventsForCourse.getFirst();
        Assertions.assertThat(topAthleteCount[0]).isEqualTo(0);   // athleteId
        Assertions.assertThat(topAthleteCount[1]).isEqualTo(1);   // courseCount

//        mostEventsForCourse.forEach(objects -> {
//            System.out.printf("%d %d%n", (int)objects[0], (int)objects[1]);
//        });
    }
}