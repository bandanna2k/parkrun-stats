package dnt.parkrun.menu;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.AgeCategory;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.stats.MostEventStats;
import dnt.parkrun.stats.invariants.postdownload.DatabaseInvariantTest;
import dnt.parkrun.stats.invariants.postdownload.DatabaseWeeklyResultsInvariantTest;
import dnt.parkrun.stats.invariants.postdownload.InvariantTest;
import dnt.parkrun.stats.invariants.predownload.first.ParsersTest;
import dnt.parkrun.stats.invariants.predownload.first.PendingCoursesTest;
import dnt.parkrun.stats.invariants.predownload.first.ProvinceTest;
import dnt.parkrun.stats.invariants.predownload.last.HowYouDoingTest;
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

import static dnt.parkrun.common.ParkrunDay.getParkrunDay;
import static dnt.parkrun.database.DataSourceUrlBuilder.Type.PARKRUN_STATS;
import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;

public class Menu
{
    private final BufferedReader reader;
    private final Country country;

    public Menu(BufferedReader reader, Country country)
    {
        this.reader = reader;
        this.country = country;
    }

    public static void main(String[] args) throws IOException
    {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
        {
            String countryProperty = System.getProperty("parkrun_stats.country");
            final Country country;
            if (countryProperty == null)
            {
                System.out.println("Please entry a country.");
                country = Country.valueOf(reader.readLine());
            }
            else
            {
                country = Country.valueOf(countryProperty);
            }

            new Menu(reader, country).go();
        }
    }
    private void go() throws IOException
    {
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
                break;
            default:
                System.out.printf("No action for '%s'%n", choice);
        }
    }

    private void captureMostEvent()
    {
        try
        {
            DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                    getDataSourceUrl(PARKRUN_STATS, country), "stats", "statsfractalstats");
            DataSource statsDataSource = new SimpleDriverDataSource(new Driver(),
                    getDataSourceUrl(PARKRUN_STATS, country), "stats", "statsfractalstats");

            MostEventStats stats = MostEventStats.newInstance(country, dataSource, statsDataSource, getParkrunDay(new Date()));
            File file = stats.generateStats();
            File modified = new File(file.getAbsoluteFile().getParent() + "/modified_" + file.getName());
            MostEventStats.findAndReplace(file, modified);

            new ProcessBuilder("xdg-open", modified.getAbsolutePath()).start();
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
                    getDataSourceUrl(PARKRUN_STATS, country), "dao", "daoFractaldao");
            WeekendResults weekendResults = WeekendResults.newInstance(
                    country, dataSource,
                    new WebpageProviderFactoryImpl(new UrlGenerator(country.baseUrl)));
            weekendResults.fetchWeekendResults();
        }
        catch (SQLException | IOException e)
        {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    private void fetchSpeedStats()
    {
        try
        {
            DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                    getDataSourceUrl(PARKRUN_STATS, country), "stats", "statsfractalstats");
            SpeedStats stats = SpeedStats.newInstance(dataSource);

            Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToAgeGradeRecord =
                    stats.collectCourseToAgeGroupToAgeGradeRecord();

            File file = stats.generateFastTimeStats(courseToAgeGroupToAgeGradeRecord);
            File modified = new File(file.getAbsoluteFile().getParent() + "/modified_" + file.getName());
            SpeedStats.findAndReplace(file, modified);

            new ProcessBuilder("xdg-open", modified.getAbsolutePath()).start();
        }
        catch (SQLException | IOException | XMLStreamException e)
        {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void runInvariantsQuick()
    {
        runInvariants(
                ParsersTest.class,
                DatabaseInvariantTest.class,
                DatabaseWeeklyResultsInvariantTest.class,
                InvariantTest.class,
                ProvinceTest.class);
    }

    private void runInvariantsFull()
    {
        runInvariantsQuick();
        runInvariants(
                HowYouDoingTest.class,
                PendingCoursesTest.class
        );
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
                System.err.printf("FAILED: Test: %s, Failure: %s%n", methodName, failure.getMessage());
                super.testFailure(failure);
            }
            @Override
            public void testFinished(Description description)
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
            System.err.println(failure.getTestHeader() + "\t" + failure.getMessage());
        });
    }

    private void displayOptions()
    {
        System.out.println("----------------------");
        System.out.printf("Country: %s (%s) %n", country.name(), country.countryName);
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