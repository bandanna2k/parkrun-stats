package dnt.parkrun.stats;

import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.*;
import dnt.parkrun.database.stats.MostEventsDao;
import dnt.parkrun.database.weekly.AthleteCourseSummaryDao;
import dnt.parkrun.database.weekly.PIndexDao;
import dnt.parkrun.database.weekly.VolunteerCountDao;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.datastructures.stats.AttendanceRecord;
import dnt.parkrun.datastructures.stats.EventDateCount;
import dnt.parkrun.datastructures.stats.MostEventsRecord;
import dnt.parkrun.htmlwriter.HtmlWriter;
import dnt.parkrun.htmlwriter.StatsRecord;
import dnt.parkrun.htmlwriter.writers.*;
import dnt.parkrun.pindex.PIndex;
import dnt.parkrun.region.RegionChecker;
import dnt.parkrun.region.RegionCheckerFactory;
import dnt.parkrun.stats.invariants.CourseEventSummaryChecker;
import dnt.parkrun.stats.processors.AttendanceProcessor;
import dnt.parkrun.stats.processors.AverageAttendanceProcessor;
import dnt.parkrun.stats.processors.AverageTimeProcessor;
import dnt.parkrun.stats.processors.InauguralEventsProcessor;
import dnt.parkrun.stats.processors.mostevents.MostRunsAtCourseProcessor;
import dnt.parkrun.stats.processors.mostevents.MostVolunteersAtCourseProcessor;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dnt.parkrun.common.DateConverter.SEVEN_DAYS_IN_MILLIS;
import static dnt.parkrun.common.FindAndReplace.findAndReplace;
import static dnt.parkrun.common.FindAndReplace.getTextFromFile;
import static dnt.parkrun.common.ParkrunDay.getParkrunDay;
import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Course.Status.*;
import static dnt.parkrun.pindex.PIndex.pIndexAndNeeded;
import static java.util.Collections.reverseOrder;

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
    private final Map<String, Integer> courseToCount;

    /*
            02/03/2024
     */
    public static void main(String[] args) throws SQLException, IOException, XMLStreamException
    {
        Country country = args.length == 0 ? Country.NZ : Country.valueOf(args[0]);
        Date date = args.length > 1 ? DateConverter.parseWebsiteDate(args[1]) : getParkrunDay(new Date());

        Database database = new LiveDatabase(country, getDataSourceUrl(), "stats", "4b0e7ff1", "sudo");

        MostEventStats stats = MostEventStats.newInstance(database, date);

        {
            File file = stats.generateStats();
            File modified = new File(file.getAbsoluteFile().getParent() + "/modified_" + file.getName());

            findAndReplace(file, modified, fileReplacements());

            new ProcessBuilder("xdg-open", modified.getAbsolutePath()).start();
        }

        CourseEventSummaryChecker checker = new CourseEventSummaryChecker(database, System.currentTimeMillis());
        List<String> validate = checker.validate();
        if(!validate.isEmpty())
        {
            throw new RuntimeException(String.join("\n", validate));
        }
    }

    public static Object[][] fileReplacements()
    {
        return new Object[][]{
                {"Cornwall parkrun", (Supplier<String>) () -> "Cornwall Park parkrun"},
                {"{{dialog}}", (Supplier<String>) () ->
                {
                    InputStream inputStream = MostEventStats.class.getResourceAsStream("/dialog.html");
                    try
                    {
                        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }},
                {"{{css}}", (Supplier<String>) () ->
                        "<style>" +
                                getTextFromFile(MostEventStats.class.getResourceAsStream("/css/most_events.css")) +
                                "</style>"
                },
                {"{{meta}}", (Supplier<String>) () ->
                        getTextFromFile(MostEventStats.class.getResourceAsStream("/meta_most_events.xml"))
                }
        };
    }

    private final CourseRepository courseRepository;
    private final Country country;
    private final Date date;
    private final Database database;
    private final ResultDao resultDao;
    private final AthleteCourseSummaryDao acsDao;
    private final CourseEventSummaryDao courseEventSummaryDao;
    private final PIndexDao pIndexDao;
    private final VolunteerCountDao volunteerCountDao;
    private final VolunteerDao volunteerDao;

    private final Map<Integer, Athlete> athleteIdToAthlete;
    private final Map<Integer, List<AthleteCourseSummary>> athleteIdToAthleteCourseSummaries = new HashMap<>();
    private final List<CourseDate> startDates = new ArrayList<>();
    private final List<CourseDate> stopDates = new ArrayList<>();
    private final UrlGenerator urlGenerator;

    private final AverageAttendanceProcessor averageAttendanceProcessor = new AverageAttendanceProcessor();
    private final AttendanceProcessor attendanceProcessor = new AttendanceProcessor();
    private final AverageTimeProcessor averageTimeProcessor = new AverageTimeProcessor();
    private final MostRunsAtCourseProcessor mostRunsAtCourseProcessor = new MostRunsAtCourseProcessor();
    private final InauguralEventsProcessor inauguralEventsProcessor = new InauguralEventsProcessor();
    private final ResultDao.ResultProcessor[] processors = new ResultDao.ResultProcessor[] {
            mostRunsAtCourseProcessor,
            averageAttendanceProcessor,
            attendanceProcessor,
            averageTimeProcessor,
            inauguralEventsProcessor
    };

    private final MostVolunteersAtCourseProcessor mostVolunteersAtCourseProcessor = new MostVolunteersAtCourseProcessor();
    private final VolunteerDao.Processor[] volunteerProcessors = new VolunteerDao.Processor[]
            {
                    mostVolunteersAtCourseProcessor
            };

    private MostEventStats(Database database, Date date)
    {
        this.database = database;
        this.country = database.country;
        this.urlGenerator = new UrlGenerator(database.country.baseUrl);

        this.date = date;

        Date lastWeek = new Date();
        lastWeek.setTime(date.getTime() - SEVEN_DAYS_IN_MILLIS);

        this.acsDao = AthleteCourseSummaryDao.getInstance(database, this.date);
        this.pIndexDao = PIndexDao.getInstance(database, date);
        this.volunteerCountDao = VolunteerCountDao.getInstance(database, this.date);

        this.resultDao = new ResultDao(database);
        this.volunteerDao = new VolunteerDao(database);

        this.courseRepository = new CourseRepository();
        new CourseDao(database, courseRepository);
        this.courseEventSummaryDao = new CourseEventSummaryDao(database, courseRepository);

        System.out.print("* Getting course counts ");
        courseToCount = courseEventSummaryDao.getCourseCount();
        System.out.printf(" DONE%n");

        System.out.print("* Getting all athletes ");
        AthleteDao athleteDao = new AthleteDao(database);
        athleteIdToAthlete = athleteDao.getAllAthletes();
        System.out.printf(" DONE%n");
    }

    public static MostEventStats newInstance(Database database, Date date) throws SQLException
    {
        return new MostEventStats(database, date);
    }

    public File generateStats() throws IOException, XMLStreamException
    {
        System.out.print("Start result table scan with processors ... ");
        resultDao.tableScan(processors);
        System.out.println("Done");

        System.out.print("Start volunteer table scan with processors ... ");
        volunteerDao.tableScan(volunteerProcessors);
        System.out.println("Done");

        System.out.print("Getting start dates ");
        startDates.addAll(courseEventSummaryDao.getCourseStartDates());
        System.out.println("Done");

        System.out.print("Getting stop dates ");
        stopDates.addAll(courseEventSummaryDao.getCourseStopDates(country));
        System.out.println("Done");

        System.out.println("* Populate most events table *");
        MostEventsDao mostEventsDao = MostEventsDao.getOrCreate(database, date);
        mostEventsDao.populateMostEventsTable();

        System.out.println("* Get most events *");
        List<MostEventsRecord> differentEventRecords = mostEventsDao.getMostEvents();

        downloadAthleteCourseSummaries(differentEventRecords);

        Map<Integer, List<CourseDate>> athletesFirstRuns;
        {
            System.out.println("* Populate most event records (part 2) *");
            athletesFirstRuns = getAthletesFirstRuns(mostEventsDao);
            populateMostEventRecordsPart2(differentEventRecords, athletesFirstRuns);
            populateMostEventRecordsPart3(differentEventRecords, athletesFirstRuns);
        }

        // Write extra most event data to database
        for (MostEventsRecord der : differentEventRecords)
        {
            mostEventsDao.updateDifferentCourseRecord(der.athleteId, der.differentGlobalCourseCount, der.totalGlobalRuns, der.runsNeeded,
                    der.inauguralRuns, der.regionnaireCount);
        }

        // I need to have these re-sorted after updating with extra most event data. But is there a nicer way?
        System.out.println("* Get most events from database again *");
        differentEventRecords = mostEventsDao.getMostEvents();
        List<MostEventsRecord> mostEventRecordsFromLastWeekSorted = mostEventsDao.getMostEventsForLastWeek();
        populateMostEventRecordsPart3(differentEventRecords, athletesFirstRuns); // Grrr, another loop.

        System.out.println("* Calculate most event position deltas *");
        calculatePositionDeltas(differentEventRecords, mostEventRecordsFromLastWeekSorted);

        try (HtmlWriter writer = HtmlWriter.newInstance(date, country, "most_event_stats"))
        {
            writer.writer.writeCharacters("{{css}}");

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
            writer.writer.writeCharacters(new SimpleDateFormat("yyyy MMM dd HH:mm").format(new Date()));
            writer.writer.writeEndElement();

            writeAttendanceRecords(writer, false);

            writeMostEvents(writer, differentEventRecords);

            writer.writer.writeStartElement("hr");
            writer.writer.writeEndElement();

            writeAttendanceRecords(writer, true);

            writeMostEventsExtended(writer, differentEventRecords);

            writeMostVolunteers(writer);

            writePIndex(writer);

            writeTop10Runs(writer);

            writeTop10Volunteers(writer);
            return writer.getFile();
        }
    }

    private Map<Integer, List<CourseDate>> getAthletesFirstRuns(MostEventsDao mostEventsDao)
    {
        System.out.print("Getting first runs ");
        final Map<Integer, List<CourseDate>> athleteIdToFirstRuns = new HashMap<>();

        mostEventsDao.getFirstRuns().forEach(record ->
        {
            int athleteId = (int) record[0];
            CourseDate firstRun = new CourseDate(courseRepository.getCourse((int) record[1]), (Date) record[2]);
            List<CourseDate> firstRuns = athleteIdToFirstRuns.computeIfAbsent(athleteId, k -> new ArrayList<>());
            firstRuns.add(firstRun);
        });
        System.out.println("DONE");
        return athleteIdToFirstRuns;
    }

    private void sortMostEventRecords(List<MostEventsRecord> mostEventsRecords)
    {
        mostEventsRecords.sort((r1, r2) ->
        {
            if(r1.runsNeeded < r2.runsNeeded) return -1;
            if(r1.runsNeeded > r2.runsNeeded) return 1;
            if(r1.differentRegionCourseCount > r2.differentRegionCourseCount) return -1;
            if(r1.differentRegionCourseCount < r2.differentRegionCourseCount) return 1;
            if(r1.totalRegionRuns > r2.totalRegionRuns) return -1;
            if(r1.totalRegionRuns < r2.totalRegionRuns) return 1;
            if(r1.differentGlobalCourseCount > r2.differentGlobalCourseCount) return -1;
            if(r1.differentGlobalCourseCount < r2.differentGlobalCourseCount) return 1;
            if(r1.totalGlobalRuns > r2.totalGlobalRuns) return -1;
            if(r1.totalGlobalRuns < r2.totalGlobalRuns) return 1;
            if(r1.athleteId > r2.athleteId) return -1;
            if(r1.athleteId < r2.athleteId) return 1;
            return 0;
        });
    }

    private static String getStopDates()
    {
        return "[{'x':1670025600000,'y':1,'name':'Porirua'}]";
    }

    private void writeMostVolunteers(HtmlWriter writer) throws XMLStreamException
    {
        try (CollapsableTitleHtmlWriter ignored = new CollapsableTitleHtmlWriter.Builder(writer.writer, "Most Events, Volunteering").build())
        {
            try (MostVolunteersTableHtmlWriter tableWriter = new MostVolunteersTableHtmlWriter(writer, urlGenerator))
            {
                List<Object[]> mostVolunteers = volunteerCountDao.getMostVolunteers();
                assert !mostVolunteers.isEmpty() : "WARNING: No records for Most Volunteers";
                for (Object[] record : mostVolunteers)
                {
                    int vIndex = (int)record[4];
                    int neededForNextVIndex = (int)record[5];
                    tableWriter.writeRecord(new MostVolunteersTableHtmlWriter.Record(
                            (Athlete)record[0], (int) record[1], (int) record[2], (int) record[3], vIndex, neededForNextVIndex));
                }
            }
        }
    }

    private void writePIndex(HtmlWriter writer) throws XMLStreamException
    {
        RegionChecker regionChecker = new RegionCheckerFactory().getRegionChecker(country); // TODO Check this out regarding country
        try (CollapsableTitleHtmlWriter ignored = new CollapsableTitleHtmlWriter.Builder(writer.writer, "p-Index Tables").build())
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
            try (PIndexTableHtmlWriter tableWriter = new PIndexTableHtmlWriter(
                    country, writer.writer, urlGenerator, country.countryName + " p-Index"))
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

                    PIndex.Result globalPIndex = athleteCourseSummaryPIndex(summariesForAthlete);
                    if (globalPIndex.pIndex <= MIN_P_INDEX)
                    {
                        continue;
                    }

                    PIndex.Result regionPIndex = athleteCourseSummaryPIndex(summariesForAthlete.stream()
                            .filter(acs -> acs.course.country == country).collect(Collectors.toList()));
                    if (regionPIndex.pIndex <= MIN_P_INDEX)
                    {
                        continue;
                    }

                    {
                        AthleteCourseSummary maxAthleteCourseSummary = getMaxAthleteCourseSummary(summariesForAthlete);
                        Course globalHomeParkrun = maxAthleteCourseSummary.course;
                        assert globalHomeParkrun != null : "Home parkrun is null, how?";
                        if (globalHomeParkrun.country != country)
                        {
                            continue;
                        }
                        regionalPIndexAthletes.add(athleteId);
                    }

                    AthleteCourseSummary maxAthleteCourseSummaryInRegion = getMaxAthleteCourseSummaryInRegion(summariesForAthlete, country);
                    double provinceRunCount = regionChecker.getRegionRunCount(maxAthleteCourseSummaryInRegion.course, summariesForAthlete);
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
            try (PIndexTableHtmlWriter tableWriter = new PIndexTableHtmlWriter(
                    country, writer.writer, urlGenerator, "Legacy p-Index"))
            {
                List<PIndexTableHtmlWriter.Record> records = new ArrayList<>();
                for (Map.Entry<Integer, List<AthleteCourseSummary>> entry : athleteIdToAthleteCourseSummaries.entrySet())
                {
                    int athleteId = entry.getKey();
                    Athlete athlete = athleteIdToAthlete.get(athleteId);
                    List<AthleteCourseSummary> summariesForAthlete = entry.getValue();

                    PIndex.Result globalPIndex = athleteCourseSummaryPIndex(summariesForAthlete);
                    if (globalPIndex.pIndex <= MIN_P_INDEX)
                    {
                        continue;
                    }

                    PIndex.Result regionPIndex = athleteCourseSummaryPIndex(summariesForAthlete.stream()
                            .filter(acs -> acs.course.country == country).collect(Collectors.toList()));
                    if (regionPIndex.pIndex <= MIN_P_INDEX)
                    {
                        continue;
                    }

//                    {
//                        AthleteCourseSummary maxAthleteCourseSummary = getMaxAthleteCourseSummary(summariesForAthlete);
//                        Course globalHomeParkrun = maxAthleteCourseSummary.course;
//                        assert globalHomeParkrun != null : "Home parkrun is null, how?";
//                        if (globalHomeParkrun.country != country)
//                        {
//                            continue;
//                        }
//                        regionalPIndexAthletes.add(athleteId);
//                    }

                    AthleteCourseSummary maxAthleteCourseSummaryInRegion = getMaxAthleteCourseSummaryInRegion(summariesForAthlete, country);
                    double provinceRunCount = regionChecker.getRegionRunCount(maxAthleteCourseSummaryInRegion.course, summariesForAthlete);
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

    private void populateMostEventRecordsPart2(List<MostEventsRecord> mostEventsRecords,
                                               Map<Integer, List<CourseDate>> athleteIdToFirstRuns)
    {
        for (MostEventsRecord der : mostEventsRecords)
        {
            {
                List<AthleteCourseSummary> athleteCourseSummaries = athleteIdToAthleteCourseSummaries.get(der.athleteId);
                int globalCourseCount = athleteCourseSummaries.size();
                int globalTotalCourseCount = athleteCourseSummaries.stream().mapToInt(acs -> acs.countOfRuns).sum();

                der.differentGlobalCourseCount = globalCourseCount;
                der.totalGlobalRuns = globalTotalCourseCount;
            }

            {
                final List<CourseDate> listOfFirstRuns;
                listOfFirstRuns = athleteIdToFirstRuns.get(der.athleteId);
                listOfFirstRuns.sort(CourseDate.COMPARATOR);

                List<CourseDate> listOfRegionnaireDates = getListOfRegionnaireDates(startDates, stopDates, listOfFirstRuns);
                der.regionnaireCount = listOfRegionnaireDates.size();

                Object[] result = getRunsNeededAndMaxRunsNeeded(startDates, stopDates, listOfFirstRuns);
                der.runsNeeded = (int) result[0];
                der.maxRunsNeeded = (int) result[1];
            }

            {
                der.inauguralRuns = inauguralEventsProcessor.getInauguralCount(der.athleteId);
            }
        }
    }

    private void populateMostEventRecordsPart3(List<MostEventsRecord> mostEventsRecords,
                                               Map<Integer, List<CourseDate>> athleteIdToFirstRuns)
    {
        for (MostEventsRecord der : mostEventsRecords)
        {
            final List<CourseDate> listOfFirstRuns;
            listOfFirstRuns = athleteIdToFirstRuns.get(der.athleteId);
            listOfFirstRuns.sort(CourseDate.COMPARATOR);

            List<CourseDate> listOfRegionnaireDates = getListOfRegionnaireDates(startDates, stopDates, listOfFirstRuns);

            String firstRunDatesHtmlString = listOfFirstRuns.stream().map(fr ->
            {
                int multiplier = listOfRegionnaireDates.contains(fr) ? -1 : 1;
                return String.valueOf(multiplier * fr.date.getTime() / 1000);
            }).collect(Collectors.joining(","));
            der.firstRuns = "[" +
                    "[" + listOfFirstRuns.stream().map(fr -> String.valueOf(fr.course.courseId)).collect(Collectors.joining(",")) + "]," +
                    "[" + firstRunDatesHtmlString + "]" +
                    "]";
        }
    }

    private void writeMostEventsExtended(HtmlWriter writer,
                                         List<MostEventsRecord> mostEventsRecords) throws XMLStreamException
    {
        try(CollapsableTitleHtmlWriter collapse1 = new CollapsableTitleHtmlWriter.Builder(writer.writer, "Most Events (Extended)").build())
        {
            try (MostEventsTableHtmlWriter tableWriter = new MostEventsTableHtmlWriter(writer.writer, country, true))
            {
                for (MostEventsRecord mostEventsRecord : mostEventsRecords)
                {
                    tableWriter.writeMostEventRecord(mostEventsRecord);
                }
            }
        }
    }

    private void writeTop10Runs(HtmlWriter writer) throws XMLStreamException
    {
        try (CollapsableTitleHtmlWriter ignored = new CollapsableTitleHtmlWriter.Builder(writer.writer, "Most Runs at Courses")
                .open().build())
        {
            final List<StatsRecord> clubDe90Percent = new ArrayList<>();
            final List<StatsRecord> top10forRegion = new ArrayList<>();

            List<Course> courses = courseRepository.getCourses(country).stream()
                    .filter(c -> c.status == RUNNING).toList();
            for (Course course : courses)
            {
                // Runs 90% Club
                List<Object[]> mostRunsForCourse = mostRunsAtCourseProcessor.getMostRunsForCourse(course.courseId);
                mostRunsForCourse.forEach(r -> {
                    int athleteId = (int)r[0];
                    Athlete athlete = athleteIdToAthlete.get(athleteId);

                    int runCount = (int) r[1];
                    double courseCount = courseToCount.get(course.name); // TODO Change to courseId
                    double percentage = ((double)runCount) * 100.0 / courseCount;
                    if(percentage > 90 && ((double)runCount) > 5)
                    {
                        clubDe90Percent.add(new StatsRecord()
                                .athlete(athlete).course(course).count(runCount).percentage(percentage));
                    }
                });

                // Top 10 for region
                mostRunsForCourse.forEach(r -> {
                    int athleteId = (int)r[0];
                    Athlete athlete = athleteIdToAthlete.get(athleteId);

                    int runCount = (int) r[1];

                    top10forRegion.add(new StatsRecord().athlete(athlete).course(course).count(runCount));
                });
            }

            // Sort
            clubDe90Percent.sort(StatsRecord.COMPARATOR_FOR_SCORE);

            // Sort top 10 and then crop
            top10forRegion.sort(reverseOrder(Comparator.comparingInt(StatsRecord::count)));

            // Top 10 volunteers in region
            try(CollapsableTitleHtmlWriter collapsableWriter = new CollapsableTitleHtmlWriter.Builder(writer.writer, country.countryName)
                    .level(2).fontSizePercent(95).build())
            {
                try (Top10InRegionHtmlWriter top10InRegionHtmlWriter = new Top10InRegionHtmlWriter(
                        writer.writer, urlGenerator, "Run"))
                {
                    for (StatsRecord statsRecord : top10forRegion.subList(0, 20))
                    {
                        top10InRegionHtmlWriter.writeRecord(statsRecord);
                    }
                }
            }

            try(CollapsableTitleHtmlWriter collapsableWriter = new CollapsableTitleHtmlWriter.Builder(writer.writer, "90% Club")
                    .level(2).fontSizePercent(95).build())
            {
                try (Top10InRegionHtmlWriter top10atCourse = new Top10InRegionHtmlWriter(
                        writer.writer, urlGenerator, "Run", true))
                {
                    for (StatsRecord record : clubDe90Percent)
                    {
                        top10atCourse.writeRecord(record);
                    }
                }

                writer.writer.writeStartElement("center");
                writer.writer.writeStartElement("p");
                writer.writer.writeCharacters("* Table sorted by percentage x count");
                writer.writer.writeEndElement();
                writer.writer.writeEndElement();
            }
            writer.writer.writeStartElement("hr");
            writer.writer.writeEndElement();

            // Top 10 runs per course
            for (Course course : courses)
            {
                try(CollapsableTitleHtmlWriter collapsableWriter = new CollapsableTitleHtmlWriter.Builder(writer.writer, course.longName)
                        .level(2).fontSizePercent(95).build())
                {
                    try (Top10AtCourseHtmlWriter top10atCourse = new Top10AtCourseHtmlWriter(
                            writer.writer, urlGenerator, "Run"))
                    {
                        List<Object[]> mostRunsForCourse = mostRunsAtCourseProcessor.getMostRunsForCourse(course.courseId);
                        for (Object[] objects : mostRunsForCourse)
                        {
                            int athleteId = (int)objects[0];
                            Athlete athlete = athleteIdToAthlete.get(athleteId);

                            int runCount = (int)objects[1];

                            double courseCount = courseToCount.get(course.name);
                            double percentage = ((double) runCount) * 100.0 / courseCount;
                            top10atCourse.writeRecord(new StatsRecord()
                                    .athlete(athlete).count(runCount).percentage(percentage));
                        }
                    }
                }
            }
        }
    }

    private void writeTop10Volunteers(HtmlWriter writer) throws XMLStreamException
    {
        try (CollapsableTitleHtmlWriter ignored = new CollapsableTitleHtmlWriter.Builder(writer.writer, "Most Volunteers at Courses")
                .open().build())
        {
            final List<StatsRecord> clubDe90Percent = new ArrayList<>();
            final List<StatsRecord> top10forRegion = new ArrayList<>();

            List<Course> courses = courseRepository.getCourses(country).stream()
                    .filter(c -> c.status == RUNNING).toList();
            for (Course course : courses)
            {
                // Volunteer 90% Club
                List<Object[]> mostVolunteersForCourse = mostVolunteersAtCourseProcessor.getMostVolunteersForCourse(course.courseId);
                mostVolunteersForCourse.forEach(r -> {
                    int athleteId = (int)r[0];
                    Athlete athlete = athleteIdToAthlete.get(athleteId);

                    int runCount = (int) r[1];
                    double courseCount = courseToCount.get(course.name); // TODO Change to courseId
                    double percentage = ((double)runCount) * 100.0 / courseCount;
                    if(percentage > 90 && ((double)runCount) > 5)
                    {
                        clubDe90Percent.add(new StatsRecord()
                                .athlete(athlete).course(course).count(runCount).percentage(percentage));
                    }
                });

                // Top 10 for region
                mostVolunteersForCourse.forEach(r -> {
                    int athleteId = (int)r[0];
                    Athlete athlete = athleteIdToAthlete.get(athleteId);

                    int runCount = (int) r[1];

                    top10forRegion.add(new StatsRecord().athlete(athlete).course(course).count(runCount));
                });
            }

            // Sort
            clubDe90Percent.sort(StatsRecord.COMPARATOR_FOR_SCORE);

            // Sort top 10 and then crop
            top10forRegion.sort(reverseOrder(Comparator.comparingInt(StatsRecord::count)));

            // Top 10 volunteers in region
            try(CollapsableTitleHtmlWriter collapsableWriter = new CollapsableTitleHtmlWriter.Builder(writer.writer, country.countryName)
                    .level(2).fontSizePercent(95).build())
            {
                try (Top10InRegionHtmlWriter top10InRegionHtmlWriter = new Top10InRegionHtmlWriter(
                        writer.writer, urlGenerator, "Vol."))
                {
                    for (StatsRecord statsRecord : top10forRegion.subList(0, 20))
                    {
                        top10InRegionHtmlWriter.writeRecord(statsRecord);
                    }
                }
            }

            try(CollapsableTitleHtmlWriter collapsableWriter = new CollapsableTitleHtmlWriter.Builder(writer.writer, "90% Club")
                    .level(2).fontSizePercent(95).build())
            {
                try (Top10InRegionHtmlWriter top10atCourse = new Top10InRegionHtmlWriter(
                        writer.writer, urlGenerator, "Vol.", true))
                {
                    for (StatsRecord record : clubDe90Percent)
                    {
                        top10atCourse.writeRecord(record);
                    }
                }

                writer.writer.writeStartElement("center");
                writer.writer.writeStartElement("p");
                writer.writer.writeCharacters("* Table sorted by percentage x count");
                writer.writer.writeEndElement();
                writer.writer.writeEndElement();
            }
            writer.writer.writeStartElement("hr");
            writer.writer.writeEndElement();

            // Top 10 volunteers per course
            for (Course course : courses)
            {
                try(CollapsableTitleHtmlWriter collapsableWriter = new CollapsableTitleHtmlWriter.Builder(writer.writer, course.longName)
                        .level(2).fontSizePercent(95).build())
                {
                    try (Top10AtCourseHtmlWriter top10atCourse = new Top10AtCourseHtmlWriter(
                            writer.writer, urlGenerator, "Vol."))
                    {
                        List<Object[]> mostVolunteersForCourse = mostVolunteersAtCourseProcessor.getMostVolunteersForCourse(course.courseId);
                        for (Object[] objects : mostVolunteersForCourse)
                        {
                            int athleteId = (int)objects[0];
                            Athlete athlete = athleteIdToAthlete.get(athleteId);

                            int volunteerCount = (int)objects[1];

                            double courseCount = courseToCount.get(course.name);
                            double percentage = ((double) volunteerCount) * 100.0 / courseCount;
                            top10atCourse.writeRecord(new StatsRecord()
                                    .athlete(athlete).count(volunteerCount).percentage(percentage));
                        }
                    }
                }
            }
        }
    }

    private void writeMostEvents(HtmlWriter writer, List<MostEventsRecord> differentEventRecords) throws XMLStreamException
    {
        try(CollapsableTitleHtmlWriter collapse1 = new CollapsableTitleHtmlWriter.Builder(writer.writer, "Most Events").build())
        {
            try (MostEventsTableHtmlWriter tableWriter = new MostEventsTableHtmlWriter(writer.writer, country, false))
            {
                for (MostEventsRecord der : differentEventRecords)
                {
                    tableWriter.writeMostEventRecord(der);
                }
            }
        }
    }

    static Object[] getRunsNeededAndMaxRunsNeeded(
            List<CourseDate> startDates,
            List<CourseDate> stopDates,
            List<CourseDate> firstRuns)
    {
        List<CourseDate> sortedStartDates = new ArrayList<>(startDates);
        List<CourseDate> sortedStopDates = new ArrayList<>(stopDates);
        List<CourseDate> sortedFirstRuns = new ArrayList<>(firstRuns);
        sortedStartDates.sort(CourseDate.COMPARATOR);
        sortedStopDates.sort(CourseDate.COMPARATOR);
        sortedFirstRuns.sort(CourseDate.COMPARATOR);

        int maxBehind = -1;
        int behindNow = -1;
        Set<Integer> coursesDone = new HashSet<>();
        Set<Integer> coursesRunning = new HashSet<>();
        while(
                !(sortedFirstRuns.isEmpty() && sortedStartDates.isEmpty() && sortedStopDates.isEmpty()))
        {
            Date loopDate = getEarliestDate(sortedFirstRuns, sortedStartDates, sortedStopDates);

            CourseDate firstRun = null;
            CourseDate stopDate = null;
            if(!sortedFirstRuns.isEmpty() && loopDate.getTime() == sortedFirstRuns.getFirst().date.getTime())
            {
                firstRun = sortedFirstRuns.removeFirst();
                coursesDone.add(firstRun.course.courseId);
            }
            if(!sortedStartDates.isEmpty() && loopDate.getTime() == sortedStartDates.getFirst().date.getTime())
            {
                CourseDate startDate = sortedStartDates.removeFirst();
                coursesRunning.add(startDate.course.courseId);
            }
            if(!sortedStopDates.isEmpty() && loopDate.getTime() == sortedStopDates.getFirst().date.getTime())
            {
                stopDate = sortedStopDates.removeFirst();
                final int stopDateCourseId = stopDate.course.courseId;
                coursesRunning.removeIf(n -> n == stopDateCourseId);
            }

            Set<Integer> coursesNotDone = new HashSet<>(coursesRunning);
            coursesNotDone.removeAll(coursesDone);

            behindNow = coursesNotDone.size();
            maxBehind = Math.max(behindNow, maxBehind);

        }
        return new Object[] { behindNow, maxBehind };
    }

    static int getRegionnaireCount(
            List<CourseDate> startDates,
            List<CourseDate> stopDates,
            List<CourseDate> firstRuns)
    {
        return getListOfRegionnaireDates(startDates, stopDates, firstRuns).size();
    }
    static List<CourseDate> getListOfRegionnaireDates(
            List<CourseDate> startDates,
            List<CourseDate> stopDates,
            List<CourseDate> firstRuns)
    {
        List<CourseDate> sortedStartDates = new ArrayList<>(startDates);
        List<CourseDate> sortedStopDates = new ArrayList<>(stopDates);
        List<CourseDate> sortedFirstRuns = new ArrayList<>(firstRuns);
        sortedStartDates.sort(CourseDate.COMPARATOR);
        sortedStopDates.sort(CourseDate.COMPARATOR);
        sortedFirstRuns.sort(CourseDate.COMPARATOR);

        List<CourseDate> regionnaireDates = new ArrayList<>();

        Set<Integer> coursesDone = new HashSet<>();
        Set<Integer> coursesRunning = new HashSet<>();
        while(
                !(sortedFirstRuns.isEmpty() && sortedStartDates.isEmpty() && sortedStopDates.isEmpty()))
        {
            Date loopDate = getEarliestDate(sortedFirstRuns, sortedStartDates, sortedStopDates);

            CourseDate firstRun = null;
            CourseDate stopDate = null;
            if(!sortedFirstRuns.isEmpty() && loopDate.getTime() == sortedFirstRuns.getFirst().date.getTime())
            {
                firstRun = sortedFirstRuns.removeFirst();
                coursesDone.add(firstRun.course.courseId);
            }
            if(!sortedStartDates.isEmpty() && loopDate.getTime() == sortedStartDates.getFirst().date.getTime())
            {
                CourseDate startDate = sortedStartDates.removeFirst();
                coursesRunning.add(startDate.course.courseId);
            }
            if(!sortedStopDates.isEmpty() && loopDate.getTime() == sortedStopDates.getFirst().date.getTime())
            {
                stopDate = sortedStopDates.removeFirst();
                final int stopDateCourseId = stopDate.course.courseId;
                coursesRunning.removeIf(n -> n == stopDateCourseId);
            }

            if(firstRun == null && stopDate == null) continue;

            Set<Integer> coursesNotDone = new HashSet<>(coursesRunning);
            coursesNotDone.removeAll(coursesDone);

            if(coursesNotDone.isEmpty())
            {
                if(firstRun != null)
                    regionnaireDates.add(firstRun);
                else if(stopDate != null)
                    regionnaireDates.add(stopDate);
            }
        }
        return regionnaireDates;
    }

    @SafeVarargs
    private static Date getEarliestDate(List<CourseDate>... lists)
    {
        Date date = null;
        long minDate = Long.MAX_VALUE;
        for (List<CourseDate> list : lists)
        {
            if (!list.isEmpty())
            {
                minDate = Math.min(minDate, list.getFirst().date.getTime());
                date = new Date(minDate);
            }
        }
        return date;
    }

    private void writeAttendanceRecords(HtmlWriter writer, boolean extended) throws XMLStreamException
    {
        String title = extended ? "Attendance Records (Extended)" : "Attendance Records";
        try(CollapsableTitleHtmlWriter collapsableWriter = new CollapsableTitleHtmlWriter.Builder(writer.writer, title).build())
        {
            try (AttendanceRecordsTableHtmlWriter tableWriter =
                         new AttendanceRecordsTableHtmlWriter(writer.writer, urlGenerator, extended))
            {
                List<Course> courses = courseRepository.getCourses(country).stream()
                        .filter(c -> c.status == RUNNING).toList();
                for (Course course : courses)
                {
                    int courseId = course.courseId;
                    List<EventDateCount> maxAttendances = attendanceProcessor.getMaxAttendance(courseId);

                    EventDateCount lastAttendance = attendanceProcessor.getLastAttendance(courseId);

                    double averageAttendance = averageAttendanceProcessor.getAverageAttendance(courseId);
                    double recentAverageAttendance = averageAttendanceProcessor.getRecentAverageAttendance(courseId);

                    Time averageTime = averageTimeProcessor.getAverageTime(courseId);

                    AttendanceRecord ar = new AttendanceRecord(courseId, lastAttendance, maxAttendances);
                    if(course.status == PENDING) ar.courseSmallTest = "not started yet";
                    else if(course.status == STOPPED) ar.courseSmallTest = "no longer takes place";
                    else if(ar.recentEvent.date.before(date)) ar.courseSmallTest = "not run this week";

                    ar.recentAttendanceDelta = attendanceProcessor.getLastDifference(courseId);
                    ar.maxAttendanceDelta = attendanceProcessor.getMaxDifference(courseId);

                    tableWriter.writeAttendanceRecord(course, ar,
                            averageAttendance, recentAverageAttendance, averageTime);
                }
            }
        }
    }

    private void downloadAthleteCourseSummaries(List<MostEventsRecord> differentEventRecords)
    {
        System.out.println("* Calculate athlete summaries that need to be downloaded (1. Most event table) *");
        Set<Integer> athletesFromMostEventTable = differentEventRecords.stream().map(der -> der.athleteId).collect(Collectors.toSet());
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

            List<Integer> listOfVolunteers = new ArrayList<>();
            List<Integer> listOfTotalCredits = new ArrayList<>();

            Parser parser = new Parser.Builder()
                    .webpageProvider(new WebpageProviderImpl(urlGenerator.generateAthleteEventSummaryUrl(athleteId)))
                    .courseNotFound(courseNotFound -> {
                        throw new RuntimeException("Course not found, possibly renamed. " + courseNotFound);
                    })
                    .forEachAthleteCourseSummary(acsDao::writeAthleteCourseSummary)
                    .forEachVolunteerRecord(objects ->
                    {
                        String type = (String)objects[1];
                        int count = (int) objects[2];
                        if("Total Credits".equals(type))
                        {
                            listOfTotalCredits.add(count);
                        }
                        else
                        {
                            listOfVolunteers.add(count);
                        }
                    })
                    .build(courseRepository);
            parser.parse();

            assert listOfTotalCredits.size() == 1;
            PIndex.Result vIndex = pIndexAndNeeded(listOfVolunteers);
            volunteerCountDao.insertVolunteerCount(athleteId, listOfTotalCredits.getFirst(),
                    vIndex.pIndex, vIndex.neededForNextPIndex);
        }

        acsDao.getAthleteCourseSummaries().forEach(objects ->
        {
            Athlete athlete = Athlete.from((String) objects[0], (int) objects[1]);
            List<AthleteCourseSummary> summaries =
                    athleteIdToAthleteCourseSummaries.computeIfAbsent(athlete.athleteId, k -> new ArrayList<>());
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

    private static void calculatePositionDeltas(List<MostEventsRecord> differentEventRecords,
                                                List<MostEventsRecord> differentEventRecordsFromLastWeek)
    {
        for (int indexThisWeek = 0; indexThisWeek < differentEventRecords.size(); indexThisWeek++)
        {
            MostEventsRecord thisWeek = differentEventRecords.get(indexThisWeek);

            boolean found = false;
            for (int indexLastWeek = 0; indexLastWeek < differentEventRecordsFromLastWeek.size(); indexLastWeek++)
            {
                MostEventsRecord lastWeek = differentEventRecordsFromLastWeek.get(indexLastWeek);

                if (thisWeek.athleteId == lastWeek.athleteId)
                {
                    found = true;
                    thisWeek.positionDelta = indexLastWeek - indexThisWeek;
                    break;
                }
            }
            if(!found) thisWeek.isNewEntry = true;
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

    private static PIndex.Result athleteCourseSummaryPIndex(List<AthleteCourseSummary> listOfRuns)
    {
        return pIndexAndNeeded(new ArrayList<>(
                listOfRuns.stream().map(acs -> acs.countOfRuns).toList()));
    }
}
