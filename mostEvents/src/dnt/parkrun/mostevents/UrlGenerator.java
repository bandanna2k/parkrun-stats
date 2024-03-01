package dnt.parkrun.mostevents;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class UrlGenerator
{
    private final Random random = new Random();

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
            Thread.sleep(3000 + random.nextInt(1000));
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
}
