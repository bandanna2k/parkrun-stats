package dnt.parkrun.region;

import dnt.parkrun.datastructures.AthleteCourseSummary;
import dnt.parkrun.datastructures.Course;

import java.util.Arrays;
import java.util.List;

public abstract class Region
{
    public static int getNzRegionRunCount(Course homeParkrun, List<AthleteCourseSummary> summariesForAthlete)
    {
        int count = 0;
        for (AthleteCourseSummary acs : summariesForAthlete)
        {
            if (isSameNzRegion(homeParkrun, acs.course))
            {
                count += acs.countOfRuns;
            }
        }
        return count;
    }

    static boolean isSameNzRegion(Course homeParkrun, Course course)
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
        String[] list = {"eastend", "whanganuiriverbank"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }

    public static boolean isAuckland(Course course)
    {
        String[] list = {"hobsonvillepoint",
                "cornwall",
                "barrycurtis",
                "millwater",
                "westernsprings",
                "northernpathway",
                "sherwoodreserve",
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
        String[] list = {"dunedin", "balclutha", "queenstown", "wanaka"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }

    public static boolean isCantebury(Course course)
    {
        String[] list = {"broadpark", "hagley", "pegasus", "foster"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }

    public static boolean isSouthland(Course course)
    {
        String[] list = {"hamiltonpark", "invercargill", "lake2laketrail"};
        return Arrays.stream(list).anyMatch(v -> v.equals(course.name));
    }
}
