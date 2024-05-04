package dnt.parkrun.menu;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.AgeCategory;
import dnt.parkrun.stats.invariants.*;
import dnt.parkrun.stats.speed.AgeCategoryRecord;
import dnt.parkrun.stats.speed.SpeedStats;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;

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
                runWeeklyResults();
                break;
            case "SPEED", "S":
                runSpeedStats();
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

    private void runWeeklyResults()
    {

    }

    private void runSpeedStats()
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

    private void runInvariantsFull()
    {
        runInvariants(InvariantTest.class,
                ParsersTest.class,
                ProvinceTest.class);
    }

    private void runInvariantsQuick()
    {
        runInvariants(HowYouDoingTest.class,
                InvariantTest.class,
                ParsersTest.class,
                PendingCoursesTest.class,
                ProvinceTest.class);
    }

    private void runInvariants(Class... classes)
    {
        JUnitCore junit = new JUnitCore();

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
        System.out.println("Quick     - Quick run invariants                            (re-runnable, takes 1 minute");
        System.out.println("Invariant - Run Invariants                                  (re-runnable, takes 5 minutes)");
        System.out.println("Speed     - Speed Stats           needs 'Weekly Results'    (just uses database, 10 seconds, )");
        System.out.println("Exit(X)   - Exit");
        System.out.println("----------------------");
    }
}