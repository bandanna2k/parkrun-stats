package dnt.parkrun.filewebpageprovider;

import dnt.parkrun.webpageprovider.WebpageProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;

public class FileWebpageProvider implements WebpageProvider
{
    private final File file;

    public FileWebpageProvider(File file)
    {
        this.file = file;
    }

    @Override
    public Document getDocument()
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