package dnt.parkrun.courseeventsummary;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ParserTest
{
    @Test
    public void shouldParse() throws IOException
    {
        URL resource = this.getClass().getResource("/example.eventhistory.html");
        new Parser.Builder()
                .forEachCourseEvent(System.out::println)
                .file(new File(resource.getFile()))
                .build()
                .parse();
    }

    @Test
    public void shouldParseWithNullFirstAthlete() throws IOException
    {
        URL resource = this.getClass().getResource("/example.eventhistory.with.nulls.html");
        new Parser.Builder()
                .forEachCourseEvent(System.out::println)
                .file(new File(resource.getFile()))
                .build()
                .parse();
    }
}
