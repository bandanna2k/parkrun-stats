package dnt.jsoupwrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public abstract class JsoupWrapper
{
    public static Document newDocument(URL url)
    {
        int counter = 2;
        Document document = null;
        while(document == null && counter > 0)
        {
            counter--;
            try
            {
                System.out.print("Sleeping... ");
                Thread.sleep(3000);
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
