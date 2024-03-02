package dnt.parkrun.addathleteevents;

import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.AthleteCourseSummary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddAthleteEvents
{
    public static final String PARKRUN_CO_NZ = "parkrun.co.nz";
    private UrlGenerator urlGenerator = new UrlGenerator();


    public static void main(String[] args) throws IOException
    {
        new AddAthleteEvents().go(Arrays.stream(args).map(Integer::parseInt).collect(Collectors.toList()));
    }

    private void go(List<Integer> athletes) throws IOException
    {
        System.out.println(athletes);

        List<AthleteCourseSummary> courseSummaries = new ArrayList<>();
        for (Integer athleteId : athletes)
        {
            urlGenerator = new UrlGenerator();
            Parser parser = new Parser.Builder()
                    .url(urlGenerator.generateAthleteEventUrl(PARKRUN_CO_NZ, athleteId))
                    .forEachAthleteCourseSummary(courseSummaries::add)
                    .build();
            parser.parse();
        }

        AthleteCourseSummary acs = courseSummaries.get(0);

        dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder()
                .url(urlGenerator.generateCourseEventUrl(PARKRUN_CO_NZ, acs.course, 99999))
                .forEachResult(result -> {
                    System.out.println(result);
                })
                .build();
    }
}
