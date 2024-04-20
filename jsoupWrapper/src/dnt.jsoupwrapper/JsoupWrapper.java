package dnt.jsoupwrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class JsoupWrapper
{
    private static final Random RANDOM = new Random();

    public final boolean shouldSleep;

    public JsoupWrapper()
    {
        this(true);
    }
    public JsoupWrapper(boolean shouldSleep)
    {
        this.shouldSleep = shouldSleep;
    }

    public Document newDocument(URL url)
    {
        int counter = 2;
        Document document = null;
        while(document == null && counter > 0)
        {
            counter--;
            try
            {
                if(shouldSleep)
                {
                    int sleepTime = 3000 + RANDOM.nextInt(3000);
                    System.out.print("Sleeping for " + sleepTime + " ... ");
                    Thread.sleep(sleepTime);
                    System.out.print("Loading URL: " + url + " ");
                    document = Jsoup.parse(url, 10000);
                    System.out.println("Done");
                }
                else
                {
                    System.out.print("Loading URL: " + url + " ");
                    document = Jsoup.parse(url, 10000);
                    System.out.println("Done");
                }
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return document;
    }

    public Document newDocument(File file)
    {
        try
        {
            return Jsoup.parse(file);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
