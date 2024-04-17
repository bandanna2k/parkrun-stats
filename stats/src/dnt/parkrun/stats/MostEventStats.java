package dnt.parkrun.stats;

import com.mysql.jdbc.Driver;
import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.CourseEventSummaryDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.database.VolunteerDao;
import dnt.parkrun.database.stats.MostEventsDao;
import dnt.parkrun.database.stats.Top10RunsDao;
import dnt.parkrun.database.stats.Top10VolunteersDao;
import dnt.parkrun.database.weekly.*;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.datastructures.stats.AtEvent;
import dnt.parkrun.datastructures.stats.AttendanceRecord;
import dnt.parkrun.datastructures.stats.VolunteersAtEvent;
import dnt.parkrun.htmlwriter.*;
import dnt.parkrun.pindex.PIndex;
import dnt.parkrun.stats.invariants.CourseEventSummaryChecker;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dnt.parkrun.common.DateConverter.SEVEN_DAYS_IN_MILLIS;
import static dnt.parkrun.datastructures.Country.NZ;
import static dnt.parkrun.datastructures.Course.Status.*;
import static dnt.parkrun.region.Region.getNzRegionRunCount;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class MostEventStats
{
    public static final int MIN_P_INDEX = 5;
    public static final Comparator<PIndexTableHtmlWriter.Record> PINDEX_RECORD_COMPARATOR = (pIndexRecord1, pIndexRecord2) ->
    {
        if (pIndexRecord1.globalPIndex.pIndex < pIndexRecord2.globalPIndex.pIndex) return 1;
        if (pIndexRecord1.globalPIndex.pIndex > pIndexRecord2.globalPIndex.pIndex) return -1;
        if (pIndexRecord1.globalPIndex.neededForNextPIndex > pIndexRecord2.globalPIndex.neededForNextPIndex) return 1;
        if (pIndexRecord1.globalPIndex.neededForNextPIndex < pIndexRecord2.globalPIndex.neededForNextPIndex) return -1;
        if (pIndexRecord1.homeRatio < pIndexRecord2.homeRatio) return 1;
        if (pIndexRecord1.homeRatio > pIndexRecord2.homeRatio) return -1;
        if (pIndexRecord1.athlete.athleteId > pIndexRecord2.athlete.athleteId) return 1;
        if (pIndexRecord1.athlete.athleteId < pIndexRecord2.athlete.athleteId) return -1;
        return 0;
    };
    private final CourseRepository courseRepository;

    /*
            02/03/2024
     */
    public static void main(String[] args) throws SQLException, IOException, XMLStreamException
    {
        Date date = args.length == 0 ? getParkrunDay(new Date()) : DateConverter.parseWebsiteDate(args[0]);

        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "stats", "statsfractalstats");
        DataSource statsDataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/weekly_stats", "stats", "statsfractalstats");

        MostEventStats stats = MostEventStats.newInstance(dataSource, statsDataSource, date);
        File file = stats.generateStats();

        new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();

        CourseEventSummaryChecker checker = new CourseEventSummaryChecker(dataSource, 1);
        List<String> validate = checker.validate();
        if(!validate.isEmpty())
        {
            throw new RuntimeException(String.join("\n", validate));
        }
    }

    private final Date date;
    private final Date lastWeek;
    private final DataSource statsDataSource;
    final AttendanceRecordsDao attendanceRecordsDao;
    private final ResultDao resultDao;
    private final AthleteCourseSummaryDao acsDao;
    private final Top10AtCourseDao top10Dao;
    private final Top10VolunteersAtCourseDao top10VolunteerDao;
    private final CourseEventSummaryDao courseEventSummaryDao;
    private final PIndexDao pIndexDao;
    private final VolunteerCountDao volunteerCountDao;
    private MostEventsDao mostEventsDao;
    private final VolunteerDao volunteerDao;

    private final Map<Integer, Athlete> athleteIdToAthlete = new HashMap<>();
    private final Map<Integer, List<AthleteCourseSummary>> athleteIdToAthleteCourseSummaries = new HashMap<>();
    private final List<CourseDate> startDates = new ArrayList<>();
    private final List<CourseDate> stopDates = new ArrayList<>();
    private final UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);

    private MostEventStats(DataSource dataSource,
                           DataSource statsDataSource,
                           Date date)
    {
        this.date = date;
        lastWeek = new Date();
        lastWeek.setTime(date.getTime() - SEVEN_DAYS_IN_MILLIS);

        this.statsDataSource = statsDataSource;
        this.attendanceRecordsDao = AttendanceRecordsDao.getInstance(statsDataSource, this.date);
        this.acsDao = AthleteCourseSummaryDao.getInstance(statsDataSource, this.date);
        this.top10Dao = Top10AtCourseDao.getInstance(statsDataSource, this.date);
        this.top10VolunteerDao = Top10VolunteersAtCourseDao.getInstance(statsDataSource, this.date);
        this.pIndexDao = PIndexDao.getInstance(statsDataSource, date);
        this.volunteerCountDao = VolunteerCountDao.getInstance(statsDataSource, this.date);

        this.resultDao = new ResultDao(dataSource);
        this.volunteerDao = new VolunteerDao(statsDataSource);

        this.courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);
        this.courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);
    }

    public static MostEventStats newInstance(DataSource dataSource, DataSource statsDataSource, Date date) throws SQLException
    {
        return new MostEventStats(dataSource, statsDataSource, date);
    }

    public File generateStats() throws IOException, XMLStreamException
    {
        System.out.print("Getting start dates ");
        startDates.addAll(courseEventSummaryDao.getCourseStartDates());
        System.out.println("Done");

        System.out.print("Getting stop dates ");
        stopDates.addAll(courseEventSummaryDao.getCourseStopDates(NZ));
        System.out.println("Done");

        System.out.println("* Generating most events table *");
        this.mostEventsDao = MostEventsDao.getOrCreate(statsDataSource, date);
        this.mostEventsDao.populateMostEventsTable();

        System.out.println("* Get most events *");
        List<MostEventsDao.MostEventsRecord> differentEventRecords = mostEventsDao.getMostEvents();

        System.out.println("* Get most events for last week  *");
        List<MostEventsDao.MostEventsRecord> differentEventRecordsFromLastWeek = mostEventsDao.getMostEventsForLastWeek();

        System.out.println("* Calculate most event position deltas *");
        calculatePositionDeltas(differentEventRecords, differentEventRecordsFromLastWeek);

        downloadAthleteCourseSummaries(differentEventRecords);

        try (HtmlWriter writer = HtmlWriter.newInstance(date, "stats", "most_events.css"))
        {
            String startDatesJs = "[" +
                    "[" + startDates.stream().map(sd -> String.valueOf(sd.date.getTime() / 1000)).collect(Collectors.joining(",")) + "]," +
                    "[" + startDates.stream().map(sd -> String.valueOf(sd.course.courseId)).collect(Collectors.joining(",")) + "]," +
                    "['" + startDates.stream().map(sd -> courseRepository.getCourse(sd.course.courseId).longName).collect(Collectors.joining("','")) + "']" +
                    "]";
            writer.writer.writeStartElement("script");
            writer.writer.writeCharacters("\nconst courses = " + startDatesJs + ";");
            writer.writer.writeCharacters("\nconst stopDates = " + getStopDates() + ";");
            writer.writer.writeEndElement();

            writer.writer.writeCharacters("{{dialog}}");

            writer.writer.writeStartElement("p");
            writer.writer.writeAttribute("align", "right");
            writer.writer.writeCharacters(new SimpleDateFormat("yyyy MMM dd hh:mm").format(new Date()));
            writer.writer.writeEndElement();

            writeAttendanceRecords(writer);

            writeMostEvents(writer, differentEventRecords, false);

            writer.writer.writeStartElement("hr");
            writer.writer.writeEndElement();

            writeMostEvents(writer, differentEventRecords, true);

            writeMostVolunteers(writer);

            writePIndex(writer);

            writeTop10Runs(writer);

            writeTop10Volunteers(writer);
        }

        File htmlFile = new File("stats_" + DateConverter.formatDateForDbTable(date) + ".html");
        File htmlFileModified = new File("stats_modified_" + DateConverter.formatDateForDbTable(date) + ".html");

        try (FileInputStream fis = new FileInputStream(htmlFile);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr))
        {
            try (FileOutputStream fos = new FileOutputStream(htmlFileModified);
                 OutputStreamWriter osw = new OutputStreamWriter(fos);
                 BufferedWriter writer = new BufferedWriter(osw))
            {
                final Object[][] replacements = new Object[][]{
                        {"Cornwall parkrun", (Supplier<String>) () -> "Cornwall Park parkrun"},
                        {"{{dialog}}", (Supplier<String>) () -> {
                            InputStream inputStream = this.getClass().getResourceAsStream("/dialog.html");
                            try
                            {
                                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                            }
                            catch (IOException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }}
                };

                String line;
                while (null != (line = reader.readLine()))
                {
                    String lineModified = line;
                    for (Object[] replacement : replacements)
                    {
                        final String criteria = (String) replacement[0];
                        if(line.contains(criteria))
                        {
                            lineModified = lineModified.replace(criteria, ((Supplier<String>)replacement[1]).get());
                        }
                    }
                    writer.write(lineModified + "\n");
                }
            }
        }
        return htmlFileModified;
    }

    private static String getStopDates()
    {
        return "[{'x':1670025600000,'y':1,'name':'Porirua'}]";
    }

    private void writeMostVolunteers(HtmlWriter writer) throws XMLStreamException
    {
        try (CollapsableTitleHtmlWriter ignored = new CollapsableTitleHtmlWriter(writer.writer, "Most Events, Volunteering"))
        {
            try (MostVolunteersTableHtmlWriter tableWriter = new MostVolunteersTableHtmlWriter(writer, urlGenerator))
            {
                List<Object[]> mostVolunteers = volunteerCountDao.getMostVolunteers();
                assert !mostVolunteers.isEmpty() : "WARNING: No records for Most Volunteers";
                for (Object[] record : mostVolunteers)
                {
                    tableWriter.writeRecord(new MostVolunteersTableHtmlWriter.Record(
                            (Athlete)record[0], (int) record[1], (int) record[2], (int) record[3]));
                }
            }
        }
    }

    private void writePIndex(HtmlWriter writer) throws XMLStreamException
    {
        try (CollapsableTitleHtmlWriter ignored = new CollapsableTitleHtmlWriter(writer.writer, "p-Index Tables"))
        {
            writer.writer.writeStartElement("p");
            writer.writer.writeCharacters("p-Index tables with credit to ");
            writer.writer.writeStartElement("a");
            writer.writer.writeAttribute("href", urlGenerator.generateAthleteUrl(4225353).toString());
            writer.writer.writeAttribute("style", "color:inherit;text-decoration:none;");
            writer.writer.writeCharacters("Dan Joe");
            writer.writer.writeEndElement(); // a
            writer.writer.writeEndElement(); // p

            // p-Index
            Set<Integer> regionalPIndexAthletes = new HashSet<>();
            try (PIndexTableHtmlWriter tableWriter = new PIndexTableHtmlWriter(writer.writer, urlGenerator, "New Zealand p-Index"))
            {
                List<PIndexTableHtmlWriter.Record> records = new ArrayList<>();
                for (Map.Entry<Integer, List<AthleteCourseSummary>> entry : athleteIdToAthleteCourseSummaries.entrySet())
                {
                    int athleteId = entry.getKey();
                    Athlete athlete = athleteIdToAthlete.get(athleteId);
                    List<AthleteCourseSummary> summariesForAthlete = entry.getValue();

//                    summariesForAthlete.stream().forEach(summary -> {
//                        if(summary.course.courseId == 21 && summary.countOfRuns >= 5)
//                        {
//                            System.out.printf("%s %d%n", athlete, summary.countOfRuns);
//                        }
//                    });

                    PIndex.Result globalPIndex = PIndex.pIndexAndNeeded(summariesForAthlete);
                    if (globalPIndex.pIndex <= MIN_P_INDEX)
                    {
                        continue;
                    }

                    PIndex.Result regionPIndex = PIndex.pIndexAndNeeded(summariesForAthlete.stream()
                            .filter(acs -> acs.course.country == NZ).collect(Collectors.toList()));
                    if (regionPIndex.pIndex <= MIN_P_INDEX)
                    {
                        continue;
                    }

                    {
                        AthleteCourseSummary maxAthleteCourseSummary = getMaxAthleteCourseSummary(summariesForAthlete);
                        Course globalHomeParkrun = maxAthleteCourseSummary.course;
                        assert globalHomeParkrun != null : "Home parkrun is null, how?";
                        if (globalHomeParkrun.country != NZ)
                        {
                            continue;
                        }
                        regionalPIndexAthletes.add(athleteId);
                    }

                    AthleteCourseSummary maxAthleteCourseSummaryInRegion = getMaxAthleteCourseSummaryInRegion(summariesForAthlete, NZ);
                    double provinceRunCount = getNzRegionRunCount(maxAthleteCourseSummaryInRegion.course, summariesForAthlete);
                    double totalRuns = getTotalRuns(summariesForAthlete);
                    double homeRatio = provinceRunCount / totalRuns;

                    // Add pIndex
                    records.add(new PIndexTableHtmlWriter.Record(athlete, globalPIndex, homeRatio, regionPIndex));
                }

                List<PIndexTableHtmlWriter.Record> recordsLastWeek = pIndexDao.getPIndexRecordsLastWeek().stream()
                        .filter(r -> regionalPIndexAthletes.contains(r.athleteId))
                        .map(r ->
                                new PIndexTableHtmlWriter.Record(
                                    athleteIdToAthlete.get(r.athleteId),
                                    new PIndex.Result(r.pIndex, r.neededForNextPIndex),
                                    r.homeRatio, null))
                        .collect(Collectors.toList());

                records.sort(PINDEX_RECORD_COMPARATOR);
                recordsLastWeek.sort(PINDEX_RECORD_COMPARATOR);

                calculatePIndexDeltas(records, recordsLastWeek);

                for (PIndexTableHtmlWriter.Record record : records)
                {
                    tableWriter.writePIndexRecord(record);
                }
            }

            // Legacy p-Index
            try (PIndexTableHtmlWriter tableWriter = new PIndexTableHtmlWriter(writer.writer, urlGenerator, "Legacy p-Index"))
            {
                List<PIndexTableHtmlWriter.Record> records = new ArrayList<>();
                for (Map.Entry<Integer, List<AthleteCourseSummary>> entry : athleteIdToAthleteCourseSummaries.entrySet())
                {
                    int athleteId = entry.getKey();
                    Athlete athlete = athleteIdToAthlete.get(athleteId);
                    List<AthleteCourseSummary> summariesForAthlete = entry.getValue();

                    PIndex.Result globalPIndex = PIndex.pIndexAndNeeded(summariesForAthlete);
                    if (globalPIndex.pIndex <= MIN_P_INDEX)
                    {
                        continue;
                    }

                    PIndex.Result regionPIndex = PIndex.pIndexAndNeeded(summariesForAthlete.stream()
                            .filter(acs -> acs.course.country == NZ).collect(Collectors.toList()));
                    if (regionPIndex.pIndex <= MIN_P_INDEX)
                    {
                        continue;
                    }

//                    {
//                        AthleteCourseSummary maxAthleteCourseSummary = getMaxAthleteCourseSummary(summariesForAthlete);
//                        Course globalHomeParkrun = maxAthleteCourseSummary.course;
//                        assert globalHomeParkrun != null : "Home parkrun is null, how?";
//                        if (globalHomeParkrun.country != NZ)
//                        {
//                            continue;
//                        }
//                        regionalPIndexAthletes.add(athleteId);
//                    }

                    AthleteCourseSummary maxAthleteCourseSummaryInRegion = getMaxAthleteCourseSummaryInRegion(summariesForAthlete, NZ);
                    double provinceRunCount = getNzRegionRunCount(maxAthleteCourseSummaryInRegion.course, summariesForAthlete);
                    double totalRuns = getTotalRuns(summariesForAthlete);
                    double homeRatio = provinceRunCount / totalRuns;
                    pIndexDao.writePIndexRecord(new PIndexDao.PIndexRecord(athleteId, globalPIndex.pIndex, globalPIndex.neededForNextPIndex, homeRatio));

                    // Add pIndex
                    boolean isRegionalPIndexAthlete = regionalPIndexAthletes.contains(athleteId);
                    records.add(new PIndexTableHtmlWriter.Record(athlete, globalPIndex, homeRatio, regionPIndex, isRegionalPIndexAthlete));
                }

                records.sort(PINDEX_RECORD_COMPARATOR);
                for (PIndexTableHtmlWriter.Record record : records)
                {
                    tableWriter.writePIndexRecord(record);
                    pIndexDao.writePIndexRecord(
                            new PIndexDao.PIndexRecord(
                                    record.athlete.athleteId, record.globalPIndex.pIndex, record.globalPIndex.neededForNextPIndex, record.homeRatio));
                }
            }
        }
    }

    private static double getTotalRuns(List<AthleteCourseSummary> summariesForAthlete)
    {
        int result = 0;
        for (AthleteCourseSummary acs : summariesForAthlete)
        {
            result += acs.countOfRuns;
        }
        return result;
    }

    private static AthleteCourseSummary getMaxAthleteCourseSummary(List<AthleteCourseSummary> summariesForAthlete)
    {
        AthleteCourseSummary result = null;
        for (AthleteCourseSummary acs : summariesForAthlete)
        {
            if(result == null || acs.countOfRuns > result.countOfRuns)
            {
                result = acs;
            }
        }
        return result;
    }

    private static AthleteCourseSummary getMaxAthleteCourseSummaryInRegion(List<AthleteCourseSummary> summariesForAthlete, Country country)
    {
        AthleteCourseSummary result = null;
        for (AthleteCourseSummary acs : summariesForAthlete)
        {
            if (acs.course.country == country)
            {
                if(result == null || acs.countOfRuns > result.countOfRuns)
                {
                    result = acs;
                }
            }
        }
        return result;
    }

    private void writeTop10Runs(HtmlWriter writer) throws XMLStreamException
    {
        Map<String, Integer> courseToCount = courseEventSummaryDao.getCourseCount();
        try (CollapsableTitleHtmlWriter ignored = new CollapsableTitleHtmlWriter(writer.writer, "Most Runs at Courses"))
        {
            // Populate top 10 runs at courses
            Top10RunsDao top10RunsDao = new Top10RunsDao(statsDataSource);
            List<Course> courses = courseRepository.getCourses(NZ).stream()
                    .filter(c -> c.status == RUNNING).collect(Collectors.toList());
            for (Course course : courses)
            {
                List<AtEvent> top10 = top10Dao.getTop10AtCourse(course.name);
                if (top10.isEmpty())
                {
                    System.out.println("* Populating top 10 run table for " + course.longName);
                    top10.addAll(top10RunsDao.getTop10AtEvent(course.courseId));
                    top10Dao.writeRunsAtEvents(top10);
                }
            }

            try (Top10InRegionHtmlWriter top10InRegionHtmlWriter = new Top10InRegionHtmlWriter(
                    writer.writer, urlGenerator, "New Zealand", "Run"))
            {
                List<AtEvent> top10InRegion = top10Dao.getTop10InRegion();

                assert !top10InRegion.isEmpty() : "WARNING: Top 10 runs in NZ list is empty";
                for (AtEvent r : top10InRegion)
                {
                    top10InRegionHtmlWriter.writeRecord(new Top10InRegionHtmlWriter.Record(r.athlete, r.course.longName, r.count, -1.d));
                }
            }

            List<Top10InRegionHtmlWriter.Record> clubDe90Percent = new ArrayList<>();
            for (Course course : courses)
            {
                List<AtEvent> top10 = top10Dao.getTop10AtCourse(course.name);
                for (AtEvent rae : top10)
                {
                    double courseCount = courseToCount.get(course.name);
                    double runCount = rae.count;
                    double percentage = runCount * 100.0 / courseCount;
                    if(percentage > 90 && runCount > 5)
                    {
                        clubDe90Percent.add(
                                new Top10InRegionHtmlWriter.Record(rae.athlete, course.longName, rae.count, percentage));
                    }
                }
            }
            clubDe90Percent.sort(Top10InRegionHtmlWriter.Record.COMPARATOR);
            try (Top10InRegionHtmlWriter top10atCourse = new Top10InRegionHtmlWriter(
                    writer.writer, urlGenerator, "90% Club", "Run", true))
            {
                for (Top10InRegionHtmlWriter.Record record : clubDe90Percent)
                {
                    top10atCourse.writeRecord(record);
                }
            }

            writer.writer.writeStartElement("hr");
            writer.writer.writeEndElement();

            for (Course course : courses)
            {
                try (Top10AtCourseHtmlWriter top10atCourse = new Top10AtCourseHtmlWriter(writer.writer, urlGenerator, course.longName, "Run"))
                {
                    List<AtEvent> top10 = top10Dao.getTop10AtCourse(course.name);
                    for (AtEvent rae : top10)
                    {
                        double courseCount = courseToCount.get(course.name);
                        double runCount = rae.count;
                        double percentage = runCount * 100.0 / courseCount;
                        top10atCourse.writeRecord(new Top10AtCourseHtmlWriter.Record(rae.athlete, rae.count, percentage));
                    }
                }
            }
        }
    }

    private void writeTop10Volunteers(HtmlWriter writer) throws XMLStreamException
    {
        Map<String, Integer> courseToCount = courseEventSummaryDao.getCourseCount();
        try (CollapsableTitleHtmlWriter ignored = new CollapsableTitleHtmlWriter(writer.writer, "Most Volunteers at Courses"))
        {
            Top10VolunteersDao top10VolunteersDao = new Top10VolunteersDao(statsDataSource);
            List<Course> courses = courseRepository.getCourses(NZ).stream()
                    .filter(c -> c.status == RUNNING).collect(Collectors.toList());
            for (Course course : courses)
            {
                List<AtEvent> top10 = top10VolunteerDao.getTop10VolunteersAtCourse(course.name);
                if (top10.isEmpty())
                {
                    System.out.println("* Populating top 10 volunteer table for " + course.longName);
                    top10.addAll(top10VolunteersDao.getTop10VolunteersAtEvent(course.courseId));
                    top10VolunteerDao.writeVolunteersAtEvents(top10);
                }
            }

            try (Top10InRegionHtmlWriter top10InRegionHtmlWriter = new Top10InRegionHtmlWriter(
                    writer.writer, urlGenerator,"New Zealand", "Volunteer"))
            {
                List<Object[]> top10VolunteersInRegion = top10VolunteerDao.getTop10VolunteersInRegion();
                assert !top10VolunteersInRegion.isEmpty() : "WARNING: Top 10 runs in NZ list is empty";

                for (Object[] record : top10VolunteersInRegion)
                {
                    Athlete athlete = (Athlete)record[0];
                    Course course = courseRepository.getCourse((int) record[1]);
                    int countOfVolunteersAtCourse = (int)record[2];
                    top10InRegionHtmlWriter.writeRecord(
                            new Top10InRegionHtmlWriter.Record(athlete, course.longName, countOfVolunteersAtCourse, -1d));
                }
            }

            List<Top10InRegionHtmlWriter.Record> clubDe90Percent = new ArrayList<>();
            for (Course course : courses)
            {
                List<VolunteersAtEvent> top10 = top10VolunteersDao.getTop10VolunteersAtEvent(course.courseId);
                for (AtEvent rae : top10)
                {
                    double courseCount = courseToCount.get(course.name);
                    double runCount = rae.count;
                    double percentage = runCount * 100.0 / courseCount;
                    if(percentage > 90 && runCount > 5)
                    {
                        clubDe90Percent.add(new Top10InRegionHtmlWriter.Record(rae.athlete, course.longName, rae.count, percentage));
                    }
                }
            }
            clubDe90Percent.sort(Top10InRegionHtmlWriter.Record.COMPARATOR);
            try (Top10InRegionHtmlWriter top10atCourse = new Top10InRegionHtmlWriter(
                    writer.writer, urlGenerator, "90% Club", "Volunteer", true))
            {
                for (Top10InRegionHtmlWriter.Record record : clubDe90Percent)
                {
                    top10atCourse.writeRecord(record);
                }
            }

            writer.writer.writeStartElement("hr");
            writer.writer.writeEndElement();

            for (Course course : courses)
            {
                try (Top10AtCourseHtmlWriter top10atCourse = new Top10AtCourseHtmlWriter(
                        writer.writer, urlGenerator, course.longName, "Volunteer"))
                {
                    List<AtEvent> top10 = top10VolunteerDao.getTop10VolunteersAtCourse(course.name);
                    for (AtEvent rae : top10)
                    {
                        double courseCount = courseToCount.get(course.name);
                        double volunteerCount = rae.count;
                        double percentage = volunteerCount * 100.0 / courseCount;
                        top10atCourse.writeRecord(new Top10AtCourseHtmlWriter.Record(rae.athlete, rae.count, percentage));
                    }
                }
            }
        }
    }

    private void writeMostEvents(HtmlWriter writer, List<MostEventsDao.MostEventsRecord> differentEventRecords, boolean extended) throws XMLStreamException
    {
        final Map<Integer, List<CourseDate>> athleteIdToFirstRuns;
        if(extended)
        {
            System.out.print("Getting first runs ");
            athleteIdToFirstRuns = new HashMap<>();

            mostEventsDao.getFirstRuns().forEach(record ->
            {
                int athleteId = (int) record[0];
                CourseDate firstRun = new CourseDate(courseRepository.getCourse((int) record[1]), (Date) record[2]);
                List<CourseDate> firstRuns = athleteIdToFirstRuns.computeIfAbsent(athleteId, k -> new ArrayList<>());
                firstRuns.add(firstRun);
            });
            System.out.println("DONE");
        }
        else
        {
            athleteIdToFirstRuns = emptyMap();
        }

        try (MostEventsTableHtmlWriter tableWriter = new MostEventsTableHtmlWriter(writer.writer, urlGenerator, extended))
        {
            for (MostEventsDao.MostEventsRecord der : differentEventRecords)
            {
                Athlete athlete = athleteIdToAthlete.get(der.athlete.athleteId);
                List<AthleteCourseSummary> athleteCourseSummaries = athleteIdToAthleteCourseSummaries.get(der.athlete.athleteId);

                int courseCount = athleteCourseSummaries.size();
                int totalCourseCount = athleteCourseSummaries.stream().mapToInt(acs -> acs.countOfRuns).sum();

                mostEventsDao.updateDifferentCourseRecord(athlete.athleteId, courseCount, totalCourseCount);

                final List<CourseDate> listOfFirstRuns;
                final String firstRuns;
                final int regionnaireCount;
                final String runsNeeded;
                if (extended)
                {
                    listOfFirstRuns = athleteIdToFirstRuns.get(der.athlete.athleteId);
                    listOfFirstRuns.sort(CourseDate.COMPARATOR);

                    regionnaireCount = getRegionnaireCount(new ArrayList<>(startDates), new ArrayList<>(), new ArrayList<>(listOfFirstRuns));
                    List<CourseDate> listOfRegionnaireDates = getListOfRegionnaireDates(new ArrayList<>(startDates), new ArrayList<>(), new ArrayList<>(listOfFirstRuns));

                    Object[] result = getRunsNeededAndMaxRunsNeeded(
                            new ArrayList<>(startDates), new ArrayList<>(stopDates), new ArrayList<>(listOfFirstRuns));
                    runsNeeded = result[0] + " (" + result[1] + ")";

                    String firstRunDatesHtmlString = listOfFirstRuns.stream().map(fr ->
                    {
                        int multiplier = listOfRegionnaireDates.contains(fr) ? -1 : 1;
                        return String.valueOf(multiplier * fr.date.getTime() / 1000);
                    }).collect(Collectors.joining(","));
                    firstRuns = "[" +
                            "[" + listOfFirstRuns.stream().map(fr -> String.valueOf(fr.course.courseId)).collect(Collectors.joining(",")) + "]," +
                            "[" + firstRunDatesHtmlString + "]" +
                            "]";
                }
                else
                {
                    runsNeeded = null;
                    firstRuns = null;
                    regionnaireCount = -1;
                }

                tableWriter.writeMostEventRecord(
                        new MostEventsTableHtmlWriter.Record(athlete,
                                der.differentRegionCourseCount, der.totalRegionRuns,
                                courseCount, totalCourseCount,
                                der.positionDelta, der.isNewEntry,
                                firstRuns,
                                regionnaireCount,
                                runsNeeded));
            }
        }
    }

    static Object[] getRunsNeededAndMaxRunsNeeded(
            List<CourseDate> sortedStartDates,
            List<CourseDate> sortedStopDates,
            List<CourseDate> sortedFirstRuns)
    {
        int maxBehind = -1;
        int behindNow = -1;
        final int totalEvents = sortedStartDates.size();
        final int totalEventsStopped = sortedStopDates.size();
        final int totalEventsRun = sortedFirstRuns.size();
        while(!sortedStartDates.isEmpty())
        {
            CourseDate startDate = sortedStartDates.removeFirst();
            sortedFirstRuns.removeIf(record -> record.date.getTime() <= startDate.date.getTime());
            sortedStopDates.removeIf(record -> record.date.getTime() <= startDate.date.getTime());

            int coursesDone = totalEventsRun - sortedFirstRuns.size();
            int coursesThatHaveStarted = totalEvents - sortedStartDates.size();
            int coursesThatHaveStopped = totalEventsStopped - sortedStopDates.size();
            int coursesRunning = coursesThatHaveStarted - coursesThatHaveStopped;
            int coursesRunningDone = Math.max(0, coursesDone - coursesThatHaveStopped);
            behindNow = coursesRunning - coursesRunningDone;
            maxBehind = Math.max(behindNow, maxBehind);
        }
        while(!sortedFirstRuns.isEmpty())
        {
            CourseDate firstRun = sortedFirstRuns.removeFirst();
            sortedStartDates.removeIf(record -> record.date.getTime() <= firstRun.date.getTime());
            sortedStopDates.removeIf(record -> record.date.getTime() <= firstRun.date.getTime());

            int coursesDone = totalEventsRun - sortedFirstRuns.size();
            int coursesThatHaveStarted = totalEvents - sortedStartDates.size();
            int coursesThatHaveStopped = totalEventsStopped - sortedStopDates.size();
            int coursesRunning = coursesThatHaveStarted - coursesThatHaveStopped;
            int coursesRunningDone = Math.max(0, coursesDone - coursesThatHaveStopped);
            behindNow = coursesRunning - coursesRunningDone;
            maxBehind = Math.max(behindNow, maxBehind);
        }
        return new Object[] { behindNow, maxBehind };
    }

    static int getRegionnaireCount(
            List<CourseDate> sortedStartDates,
            List<CourseDate> sortedStopDates,
            List<CourseDate> sortedFirstRuns)
    {
        return getListOfRegionnaireDates(sortedStartDates, sortedStopDates, sortedFirstRuns).size();
    }
    static List<CourseDate> getListOfRegionnaireDates(
            List<CourseDate> sortedStartDates,
            List<CourseDate> sortedStopDates,
            List<CourseDate> sortedFirstRuns)
        {
        final int totalEvents = sortedStartDates.size();
        final int totalEventsStopped = sortedStopDates.size();
        final int totalEventsRun = sortedFirstRuns.size();
        List<CourseDate> regionnaireDates = new ArrayList<>();
        while(!sortedFirstRuns.isEmpty())
        {
            CourseDate firstRun = sortedFirstRuns.remove(0);
            sortedStartDates.removeIf(record -> record.date.getTime() <= firstRun.date.getTime());
            sortedStopDates.removeIf(record -> record.date.getTime() <= firstRun.date.getTime());

            // Courses running
            int coursesThatAreStopped = totalEventsStopped - sortedStopDates.size();
            int courseThatAreRunning = totalEvents - coursesThatAreStopped - sortedStartDates.size();

            // Athlete Runs
            int coursesRunSoFar = totalEventsRun - sortedFirstRuns.size();

            if(coursesRunSoFar == courseThatAreRunning)
            {
                regionnaireDates.add(firstRun);
            }
        }
        return regionnaireDates;
    }

    private void writeAttendanceRecords(HtmlWriter writer) throws XMLStreamException
    {
        // Read attendance records from, and write html table
        try (AttendanceRecordsTableHtmlWriter tableWriter = new AttendanceRecordsTableHtmlWriter(writer.writer, urlGenerator))
        {
            tableWriter.writer.writeStartElement("tbody");
            List<AttendanceRecord> attendanceRecords = generateAndGetAttendanceRecords().stream()
                    .peek(ar -> {
                        Course course = courseRepository.getCourse(ar.courseId);
                        if(course.status == PENDING) ar.courseSmallTest = "not started yet";
                        else if(course.status == STOPPED) ar.courseSmallTest = "no longer takes place";
                        else if(ar.recentEventDate.before(date)) ar.courseSmallTest = "not run this week";
                    })
                    .collect(Collectors.toList());
            attendanceRecords.sort(Comparator.comparing(attendanceRecord ->
            {
                Course course = courseRepository.getCourse(attendanceRecord.courseId);
                return course.name;
            }));
            for (AttendanceRecord ar : attendanceRecords)
            {
                tableWriter.writeAttendanceRecord(ar, courseRepository.getCourse(ar.courseId));
            }
            tableWriter.writer.writeEndElement(); // tbody
        }
    }

    private List<AttendanceRecord> generateAndGetAttendanceRecords()
    {
        System.out.println("* Fetching attendance records *");
        List<AttendanceRecord> attendanceRecords = attendanceRecordsDao.getAttendanceRecords(date);

        System.out.println("* Calculate attendance deltas *");
        List<AttendanceRecord> attendanceRecordsFromLastWeek = getAttendanceRecordsForLastWeek();
        calculateAttendanceDeltas(attendanceRecords, attendanceRecordsFromLastWeek);
        return attendanceRecords;
    }

    private List<AttendanceRecord> getAttendanceRecordsForLastWeek()
    {
        try
        {
            return attendanceRecordsDao.getAttendanceRecords(lastWeek);
        }
        catch (Exception ex)
        {
            assert false : "WARNING No attendance records for last week.";
        }
        return emptyList();
    }

    private void downloadAthleteCourseSummaries(List<MostEventsDao.MostEventsRecord> differentEventRecords)
    {
        System.out.println("* Calculate athlete summaries that need to be downloaded (1. Most event table) *");
        Set<Integer> athletesFromMostEventTable = differentEventRecords.stream().map(der -> der.athlete.athleteId).collect(Collectors.toSet());
        System.out.println("Size A " + athletesFromMostEventTable.size());

        System.out.println("* Calculate athlete summaries that need to be downloaded (2. p-Index) *");
        Set<Integer> athletesWithMinimumPIndex = getAthletesFromDbWithMinimumPIndex(MIN_P_INDEX);
        System.out.println("Size B " + athletesWithMinimumPIndex.size());

        System.out.println("* Calculate athlete summaries that need to be downloaded (3. Volunteer Counts) *");
        Set<Integer> athletesFromVolunteers = volunteerDao.getMostVolunteers().stream()
                .map(record -> ((Athlete)record[0]).athleteId).collect(Collectors.toSet());
        System.out.println("Size C " + athletesFromVolunteers.size());

//        Set<Integer> athletesInMostEventsNotInResults = new HashSet<>(athletesFromMostEventTable);
//        athletesInMostEventsNotInResults.removeAll(athletesFromResults);
//        System.out.println("Athletes in most events, not in results. " + athletesInMostEventsNotInResults.size());
//
//        Set<Integer> athletesInResultsNotInMostEvents = new HashSet<>(athletesFromResults);
//        athletesInResultsNotInMostEvents.removeAll(athletesFromMostEventTable);
//        System.out.println("Athletes in results, not in most events. " + athletesInResultsNotInMostEvents.size());

        Set<Integer> athletesToDownload = new HashSet<>();
        {
            System.out.println("Merging athletes from most event");
            int before = athletesToDownload.size();
            athletesToDownload.addAll(athletesFromMostEventTable);
            int after = athletesToDownload.size();
            int added = athletesToDownload.size() - before;
            int notAdded = athletesFromMostEventTable.size() - added;
            System.out.println(String.format("Size before: %s, Size after: %s, Added: %d, Not added: %d",
                    before, after, added, notAdded));
        }
        {
            System.out.println("Merging athletes with pIndex");
            int before = athletesToDownload.size();
            athletesToDownload.addAll(athletesWithMinimumPIndex);
            int after = athletesToDownload.size();
            int added = athletesToDownload.size() - before;
            int notAdded = athletesWithMinimumPIndex.size() - added;
            System.out.println(String.format("Size before: %s, Size after: %s, Added: %d, Not added: %d",
                    before, after, added, notAdded));
        }
        {
            System.out.println("Merging athletes from most event volunteers");
            int before = athletesToDownload.size();
            athletesToDownload.addAll(athletesFromVolunteers);
            int after = athletesToDownload.size();
            int added = athletesToDownload.size() - before;
            int notAdded = athletesFromVolunteers.size() - added;
            System.out.println(String.format("Size before: %s, Size after: %s, Added: %d, Not added: %d",
                    before, after, added, notAdded));
        }

        Set<Integer> athletesAlreadyDownloaded = acsDao.getAthleteCourseSummaries().stream()
                .map(acs -> (int) acs[1]).collect(Collectors.toSet());
        System.out.println("Athletes already downloaded. " + athletesAlreadyDownloaded.size());
        athletesToDownload.removeAll(athletesAlreadyDownloaded);
        System.out.println("Athletes too download. " + athletesToDownload.size());

        List<Integer> listOfAthletesToDownload = new ArrayList<>(athletesToDownload);
        int countOfAthletesToDownload = listOfAthletesToDownload.size();
        System.out.println("Downloading athlete course summaries. Athletes too download. " + countOfAthletesToDownload);

        for (int i = 1; i <= countOfAthletesToDownload; i++)
        {
            int athleteId = listOfAthletesToDownload.get(i - 1);

            System.out.printf("Downloading %d of %d ", i, countOfAthletesToDownload);
            Parser parser = new Parser.Builder()
                    .url(urlGenerator.generateAthleteEventSummaryUrl(athleteId))
                    .courseNotFound(courseNotFound -> {
                        throw new RuntimeException("Course not found. " + courseNotFound);
                    })
                    .forEachAthleteCourseSummary(acsDao::writeAthleteCourseSummary)
                    .forEachVolunteerRecord(objects ->
                    {
                        String type = (String)objects[1];
                        if("Total Credits".equals(type))
                        {
                            int count = (int) objects[2];
                            volunteerCountDao.insertVolunteerCount(athleteId, count);
                        }
                    })
                    .build(courseRepository);
            parser.parse();
        }

        acsDao.getAthleteCourseSummaries().forEach(objects ->
        {
            Athlete athlete = Athlete.from((String) objects[0], (int) objects[1]);
            List<AthleteCourseSummary> summaries = athleteIdToAthleteCourseSummaries.get(athlete.athleteId);
            if (summaries == null)
            {
                athleteIdToAthlete.put(athlete.athleteId, athlete);
                summaries = new ArrayList<>();
                athleteIdToAthleteCourseSummaries.put(athlete.athleteId, summaries);
            }
            Course course = courseRepository.getCourse((int) objects[2]);
//            assert course != null : "Course is null: " + (int) objects[2];
            summaries.add(new AthleteCourseSummary(
                    athlete,
                    course,
                    (int) objects[3]
            ));
        });
    }

    private Set<Integer> getAthletesFromDbWithMinimumPIndex(int minPIndex)
    {
        Map<Integer, Map<Integer, Integer>> athleteToCourseCount = new HashMap<>();
        resultDao.tableScan(r ->
        {
            if (r.athlete.athleteId < 0)
            {
                return;
            }

            Map<Integer, Integer> courseToCount = athleteToCourseCount.get(r.athlete.athleteId);
            if (courseToCount == null)
            {
                courseToCount = new HashMap<>();
                courseToCount.put(r.courseId, 1);
                athleteToCourseCount.put(r.athlete.athleteId, courseToCount);
            }
            else
            {
                Integer count = courseToCount.get(r.courseId);
                if (count == null)
                {
                    courseToCount.put(r.courseId, 1);
                }
                else
                {
                    courseToCount.put(r.courseId, count + 1);
                }
            }
        });

        Set<Integer> athletes = new HashSet<>();
        athleteToCourseCount.forEach((athleteId, courseToCount) ->
        {
            int pIndex = PIndex.pIndex(new ArrayList<>(courseToCount.values()));
            if (pIndex >= minPIndex)
            {
                athletes.add(athleteId);
            }
        });
        return athletes;
    }

    private static void calculatePositionDeltas(List<MostEventsDao.MostEventsRecord> differentEventRecords,
                                                List<MostEventsDao.MostEventsRecord> differentEventRecordsFromLastWeek)
    {
        for (int indexThisWeek = 0; indexThisWeek < differentEventRecords.size(); indexThisWeek++)
        {
            MostEventsDao.MostEventsRecord thisWeek = differentEventRecords.get(indexThisWeek);

            boolean found = false;
            for (int indexLastWeek = 0; indexLastWeek < differentEventRecordsFromLastWeek.size(); indexLastWeek++)
            {
                MostEventsDao.MostEventsRecord lastWeek = differentEventRecordsFromLastWeek.get(indexLastWeek);

                if (thisWeek.athlete.athleteId == lastWeek.athlete.athleteId)
                {
                    found = true;
                    thisWeek.positionDelta = indexLastWeek - indexThisWeek;
                    break;
                }
            }
            if(!found) thisWeek.isNewEntry = true;
        }
    }

    private static void calculateAttendanceDeltas(List<AttendanceRecord> attendanceRecords,
                                           List<AttendanceRecord> attendanceRecordsFromLastWeek)
    {
        for (AttendanceRecord thisWeek : attendanceRecords)
        {
            for (AttendanceRecord lastWeek : attendanceRecordsFromLastWeek)
            {
                if (thisWeek.courseId == lastWeek.courseId)
                {
                    // Found course
                    thisWeek.maxAttendanceDelta = thisWeek.recordEventFinishers - lastWeek.recordEventFinishers;
                    thisWeek.recentAttendanceDelta = thisWeek.recentEventFinishers - lastWeek.recentEventFinishers;
                }
            }
        }
    }

    private void calculatePIndexDeltas(List<PIndexTableHtmlWriter.Record> pIndexRecords,
                                       List<PIndexTableHtmlWriter.Record> pIndexRecordsLastWeek)
    {
        for (int indexThisWeek = 0; indexThisWeek < pIndexRecords.size(); indexThisWeek++)
        {
            PIndexTableHtmlWriter.Record thisWeek = pIndexRecords.get(indexThisWeek);

            boolean found = false;
            for (int indexLastWeek = 0; indexLastWeek < pIndexRecordsLastWeek.size(); indexLastWeek++)
            {
                PIndexTableHtmlWriter.Record lastWeek = pIndexRecordsLastWeek.get(indexLastWeek);

                if (thisWeek.athlete.athleteId == lastWeek.athlete.athleteId)
                {
                    found = true;
                    thisWeek.positionDelta = indexLastWeek - indexThisWeek;
                    break;
                }
            }
            if(!found) thisWeek.isNewEntry = true;
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
            calResult.add(Calendar.DAY_OF_MONTH, -1);
        }
        throw new UnsupportedOperationException();
    }
}