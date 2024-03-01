package dnt.parkrun.courses.reader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.CountryEnum;
import dnt.parkrun.datastructures.Course;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EventsJsonFileReader
{
    private final JsonFactory jsonFactory = new JsonFactory();
    private final Supplier<InputStream> inputStreamSupplier;
    private final Consumer<Country> countryConsumer;
    private final Consumer<Course> eventConsumer;
    private final Supplier<Course.Status> statusSupplier;
    private final Map<Integer, Country> countryCodeToCountry = new HashMap<>();

    private EventsJsonFileReader(
            Supplier<InputStream> inputStreamSupplier,
            Consumer<Country> countryConsumer,
            Consumer<Course> eventConsumer,
            Supplier<Course.Status> statusSupplier)
    {
        this.inputStreamSupplier = inputStreamSupplier;
        this.countryConsumer = countryConsumer;
        this.eventConsumer = eventConsumer;
        this.statusSupplier = statusSupplier;
    }

    public void read() throws IOException
    {
        JsonParser jsonParser = null;
        try
        {
            jsonParser = jsonFactory.createParser(inputStreamSupplier.get());

            jsonParser.nextToken();
            parseRoot(jsonParser);
        }
        finally
        {
            if (jsonParser != null)
            {
                jsonParser.close();
            }
        }
    }

    private void parseRoot(JsonParser jsonParser) throws IOException
    {
        while (jsonParser.nextToken() != JsonToken.END_OBJECT)
        {
            String fieldname = jsonParser.getCurrentName();
            if ("countries".equals(fieldname))
            {
                jsonParser.nextToken();
                parseCountries(jsonParser);
            }

            if ("events".equals(fieldname))
            {
                jsonParser.nextToken();
                parseEvents(jsonParser);
            }
        }
    }

    private void parseEvents(JsonParser jsonParser) throws IOException
    {
        while (jsonParser.nextToken() != JsonToken.END_OBJECT)
        {
            String fieldname = jsonParser.getCurrentName();
            if ("type".equals(fieldname))
            {
                jsonParser.nextToken();
            }
            if ("features".equals(fieldname))
            {
                jsonParser.nextToken();
                parseEventFeatures(jsonParser);
            }
        }
    }

    private void parseEventFeatures(JsonParser jsonParser) throws IOException
    {
        while (jsonParser.nextToken() != JsonToken.END_ARRAY)
        {
            parseEventFeature(jsonParser);
        }
    }

    private void parseEventFeature(JsonParser jsonParser) throws IOException
    {
        while (jsonParser.nextToken() != JsonToken.END_OBJECT)
        {
            String fieldname = jsonParser.getCurrentName();
            if ("geometry".equals(fieldname))
            {
                jsonParser.nextToken();
                ignoreObject(jsonParser);
            }
            if ("properties".equals(fieldname))
            {
                jsonParser.nextToken();
                parseEventFeatureProperties(jsonParser);
            }
        }
    }

    private void parseEventFeatureProperties(JsonParser jsonParser) throws IOException
    {
        final Course.Builder builder = new Course.Builder();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT)
        {
            String fieldname = jsonParser.getCurrentName();
            if ("eventname".equals(fieldname))
            {
                jsonParser.nextToken();
                builder.name(jsonParser.getText());
            }
            if ("EventLongName".equals(fieldname))
            {
                jsonParser.nextToken();
                builder.longName(jsonParser.getText());
            }
            if ("countrycode".equals(fieldname))
            {
                jsonParser.nextToken();
                int countryCode = jsonParser.getIntValue();
                builder.country(countryCodeToCountry.get(countryCode));
                builder.status(statusSupplier.get());
                eventConsumer.accept(builder.build());
            }
        }
    }

    private void parseCountries(JsonParser jsonParser) throws IOException
    {
        while (jsonParser.nextToken() != JsonToken.END_OBJECT)
        {
            String country = jsonParser.getCurrentName();
            if (isNumeric(country))
            {
                int countryId = Integer.parseInt(country);
                CountryEnum countryCode = CountryEnum.valueOf(countryId);
                parseCountryObject(jsonParser, countryCode);
            }
        }
    }

    private void parseCountryObject(JsonParser jsonParser, CountryEnum countryCode) throws IOException
    {
        while (jsonParser.nextToken() != JsonToken.END_OBJECT)
        {
            String fieldname = jsonParser.getCurrentName();
            if ("url".equals(fieldname))
            {
                jsonParser.nextToken();
                String url = jsonParser.getText();
                Country country = new Country(countryCode, url);
                countryCodeToCountry.put(country.countryEnum.getCountryCode(), country);
                countryConsumer.accept(country);
            }

            if ("bounds".equals(fieldname))
            {

                ignoreArray(jsonParser);
            }
        }
    }

    private static void ignoreObject(JsonParser jsonParser) throws IOException
    {
        while (jsonParser.nextToken() != JsonToken.END_OBJECT)
        {
        }
    }

    private static void ignoreArray(JsonParser jsonParser) throws IOException
    {
        while (jsonParser.nextToken() != JsonToken.END_ARRAY)
        {
        }
    }

    public static boolean isNumeric(String strNum)
    {
        if (strNum == null)
        {
            return false;
        }
        try
        {
            double d = Double.parseDouble(strNum);
        }
        catch (NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public static class Builder
    {
        private final Supplier<InputStream> inputStreamSupplier;
        private Consumer<Country> countryConsumer = c -> {};
        private Consumer<Course> eventConsumer = e -> {};
        private Supplier<Course.Status> statusSupplier = () -> Course.Status.RUNNING;

        public Builder(Supplier<InputStream> inputStreamSupplier)
        {
            this.inputStreamSupplier = inputStreamSupplier;
        }

        public EventsJsonFileReader build()
        {
            return new EventsJsonFileReader(
                    inputStreamSupplier,
                    countryConsumer,
                    eventConsumer,
                    statusSupplier);
        }

        public Builder forEachCountry(Consumer<Country> countryConsumer)
        {
            this.countryConsumer = countryConsumer;
            return this;
        }

        public Builder forEachCourse(Consumer<Course> eventConsumer)
        {
            this.eventConsumer = eventConsumer;
            return this;
        }

        public Builder statusSupplier(Supplier<Course.Status> statusSupplier)
        {
            this.statusSupplier = statusSupplier;
            return this;
        }
    }
}
