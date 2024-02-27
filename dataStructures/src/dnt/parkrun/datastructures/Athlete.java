package dnt.parkrun.datastructures;

import java.net.MalformedURLException;
import java.net.URL;

public class Athlete
{
    public static final long NO_ATHLETE_ID = Long.MIN_VALUE;

    private final String name;
    public final long athleteId;
    public final URL url;

    private Athlete(String name, URL url, long athleteId)
    {
        if(athleteId == NO_ATHLETE_ID)
        {
            this.url = url;
            this.name = null;
            this.athleteId = NO_ATHLETE_ID;
        }
        else
        {
            this.url = url;
            this.name = name;
            this.athleteId = athleteId;
        }
    }

    @Override
    public String toString()
    {
        return "Athlete{" +
                "name='" + name + '\'' +
                ", athleteId=" + athleteId +
                ", url=" + url +
                '}';
    }

    public static Athlete fromSummaryLink(String name, String link)
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
        return new Athlete(name, url, extractIdFromSummaryLink(link));
    }

    public static Athlete fromEventLink(String name, String link)
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
        return new Athlete(name, url, extractIdFromEventLink(link));
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
