package dnt.parkrun.common;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class UrlGenerator
{
    public static URL generateCourseEventSummaryUrl(String countryBaseUrl, String courseName)
    {
        try
        {
            return new URL("https://" + countryBaseUrl + "/" + courseName + "/results/eventhistory/");
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static URL generateCourseEventUrl(String countryUrl, String courseName, int eventNumber)
    {
        try
        {
            return new URL("https://" + countryUrl + "/" + courseName + "/results/" + eventNumber + "/");
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static URL generateAthleteEventSummaryUrl(String countryBaseUrl, int athleteId)
    {
        try
        {
            return new URL("https://" + countryBaseUrl + "/" + "/parkrunner/" + athleteId + "/");
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static URL generateAthleteEventUrl(String countryBaseUrl, String countryUrl, int athleteId)
    {
        try
        {
            return new URL("https://" + countryBaseUrl + "/" + countryUrl + "/parkrunner/" + athleteId + "/");
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /*
        https://www.parkrun.ca/shawniganhills/results/latestresults/
     */
    public static URL generateCourseLatestResultsUrl(String countryBaseUrl, String courseName)
    {
        try
        {
            return new URL("https://" + countryBaseUrl + "/" + courseName + "/results/latestresults/");
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }


    public static URL generateCourseUrl(String countryBaseUrl, String courseName)
    {
        try
        {
            return new URL("https://" + countryBaseUrl + "/" + courseName + "/");
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /*
        https://www.parkrun.co.nz/parkrunner/414811/all/
     */
    public static URL generateAthleteUrl(String countryBaseUrl, int athleteId)
    {
        try
        {
            return new URL("https://" + countryBaseUrl + "/parkrunner/" + athleteId + "/all/");
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
