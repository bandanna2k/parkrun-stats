package dnt.parkrun.webpageprovider;

import dnt.parkrun.common.UrlGenerator;

public class WebpageProviderFactoryImpl implements WebpageProviderFactory
{
    private final UrlGenerator urlGenerator;

    public WebpageProviderFactoryImpl(UrlGenerator urlGenerator)
    {
        this.urlGenerator = urlGenerator;
    }

    public WebpageProvider createCourseEventWebpageProvider(String courseName, int eventNumber)
    {
        return new WebpageProviderImpl(urlGenerator.generateCourseEventUrl(courseName, eventNumber));
    }

    public WebpageProvider createCourseEventSummaryWebpageProvider(String courseName)
    {
        return new WebpageProviderImpl(urlGenerator.generateCourseEventSummaryUrl(courseName));
    }
}
