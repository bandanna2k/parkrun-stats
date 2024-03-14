package dnt.parkrun.stats;

import com.mysql.jdbc.Driver;
import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.*;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.datastructures.stats.AttendanceRecord;
import dnt.parkrun.datastructures.stats.RunsAtEvent;
import dnt.parkrun.htmlwriter.*;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static dnt.parkrun.common.UrlGenerator.generateAthleteEventSummaryUrl;
import static dnt.parkrun.database.StatsDao.DifferentCourseCount;
import static dnt.parkrun.stats.PIndex.pIndex;

public class Stats
{
    private static final int SEVEN_DAYS_IN_MILLIS = (7 * 24 * 60 * 60 * 1000);

    public static final String PARKRUN_CO_NZ = "parkrun.co.nz";
    private final CourseRepository courseRepository;

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
    private final Date lastWeek;
    private final StatsDao statsDao;
    private final ResultDao resultDao;
    private final AthleteCourseSummaryDao acsDao;
    private final CourseDao courseDao;
    private final Top10AtCourseDao top10Dao;
    private final CourseEventSummaryDao courseEventSummaryDao;
    private final Map<Integer, Athlete> athleteIdToAthlete = new HashMap<>();
    private final Map<Integer, List<AthleteCourseSummary>> athleteIdToAthleteCourseSummaries = new HashMap<>();

