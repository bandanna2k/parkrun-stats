package dnt.parkrun.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class UrlGenerator
{
    private final Random random = new Random();
    private final int fixedSleep;
    private final int randomSleep;

    public UrlGenerator(int fixedSleep, int randomSleep)
    {
        this.fixedSleep = fixedSleep;
        this.randomSleep = randomSleep;
    }

    public UrlGenerator()
    {
        this(3000, 1000);
    }

    public URL generateCourseEventSummaryUrl(String countryBaseUrl, String courseName)
    {
        try
        {
            sleep();
            return new URL("https://" + countryBaseUrl + "/" + courseName + "/results/eventhistory/");
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void sleep()
    {
        try
        {
            Thread.sleep(fixedSleep + random.nextInt(randomSleep));
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public URL generateCourseEventUrl(String countryUrl, String courseName, int eventNumber)
    {
        sleep();
        try
        {
            return new URL("https://" + countryUrl + "/" + courseName + "/results/" + eventNumber + "/");
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public URL generateAthleteEventSummaryUrl(String countryBaseUrl, int athleteId)
    {
        sleep();
        try
        {
            return new URL("https://" + countryBaseUrl + "/" + "/parkrunner/" + athleteId + "/");
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public URL generateAthleteEventUrl(String countryBaseUrl, String countryUrl, int athleteId)
    {
        sleep();
        try
        {
            return new URL("https://" + countryBaseUrl + "/" + countryUrl + "/parkrunner/" + athleteId + "/");
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
