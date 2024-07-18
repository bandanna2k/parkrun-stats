package dnt.parkrun.stats.processors;

import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class InauguralEventsProcessor implements ResultDao.ResultProcessor
{
    private final Map<Integer, Set<Integer>> athleteIdToInauguralCourseIds = new HashMap<>();

    @Override
    public void visitInOrder(Result result)
    {
        if(result.eventNumber == 1)
        {
            int athleteId = result.athlete.athleteId;
            Set<Integer> courseIds = athleteIdToInauguralCourseIds.computeIfAbsent(athleteId, athleteId1 -> new TreeSet<>());
            courseIds.add(result.courseId);
        }
    }

    @Override
    public void onFinish()
    {
    }

    public int getInauguralCount(int athleteId)
    {
        Set<Integer> inauguralCourseIds = athleteIdToInauguralCourseIds.get(athleteId);
        return inauguralCourseIds == null ? 0 : inauguralCourseIds.size();
    }
}
