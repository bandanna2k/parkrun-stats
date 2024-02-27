package dnt.parkrun.datastructures;

import java.net.MalformedURLException;
import java.net.URL;

public class Athlete
{
    public static final long NO_ATHLETE_ID = Long.MIN_VALUE;

    public final URL url;
    public final long athleteId;

    private Athlete(URL url, long athleteId)
    {
        this.url = url;
        this.athleteId = athleteId;
    }

    @Override
    public String toString()
    {
        return "AthleteId{" +
                "athleteId=" + athleteId +
                '}';
    }

    public static Athlete fromSummaryLink(String link)
    {
        URL url = null;
        try
        {
            url = new URL(link);
        }
        catch (MalformedURLException e)
        {
            // Do nothing at present
        }
        return new Athlete(url, extractIdFromSummaryLink(link));
    }

    public static Athlete fromEventLink(String link)
    {
        URL url = null;
        try
        {
            url = new URL(link);
        }
        catch (MalformedURLException e)
        {
            // Do nothing at present
        }
        return new Athlete(url, extractIdFromEventLink(link));
    }

    static long extractIdFromSummaryLink(String href)
    {
        if(href == null)
        {
            return NO_ATHLETE_ID;
        }
        int lastIndexOf = href.lastIndexOf("=");
        if (lastIndexOf >= 0)
        {
            try
            {
                return Long.parseLong(href.substring(lastIndexOf + 1));
            }
            catch (NumberFormatException ex)
            {
                return NO_ATHLETE_ID;
            }
        }
        return NO_ATHLETE_ID;
    }

    static long extractIdFromEventLink(String href)
    {
        if(href == null)
        {
            return NO_ATHLETE_ID;
        }
        int lastIndexOf = href.lastIndexOf("/");
        if (lastIndexOf >= 0)
        {
            try
            {
                return Long.parseLong(href.substring(lastIndexOf + 1));
            }
            catch (NumberFormatException ex)
            {
                return NO_ATHLETE_ID;
            }
        }
        return NO_ATHLETE_ID;
    }
}
