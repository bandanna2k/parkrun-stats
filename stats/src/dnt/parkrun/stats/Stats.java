package dnt.parkrun.stats;

import com.mysql.jdbc.Driver;
import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.CourseEventSummaryDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.database.StatsDao;
import dnt.parkrun.database.weekly.AthleteCourseSummaryDao;
import dnt.parkrun.database.weekly.PIndexDao;
import dnt.parkrun.database.weekly.Top10AtCourseDao;
import dnt.parkrun.database.weekly.Top10VoluteersAtCourseDao;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.AthleteCourseSummary;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.datastructures.stats.AtEvent;
import dnt.parkrun.datastructures.stats.AttendanceRecord;
import dnt.parkrun.htmlwriter.*;
import dnt.parkrun.pindex.PIndex;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static dnt.parkrun.common.DateConverter.SEVEN_DAYS_IN_MILLIS;
import static dnt.parkrun.common.UrlGenerator.generateAthleteEventSummaryUrl;
import static dnt.parkrun.database.StatsDao.DifferentCourseCount;
import static dnt.parkrun.datastructures.Country.NZ;
import static dnt.parkrun.datastructures.Course.Status.RUNNING;
import static dnt.parkrun.region.Region.getNzRegionRunCount;
import static java.util.Collections.emptyList;

