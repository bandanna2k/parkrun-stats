package dnt.parkrun.stats;

import com.mysql.jdbc.Driver;
import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.StatsDao;
import dnt.parkrun.datastructures.AthleteCourseSummary;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dnt.parkrun.common.UrlGenerator.generateAthleteEventSummaryUrl;
import static dnt.parkrun.database.StatsDao.DifferentCourseCount;
import static dnt.parkrun.stats.PIndex.pIndex;

public class Stats
{
    private static final int SEVEN_DAYS_IN_MILLIS = (7 * 24 * 60 * 60 * 1000);

    /*
                    02/03/2024
     */
    public static void main(String[] args) throws SQLException, IOException, XMLStreamException
    {
        Date date = args.length == 0 ? getParkrunDay(new Date()) : DateConverter.parseWebsiteDate(args[0]);
        Stats stats = Stats.newInstance(date);
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
        List<DifferentCourseCount> differentEventRecordsFromLastWeek = statsDao.getDifferentCourseCount(lastWeek);
        calculatePositionDeltas(differentEventRecords, differentEventRecordsFromLastWeek);

        System.out.println("* Generating attendance record table *");
        statsDao.generateAttendanceRecordTable();
        List<AttendanceRecord> attendanceRecords = statsDao.getAttendanceRecords(date);
        attendanceRecords.forEach(System.out::println);

        System.out.println("* Calculate attendance deltas *");
        List<AttendanceRecord> attendanceRecordsFromLastWeek = statsDao.getAttendanceRecords(lastWeek);
        calculateAttendanceDeltas(attendanceRecords, attendanceRecordsFromLastWeek);

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

                        List<AthleteCourseSummary> listOfRuns = new ArrayList<>();
                        Parser parser = new Parser.Builder()
                                .url(generateAthleteEventSummaryUrl("parkrun.co.nz", der.athleteId))
                                .forEachAthleteCourseSummary(acs ->
                                {
                                    listOfRuns.add(acs);
                                    differentCourseCount.incrementAndGet();
                                    totalRuns.addAndGet(acs.countOfRuns);
                                })
                                .build();
                        parser.parse();

                        // pIndex stuff to go here.
                        int pIndex = pIndex(listOfRuns);

                        TotalEventCountUpdate update = new TotalEventCountUpdate(der.athleteId, differentCourseCount.get(), totalRuns.get(), pIndex);
                        System.out.println(update);
                        statsDao.updateDifferentCourseRecord(update.athleteId, update.differentCourseCount, update.totalRuns, update.pIndex);
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
                for (AttendanceRecord ar : attendanceRecords)
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
                DifferentCourseCount thisWeek = differentEventRecords.get(indexThisWeek);
                DifferentCourseCount lastWeek = differentEventRecordsFromLastWeek.get(indexLastWeek);

                if(thisWeek.athleteId == lastWeek.athleteId)
                {
                    // Found athlete
                    thisWeek.positionDelta = indexLastWeek - indexThisWeek;
                }
            }
        }
    }

    private void calculateAttendanceDeltas(List<AttendanceRecord> attendanceRecords,
                                           List<AttendanceRecord> attendanceRecordsFromLastWeek)
    {
        for (AttendanceRecord thisWeek : attendanceRecords)
        {
            for (AttendanceRecord lastWeek : attendanceRecordsFromLastWeek)
            {
                if (thisWeek.courseName.equals(lastWeek.courseName))
                {
                    // Found course
                    thisWeek.maxAttendanceDelta = thisWeek.maxAttendance - lastWeek.maxAttendance;
                    thisWeek.recentAttendanceDelta = thisWeek.recentAttendance - lastWeek.recentAttendance;
                }
            }
        }
    }

    private static class TotalEventCountUpdate
    {
        public final int athleteId;
        public final int differentCourseCount;
        public final int totalRuns;
        public final int pIndex;

        public TotalEventCountUpdate(int athleteId, int differentCourseCount, int totalRuns, int pIndex)
        {
            this.athleteId = athleteId;
            this.differentCourseCount = differentCourseCount;
            this.totalRuns = totalRuns;
            this.pIndex = pIndex;
        }

        @Override
        public String toString()
        {
            return "TotalEventCountUpdate{" +
                    "athleteId=" + athleteId +
                    ", differentCourseCount=" + differentCourseCount +
                    ", totalRuns=" + totalRuns +
                    ", pIndex=" + pIndex +
                    '}';
        }
    }

    public static Date getParkrunDay(Date result)
    {
        Calendar calResult = Calendar.getInstance();
        calResult.setTime(result);

        for (int i = 0; i < 7; i++)
        {
            if(calResult.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
            {
                calResult.set(Calendar.HOUR_OF_DAY, 0);
                calResult.set(Calendar.MINUTE, 0);
                calResult.set(Calendar.SECOND, 0);
                calResult.set(Calendar.MILLISECOND, 0);
                return calResult.getTime();
            }
            calResult.add(Calendar.DAY_OF_MONTH, i * -1);
        }
        throw new UnsupportedOperationException();
    }

}
