package dnt.parkrun.webpageprovider;

import dnt.jsoupwrapper.JsoupWrapper;
import org.jsoup.nodes.Document;

import java.net.URL;

public class WebpageProviderImpl implements WebpageProvider
{
    private final URL url;
    private final JsoupWrapper jsoupWrapper;

    public WebpageProviderImpl(URL url)
    {
        this.url = url;
        this.jsoupWrapper = new JsoupWrapper();
    }

    @Override
    public Document getDocument()
    {
        return jsoupWrapper.newDocument(url);
    }
}