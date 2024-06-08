package dnt.parkrun.downloaded;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Volunteer;
import dnt.parkrun.filewebpageprovider.FileWebpageProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessDownloadedHtmlFiles
{
    public static void main(String[] args)
    {
        File dir = new File("/home/northd/Downloads/TBP");
        new ProcessDownloadedHtmlFiles().go(dir);
    }

    private void go(File dir)
    {
        Arrays.stream(dir.listFiles()).forEach(file -> {

            processFile(file);

            System.out.print("Deleting " + file + "... ");
            file.delete();
            System.out.println("DONE");
        });
    }

    private void processFile(File file)
    {
        List<Athlete> runners = new ArrayList<>();
        List<Result> results = new ArrayList<>();
        List<Volunteer> volunteers = new ArrayList<>();

        FileWebpageProvider fileWebpageProvider = new FileWebpageProvider(file);
//        dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder(ces.course)
//                .webpageProvider(fileWebpageProvider)
//                .forEachAthlete(runners::add)
//                .forEachResult(results::add)
//                .forEachVolunteer(volunteers::add)
//                .build();
//        parser.parse();
//
//        courseEventSummaryDao.insert(ces);
//        athleteDao.insert(runners);
//        volunteerDao.insert(volunteers);
//        resultDao.insert(results);
//
    }
}