    private Stats(DataSource dataSource,
                  DataSource statsDataSource,
                  Date date)
    {
        this.date = date;
        lastWeek = new Date();
        lastWeek.setTime(date.getTime() - SEVEN_DAYS_IN_MILLIS);

        this.statsDao = new StatsDao(dataSource, this.date);
        this.acsDao = new AthleteCourseSummaryDao(statsDataSource, this.date);
        this.top10Dao = new Top10AtCourseDao(statsDataSource, this.date);
        this.resultDao = new ResultDao(dataSource);
        this.courseRepository = new CourseRepository();
        this.courseDao = new CourseDao(dataSource, courseRepository);
        this.courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);
    }

    public static Stats newInstance(Date date) throws SQLException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "stats", "statsfractalstats");
        DataSource statsDataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/weekly_stats", "stats", "statsfractalstats");
        return new Stats(dataSource, statsDataSource, date);
    }

    private void generateStats() throws IOException, XMLStreamException
    {
        System.out.println("* Generating most events table *");
        statsDao.generateDifferentCourseCountTable();

        System.out.println("* Get most events *");
        List<DifferentCourseCount> differentEventRecords = statsDao.getDifferentCourseCount(date);

        System.out.println("* Get most events for last week  *");
        List<DifferentCourseCount> differentEventRecordsFromLastWeek = statsDao.getDifferentCourseCount(lastWeek);

        System.out.println("* Calculate most event position deltas *");
        calculatePositionDeltas(differentEventRecords, differentEventRecordsFromLastWeek);

        downloadAthleteCourseSummaries(differentEventRecords);

        try(HtmlWriter writer = HtmlWriter.newInstance(date))
        {
            writeAttendanceRecords(writer);

            try(MostEventsTableHtmlWriter tableWriter = new MostEventsTableHtmlWriter(writer.writer))
            {
                for (DifferentCourseCount der : differentEventRecords)
                {
                    Athlete athlete = athleteIdToAthlete.get(der.athleteId);
                    List<AthleteCourseSummary> athleteCourseSummaries = athleteIdToAthleteCourseSummaries.get(der.athleteId);

                    int pIndex = pIndex(athleteCourseSummaries);
                    int courseCount = athleteCourseSummaries.size();
                    int totalCourseCount = athleteCourseSummaries.stream().mapToInt(acs -> acs.countOfRuns).sum();

                    statsDao.updateDifferentCourseRecord(athlete.athleteId, courseCount, totalCourseCount, pIndex);

                    tableWriter.writeMostEventRecord(
                            new MostEventsTableHtmlWriter.Record(athlete,
                                    der.differentRegionCourseCount, der.totalRegionRuns,
                                    courseCount, totalCourseCount, der.positionDelta, der.pIndex));
                }
            }

            writer.writer.writeStartElement("hr");
            writer.writer.writeEndElement();

            /*
            try(PIndexTableHtmlWriter tableWriter = new PIndexTableHtmlWriter(writer.writer))
            {
                List<PIndexTableHtmlWriter.Record> records = new ArrayList<>();
                acsMap.forEach((key, value) ->
                {
                    int pIndex = pIndex(value);
                    if (pIndex >= 5)
                    {
                        records.add(new PIndexTableHtmlWriter.Record(key, pIndex));
                    }
                });

                records.sort((der1, der2) -> {
                    if(der1.pIndex < der2.pIndex) return 1;
                    if(der1.pIndex > der2.pIndex) return -1;
                    if(der1.athlete.athleteId > der2.athlete.athleteId) return 1;
                    if(der1.athlete.athleteId < der2.athlete.athleteId) return -1;
                    return 0;
                });
                for (PIndexTableHtmlWriter.Record record : records)
                {
                    tableWriter.writePIndexRecord(record);
                }
            }

             */

            Map<String, Integer> courseToCount = courseEventSummaryDao.getCourseCount();
            try(Top10AtCoursesHtmlWriter ignored = new Top10AtCoursesHtmlWriter(writer.writer))
            {
                for (Course course : courseDao.getCourses(CountryEnum.NZ))
                {
                    try(Top10AtCourseHtmlWriter top10atCourse = new Top10AtCourseHtmlWriter(writer.writer, course.longName))
                    {
                        List<RunsAtEvent> top10 = top10Dao.getTop10AtCourse(course.name);
                        if(top10.isEmpty())
                        {
                            System.out.println("* Getting top 10 athletes for " + course.longName);
                            top10.addAll(statsDao.getTop10AtEvent(course.name));
                            top10Dao.writeRunsAtEvents(top10);
                        }

                        for (RunsAtEvent rae : top10)
                        {
                            double courseCount = courseToCount.get(course.name);
                            double runCount = rae.runCount;
                            String percentage = String.format("%.1f", runCount * 100 / courseCount);
                            top10atCourse.writeRecord(new Top10AtCourseHtmlWriter.Record(rae.athlete, rae.runCount, percentage));
                        }
                    }
                }
            }

            /*
            try(MostRunsAtEventTableWriter tableWriter = new MostRunsAtEventTableWriter(writer.writer))
            {
                List<RunsAtEvent> records = acsDao.getMostRunsAtEvent();

                for (RunsAtEvent record : records)
                {
                    tableWriter.writeRecord(record);
                }
            }
            */
        }
    }

    private void writeAttendanceRecords(HtmlWriter writer) throws XMLStreamException
    {
        // Read attendance records from, and write html table
        try(AttendanceRecordsTableHtmlWriter tableWriter = new AttendanceRecordsTableHtmlWriter(writer.writer))
        {
            tableWriter.writer.writeStartElement("tbody");
            for (AttendanceRecord ar : getAttendanceRecords())
            {
                tableWriter.writeAttendanceRecord(ar);
            }
            tableWriter.writer.writeEndElement(); // tbody
        }
    }

    private List<AttendanceRecord> getAttendanceRecords()
    {
        System.out.println("* Generating attendance record table *");
        statsDao.generateAttendanceRecordTable();
        List<AttendanceRecord> attendanceRecords = statsDao.getAttendanceRecords(date);
        attendanceRecords.forEach(System.out::println);

        System.out.println("* Calculate attendance deltas *");
        List<AttendanceRecord> attendanceRecordsFromLastWeek = statsDao.getAttendanceRecords(lastWeek);
        calculateAttendanceDeltas(attendanceRecords, attendanceRecordsFromLastWeek);
        return attendanceRecords;
    }

    private void downloadAthleteCourseSummaries(List<DifferentCourseCount> differentEventRecords) throws MalformedURLException
    {
        System.out.println("* Calculate athlete summaries that need to be downloaded (1. Most event table) *");
        Set<Integer> athletesFromMostEventTable = differentEventRecords.stream().map(der -> der.athleteId).collect(Collectors.toSet());
        System.out.println("Size A " + athletesFromMostEventTable.size());

        System.out.println("* Calculate athlete summaries that need to be downloaded (2. Results table) *");
        Set<Integer> athletesFromResults = getAthletesFromDbWithMinimumPIndex(5);
        System.out.println("Size B " + athletesFromResults.size());

        Set<Integer> athletesInMostEventsNotInResults = new HashSet<>(athletesFromMostEventTable);
        athletesInMostEventsNotInResults.removeAll(athletesFromResults);
        System.out.println("Athletes in most events, not in results. " + athletesInMostEventsNotInResults.size());

        Set<Integer> athletesInResultsNotInMostEvents = new HashSet<>(athletesFromResults);
        athletesInResultsNotInMostEvents.removeAll(athletesFromMostEventTable);
        System.out.println("Athletes in results, not in most events. " + athletesInResultsNotInMostEvents.size());

        Set<Integer> athletesToDownload = new HashSet<>();
        athletesToDownload.addAll(athletesFromMostEventTable);
        athletesToDownload.addAll(athletesFromResults);
        Set<Integer> athletesAlreadyDownloaded = acsDao.getAthleteCourseSummaries().stream().map(acs -> (int)acs[0]).collect(Collectors.toSet());
        System.out.println("Athletes already downloaded. " + athletesAlreadyDownloaded.size());
        athletesToDownload.removeAll(athletesAlreadyDownloaded);
        System.out.println("Athletes too download. " + athletesToDownload.size());

        for (int athlete_id : athletesToDownload)
        {
            Parser parser = new Parser.Builder()
                    .url(generateAthleteEventSummaryUrl(PARKRUN_CO_NZ, athlete_id))
                    .forEachAthleteCourseSummary(acsDao::writeAthleteCourseSummary)
                    .build(courseRepository);
            parser.parse();
        }

        acsDao.getAthleteCourseSummariesMap().forEach(objects -> {

            int athleteId = (int)objects[0];
            List<AthleteCourseSummary> summaries = athleteIdToAthleteCourseSummaries.get(athleteId);
            if(summaries == null)
            {
                summaries = new ArrayList<>();
            }
            Course course = courseRepository.getCourse((int) objects[1]);
            Athlete athlete = athleteIdToAthlete.get(athleteId);
            summaries.add(new AthleteCourseSummary(
                    athlete,
                    course,
                    (int) objects[2]
            ));
        });
    }

    private Set<Integer> getAthletesFromDbWithMinimumPIndex(int minPIndex)
    {
        Map<Integer, Map<String, Integer>> athleteToCourseCount = new HashMap<>();
        resultDao.tableScan(r -> {
            if(r.athlete.athleteId < 0)
            {
                return;
            }

            Map<String, Integer> courseToCount = athleteToCourseCount.get(r.athlete.athleteId);
            if(courseToCount == null)
            {
                courseToCount = new HashMap<>();
                courseToCount.put(r.courseName, 1);
                athleteToCourseCount.put(r.athlete.athleteId, courseToCount);
            }
            else
            {
                Integer count = courseToCount.get(r.courseName);
                if(count == null)
                {
                    courseToCount.put(r.courseName, 1);
                }
                else
                {
                    courseToCount.put(r.courseName, count + 1);
                }
            }
        });

        Set<Integer> athletes = new HashSet<>();
        athleteToCourseCount.forEach((athleteId, courseToCount) -> {
            int pIndex = pIndex(courseToCount);
            if(pIndex >= minPIndex)
            {
                athletes.add(athleteId);
            }
        });
        return athletes;
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
                    thisWeek.maxAttendanceDelta = thisWeek.recordEventFinishers - lastWeek.recordEventFinishers;
                    thisWeek.recentAttendanceDelta = thisWeek.recentEventFinishers - lastWeek.recentEventFinishers;
                }
            }
        }
    }

    public static Date getParkrunDay(Date result)
    {
        Calendar calResult = Calendar.getInstance();
        calResult.setTime(result);

        for (int i = 0; i < 7; i++)
        {
            if (calResult.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
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
