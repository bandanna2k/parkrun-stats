package dnt.parkrun.networking;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import static dnt.parkrun.datastructures.Country.NZ;

public class NetworkingWithParserTest
{
    @Test
    @Ignore("Looks like websites are blocked from random IPs")
    public void shouldParseThroughProxy() throws IOException
    {
        UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);

        List<CourseEventSummary> courseEventSummaries = new ArrayList<>();
        Course course = new Course(-1, "cornwallpark", NZ, "Cornwall Park parkrun", Course.Status.RUNNING);
        WebpageProviderImpl webpageProvider = new WebpageProviderImpl(
                urlGenerator.generateCourseEventSummaryUrl(course.name), this::getProxy, false);
        Parser parser = new Parser.Builder()
                .course(course)
                .forEachCourseEvent(courseEventSummaries::add)
                .webpageProvider(webpageProvider)
                .build();
        parser.parse();

        courseEventSummaries.forEach(ces -> {
            System.out.println(ces);
        });
    }

    private Proxy getProxy()
    {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 9051);
        return new Proxy(Proxy.Type.SOCKS, address);
    }
}
