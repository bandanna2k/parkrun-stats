package dnt.parkrun.courses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.datastructures.Country;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class UnknownCoursesTest
{
    @Test
    public void prettyPrintUnknownCourses() throws IOException
    {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        InputStream inputStream = EventsJsonFileReader.class.getResourceAsStream("/unknown.courses.1.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        Map<String, Country> baseUrlToCountry = new HashMap<>();
        Arrays.stream(Country.values()).forEach(country -> baseUrlToCountry.put(country.baseUrl, country));

        List<Feature> features = new ArrayList<>();
        String line = null;
        while((line = reader.readLine()) != null)
        {
            // WARNING Course not found: Inverness parkrun (https://www.parkrun.org.uk/inverness/parkrunner/59830)

            int indexOfColon = line.indexOf(":");
            int indexOfOpenBracket = line.indexOf("(");
            String courseLongName = line.substring(indexOfColon + 1, indexOfOpenBracket).trim();

            int indexOfParkrun = line.indexOf("parkrun", indexOfOpenBracket);
            int indexOfSlash = line.indexOf("/", indexOfParkrun);
            String baseUrl = line.substring(indexOfParkrun, indexOfSlash);
            Country country = baseUrlToCountry.get(baseUrl);
            assert country != null : "Unknown country: " + baseUrl;

            int indexOfCourseNameEndSlash = line.indexOf("/", indexOfSlash + 1);
            String courseName = line.substring(indexOfSlash + 1, indexOfCourseNameEndSlash);

            Feature feature = new Feature();
            feature.properties = new Properties(courseName, courseLongName, country.countryCode);
            //System.out.println(gson.toJson(feature));
            features.add(feature);
        }
        System.out.println(gson.toJson(features));
    }

    public static class Feature
    {
        public int id = 0;
        public String type = "Feature";
        public Geometry geometry = new Geometry();
        public Properties properties;
    }
    private static class Geometry
    {
        String type = "Point";
    }
    public static class Properties
    {
        public final String eventname;
        public final String EventLongName;
        public final int countrycode;

        public Properties(String eventname, String EventLongName, int countrycode)
        {
            this.eventname = eventname;
            this.EventLongName = EventLongName;
            this.countrycode = countrycode;
        }
    }

    /*

          {
        "id": 1,
        "type": "Feature",
        "geometry": {
          "type": "Point",
          "coordinates": [
            -0.335791,
            51.410992
          ]
        },
        "properties": {
          "eventname": "bushy",
          "EventLongName": "Bushy parkrun",
          "EventShortName": "Bushy Park",
          "LocalisedEventLongName": null,
          "countrycode": 97,
          "seriesid": 1,
          "EventLocation": "Bushy Park, Teddington"
        }
      },


     */


}
