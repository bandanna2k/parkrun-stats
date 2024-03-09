package dnt.parkrun.stats;

import com.mysql.jdbc.Driver;
import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.StatsDao;
import dnt.parkrun.datastructures.stats.AttendanceRecord;
import dnt.parkrun.datastructures.stats.MostEventsRecord;
import dnt.parkrun.htmlwriter.AttendanceRecordsTableHtmlWriter;
import dnt.parkrun.htmlwriter.HtmlWriter;
import dnt.parkrun.htmlwriter.MostEventsTableHtmlWriter;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dnt.parkrun.common.UrlGenerator.generateAthleteEventSummaryUrl;
import static dnt.parkrun.database.StatsDao.DifferentCourseCount;

public class Stats
{
    private static final int SEVEN_DAYS_IN_MILLIS = (7 * 24 * 60 * 60 * 1000);

    /*
                    02/03/2024
     */
    public static void main(String[] args) throws SQLException, IOException, XMLStreamException
    {
        Stats stats = Stats.newInstance(DateConverter.parseWebsiteDate(args[0]));
        stats.generateStats();
    }

    private final Date date;
    private final StatsDao statsDao;

    private Stats(DataSource dataSource, Date date)
    {
        this.date = date;
        this.statsDao = new StatsDao(dataSource, this.date);
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

        System.out.println("* Displaying most events table *");
        List<DifferentCourseCount> differentEventRecords = statsDao.getDifferentCourseCount(date);
        differentEventRecords.forEach(differentCourseCount -> {
            System.out.println(differentCourseCount);
        });

        System.out.println("* Calculate table position deltas *");
        Date lastWeek = new Date();
        lastWeek.setTime(date.getTime() - SEVEN_DAYS_IN_MILLIS);
//        List<DifferentCourseCount> differentEventRecordsFromLastWeek = statsDao.getDifferentCourseCount(lastWeek);
//        calculatePositionDeltas(differentEventRecords, differentEventRecordsFromLastWeek);

        System.out.println("* Generating attendance record table *");
        statsDao.generateAttendanceRecordTable();

        System.out.println("* Displaying most events table *");
        List<AttendanceRecord> listOfAttendanceRecords = statsDao.getAttendanceRecords();
        listOfAttendanceRecords.forEach(System.out::println);

        try(HtmlWriter writer = HtmlWriter.newInstance(date))
        {
            try(MostEventsTableHtmlWriter tableWriter = new MostEventsTableHtmlWriter(writer.writer))
            {
                for (DifferentCourseCount der : differentEventRecords)
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
                                    der.differentCourseCount, der.totalRuns, der.positionDelta));
                }
            }

            // Read attendance records from, and write html table
            try(AttendanceRecordsTableHtmlWriter tableWriter = new AttendanceRecordsTableHtmlWriter(writer.writer))
            {
                tableWriter.writer.writeStartElement("tbody");
                for (AttendanceRecord ar : listOfAttendanceRecords)
                {
                    tableWriter.writeAttendanceRecord(ar);
                }
                tableWriter.writer.writeEndElement(); // tbody
            }
        }
    }

    private void calculatePositionDeltas(List<DifferentCourseCount> differentEventRecords,
                                         List<DifferentCourseCount> differentEventRecordsFromLastWeek)
    {
        for (int indexThisWeek = 0; indexThisWeek < differentEventRecords.size(); indexThisWeek++)
        {
            for (int indexLastWeek = 0; indexLastWeek < differentEventRecordsFromLastWeek.size(); indexLastWeek++)
            {
                DifferentCourseCount thisWeek = differentEventRecords.get(0);
                DifferentCourseCount lastWeek = differentEventRecordsFromLastWeek.get(0);

                if(thisWeek.athleteId == lastWeek.athleteId)
                {
                    // Found athlete
                    thisWeek.positionDelta = indexLastWeek - indexThisWeek;
                }
            }
        }
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
