package dnt.parkrun.courses.reader;

import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static dnt.parkrun.datastructures.CountryEnum.NZ;
import static org.assertj.core.api.Assertions.assertThat;

public class EventsJsonFileReaderTest
{
    private final Supplier<InputStream> inputStreamSupplier = () ->
            EventsJsonFileReader.class.getResourceAsStream("/events.json");

    private List<Country> countryList = new ArrayList<>();
    private List<Course> eventList = new ArrayList<>();

    private EventsJsonFileReader reader = new EventsJsonFileReader.Builder(inputStreamSupplier)
            .forEachCountry(country -> countryList.add(country))
            .forEachCourse(course -> eventList.add(course))
            .build();

    @Before
    public void setUp() throws Exception
    {
        reader.read();
    }

    @Test
    public void shouldReadCountries()
    {
        assertThat(countryList.size()).isEqualTo(21);
    }

    @Test
    public void shouldReadEvents()
    {
        assertThat(eventList.size()).isEqualTo(2496);
    }

    @Test
    public void shouldReadNzEvents()
    {
        assertThat(eventList.stream()
                .filter(course -> course.country.countryEnum == NZ)
                .count()).isEqualTo(43);
    }
}
