package dnt.parkrun.networking;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class WhatsMyIp
{
    @Test
    public void shouldGetIpAddress() throws IOException
    {
        /*
            curl -s https://api.my-ip.io/v2/ip.xml

            OR

            proxychains curl -s https://api.my-ip.io/v2/ip.xml
         */
        Document doc = Jsoup
                .connect("https://api.my-ip.io/v2/ip.xml")
                .get();
        Elements result = doc.getElementsByTag("result");
        System.out.println(result);
    }

    @Test
    @Ignore
    public void shouldGetIpAddressWithSocks() throws IOException
    {
        for (int i = 0; i < 2; i++)
        {
            shouldGetIpAddressWithSocks(i);
        }
    }
    public void shouldGetIpAddressWithSocks(int i) throws IOException
    {
        /*
         ssh -D 9050 -q -C -N northd@localhost
         -D = Opens a SOCKS proxy on given port
         -q = quite, don't output anything
         -C = compress data
         -N = no executing remote commands, just port forwarding
         Note: Check you have ssh server installed.

         OR

         sudo systemctl start tor

         */
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 9050);
        Document doc = Jsoup
                .connect("https://api.my-ip.io/v2/ip.xml")
                .proxy(new Proxy(Proxy.Type.SOCKS, address))
                .get();
        Elements result = doc.getElementsByTag("result");
        System.out.println(result);
    }

    @Test
    @Ignore
    public void shouldGetIpAddressWithTorAndProxyChains() throws IOException
    {
        /*
         sudo systemctl start tor
//         sudo nano /etc/proxychains.conf
         proxychains ssh -L 9051:localhost:9050 northd@localhost
         */
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 9051);
        Document doc = Jsoup
                .connect("https://api.my-ip.io/v2/ip.xml")
                .proxy(new Proxy(Proxy.Type.SOCKS, address))
                .get();
        Elements result = doc.getElementsByTag("result");
        System.out.println(result);
    }

    @Test
    @Ignore
    public void shouldGetIpAddressWithRemoteSocks() throws IOException
    {
        for (int i = 0; i < 4; i++)
        {
            shouldGetIpAddressWithRemoteSocks(i);
        }
    }
    public void shouldGetIpAddressWithRemoteSocks(int i) throws IOException
    {
        /*
https://www.freeproxy.world/?type=https&anonymity=&country=US&speed=400&port=&page=1
         */
//        InetSocketAddress address = new InetSocketAddress("52.35.240.119", 1080);
        InetSocketAddress address = new InetSocketAddress("132.148.166.93", 19797);
        Document doc = Jsoup
                .connect("https://api.my-ip.io/v2/ip.xml")
                .proxy(new Proxy(Proxy.Type.SOCKS, address))
                .get();
        Elements result = doc.getElementsByTag("result");
        System.out.println(result);
    }
}
