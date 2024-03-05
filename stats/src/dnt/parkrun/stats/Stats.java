package dnt.parkrun.stats;

import com.mysql.jdbc.Driver;
import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.StatsDao;
import dnt.parkrun.datastructures.stats.MostEventsRecord;
import dnt.parkrun.htmlwriter.HtmlWriter;
import dnt.parkrun.htmlwriter.MostEventsTableHtmlWriter;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dnt.parkrun.common.UrlGenerator.generateAthleteEventSummaryUrl;

public class Stats
{
    public static void main(String[] args) throws SQLException, IOException, XMLStreamException
    {
        Stats stats = Stats.newInstance(DateConverter.parseWebsiteDate(args[0]));
        stats.generateStats();
    }


    private final StatsDao statsDao;
    private final File htmlFile;

    private Stats(DataSource dataSource, Date date)
    {
        this.statsDao = new StatsDao(dataSource, date);
        this.htmlFile = new File("most_events_" + DateConverter.formatDateForDbTable(date) + ".html");
    }

    public static Stats newInstance(Date date) throws SQLException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "stats", "statsfractalstats");
        return new Stats(dataSource, date);
    }

    private void generateStats() throws IOException, XMLStreamException
    {
        System.out.println("* Generating most events table *");
        statsDao.generateDifferentCourseCountTable();

        System.out.println("* Displaying records *");
        List<StatsDao.DifferentCourseCount> listOfDifferentEventRecords = statsDao.getDifferentCourseCount();
        listOfDifferentEventRecords.forEach(differentCourseCount -> {
            System.out.println(differentCourseCount);
        });

        try(HtmlWriter writer = HtmlWriter.newInstance(htmlFile))
        {
            try(MostEventsTableHtmlWriter tableWriter = new MostEventsTableHtmlWriter(writer.writer))
            {
                for (StatsDao.DifferentCourseCount der : listOfDifferentEventRecords)
                {
                    if (der.differentCourseCount == 0 || der.totalRuns == 0)
                    {
                        AtomicInteger differentCourseCount = new AtomicInteger();
                        AtomicInteger totalRuns = new AtomicInteger();
                        Parser parser = new Parser.Builder()
                                .url(generateAthleteEventSummaryUrl("parkrun.co.nz", der.athleteId))
                                .forEachAthleteCourseSummary(acs ->
                                {
                                    differentCourseCount.incrementAndGet();
                                    totalRuns.addAndGet(acs.countOfRuns);
                                })
                                .build();
                        parser.parse();

                        TotalEventCountUpdate update = new TotalEventCountUpdate(der.athleteId, differentCourseCount.get(), totalRuns.get());
                        System.out.println(update);
                        statsDao.updateDifferentCourseRecord(update.athleteId, update.differentCourseCount, update.totalRuns);

                    }
                    tableWriter.writeMostEventRecord(
                            new MostEventsRecord(der.name, der.athleteId,
                                    der.differentRegionCourseCount, der.totalRegionRuns,
                                    der.differentCourseCount, der.totalRuns));
                }
            }
        }
        Process myProcess = new ProcessBuilder("xdg-open", htmlFile.getAbsolutePath()).start();
    }

    private static class TotalEventCountUpdate
    {
        public final int athleteId;
        public final int differentCourseCount;
        public final int totalRuns;

        public TotalEventCountUpdate(int athleteId, int differentCourseCount, int totalRuns)
        {
            this.athleteId = athleteId;
            this.differentCourseCount = differentCourseCount;
            this.totalRuns = totalRuns;
        }

        @Override
        public String toString()
        {
            return "TotalEventCountUpdate{" +
                    "athleteId=" + athleteId +
                    ", differentCourseCount=" + differentCourseCount +
                    ", totalRuns=" + totalRuns +
                    '}';
        }
    }
}
