package dnt.parkrun.stats;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseDate;
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
        List<CourseDate> sortedStartDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), DateConverter.parseWebsiteDate("01/01/2024")),
                new CourseDate(courses.get(1), DateConverter.parseWebsiteDate("15/01/2024")),
                new CourseDate(courses.get(1), DateConverter.parseWebsiteDate("01/02/2024")),
                new CourseDate(courses.get(1), DateConverter.parseWebsiteDate("15/02/2024"))
        ));
        List<CourseDate> sortedFirstRuns = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), DateConverter.parseWebsiteDate("01/03/2024")),
                new CourseDate(courses.get(1), DateConverter.parseWebsiteDate("08/03/2024")),
                new CourseDate(courses.get(2), DateConverter.parseWebsiteDate("15/03/2024")),
                new CourseDate(courses.get(3), DateConverter.parseWebsiteDate("22/03/2024"))
        ));

        Assertions.assertThat(Stats.getRegionnaireCount(sortedStartDates, sortedFirstRuns)).isEqualTo(1);
    }


    @Test
    public void shouldCalculateRegionnaireCountWithNoRuns()
    {
        List<CourseDate> sortedStartDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), DateConverter.parseWebsiteDate("01/01/2024")),
                new CourseDate(courses.get(1), DateConverter.parseWebsiteDate("15/01/2024")),
                new CourseDate(courses.get(2), DateConverter.parseWebsiteDate("01/02/2024")),
                new CourseDate(courses.get(3), DateConverter.parseWebsiteDate("15/02/2024"))
        ));
        List<CourseDate> sortedFirstRuns = new ArrayList<>();

        Assertions.assertThat(Stats.getRegionnaireCount(sortedStartDates, sortedFirstRuns)).isEqualTo(0);
    }

    @Test
    public void shouldCalculateMaxRegionnaireCount()
    {
        List<CourseDate> sortedStartDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), DateConverter.parseWebsiteDate("01/01/2024")),
                new CourseDate(courses.get(1), DateConverter.parseWebsiteDate("15/01/2024")),
                new CourseDate(courses.get(2), DateConverter.parseWebsiteDate("01/02/2024")),
                new CourseDate(courses.get(3), DateConverter.parseWebsiteDate("15/02/2024"))
        ));
        List<CourseDate> sortedFirstRuns = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), DateConverter.parseWebsiteDate("08/01/2024")),
                new CourseDate(courses.get(1), DateConverter.parseWebsiteDate("22/01/2024")),
                new CourseDate(courses.get(2), DateConverter.parseWebsiteDate("08/02/2024")),
                new CourseDate(courses.get(3), DateConverter.parseWebsiteDate("22/02/2024"))
        ));

        Assertions.assertThat(Stats.getRegionnaireCount(sortedStartDates, sortedFirstRuns)).isEqualTo(4);
    }

    @Test
    public void shouldCalculateMaxRegionnaireCountWithStoppedRun()
    {
        List<CourseDate> sortedStartDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), DateConverter.parseWebsiteDate("01/01/2024")),
                new CourseDate(courses.get(1), DateConverter.parseWebsiteDate("15/01/2024")),
                new CourseDate(courses.get(2), DateConverter.parseWebsiteDate("01/02/2024")),
                new CourseDate(courses.get(3), DateConverter.parseWebsiteDate("15/02/2024"))
        ));
        List<CourseDate> sortedStopDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), DateConverter.parseWebsiteDate("01/02/2024"))
        ));
        List<CourseDate> sortedFirstRuns = new ArrayList<>(List.of(
                new CourseDate(courses.get(1), DateConverter.parseWebsiteDate("08/03/2024")),
                new CourseDate(courses.get(2), DateConverter.parseWebsiteDate("15/03/2024")),
                new CourseDate(courses.get(3), DateConverter.parseWebsiteDate("22/03/2024"))
        ));

        Assertions.assertThat(Stats.getRegionnaireCount(sortedStartDates, sortedFirstRuns)).isEqualTo(1);
    }
}
