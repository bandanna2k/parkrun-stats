package dnt.parkrun.common;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class UrlGenerator
{
    private final String countryBaseUrl;

    public UrlGenerator(String countryBaseUrl)
    {
        this.countryBaseUrl = countryBaseUrl;
    }

    public URL generateCourseEventSummaryUrl(String courseName)
    {
        try
        {
            return URI.create("https://" + countryBaseUrl + "/" + courseName + "/results/eventhistory/").toURL();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public URL generateCourseEventUrl(String courseName, int eventNumber)
    {
        try
        {
            return URI.create("https://" + countryBaseUrl + "/" + courseName + "/results/" + eventNumber + "/").toURL();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public URL generateAthleteEventSummaryUrl(int athleteId)
    {
        try
        {
            return URI.create("https://" + countryBaseUrl + "/" + "/parkrunner/" + athleteId + "/").toURL();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public URL generateAthleteEventUrl(String countryUrl, int athleteId)
    {
        try
        {
            return URI.create("https://" + countryBaseUrl + "/" + countryUrl + "/parkrunner/" + athleteId + "/").toURL();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /*
        https://www.parkrun.ca/shawniganhills/results/latestresults/
     */
    public URL generateCourseLatestResultsUrl(String courseName)
    {
        try
        {
            return URI.create("https://" + countryBaseUrl + "/" + courseName + "/results/latestresults/").toURL();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }


    public URL generateCourseUrl(String courseName)
    {
        try
        {
            return URI.create("https://" + countryBaseUrl + "/" + courseName + "/").toURL();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /*
        https://www.parkrun.co.nz/parkrunner/414811/all/
     */
    public URL generateAthleteUrl(int athleteId)
    {
        try
        {
            return URI.create("https://" + countryBaseUrl + "/parkrunner/" + athleteId + "/all/").toURL();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
