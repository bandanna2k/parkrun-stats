package dnt.parkrun.common;

import java.net.MalformedURLException;
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
            return new URL("https://" + countryBaseUrl + "/" + courseName + "/results/eventhistory/");
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
            return new URL("https://" + countryBaseUrl + "/" + courseName + "/results/" + eventNumber + "/");
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
            return new URL("https://" + countryBaseUrl + "/" + "/parkrunner/" + athleteId + "/");
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
    public URL generateCourseLatestResultsUrl(String courseName)
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


    public URL generateCourseUrl(String courseName)
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
    public URL generateAthleteUrl(int athleteId)
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
