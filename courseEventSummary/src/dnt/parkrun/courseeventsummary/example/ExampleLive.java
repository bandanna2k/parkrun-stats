package dnt.parkrun.courseeventsummary.example;

import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;

import java.io.IOException;
import java.net.URL;

public class ExampleLive
{
    public static void main(String[] args) throws IOException
    {
        if (true)
        {
            return;
        }
        new Parser.Builder()
                .webpageProvider(
                        new WebpageProviderImpl(
                                new URL("https://www.parkrun.org.uk/bushy/results/eventhistory/")));
    }
}
