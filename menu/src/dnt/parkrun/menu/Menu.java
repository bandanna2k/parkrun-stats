package dnt.parkrun.menu;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.googlecode.htmlcompressor.compressor.YuiCssCompressor;
import com.googlecode.htmlcompressor.compressor.YuiJavaScriptCompressor;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.Database;
import dnt.parkrun.database.LiveDatabase;
import dnt.parkrun.datastructures.AgeCategory;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.stats.MostEventStats;
import dnt.parkrun.stats.invariants.postdownload.DatabaseInvariantTest;
import dnt.parkrun.stats.invariants.postdownload.DatabaseWeeklyResultsInvariantTest;
import dnt.parkrun.stats.invariants.postdownload.InvariantTest;
import dnt.parkrun.stats.invariants.predownload.first.AreCoursesUpToDateTest;
import dnt.parkrun.stats.invariants.predownload.first.ParsersTest;
import dnt.parkrun.stats.invariants.predownload.first.PendingCoursesTest;
import dnt.parkrun.stats.invariants.predownload.first.ProvinceTest;
import dnt.parkrun.stats.invariants.predownload.last.AreLastResultsInFromCanadaTest;
import dnt.parkrun.stats.invariants.predownload.last.LatestResultsAndEventHistoryTest;
import dnt.parkrun.stats.speed.AgeCategoryRecord;
import dnt.parkrun.stats.speed.SpeedStats;
import dnt.parkrun.webpageprovider.WebpageProviderFactoryImpl;
import dnt.parkrun.weekendresults.WeekendResults;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static dnt.parkrun.common.FindAndReplace.findAndReplace;
import static dnt.parkrun.common.ParkrunDay.getParkrunDay;
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
            Database database = new LiveDatabase(country, getDataSourceUrl(), "stats", "4b0e7ff1");
            MostEventStats stats = MostEventStats.newInstance(database, getParkrunDay(new Date()));
            File file = stats.generateStats();

            File modified = new File(file.getAbsoluteFile().getParent() + "/modified_" + file.getName());
            findAndReplace(file, modified, MostEventStats.fileReplacements());

//            File compressed = new File(file.getAbsoluteFile().getParent() + "/compressed_" + file.getName());
//            compress(modified, compressed);

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
            Database database = new LiveDatabase(country, getDataSourceUrl(), "dao", "0b851094");
            WeekendResults weekendResults = WeekendResults.newInstance(
                    database,
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
            Database database = new LiveDatabase(country, getDataSourceUrl(), "stats", "4b0e7ff1");
            SpeedStats stats = SpeedStats.newInstance(database);

            Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToAgeGradeRecord = new HashMap<>();
            Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToTimeRecord = new HashMap<>();

            stats.collectCourseToAgeGroupStats(courseToAgeGroupToAgeGradeRecord, courseToAgeGroupToTimeRecord);

            File file = stats.generateFastTimeStats(courseToAgeGroupToAgeGradeRecord, courseToAgeGroupToTimeRecord);
            File modified = new File(file.getAbsoluteFile().getParent() + "/modified_" + file.getName());
            findAndReplace(file, modified, SpeedStats.fileReplacements());

//            File compressed = new File(file.getAbsoluteFile().getParent() + "/compressed_" + file.getName());
//            compress(modified, compressed);

                new ProcessBuilder("xdg-open", modified.getAbsolutePath()).start();
        }
        catch (IOException | XMLStreamException e)
        {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void runInvariantsQuick()
    {
        runInvariants(
                AreCoursesUpToDateTest.class,
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
                LatestResultsAndEventHistoryTest.class,
                AreLastResultsInFromCanadaTest.class,
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

    private static void compress(File input, File output) throws IOException
    {
        HtmlCompressor compressor = new HtmlCompressor();
        String html = Files.readString(input.toPath(), StandardCharsets.UTF_8);
        try (FileOutputStream fos = new FileOutputStream(output);
             OutputStreamWriter osw = new OutputStreamWriter(fos);
             BufferedWriter writer = new BufferedWriter(osw))
        {
            compressor.setGenerateStatistics(true);

            compressor.setEnabled(true);                   //if false all compression is off (default is true)
            compressor.setRemoveComments(true);            //if false keeps HTML comments (default is true)
            compressor.setRemoveMultiSpaces(true);         //if false keeps multiple whitespace characters (default is true)
            compressor.setRemoveIntertagSpaces(true);      //removes iter-tag whitespace characters
            compressor.setRemoveQuotes(true);              //removes unnecessary tag attribute quotes
            compressor.setSimpleDoctype(true);             //simplify existing doctype
            compressor.setRemoveScriptAttributes(true);    //remove optional attributes from script tags
            compressor.setRemoveStyleAttributes(true);     //remove optional attributes from style tags
            compressor.setRemoveLinkAttributes(true);      //remove optional attributes from link tags
            compressor.setRemoveFormAttributes(true);      //remove optional attributes from form tags
            compressor.setRemoveInputAttributes(true);     //remove optional attributes from input tags
            compressor.setSimpleBooleanAttributes(true);   //remove values from boolean tag attributes
            compressor.setRemoveJavaScriptProtocol(true);  //remove "javascript:" from inline event handlers
            compressor.setRemoveHttpProtocol(true);        //replace "http://" with "//" inside tag attributes
            compressor.setRemoveHttpsProtocol(true);       //replace "https://" with "//" inside tag attributes
            compressor.setRemoveSurroundingSpaces("br,p"); //remove spaces around provided tags

            compressor.setCompressCss(true);               //compress inline css
            compressor.setCompressJavaScript(true);        //compress inline javascript

            compressor.setJavaScriptCompressor(new YuiJavaScriptCompressor());
            compressor.setCssCompressor(new YuiCssCompressor());

            compressor.compress(html);
            System.out.println(String.format(
                    "Compression time: %,d ms, Original size: %,d bytes, Compressed size: %,d bytes",
                    compressor.getStatistics().getTime(),
                    compressor.getStatistics().getOriginalMetrics().getFilesize(),
                    compressor.getStatistics().getCompressedMetrics().getFilesize()
            ));
            writer.write(compressor.compress(html));
        }

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