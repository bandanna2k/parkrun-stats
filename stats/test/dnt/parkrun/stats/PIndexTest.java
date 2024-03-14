package dnt.parkrun.stats;

import dnt.parkrun.datastructures.AthleteCourseSummary;
import dnt.parkrun.datastructures.CourseRepository;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static dnt.parkrun.stats.PIndex.pIndex;
import static org.assertj.core.api.Assertions.assertThat;

public class PIndexTest
{
    @Test
    @Ignore("Needs to move")
    public void shouldCalculatePIndex() throws IOException
    {
        CourseRepository courseRepository = new CourseRepository();
        List<AthleteCourseSummary> list = new ArrayList<>();
        URL resource = this.getClass().getResource("/example.athlete.course.summary.html");
        new dnt.parkrun.athletecoursesummary.Parser.Builder()
                .file(new File(resource.getFile()))
                .forEachAthleteCourseSummary(list::add)
                .build(courseRepository)
                .parse();

        assertThat(pIndex(list)).isEqualTo(4);
    }
}
