package dnt.parkrun.courseevent;

import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ParserTest
{
    private Course cornwall = new Course(27, "Cornwall", Country.NZ, "Cornwall", Course.Status.RUNNING);

    @Test
    public void shouldParseWithUnknowns() throws IOException
    {
        URL resource = this.getClass().getResource("/example.event.with.unknowns.html");
        Parser parser = new Parser.Builder()
                .course(cornwall)
                .forEachResult(System.out::println)
                .file(new File(resource.getFile()))
                .build();
        parser.parse();
    }

    @Test
    public void shouldParseWithHourPlusRunners() throws IOException
    {
        URL resource = this.getClass().getResource("/example.event.with.hour.plus.times.html");
        Parser parser = new Parser.Builder()
                .course(cornwall)
                .forEachResult(System.out::println)
                .file(new File(resource.getFile()))
                .build();
        parser.parse();
    }
}
