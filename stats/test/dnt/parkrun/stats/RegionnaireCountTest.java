package dnt.parkrun.stats;

import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseDate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dnt.parkrun.common.DateConverter.parseWebsiteDate;
import static dnt.parkrun.datastructures.Country.NZ;
import static dnt.parkrun.stats.MostEventStats.getRegionnaireCount;
import static org.assertj.core.api.Assertions.assertThat;

public class RegionnaireCountTest
{
    private final List<Course> courses = getCourses();

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
        List<CourseDate> startDates = List.of(
                new CourseDate(courses.get(0), parseWebsiteDate("01/01/2024")),
                new CourseDate(courses.get(1), parseWebsiteDate("15/01/2024")),
                new CourseDate(courses.get(2), parseWebsiteDate("01/02/2024")),
                new CourseDate(courses.get(3), parseWebsiteDate("15/02/2024"))
        );
        List<CourseDate> firstRuns = List.of(
                new CourseDate(courses.get(0), parseWebsiteDate("01/03/2024")),
                new CourseDate(courses.get(1), parseWebsiteDate("08/03/2024")),
                new CourseDate(courses.get(2), parseWebsiteDate("15/03/2024")),
                new CourseDate(courses.get(3), parseWebsiteDate("22/03/2024"))
        );
        assertThat(getRegionnaireCount(startDates, Collections.emptyList(), firstRuns)).isEqualTo(1);
    }

    @Test
    public void shouldCalculateRegionnaireCountWithNoRuns()
    {
        List<CourseDate> sortedStartDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), parseWebsiteDate("01/01/2024")),
                new CourseDate(courses.get(1), parseWebsiteDate("15/01/2024")),
                new CourseDate(courses.get(2), parseWebsiteDate("01/02/2024")),
                new CourseDate(courses.get(3), parseWebsiteDate("15/02/2024"))
        ));
        List<CourseDate> sortedFirstRuns = new ArrayList<>();

        assertThat(getRegionnaireCount(sortedStartDates, Collections.emptyList(), sortedFirstRuns)).isEqualTo(0);
    }

    @Test
    public void shouldCalculateMaxRegionnaireCount()
    {
        List<CourseDate> sortedStartDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), parseWebsiteDate("01/01/2024")),
                new CourseDate(courses.get(1), parseWebsiteDate("15/01/2024")),
                new CourseDate(courses.get(2), parseWebsiteDate("01/02/2024")),
                new CourseDate(courses.get(3), parseWebsiteDate("15/02/2024"))
        ));
        List<CourseDate> sortedFirstRuns = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), parseWebsiteDate("08/01/2024")),
                new CourseDate(courses.get(1), parseWebsiteDate("22/01/2024")),
                new CourseDate(courses.get(2), parseWebsiteDate("08/02/2024")),
                new CourseDate(courses.get(3), parseWebsiteDate("22/02/2024"))
        ));

        assertThat(getRegionnaireCount(sortedStartDates, Collections.emptyList(), sortedFirstRuns)).isEqualTo(4);
    }

    @Test
    public void shouldCalculateMaxRegionnaireCountWithStoppedRun()
    {
        List<CourseDate> sortedStartDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), parseWebsiteDate("01/01/2024")),
                new CourseDate(courses.get(1), parseWebsiteDate("15/01/2024")),
                new CourseDate(courses.get(2), parseWebsiteDate("01/02/2024")),
                new CourseDate(courses.get(3), parseWebsiteDate("15/02/2024"))
        ));
        List<CourseDate> sortedStopDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), parseWebsiteDate("01/02/2024"))
        ));
        List<CourseDate> sortedFirstRuns = new ArrayList<>(List.of(
                new CourseDate(courses.get(1), parseWebsiteDate("08/03/2024")),
                new CourseDate(courses.get(2), parseWebsiteDate("15/03/2024")),
                new CourseDate(courses.get(3), parseWebsiteDate("22/03/2024"))
        ));

        assertThat(getRegionnaireCount(sortedStartDates, sortedStopDates, sortedFirstRuns)).isEqualTo(1);
    }

    @Test
    public void shouldCalculateMaxRegionnaireCountWithStoppedRun2()
    {
        int A = 0;
        int B = 1;
        int C = 2;
        int D = 3;
        List<CourseDate> sortedStartDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(A), parseWebsiteDate("01/01/2024")),
                new CourseDate(courses.get(B), parseWebsiteDate("15/01/2024")),
                new CourseDate(courses.get(C), parseWebsiteDate("01/02/2024")),
                new CourseDate(courses.get(D), parseWebsiteDate("15/02/2024"))
        ));
        List<CourseDate> sortedStopDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(A), parseWebsiteDate("01/02/2024"))
        ));
        List<CourseDate> sortedFirstRuns = new ArrayList<>(List.of(
                new CourseDate(courses.get(A), parseWebsiteDate("15/01/2024")), // A,B courses running, A run at. Not regionnaire
                new CourseDate(courses.get(B), parseWebsiteDate("01/02/2024"))  // B,C course running, A,B run at. Not regionnaire
        ));

        assertThat(getRegionnaireCount(sortedStartDates, sortedStopDates, sortedFirstRuns)).isEqualTo(0);
    }

    @Test
    public void shouldCalculateMaxRegionnaireCountWithStoppedRun3()
    {
        List<CourseDate> sortedStartDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), parseWebsiteDate("01/01/2024")),
                new CourseDate(courses.get(1), parseWebsiteDate("15/01/2024")),
                new CourseDate(courses.get(2), parseWebsiteDate("01/02/2024")),
                new CourseDate(courses.get(3), parseWebsiteDate("15/02/2024"))
        ));
        List<CourseDate> sortedStopDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), parseWebsiteDate("01/02/2024"))
        ));
        List<CourseDate> sortedFirstRuns = new ArrayList<>(List.of(
                new CourseDate(courses.get(0), parseWebsiteDate("15/01/2024")), //            0+1 started, 0 done. Not regionnaire
                new CourseDate(courses.get(1), parseWebsiteDate("01/02/2024")), // 0 stopped, 1+2 started, 0+1 done. Not regionnaire
                new CourseDate(courses.get(2), parseWebsiteDate("15/02/2024")), // 0 stopped, 1+2+3 started, 0+1+2 done. Not regionnaire
                new CourseDate(courses.get(3), parseWebsiteDate("01/03/2024"))  // 0 stopped, 1+2+3 started, 0+1+2+3 done. Regionnaire
        ));

        assertThat(getRegionnaireCount(sortedStartDates, sortedStopDates, sortedFirstRuns)).isEqualTo(1);
    }

    @Test
    public void shouldCalculateMaxRegionnaireCountWithStoppedRun4a()
    {
        int A = 0;
        int B = 1;
        int C = 2;
        int D = 3;
        List<CourseDate> sortedStartDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(A), parseWebsiteDate("01/01/2024")),
                new CourseDate(courses.get(B), parseWebsiteDate("15/01/2024")),
                new CourseDate(courses.get(C), parseWebsiteDate("01/02/2024")),
                new CourseDate(courses.get(D), parseWebsiteDate("15/02/2024"))
        ));
        List<CourseDate> sortedStopDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(D), parseWebsiteDate("01/04/2024")) // A,B,C courses running, A,B,C run at.        Regionnaire
        ));
        List<CourseDate> sortedFirstRuns = new ArrayList<>(List.of(
                new CourseDate(courses.get(A), parseWebsiteDate("01/03/2024")), // A,B,C,D courses running, A run at.     Not regionnaire
                new CourseDate(courses.get(B), parseWebsiteDate("08/03/2024")), // A,B,C,D courses running, A,B run at.   Not regionnaire
                new CourseDate(courses.get(C), parseWebsiteDate("15/03/2024"))  // A,B,C,D courses running, A,B,C run at. Not regionnaire
        ));

        assertThat(getRegionnaireCount(sortedStartDates, sortedStopDates, sortedFirstRuns)).isEqualTo(1);
    }
    @Test
    public void shouldCalculateMaxRegionnaireCountWithStoppedRun4b()
    {
        int A = 0;
        int B = 1;
        int C = 2;
        int D = 3;
        List<CourseDate> sortedStartDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(A), parseWebsiteDate("01/01/2024")),
                new CourseDate(courses.get(B), parseWebsiteDate("15/01/2024")),
                new CourseDate(courses.get(C), parseWebsiteDate("01/02/2024")),
                new CourseDate(courses.get(D), parseWebsiteDate("15/02/2024"))
        ));
        List<CourseDate> sortedStopDates = new ArrayList<>(List.of(
                new CourseDate(courses.get(B), parseWebsiteDate("01/04/2024")) // A,C,D courses running, A,B,C run at.    Not Regionnaire
        ));
        List<CourseDate> sortedFirstRuns = new ArrayList<>(List.of(
                new CourseDate(courses.get(A), parseWebsiteDate("01/03/2024")), // A,B,C,D courses running, A run at.     Not regionnaire
                new CourseDate(courses.get(B), parseWebsiteDate("08/03/2024")), // A,B,C,D courses running, A,B run at.   Not regionnaire
                new CourseDate(courses.get(C), parseWebsiteDate("15/03/2024"))  // A,B,C,D courses running, A,B,C run at. Not regionnaire
        ));

        assertThat(getRegionnaireCount(sortedStartDates, sortedStopDates, sortedFirstRuns)).isEqualTo(0);
    }
}
