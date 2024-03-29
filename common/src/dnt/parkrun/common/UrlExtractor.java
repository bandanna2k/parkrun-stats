package dnt.parkrun.common;

public class UrlExtractor
{
    public static String extractCourseFromUrl(String courseLink)
    {
        int start = courseLink.indexOf("/", 10) + 1;
        int end = courseLink.indexOf("/", start);
        return courseLink.substring(start, end);
    }
}
