package dnt.jsoupwrapper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.Random;
import java.util.function.Supplier;

public class JsoupWrapper
{
    private static final Random RANDOM = new Random();

    private final boolean shouldSleep;
    private final Supplier<Proxy> proxyFactory;


    private JsoupWrapper(boolean shouldSleep, Supplier<Proxy> proxyFactory)
    {
        this.shouldSleep = shouldSleep;
        this.proxyFactory = proxyFactory;
    }

    public Document newDocument(URL url)
    {
        /*
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("1.2.3.4", 8080));
Document doc = Jsoup.connect("url").proxy(proxy).get();
         */

        int counter = 2;
        Document document = null;
        while(document == null && counter > 0)
        {
            counter--;
            try
            {
                Connection connection = Jsoup
                        .connect(url.toString())
                        .proxy(proxyFactory.get())
                        .timeout(10000);
                if(shouldSleep)
                {
                    int sleepTime = 3000 + RANDOM.nextInt(3000);
                    System.out.print("Sleeping for " + sleepTime + " ... ");
                    Thread.sleep(sleepTime);
                    System.out.print("Loading URL: " + url + " ");
                    document = connection.get();
                    System.out.println("Done");
                }
                else
                {
                    System.out.print("Loading URL: " + url + " ");
                    document = connection.get();
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

    public static class Builder
    {
        private boolean shouldSleep = true;
        private Supplier<Proxy> proxyFactory = () -> null;

        public JsoupWrapper build()
        {
            return new JsoupWrapper(shouldSleep, proxyFactory);
        }

        public Builder shouldSleep(boolean shouldSleep)
        {
            this.shouldSleep = shouldSleep;
            return this;
        }
        public Builder proxyFactory(Supplier<Proxy> proxyFactory)
        {
            this.proxyFactory = proxyFactory;
            return this;
        }
    }
}
