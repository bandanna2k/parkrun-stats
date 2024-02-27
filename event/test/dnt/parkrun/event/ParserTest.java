package dnt.parkrun.event;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ParserTest
{
    @Test
    public void shouldParseWithUnknowns() throws IOException
    {
        URL resource = this.getClass().getResource("/example.event.with.unknowns.html");
        File file = new File(resource.getFile());
        new Parser(System.out::println).parse(file);
    }

    @Test
    public void shouldParseWithHourPlusRunners() throws IOException
    {
        URL resource = this.getClass().getResource("/example.event.with.hour.plus.times.html");
        File file = new File(resource.getFile());
        new Parser(System.out::println).parse(file);
    }
}
