package dnt.parkrun.stats;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.Course;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static dnt.parkrun.datastructures.Country.NZ;

public class RegionnaireCountTest
{
    private List<Course> courses = getCourses();

    private List<Course> getCourses()
    {
        List<Course> result = new ArrayList<>();
        for (int i = 1; i <= 20; i++)
        {
            String name = "Course" + i;
            result.add(new Course(i, name, NZ, name, Course.Status.RUNNING));
        }
        return result;
    }

    @Test
    public void shouldCalculateRegionnaireCount()
    {
        List<Stats.CourseDate> sortedStartDates = new ArrayList<>(List.of(
                new Stats.CourseDate(courses.get(0), DateConverter.parseWebsiteDate("01/01/2024")),
                new Stats.CourseDate(courses.get(1), DateConverter.parseWebsiteDate("15/01/2024")),
                new Stats.CourseDate(courses.get(1), DateConverter.parseWebsiteDate("01/02/2024")),
                new Stats.CourseDate(courses.get(1), DateConverter.parseWebsiteDate("15/02/2024"))
        ));
        List<Stats.CourseDate> sortedFirstRuns = new ArrayList<>(List.of(
                new Stats.CourseDate(courses.get(0), DateConverter.parseWebsiteDate("01/03/2024")),
                new Stats.CourseDate(courses.get(0), DateConverter.parseWebsiteDate("08/03/2024")),
                new Stats.CourseDate(courses.get(0), DateConverter.parseWebsiteDate("15/03/2024")),
                new Stats.CourseDate(courses.get(0), DateConverter.parseWebsiteDate("22/03/2024"))
        ));

        Assertions.assertThat(Stats.getRegionnaireCount(sortedStartDates, sortedFirstRuns)).isEqualTo(1);
    }
}
