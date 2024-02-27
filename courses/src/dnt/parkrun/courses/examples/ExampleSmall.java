package dnt.parkrun.courses.examples;

import dnt.parkrun.courses.reader.EventsJsonFileReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

public class ExampleSmall
{
    public static void main(String[] args) throws IOException
    {
        Supplier<InputStream> supplier = () -> EventsJsonFileReader.class.getResourceAsStream("/events.small.json");
        EventsJsonFileReader reader = new EventsJsonFileReader.Builder(supplier)
                .forEachCountry(country -> System.out.println("Country:" + country))
                .forEachEvent(event -> System.out.println("Event:" + event))
                .build();
        reader.read();
    }
}
