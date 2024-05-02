package dnt.parkrun.webpageprovider;

import dnt.jsoupwrapper.JsoupWrapper;
import org.jsoup.nodes.Document;

import java.net.Proxy;
import java.net.URL;
import java.util.function.Supplier;

public class WebpageProviderImpl implements WebpageProvider
{
    private final URL url;
    private final JsoupWrapper jsoupWrapper;

    public WebpageProviderImpl(URL url)
    {
        this.url = url;
        this.jsoupWrapper = new JsoupWrapper.Builder().build();
    }

    public WebpageProviderImpl(URL url, Supplier<Proxy> proxyFactory, boolean shouldSleep)
    {
        this.url = url;
        this.jsoupWrapper = new JsoupWrapper.Builder()
                .shouldSleep(shouldSleep)
                .proxyFactory(proxyFactory)
                .build();
    }

    @Override
    public Document getDocument()
    {
        return jsoupWrapper.newDocument(url);
    }
}