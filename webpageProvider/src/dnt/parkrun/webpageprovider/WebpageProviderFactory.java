package dnt.parkrun.webpageprovider;

import dnt.parkrun.common.UrlGenerator;

public class WebpageProviderFactory
{
    private final UrlGenerator urlGenerator;

    public WebpageProviderFactory(UrlGenerator urlGenerator)
    {
        this.urlGenerator = urlGenerator;
    }

    public WebpageProvider createCourseEventWebpageProvider(String name, int eventNumber)
    {
        return new WebpageProviderImpl(urlGenerator.generateCourseEventUrl(name, eventNumber));
    }

    public WebpageProvider createCourseEventSummaryWebpageProvider(String courseName)
    {
        return new WebpageProviderImpl(urlGenerator.generateCourseEventSummaryUrl(courseName));
    }
}
