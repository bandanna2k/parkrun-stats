package dnt.parkrun.datastructures;

import org.junit.Test;

import static dnt.parkrun.datastructures.Course.extractCourseNameFromAthleteAtCourseLink;
import static org.assertj.core.api.Assertions.assertThat;

public class CourseTest
{
    @Test
    public void shouldExtractCourseNameFromLink()
    {
        shouldExtractCourseNameFromLink(
                "https://www.parkrun.us/colermountainbikepreserve/parkrunner/9265263", "colermountainbikepreserve");
        shouldExtractCourseNameFromLink(
                "https://www.parkrun.co.nz/cornwall/", "cornwall");
    }
    private void shouldExtractCourseNameFromLink(String actual, String expected)
    {
        assertThat(extractCourseNameFromAthleteAtCourseLink(actual)).isEqualTo(expected);
    }
}
