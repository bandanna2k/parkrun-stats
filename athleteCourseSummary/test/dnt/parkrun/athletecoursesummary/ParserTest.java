package dnt.parkrun.athletecoursesummary;


import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.filewebpageprovider.FileWebpageProvider;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static dnt.parkrun.datastructures.AgeCategory.VM45_49;
import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest
{
    @Test
    public void shouldParse() throws IOException
    {
        AtomicInteger counter = new AtomicInteger(0);

        Map<String, Integer> volunteerTypeToCount = new HashMap<>();
        CourseRepository courseRepository = new CourseRepository();
        URL resource = this.getClass().getResource("/example.athlete.course.summary.html");
        Parser parser = new Parser.Builder()
                .webpageProvider(new FileWebpageProvider(new File(resource.getFile())))
                .forEachVolunteerRecord(record ->
                {
                    String volunteerType = (String) record[1];
                    int volunteerCount = (int) record[2];
                    volunteerTypeToCount.put(volunteerType, volunteerCount);
                })
                .forEachAthleteCourseSummary(x ->
                {
                    counter.addAndGet(x.countOfRuns);
                    System.out.println(x);
                })
                .build(courseRepository);
        parser.parse();
        System.out.println("Total:" + counter.get());
        assertThat(volunteerTypeToCount.get("Marshal")).isEqualTo(9);
        assertThat(volunteerTypeToCount.get("Total Credits")).isEqualTo(21);
        assertThat(parser.getAthlete().name).isEqualTo("David NORTH");
        assertThat(parser.getAgeCategory()).isEqualTo(VM45_49);
    }
}