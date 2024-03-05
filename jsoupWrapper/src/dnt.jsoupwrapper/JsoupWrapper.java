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
        int counter = 1;
        Document document = null;
        while(document == null && counter > 0)
        {
            counter--;
            try
            {
                document = Jsoup.parse(url, 5000);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
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
