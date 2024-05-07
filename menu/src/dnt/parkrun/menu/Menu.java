package dnt.parkrun.menu;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.AgeCategory;
import dnt.parkrun.stats.MostEventStats;
import dnt.parkrun.stats.invariants.InvariantTest;
import dnt.parkrun.stats.invariants.ParsersTest;
import dnt.parkrun.stats.invariants.ProvinceTest;
import dnt.parkrun.stats.speed.AgeCategoryRecord;
import dnt.parkrun.stats.speed.SpeedStats;
import dnt.parkrun.webpageprovider.WebpageProviderFactoryImpl;
import dnt.parkrun.weekendresults.WeekendResults;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static dnt.parkrun.common.ParkrunDay.getParkrunDay;
import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;

public class Menu
{
    public static void main(String[] args) throws IOException
    {
        new Menu().go();
    }

    private void go() throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String choice = "";
        while(!choice.equals("X"))
        {
            displayOptions();

            choice = reader.readLine().toUpperCase(Locale.ROOT);

            action(choice);
        }
    }

    private void action(String choice)
    {
        switch (choice)
        {
            case "X": break;
            case "WEEKLY", "W":
                fetchWeeklyResults();
                break;
            case "MOST", "M":
                captureMostEvent();
                break;
            case "SPEED", "S":
                fetchSpeedStats();
                break;
            case "QUICK", "Q":
                runInvariantsQuick();
                break;
            case "INVARIANTS", "I":
                runInvariantsFull();
            default:
                System.out.printf("No action for '%s'%n", choice);
        }
    }

    private void captureMostEvent()
    {
        try
        {
            DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                    getDataSourceUrl("parkrun_stats"), "stats", "statsfractalstats");
            DataSource statsDataSource = new SimpleDriverDataSource(new Driver(),
                    getDataSourceUrl("weekly_stats"), "stats", "statsfractalstats");

            MostEventStats stats = MostEventStats.newInstance(dataSource, statsDataSource, getParkrunDay(new Date()));
            File file = stats.generateStats();

            new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
        }
        catch (SQLException | IOException | XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void fetchWeeklyResults()
    {
        try
        {
            DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                    getDataSourceUrl("parkrun_stats"), "dao", "daoFractaldao");
            WeekendResults weekendResults = WeekendResults.newInstance(
                    dataSource,
                    new WebpageProviderFactoryImpl(new UrlGenerator(NZ.baseUrl)));
            weekendResults.fetchWeekendResults();
        }
        catch (SQLException | IOException e)
        {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void fetchSpeedStats()
    {
        try
        {
            DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                    getDataSourceUrl("parkrun_stats"), "stats", "statsfractalstats");
            SpeedStats stats = SpeedStats.newInstance(dataSource);

            Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToAgeGradeRecord =
                    stats.collectCourseToAgeGroupToAgeGradeRecord();
            File file = stats.generateFastTimeStats(courseToAgeGroupToAgeGradeRecord);
            new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
        }
        catch (SQLException | IOException | XMLStreamException e)
        {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void runInvariantsQuick()
    {
        runInvariants(InvariantTest.class,
                ParsersTest.class,
                ProvinceTest.class);
    }

    private void runInvariantsFull()
    {
        runInvariants("dnt.parkrun.stats.invariants");
    }

    private void runInvariants(String javaPackage)
    {
        Set<Class> classes = new Reflections().findAllClassesUsingClassLoader(javaPackage);
        classes.removeIf(aClass -> !aClass.getSimpleName().endsWith("Test"));
        runInvariants(classes.toArray(Class[]::new));
    }

    private void runInvariants(Class... classes)
    {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new RunListener() {
            private String methodName;

            @Override
            public void testStarted(Description description) throws Exception
            {
                methodName = description.getMethodName();
                System.out.printf("Started: Test: %s%n", methodName);
                super.testStarted(description);
            }
            @Override
            public void testFailure(Failure failure) throws Exception
            {
                System.out.printf("FAILED: Test: %s, Failure: %s%n", methodName, failure.getMessage());
                super.testFailure(failure);
            }
            @Override
            public void testFinished(Description description) throws Exception
            {
            }
        });

        Result result = junit.run(classes);

        resultReport(result);
    }

    private void resultReport(Result result)
    {
        int passCount = result.getRunCount() - result.getFailureCount() - result.getAssumptionFailureCount() - result.getIgnoreCount();
        System.out.println("Passed: " + passCount);
        System.out.println("Failed: " + result.getFailureCount());
        result.getFailures().forEach(failure -> {
            System.out.println(failure.getTestHeader() + "\t" + failure.getMessage());
        });
    }

    private void displayOptions()
    {
        System.out.println("----------------------");
        System.out.println("Quick          - Quick run invariants                                 (re-runnable, takes 1 minute");
        System.out.println("Invariant      - Run Invariants                                       (re-runnable, takes 5 minutes)");
        System.out.println("Weekly         - Weekly results        needs 'Invariants' to pass     (re-runnable, downloads from web, 15 minutes. Many days if new)");
        System.out.println("Most           - Move Events results   needs 'Weekly Results'         (re-runnable, downloads from web, 1 hours)");
        System.out.println("Speed          - Speed Stats           needs 'Weekly Results'         (just uses database, 10 seconds, )");
        System.out.println("Exit(X)        - Exit");
        System.out.println("----------------------");
    }
}