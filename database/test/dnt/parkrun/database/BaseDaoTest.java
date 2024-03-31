package dnt.parkrun.database;

import org.junit.BeforeClass;

import static org.junit.Assume.assumeTrue;

public abstract class BaseDaoTest
{
    @BeforeClass
    public static void beforeClass() { assumeTrue("Env TEST not on: " + System.getProperty("TEST"), BaseDaoTest.isTesting()); }

    static boolean isTesting()
    {
        return null != System.getProperty("TEST") && Boolean.parseBoolean(System.getProperty("TEST"));
    }
}
