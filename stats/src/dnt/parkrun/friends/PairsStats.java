package dnt.parkrun.friends;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.FindAndReplace;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.*;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.htmlwriter.HtmlWriter;
import dnt.parkrun.htmlwriter.writers.EventTableHtmlWriter;
import dnt.parkrun.htmlwriter.writers.PairsTableHtmlWriter;
import dnt.parkrun.stats.MostEventStats;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static dnt.parkrun.common.FindAndReplace.getTextFromFile;
import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;

public class PairsStats
{
    private final ResultDao resultDao;
    private final AthleteDao athleteDao;
    private final CourseRepository courseRepository;

    public static void main(String[] args) throws XMLStreamException, IOException
    {
//        final Country country = Country.valueOf(args[0]);
        Database database = new LiveDatabase(NZ, getDataSourceUrl(), "stats", "4b0e7ff1");

        PairsStats pairsStats = new PairsStats(database);
        /*
        File file = pairsStats.generateStats(
                1340853, // Jonathan
                293223, // Julie GORDON
                1048005, // Sarah JANTSCHER
                4225353, // Dan JOE
//                293227, // Paul GORDON
                2147564, // Alison KING
                141825, // Andrew CAPEL
                2225176, // Andy MEARS
                391111, // Gene RAND
                291411, // Martin O'SULLIVAN
                1590564, // Yvonne TSE
//                116049, // Richard NORTH
                414811, // David NORTH
                547976, // Allan JANES
                4072508 // Zoe NORTH
        );
        */
        File file = pairsStats.generateStats(
                4225353, // Dan JOE
                291411, // Martin O'SULLIVAN
                6459026, // Nathan HEAVER
                796322 // Tim ROBBINS
        );
        File modified = new File(file.getAbsoluteFile().getParent() + "/modified_" + file.getName());

        final Object[][] replacements = new Object[][]{
                {"{{css}}", (Supplier<String>) () ->
                        "<style>" +
                                getTextFromFile(MostEventStats.class.getResourceAsStream("/css/pairs.css")) +
                                "</style>"
                },
                {"{{meta}}", (Supplier<String>) () ->
//                                getTextFromFile(MostEventStats.class.getResourceAsStream("/meta_most_events.xml"))
                        ""
                },
                {"{{javascript}}", (Supplier<String>) () ->
                                getTextFromFile(MostEventStats.class.getResourceAsStream("/js/pairs_table.js"))
                }
        };
        FindAndReplace.findAndReplace(file, modified, replacements);

        new ProcessBuilder("xdg-open", modified.getAbsolutePath()).start();
    }

    public PairsStats(Database database)
    {
        resultDao = new ResultDao(database);
        athleteDao = new AthleteDao(database);
        courseRepository = new CourseRepository();
        new CourseDao(database, courseRepository);
    }

