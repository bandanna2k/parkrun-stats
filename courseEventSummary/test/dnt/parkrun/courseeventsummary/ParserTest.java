package dnt.parkrun.courseeventsummary;

import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.filewebpageprovider.FileWebpageProvider;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ParserTest
{
    @Test
    public void shouldParse() throws IOException
    {
        List<CourseEventSummary> courseEventSummaries = new ArrayList<>();
        URL resource = this.getClass().getResource("/example.eventhistory.html");
        new Parser.Builder()
                .forEachCourseEvent(courseEventSummaries::add)
                .webpageProvider(new FileWebpageProvider(new File(resource.getFile())))
                .build()
                .parse();
        Assertions.assertThat(courseEventSummaries).isNotEmpty();
    }

    @Test
    public void shouldParseWithUnknownFirstAthlete() throws IOException
    {
        List<CourseEventSummary> courseEventSummaries = new ArrayList<>();
        URL resource = this.getClass().getResource("/example.eventhistory.with.nulls.html");
        new Parser.Builder()
                .forEachCourseEvent(courseEventSummaries::add)
                .webpageProvider(new FileWebpageProvider(new File(resource.getFile())))
                .build()
                .parse();
        Assertions.assertThat(courseEventSummaries).isNotEmpty();
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
        Assertions.assertThat(courseEventSummaries).isNotEmpty();
    }
}
