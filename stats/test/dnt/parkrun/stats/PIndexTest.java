package dnt.parkrun.stats;

import dnt.parkrun.datastructures.AthleteCourseSummary;
import dnt.parkrun.datastructures.CourseRepository;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dnt.parkrun.stats.PIndex.pIndex;
import static org.assertj.core.api.Assertions.assertThat;

public class PIndexTest
{
    /*
                // 4, 4, 4          pIndex 3, need 4
                // 2, 2, 4, 4, 4    pIndex 3, need 1
                // 20               pIndex 1, need 2
                // 1, 1, 1, 1       pIndex 1, need 1
                // 2, 2, 2, 2       pIndex 2, need 3 to get to P3
                // 5, 5, 5, 5       pIndex 4, need 1 to get to P6
                // 100, 4, 4, 3     pIndex 3, need 1
                // 4, 4, 4, 4       pIndex 4, need 9
     */

    @Test
    public void testPIndex()
    {
        testPIndex(3,1,  2, 2, 4, 4, 4);
        testPIndex(1, 2, 20);
        testPIndex(0, 1);
        testPIndex(1,  1, 1, 1, 1, 1);
        testPIndex(2,  3, 2, 2, 2, 2);
        testPIndex(4,  1, 5, 5, 5, 5);
        testPIndex(3,  1, 100, 4, 4, 3);
        testPIndex(4,  9, 4, 4, 4, 4);
    }
    private void testPIndex(int expectedPIndex, int expectedNextMax, int ... countOfRuns)
    {
        List<AthleteCourseSummary> summaries = Arrays.stream(countOfRuns)
                .mapToObj(n -> new AthleteCourseSummary(null, null, n))
                .collect(Collectors.toList());
        Assertions.assertThat(PIndex.pIndexNextMax(summaries)).isEqualTo(new Point(expectedPIndex, expectedNextMax));
    }

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
