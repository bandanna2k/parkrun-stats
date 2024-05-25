package dnt.parkrun.region;

import dnt.parkrun.datastructures.Course;

import java.util.Arrays;

public class NewZealandRegionChecker extends RegionChecker
{
    @Override
    public boolean isSameRegion(Course homeParkrun, Course course)
    {
        if (course == null) return false;
        if (isAuckland(homeParkrun) && isAuckland(course)) { return true; }
        if (isWaikato(homeParkrun) && isWaikato(course)) { return true; }
        if (isNorthland(homeParkrun) && isNorthland(course)) { return true; }
        if (isBayOfPlenty(homeParkrun) && isBayOfPlenty(course)) { return true; }
        if (isGisbourne(homeParkrun) && isGisbourne(course)) { return true; }
        if (isTaranaki(homeParkrun) && isTaranaki(course)) { return true; }
        if (isManawatu(homeParkrun) && isManawatu(course)) { return true; }
        if (isCantebury(homeParkrun) && isCantebury(course)) { return true; }
        if (isWellington(homeParkrun) && isWellington(course)) { return true; }
        if (isMarlborough(homeParkrun) && isMarlborough(course)) { return true; }
        if (isOtago(homeParkrun) && isOtago(course)) { return true; }
        if (isSouthland(homeParkrun) && isSouthland(course)) { return true; }
        return false;
    }

    public static boolean isGisbourne(Course course)
    {
        String[] list = {"gisborne", "anderson", "flaxmere", "russellpark"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }

    public static boolean isBayOfPlenty(Course course)
    {
        String[] list = {"puarenga",
                "moanapointreserve",
                "tauranga",
                "gordonsprattreserve",
                "whakatanegardens",
                "gordoncarmichaelreserve"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }

    public static boolean isWaikato(Course course)
    {
        String[] list = {"taupo", "cambridgenz", "hamiltonlake", "universityofwaikato"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }

    public static boolean isMarlborough(Course course)
    {
        String[] list = {"blenheim"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }

    public static boolean isManawatu(Course course)
    {
        String[] list = {"palmerstonnorth"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }

    public static boolean isTaranaki(Course course)
    {
        String[] list = {"eastend", "whanganuiriverbank", "mangawheroriverwalkohakune"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }

    public static boolean isAuckland(Course course)
    {
        String[] list = {"hobsonvillepoint",
                "cornwallpark",
                "barrycurtis",
                "millwater",
                "westernsprings",
                "northernpathway",
                "sherwoodreserve",
                "orakeibay",
                "owairaka"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }

    public static boolean isWellington(Course course)
    {
        String[] list = {"otakiriver",
                "greytownwoodsidetrail",
                "lowerhutt",
                "kapiticoast",
                "trenthammemorial",
                "araharakeke",
                "waitangi",
                "porirua"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }

    public static boolean isNorthland(Course course)
    {
        String[] list = {"whangarei"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }

    public static boolean isOtago(Course course)
    {
        String[] list = {
                "dunedin",
                "balclutha",
                "otagocentralrailtrailalexandra",
                "queenstown",
                "wanaka"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }

    public static boolean isCantebury(Course course)
    {
        String[] list = {"broadpark", "hagley", "pegasus", "foster", "scarborough"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }

    public static boolean isSouthland(Course course)
    {
        String[] list = {"hamiltonpark", "invercargill", "lake2laketrail"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }
}
