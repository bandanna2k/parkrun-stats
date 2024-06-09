package dnt.parkrun.downloaded;

import dnt.parkrun.database.*;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.filewebpageprovider.FileWebpageProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;

public class ProcessDownloadedHtmlFiles
{
    private CourseRepository courseRepository;
    private VolunteerDao volunteerDao;
    private ResultDao resultDao;
    private AthleteDao athleteDao;
    private CourseEventSummaryDao courseEventSummaryDao;

    public static void main(String[] args)
    {
        File dir = new File("/home/northd/Downloads/TBP");
        String addingDataToLiveFromDownloadedFileNeedsPermission = "sudo";
        Database database = new LiveDatabase(Country.NZ, getDataSourceUrl(), "dao", "0b851094",
                addingDataToLiveFromDownloadedFileNeedsPermission);
        new ProcessDownloadedHtmlFiles().go(database, dir);
    }

    private void go(Database database, File dir)
    {
        courseRepository = new CourseRepository();
        new CourseDao(database, courseRepository);
        athleteDao = new AthleteDao(database);
        resultDao = new ResultDao(database);
        volunteerDao = new VolunteerDao(database);
        courseEventSummaryDao = new CourseEventSummaryDao(database, courseRepository);

        Arrays.stream(dir.listFiles()).forEach(file -> {

            processFile(file);

            System.out.print("Deleting " + file + "... ");
     //       file.delete();
            System.out.println("DONE");
        });
    }

    private void processFile(File file)
    {
        List<Athlete> runners = new ArrayList<>();
        List<Result> results = new ArrayList<>();
        List<Volunteer> volunteers = new ArrayList<>();

        FileWebpageProvider fileWebpageProvider = new FileWebpageProvider(file);
        dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder(courseRepository)
                .webpageProvider(fileWebpageProvider)
                .forEachAthlete(runners::add)
                .forEachResult(results::add)
                .forEachVolunteer(volunteers::add)
                .build();
        parser.parse();

        Optional<Athlete> firstMale = results.stream()
                .filter(r -> r.ageCategory.isMale())
                .map(r -> r.athlete)
                .findFirst();
        Optional<Athlete> firstFemale = results.stream()
                .filter(r -> r.ageCategory.isMale())
                .map(r -> r.athlete)
                .findFirst();
        int eventNumber = parser.getEventNumber();
        assert eventNumber > 0 : "Event number not given";
        CourseEventSummary ces = new CourseEventSummary(
                parser.getCourse(), eventNumber, parser.getDate(), results.size(), firstMale, firstFemale);
//        courseEventSummaryDao.insert(ces);
//        athleteDao.insert(runners);
//        volunteerDao.insert(volunteers);
//        resultDao.insert(results);
    }
}
