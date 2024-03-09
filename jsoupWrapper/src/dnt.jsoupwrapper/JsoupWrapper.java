package dnt.jsoupwrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

public abstract class JsoupWrapper
{
    private static final Random RANDOM = new Random();

    public static Document newDocument(URL url)
    {
        int counter = 2;
        Document document = null;
        while(document == null && counter > 0)
        {
            counter--;
            try
            {
                int sleepTime = 3000 + RANDOM.nextInt(3000);
                System.out.print("Sleeping for " + sleepTime + " ... ");
                Thread.sleep(sleepTime);
                System.out.println("Loading URL");
                document = Jsoup.parse(url, 5000);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return document;
    }

    public static Document newDocument(File file)
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