    private File generateStats(int... athleteIds) throws IOException, XMLStreamException
    {
        List<Integer> listOfAthleteIds = new ArrayList<>();
        Arrays.stream(athleteIds).forEach(listOfAthleteIds::add);

        Map<Integer, Athlete> athleteIdToAthlete = athleteDao.getAthletes(listOfAthleteIds);
        List<Athlete> athletes = athleteIdToAthlete.values().stream().toList();

        PairsTable<Athlete> pairsTable = new PairsTable<>(athletes);

        System.out.printf("%1$10s\t", "");
        pairsTable.consumeFirst((rowAthlete, colAthletes) ->
        {
            colAthletes.forEach(colAthlete ->
            {
                String name = colAthlete.name;
                System.out.printf("%s", name.charAt(0));
                System.out.printf("%s", name.substring(name.indexOf(" ") + 1, name.indexOf(" ") + 2));
                System.out.print("\t");
            });
        });
        System.out.println();

        Map<String, HowManyRunsWithFriend> processors = new HashMap<>();
        pairsTable.forEach((rowAthlete, colAthletes) ->
        {
            colAthletes.forEach(colAthlete ->
            {
                String key = rowAthlete.athleteId + " " + colAthlete.athleteId;
                processors.put(key, new HowManyRunsWithFriend(rowAthlete.athleteId, colAthlete.athleteId));
            });
        });

        resultDao.tableScan(result ->
        {
            processors.values().forEach(processor -> processor.visitInOrder(result));
        }, "order by course_id desc, date desc");

        AtomicInteger max = new AtomicInteger(Integer.MIN_VALUE);
        pairsTable.forEach((rowAthlete, colAthletes) ->
        {
            String name10Characters = String.format("%1$10s", rowAthlete.name).substring(0, 10); // Row names
            System.out.printf(name10Characters + "\t");
//            System.out.printf("%d\t", rowAthlete.athleteId);

            colAthletes.forEach(colAthlete ->
            {
                String key = rowAthlete.athleteId + " " + colAthlete.athleteId;
                HowManyRunsWithFriend processor = processors.get(key);
                int runs = processors.get(key).runs.size();
                System.out.printf("%d\t", runs);
                //System.out.printf("%d\t", colAthlete.athleteId);
                max.getAndUpdate(i -> Math.max(runs, i));
            });
            System.out.println();
        });
        System.out.println("Max: " + max.get());

        try (HtmlWriter htmlWriter = HtmlWriter.newInstance(new Date(), NZ, "pairs_stat"))
        {
            XMLStreamWriter writer = htmlWriter.writer;
            writer.writeCharacters("{{css}}");
            writer.writeCharacters("{{javascript}}");

            writer.writeStartElement("h1");
            writer.writeCharacters("Pairs Table");
            writer.writeEndElement();

            // Get latest date
            Date latestDate = Date.from(Instant.EPOCH);
            for (HowManyRunsWithFriend howManyRunsWithFriend : processors.values())
            {
                latestDate = new Date(Math.max(latestDate.getTime(), howManyRunsWithFriend.getLatestDate().getTime()));
            }

            writer.writeStartElement("h3");
            writer.writeCharacters("Lastest Date: " + DateConverter.formatDateForHtml(latestDate));
            writer.writeEndElement();

            try (PairsTableHtmlWriter tableWriter = new PairsTableHtmlWriter(writer, new UrlGenerator(NZ.baseUrl)))
            {
                pairsTable.consumeFirst((rowAthlete1, colAthletes1) ->
                {
                    try
                    {
                        tableWriter.writeHeaderRecord(rowAthlete1, colAthletes1);
                    }
                    catch (XMLStreamException e)
                    {
                        throw new RuntimeException(e);
                    }
                });
                pairsTable.forEach((rowAthlete, colAthletes) ->
                {
                    try
                    {
                        List<Object[]> list = colAthletes.stream().map(c ->
                        {
                            String key = rowAthlete.athleteId + " " + c.athleteId;
                            HowManyRunsWithFriend processor = processors.get(key);
                            return new Object[] { processor.runs.size(), key };
                        }).toList();

                        tableWriter.writeRecord(rowAthlete, max.get(), list);
                    }
                    catch (XMLStreamException e)
                    {
                        throw new RuntimeException(e);
                    }
                });
            }

            writer.writeStartElement("br"); writer.writeEndElement();
            writer.writeStartElement("br"); writer.writeEndElement();

            try (EventTableHtmlWriter tableWriter = new EventTableHtmlWriter(writer, new UrlGenerator(NZ.baseUrl)))
            {
//                String key = 291411 + " " + 4225353;
                String key = 291411 + " " + 6459026;
                HowManyRunsWithFriend processor = processors.get(key);

                List<Object[]> runs = processor.runs.stream().sorted((r1, r2) ->
                {
                    Date date1 = (Date) r1[1];
                    Date date2 = (Date) r2[1];
                    if (date1.after(date2)) return -1;
                    if (date1.before(date2)) return 1;
                    return 0;
                }).toList();
                for (Object[] objects : runs)
                {
                    Course course = courseRepository.getCourse((int) objects[0]);
                    tableWriter.writeRecord(course, (Date) objects[1], -1);
                }
            }

            writer.writeStartElement("p");
            writer.writeCharacters("* This table shows how many events runner A, has run with runner B.");
            writer.writeEndElement();

            return htmlWriter.getFile();
        }

//        processors.values().forEach(processor -> {
//            Athlete input = allAthletes.get(processor.inputAthleteId);
//            Athlete friend = allAthletes.get(processor.friendAthleteId);
//
//            if(processor.runs.size() < 10)
//            {
//                processor.runs.forEach(runValues ->
//                {
//                    int courseId = (int)runValues[0];
//                    Course course = courseRepository.getCourse(courseId);
//                    System.out.printf("%s\t%s\t ran together on %s at %s%n", input.name, friend.name, runValues[1], course.longName);
//                });
//            }
//        });
    }
}