package dnt.parkrun;

import dnt.parkrun.courses.Country;
import dnt.parkrun.courses.CountryEnum;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class CountryTest
{
    private final Supplier<InputStream> inputStreamSupplier = () ->
            EventsJsonFileReader.class.getResourceAsStream("/events.json");

    private Map<Integer, Country> countryCodeToCountry = new HashMap<>();

    private EventsJsonFileReader reader = new EventsJsonFileReader.Builder(inputStreamSupplier)
            .forEachCountry(country -> countryCodeToCountry.put(country.countryCode, country))
            .build();

    @Before
    public void setUp() throws Exception
    {
        reader.read();
    }

    @Test
    public void allCountryCodesShouldHaveAnEnum()
    {
        countryCodeToCountry.values().forEach(country -> {
            int countryCode = country.countryCode;
            assertNotNull(
                    country + " does not have an enum.",
                    CountryEnum.valueOf(countryCode));
        });
    }

    @Test
    public void allCountryEnumsShouldHaveARecord()
    {
        for (CountryEnum value : CountryEnum.values())
        {
            assertNotNull(
                    value + " does not have an input entry.",
                    countryCodeToCountry.get(value.getCountryCode())
            );
        }
    }
}