public class Stats
{
    public static final String PARKRUN_CO_NZ = "parkrun.co.nz";
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
        Stats stats = Stats.newInstance(date);
        stats.generateStats();
    }

    private final Date date;
    private final Date lastWeek;
    private final StatsDao statsDao;
    private final ResultDao resultDao;
    private final AthleteCourseSummaryDao acsDao;
    private final Top10AtCourseDao top10Dao;
    private final Top10VoluteersAtCourseDao top10VolunteerDao;
    private final CourseEventSummaryDao courseEventSummaryDao;
    private final PIndexDao pIndexDao;
    private final Map<Integer, Athlete> athleteIdToAthlete = new HashMap<>();
    private final Map<Integer, List<AthleteCourseSummary>> athleteIdToAthleteCourseSummaries = new HashMap<>();

    private Stats(DataSource dataSource,
                  DataSource statsDataSource,
                  Date date)
    {
        this.date = date;
        lastWeek = new Date();
        lastWeek.setTime(date.getTime() - SEVEN_DAYS_IN_MILLIS);

        this.statsDao = new StatsDao(statsDataSource, this.date);
        this.acsDao = new AthleteCourseSummaryDao(statsDataSource, this.date);
        this.top10Dao = new Top10AtCourseDao(statsDataSource, this.date);
        this.top10VolunteerDao = new Top10VoluteersAtCourseDao(statsDataSource, this.date);
        this.resultDao = new ResultDao(dataSource);
        this.pIndexDao = new PIndexDao(statsDataSource, date);

        this.courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);
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
        List<DifferentCourseCount> differentEventRecordsFromLastWeek = getDifferentCourseCountForLastWeek();

        System.out.println("* Calculate most event position deltas *");
        calculatePositionDeltas(differentEventRecords, differentEventRecordsFromLastWeek);

        downloadAthleteCourseSummaries(differentEventRecords);

        try (HtmlWriter writer = HtmlWriter.newInstance(date))
        {
            writer.writer.writeStartElement("p");
            writer.writer.writeAttribute("align", "right");
            writer.writer.writeCharacters(new SimpleDateFormat("yyyy MMM dd hh:mm").format(new Date()));
            writer.writer.writeEndElement();

            writeAttendanceRecords(writer);

            writeMostEvents(writer, differentEventRecords);

            writer.writer.writeStartElement("hr");
            writer.writer.writeEndElement();

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
                String line;
                while (null != (line = reader.readLine()))
                {
                    String lineModified = line.replace("Cornwall parkrun", "Cornwall Park parkrun");
                    writer.write(lineModified + "\n");
                }
            }
        }
        new ProcessBuilder("xdg-open", htmlFileModified.getAbsolutePath()).start();
    }

    private void writePIndex(HtmlWriter writer) throws XMLStreamException
    {
        try (CollapsableTitleHtmlWriter ignored = new CollapsableTitleHtmlWriter(writer.writer, " P-Index (New Zealand)"))
        {
            writer.writer.writeStartElement("p");
            writer.writer.writeAttribute("style", "margin-left:100px");
            writer.writer.writeCharacters("p-Index tables with credit to ");
            writer.writer.writeStartElement("a");
            writer.writer.writeAttribute("href", UrlGenerator.generateAthleteUrl(NZ.baseUrl, 4225353).toString());
            writer.writer.writeAttribute("style", "color:inherit;text-decoration:none;");
            writer.writer.writeCharacters("Dan Joe");
            writer.writer.writeEndElement(); // a
            writer.writer.writeEndElement(); // p

            // p-Index
            Set<Integer> regionalPIndexAthletes = new HashSet<>();
            try (PIndexTableHtmlWriter tableWriter = new PIndexTableHtmlWriter(writer.writer, "p-Index (New Zealand)"))
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

                    // Calculate max count
                    AthleteCourseSummary maxAthleteCourseSummary = getMaxAthleteCourseSummary(summariesForAthlete);
                    Course homeParkrun = maxAthleteCourseSummary.course;
                    if (homeParkrun == null)
                    {
                        continue; // TODO: How can this happen???
                    }
                    if (homeParkrun.country != NZ)
                    {
                        continue;
                    }
                    regionalPIndexAthletes.add(athleteId);

                    double provinceRunCount = getNzRegionRunCount(homeParkrun, summariesForAthlete);
                    double totalRuns = getTotalRuns(summariesForAthlete);
                    double homeRatio = provinceRunCount / totalRuns;

                    // Add pIndex
                    records.add(new PIndexTableHtmlWriter.Record(athlete, globalPIndex, homeRatio));
                }

                List<PIndexTableHtmlWriter.Record> recordsLastWeek = pIndexDao.getPIndexRecordsLastWeek().stream()
                        .filter(r -> regionalPIndexAthletes.contains(r.athleteId))
                        .map(r ->
                                new PIndexTableHtmlWriter.Record(
                                    athleteIdToAthlete.get(r.athleteId),
                                    new PIndex.Result(r.pIndex, r.neededForNextPIndex),
                                    r.homeRatio))
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
            try (PIndexTableHtmlWriter tableWriter = new PIndexTableHtmlWriter(writer.writer, "Legacy p-Index (New Zealand)"))
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

                    // Calculate max count
                    AthleteCourseSummary maxAthleteCourseSummary = getMaxAthleteCourseSummary(summariesForAthlete);
                    Course homeParkrun = maxAthleteCourseSummary.course;
                    double homeRatio;
                    if (homeParkrun == null)
                    {
                        homeRatio = 0;
                    }
                    else
                    {
                        double provinceRunCount = getNzRegionRunCount(homeParkrun, summariesForAthlete);
                        double totalRuns = getTotalRuns(summariesForAthlete);
                        homeRatio = provinceRunCount / totalRuns;
                    }
                    pIndexDao.writePIndexRecord(new PIndexDao.PIndexRecord(athleteId, globalPIndex.pIndex, globalPIndex.neededForNextPIndex, homeRatio));

                    // Add pIndex
                    boolean isRegionalPIndexAthlete = regionalPIndexAthletes.contains(athleteId);
                    records.add(new PIndexTableHtmlWriter.Record(athlete, globalPIndex, homeRatio, isRegionalPIndexAthlete));
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
        AthleteCourseSummary result = summariesForAthlete.get(0);
        for (AthleteCourseSummary acs : summariesForAthlete)
        {
            if (acs.countOfRuns > result.countOfRuns)
            {
                result = acs;
            }
        }
        return result;
    }

    private void writeTop10Runs(HtmlWriter writer) throws XMLStreamException
    {
        Map<String, Integer> courseToCount = courseEventSummaryDao.getCourseCount();
        try (CollapsableTitleHtmlWriter ignored = new CollapsableTitleHtmlWriter(writer.writer, "Most Runs at Courses (New Zealand)"))
        {
            try (Top10InRegionHtmlWriter top10InRegionHtmlWriter = new Top10InRegionHtmlWriter(writer.writer, "New Zealand"))
            {
                List<AtEvent> top10InRegion = top10Dao.getTop10InRegion();
                for (AtEvent r : top10InRegion)
                {
                    top10InRegionHtmlWriter.writeRecord(new Top10InRegionHtmlWriter.Record(r.athlete, r.course.longName, r.count));
                }
            }

            writer.writer.writeStartElement("hr");
            writer.writer.writeEndElement();

            List<Course> courses = courseRepository.getCourses(NZ).stream()
                    .filter(c -> c.status == RUNNING).collect(Collectors.toList());
            for (Course course : courses)
            {
                try (Top10AtCourseHtmlWriter top10atCourse = new Top10AtCourseHtmlWriter(writer.writer, course.longName, "Run"))
                {
                    List<AtEvent> top10 = top10Dao.getTop10AtCourse(course.name);
                    if (top10.isEmpty())
                    {
                        System.out.println("* Getting top 10 athletes for " + course.longName);
                        top10.addAll(statsDao.getTop10AtEvent(course.courseId));
                        top10Dao.writeRunsAtEvents(top10);
                    }

                    for (AtEvent rae : top10)
                    {
                        double courseCount = courseToCount.get(course.name);
                        double runCount = rae.count;
                        String percentage = String.format("%.1f", runCount * 100 / courseCount);
                        top10atCourse.writeRecord(new Top10AtCourseHtmlWriter.Record(rae.athlete, rae.count, percentage));
                    }
                }
            }
        }
    }

    private void writeTop10Volunteers(HtmlWriter writer) throws XMLStreamException
    {
        Map<String, Integer> courseToCount = courseEventSummaryDao.getCourseCount();
        try (CollapsableTitleHtmlWriter ignored = new CollapsableTitleHtmlWriter(writer.writer, "Most Volunteers at Courses (New Zealand)"))
        {
            /*
            try (Top10AtCourseHtmlWriter top10atCourse = new Top10AtCourseHtmlWriter(writer.writer, "New Zealand", "Volunteer"))
            {
                List<Object[]> top10 = top10VolunteerDao.getTop10VolunteersInRegion();
                for (Object[] vir : top10)
                {
                    top10atCourse.writeRecord(new Top10AtCourseHtmlWriter.Record((Athlete)vir[0], (int)vir[1], null));
                }
            }

            writer.writer.writeStartElement("hr");
            writer.writer.writeEndElement();
             */

            List<Course> courses = courseRepository.getCourses(NZ).stream()
                    .filter(c -> c.status == RUNNING).collect(Collectors.toList());
            for (Course course : courses)
            {
                try (Top10AtCourseHtmlWriter top10atCourse = new Top10AtCourseHtmlWriter(writer.writer, course.longName, "Volunteer"))
                {
                    List<AtEvent> top10 = top10VolunteerDao.getTop10VolunteersAtCourse(course.name);
                    if (top10.isEmpty())
                    {
                        System.out.println("* Getting top 10 volunteers for " + course.longName);
                        top10.addAll(statsDao.getTop10VolunteersAtEvent(course.courseId));
                        top10VolunteerDao.writeVolunteersAtEvents(top10);
                    }

                    for (AtEvent rae : top10)
                    {
                        double courseCount = courseToCount.get(course.name);
                        double volunteerCount = rae.count;
                        String percentage = String.format("%.1f", volunteerCount * 100 / courseCount);
                        top10atCourse.writeRecord(new Top10AtCourseHtmlWriter.Record(rae.athlete, rae.count, percentage));
                    }
                }
            }
        }
    }

    private void writeMostEvents(HtmlWriter writer, List<DifferentCourseCount> differentEventRecords) throws XMLStreamException
    {
        try (MostEventsTableHtmlWriter tableWriter = new MostEventsTableHtmlWriter(writer.writer))
        {
            for (DifferentCourseCount der : differentEventRecords)
            {
                Athlete athlete = athleteIdToAthlete.get(der.athleteId);
                List<AthleteCourseSummary> athleteCourseSummaries = athleteIdToAthleteCourseSummaries.get(der.athleteId);

                int courseCount = athleteCourseSummaries.size();
                int totalCourseCount = athleteCourseSummaries.stream().mapToInt(acs -> acs.countOfRuns).sum();

                statsDao.updateDifferentCourseRecord(athlete.athleteId, courseCount, totalCourseCount, 0);

                tableWriter.writeMostEventRecord(
                        new MostEventsTableHtmlWriter.Record(athlete,
                                der.differentRegionCourseCount, der.totalRegionRuns,
                                courseCount, totalCourseCount, der.positionDelta, der.pIndex));
            }
        }
    }

    private List<DifferentCourseCount> getDifferentCourseCountForLastWeek()
    {
        try
        {
            return statsDao.getDifferentCourseCount(lastWeek);
        }
        catch (Exception ex)
        {
            System.out.println("WARNING No different course count for last week");
        }
        return emptyList();
    }

    private void writeAttendanceRecords(HtmlWriter writer) throws XMLStreamException
    {
        // Read attendance records from, and write html table
        try (AttendanceRecordsTableHtmlWriter tableWriter = new AttendanceRecordsTableHtmlWriter(writer.writer))
        {
            tableWriter.writer.writeStartElement("tbody");
            List<AttendanceRecord> attendanceRecords = getAttendanceRecords().stream()
                    .filter(ar -> ar.recordEventFinishers != 0)
                    .collect(Collectors.toList());
            attendanceRecords.sort(Comparator.comparing(attendanceRecord -> attendanceRecord.courseName));
            for (AttendanceRecord ar : attendanceRecords)
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
        List<AttendanceRecord> attendanceRecordsFromLastWeek = getAttendanceRecordsForLastWeek();
        calculateAttendanceDeltas(attendanceRecords, attendanceRecordsFromLastWeek);
        return attendanceRecords;
    }

    private List<AttendanceRecord> getAttendanceRecordsForLastWeek()
    {
        try
        {
            return statsDao.getAttendanceRecords(lastWeek);
        }
        catch (Exception ex)
        {
            System.out.println("WARNING No attendance records for last week.");
        }
        return emptyList();
    }

    private void downloadAthleteCourseSummaries(List<DifferentCourseCount> differentEventRecords) throws MalformedURLException
    {
        System.out.println("* Calculate athlete summaries that need to be downloaded (1. Most event table) *");
        Set<Integer> athletesFromMostEventTable = differentEventRecords.stream().map(der -> der.athleteId).collect(Collectors.toSet());
        System.out.println("Size A " + athletesFromMostEventTable.size());

        System.out.println("* Calculate athlete summaries that need to be downloaded (2. Results table) *");
        Set<Integer> athletesFromResults = getAthletesFromDbWithMinimumPIndex(MIN_P_INDEX);
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
        Set<Integer> athletesAlreadyDownloaded = acsDao.getAthleteCourseSummaries().stream().map(acs -> (int) acs[0]).collect(Collectors.toSet());
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
                    .url(generateAthleteEventSummaryUrl(PARKRUN_CO_NZ, athleteId))
                    .forEachAthleteCourseSummary(acsDao::writeAthleteCourseSummary)
                    .build(courseRepository);
            parser.parse();
        }
        for (int athlete_id : athletesToDownload)
        {
            Parser parser = new Parser.Builder()
                    .url(generateAthleteEventSummaryUrl(PARKRUN_CO_NZ, athlete_id))
                    .forEachAthleteCourseSummary(acsDao::writeAthleteCourseSummary)
                    .build(courseRepository);
            parser.parse();
        }

        acsDao.getAthleteCourseSummariesMap().forEach(objects ->
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

    private static void calculatePositionDeltas(List<DifferentCourseCount> differentEventRecords,
                                                List<DifferentCourseCount> differentEventRecordsFromLastWeek)
    {
        for (int indexThisWeek = 0; indexThisWeek < differentEventRecords.size(); indexThisWeek++)
        {
            for (int indexLastWeek = 0; indexLastWeek < differentEventRecordsFromLastWeek.size(); indexLastWeek++)
            {
                DifferentCourseCount thisWeek = differentEventRecords.get(indexThisWeek);
                DifferentCourseCount lastWeek = differentEventRecordsFromLastWeek.get(indexLastWeek);

                if (thisWeek.athleteId == lastWeek.athleteId)
                {
                    // Found athlete
                    thisWeek.positionDelta = indexLastWeek - indexThisWeek;
                }
            }
        }
    }

    private static void calculateAttendanceDeltas(List<AttendanceRecord> attendanceRecords,
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

    private void calculatePIndexDeltas(List<PIndexTableHtmlWriter.Record> pIndexRecords,
                                       List<PIndexTableHtmlWriter.Record> pIndexRecordsLastWeek)
    {
        for (int indexThisWeek = 0; indexThisWeek < pIndexRecords.size(); indexThisWeek++)
        {
            for (int indexLastWeek = 0; indexLastWeek < pIndexRecordsLastWeek.size(); indexLastWeek++)
            {
                PIndexTableHtmlWriter.Record thisWeek = pIndexRecords.get(indexThisWeek);
                PIndexTableHtmlWriter.Record lastWeek = pIndexRecordsLastWeek.get(indexLastWeek);

                if (thisWeek.athlete.athleteId == lastWeek.athlete.athleteId)
                {
                    // Found athlete
                    thisWeek.positionDelta = indexLastWeek - indexThisWeek;
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
            calResult.add(Calendar.DAY_OF_MONTH, -1);
        }
        throw new UnsupportedOperationException();
    }
}
