package dnt.parkrun.webpageprovider;

public interface WebpageProviderFactory
{
    WebpageProvider createCourseEventWebpageProvider(String courseName, int eventNumber);

    WebpageProvider createCourseEventSummaryWebpageProvider(String courseName);
}
