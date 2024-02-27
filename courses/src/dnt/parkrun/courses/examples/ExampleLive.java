package dnt.parkrun.courses.examples;

import dnt.parkrun.courses.reader.EventsJsonFileReader;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

public class ExampleLive
{
    public static void main(String[] args) throws IOException
    {
        AtomicInteger counter = new AtomicInteger(0);
        EventsJsonFileReader reader = new EventsJsonFileReader.Builder(() ->
        {
            URL oracle = null;
            try
            {
                oracle = new URL("https://images.parkrun.com/events.json");
                return oracle.openStream();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        })
                .forEachCountry(country -> System.out.println("Country:" + country))
                .forEachCourse(event ->
                {
                    System.out.println("Event:" + event);
                    counter.incrementAndGet();
                })
                .build();
        reader.read();
        System.out.println("Event count: " + counter.get());
    }
}
