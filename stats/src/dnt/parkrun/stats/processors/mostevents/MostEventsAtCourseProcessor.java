package dnt.parkrun.stats.processors.mostevents;

import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dnt.parkrun.datastructures.Athlete.NO_ATHLETE_ID;

public class MostEventsAtCourseProcessor implements ResultDao.ResultProcessor
{
    static final int MAX_RESULT_SIZE = 20;

    private final Map<Integer, Map<Integer, Integer>> courseToAthleteToCount = new HashMap<>();

    @Override
    public void visitInOrder(Result result)
    {
        if(result.athlete.athleteId == NO_ATHLETE_ID) return;

        Map<Integer, Integer> athleteToCount = courseToAthleteToCount.computeIfAbsent(result.courseId, integer -> new HashMap<>());
        int i = athleteToCount.computeIfAbsent(result.athlete.athleteId, integer -> 0);
        athleteToCount.put(result.athlete.athleteId, ++i);
    }

    private final Map<Integer, List<Object[]>> courseToAthleteCount = new HashMap<>();

    @Override
    public void onFinishCourse()
    {
        courseToAthleteToCount.forEach((courseId, athleteToCount) -> {
            athleteToCount.forEach((athleteId, count) -> {

                List<Object[]> athleteCount = courseToAthleteCount.computeIfAbsent(courseId, integer -> new ArrayList<>());

                // Add
                athleteCount.add(new Object[] { athleteId, count });

                // Sort
                athleteCount.sort((r1, r2) -> {
                    int countR1 = (int)r1[1];
                    int countR2 = (int)r2[1];
                    if(countR1 < countR2) return 1;
                    if(countR1 > countR2) return -1;

                    int athleteR1 = (int)r1[0];
                    int athleteR2 = (int)r2[0];
                    if(athleteR1 > athleteR2) return 1;
                    if(athleteR1 < athleteR2) return -1;

                    else return 0;
                });

                // Remove if too many
                if(athleteCount.size() > MAX_RESULT_SIZE) athleteCount.removeLast();
            });
        });
    }

    public List<Object[]> getMostEventsForCourse(int courseId)
    {
        return courseToAthleteCount.get(courseId);
    }
}
