package dnt.parkrun.courseeventsummary;

import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.filewebpageprovider.FileWebpageProvider;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class ParserTest
{
    @Test
    public void shouldParse()
    {
        List<CourseEventSummary> courseEventSummaries = new ArrayList<>();
        URL resource = this.getClass().getResource("/example.eventhistory.html");
        new Parser.Builder()
                .forEachCourseEvent(courseEventSummaries::add)
                .webpageProvider(new FileWebpageProvider(new File(resource.getFile())))
                .build()
                .parse();
        assertCourseEventSummaries(courseEventSummaries);
    }

    private void assertCourseEventSummaries(List<CourseEventSummary> courseEventSummaries)
    {
        assertThat(courseEventSummaries).isNotEmpty();
        courseEventSummaries.forEach(ces -> {
            assertThat(ces.finishers).isGreaterThan(0);
            assertTrue(ces.volunteers == 0 || ces.volunteers > 1);
        });
    }

    @Test
    public void shouldParseWithUnknownFirstAthlete()
    {
        List<CourseEventSummary> courseEventSummaries = new ArrayList<>();
        URL resource = this.getClass().getResource("/example.eventhistory.with.nulls.html");
        new Parser.Builder()
                .forEachCourseEvent(courseEventSummaries::add)
                .webpageProvider(new FileWebpageProvider(new File(resource.getFile())))
                .build()
                .parse();
        assertCourseEventSummaries(courseEventSummaries);
    }

    @Test
    public void shouldParseWithNoFirstAthlete() throws IOException
    {
        List<CourseEventSummary> courseEventSummaries = new ArrayList<>();
        URL resource = this.getClass().getResource("/example.event.history.with.no.1st.place.html");
        new Parser.Builder()
                .forEachCourseEvent(courseEventSummaries::add)
                .webpageProvider(new FileWebpageProvider(new File(resource.getFile())))
                .build()
                .parse();
        assertCourseEventSummaries(courseEventSummaries);
    }
}
