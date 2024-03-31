package dnt.parkrun.athletecoursesummary;


import dnt.parkrun.datastructures.CourseRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ParserTest
{
    @Test
    public void shouldParse() throws IOException
    {
        AtomicInteger counter = new AtomicInteger(0);

        Map<String, Integer> volunteerTypeToCount = new HashMap<>();
        CourseRepository courseRepository = new CourseRepository();
        URL resource = this.getClass().getResource("/example.athlete.course.summary.html");
        new dnt.parkrun.athletecoursesummary.Parser.Builder()
                .file(new File(resource.getFile()))
                .forEachVolunteerRecord(record -> volunteerTypeToCount.put((String)record[1], (int)record[2]))
                .forEachAthleteCourseSummary(x ->
                {
                    counter.addAndGet(x.countOfRuns);
                    System.out.println(x);
                })
                .build(courseRepository)
                .parse();
        System.out.println("Total:" + counter.get());
        Assertions.assertThat(volunteerTypeToCount.get("Marshal")).isEqualTo(9);
        Assertions.assertThat(volunteerTypeToCount.get("Total Credits")).isEqualTo(21);
    }
}