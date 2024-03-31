package dnt.parkrun.database;

import dnt.parkrun.datastructures.Course;
import org.junit.BeforeClass;

import static dnt.parkrun.datastructures.Country.UNKNOWN;
import static org.junit.Assume.assumeTrue;

public abstract class BaseDaoTest
{
    public static final Course ELLIÐAÁRDALUR =
            new Course(9999, "Elliðaárdalur", UNKNOWN, "Elliðaárdalur", Course.Status.RUNNING);
    public static final Course CORNWALL =
            new Course(9998, "Cornwall Park", UNKNOWN, "Cornwall Park", Course.Status.RUNNING);


    @BeforeClass
    public static void beforeClass() { assumeTrue("Env TEST not on: " + System.getProperty("TEST"), BaseDaoTest.isTesting()); }

    static boolean isTesting()
    {
        return null != System.getProperty("TEST") && Boolean.parseBoolean(System.getProperty("TEST"));
    }
}
