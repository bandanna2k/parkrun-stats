package dnt.parkrun.friends;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.htmlwriter.HtmlWriter;
import dnt.parkrun.htmlwriter.writers.PairsTableHtmlWriter;
import dnt.parkrun.stats.MostEventStats;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

import static dnt.parkrun.database.DataSourceUrlBuilder.Type.PARKRUN_STATS;
import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;

public class PairsStats
{
    private final ResultDao resultDao;
    private final AthleteDao athleteDao;
    private final CourseRepository courseRepository;

    public static void main(String[] args) throws SQLException, XMLStreamException, IOException
    {
        final Country country = Country.valueOf(args[0]);
        final DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl(PARKRUN_STATS, country), "stats", "statsfractalstats");
        PairsStats pairsStats = new PairsStats(country, dataSource);
        File file = pairsStats.generateStats(
                1340853, // Jonathan
                293223, // Julie GORDON
                1048005, // Sarah JANTSCHER
                4225353, // Dan JOE
                293227, // Paul GORDON
                2147564, // Alison KING
                141825, // Andrew CAPEL
                2225176, // Andy MEARS
                391111, // Gene RAND
                291411, // Martin O'SULLIVAN
                1590564, // Yvonne TSE
                116049, // Richard NORTH
                414811, // David NORTH
                547976, // Allan JANES
                4072508 // Zoe NORTH
        );

        File modified = new File(file.getAbsoluteFile().getParent() + "/modified_" + file.getName());
        findAndReplace(file, modified);

        new ProcessBuilder("xdg-open", modified.getAbsolutePath()).start();
    }

    public PairsStats(Country country, DataSource dataSource)
    {
        resultDao = new ResultDao(country, dataSource);
        athleteDao = new AthleteDao(dataSource);
        courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);
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

        pairsTable.forEach((rowAthlete, colAthletes) ->
        {

            String name10Characters = String.format("%1$10s", rowAthlete.name).substring(0, 10); // Row names
            System.out.printf(name10Characters + "\t");
//            System.out.printf("%d\t", rowAthlete.athleteId);

            colAthletes.forEach(colAthlete ->
            {
                String key = rowAthlete.athleteId + " " + colAthlete.athleteId;
                HowManyRunsWithFriend processor = processors.get(key);
                System.out.printf("%d\t", processors.get(key).runs.size());
                //System.out.printf("%d\t", colAthlete.athleteId);
            });
            System.out.println();
        });

        try (HtmlWriter htmlWriter = HtmlWriter.newInstance(new Date(), NZ, "pairs_stat"))
        {
            htmlWriter.writer.writeCharacters("{{css}}");

            try (PairsTableHtmlWriter tableWriter = new PairsTableHtmlWriter(htmlWriter.writer, new UrlGenerator(NZ.baseUrl)))
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
                        List<Integer> list = colAthletes.stream().map(c ->
                        {
                            String key = rowAthlete.athleteId + " " + c.athleteId;
                            HowManyRunsWithFriend processor = processors.get(key);
                            return processor.runs.size();
                        }).toList();

                        tableWriter.writeRecord(rowAthlete, list);
                    }
                    catch (XMLStreamException e)
                    {
                        throw new RuntimeException(e);
                    }
                });
            }
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
    public static void findAndReplace(File input, File output) throws IOException
    {
        try (FileInputStream fis = new FileInputStream(input);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr))
        {
            try (FileOutputStream fos = new FileOutputStream(output);
                 OutputStreamWriter osw = new OutputStreamWriter(fos);
                 BufferedWriter writer = new BufferedWriter(osw))
            {
                final Object[][] replacements = new Object[][]{
                        {"{{css}}", (Supplier<String>) () ->
                                "<style>" +
                                        getTextFromFile(MostEventStats.class.getResourceAsStream("/css/pairs.css")) +
                                        "</style>"
                        },
                        {"{{meta}}", (Supplier<String>) () ->
//                                getTextFromFile(MostEventStats.class.getResourceAsStream("/meta_most_events.xml"))
                                ""
                        }
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
    }

    private static String getTextFromFile(InputStream inputStream)
    {
        StringBuilder sb = new StringBuilder();
        try(BufferedReader reader1 = new BufferedReader(new InputStreamReader(inputStream)))
        {
            String line1;
            while(null != (line1 = reader1.readLine()))
            {
                sb.append(line1).append("\n");
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}