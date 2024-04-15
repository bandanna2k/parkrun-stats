package dnt.parkrun.database.individualqueries;

import dnt.parkrun.datastructures.Result;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class HowManyRunsWithFriends
{
    private final Map<Integer, Integer> athleteIdToCount = new HashMap<>();

    private final AtomicInteger prevCourseId = new AtomicInteger(-1);
    private final AtomicReference<Date> prevDate = new AtomicReference<>();

    private final List<Integer> athleteIdsAtCourse = new ArrayList<>();
    private final AtomicBoolean didInputAthleteRun = new AtomicBoolean(false);

    public final int inputAthleteId;
    private final Set<Integer> friendAthleteIds;

    public HowManyRunsWithFriends(int inputAthleteId, int[] friendAthleteIds)
    {
        this.inputAthleteId = inputAthleteId;
        this.friendAthleteIds = Arrays.stream(friendAthleteIds).boxed().collect(Collectors.toSet());
    }

    public void visitInOrder(Result result)
    {
        int scanCourseId = result.courseId;
        Date scanDate = result.date;
        int scanAthleteId = result.athlete.athleteId;

        if(prevCourseId.get() != scanCourseId || !scanDate.equals(prevDate.get()))
        {
            // Next course
            if(didInputAthleteRun.get())
            {
                athleteIdsAtCourse.forEach(coathleteId -> {
                    Integer count = athleteIdToCount.get(coathleteId);
                    if(null == count)
                    {
                        athleteIdToCount.put(coathleteId, 1);
                    }
                    else
                    {
                        athleteIdToCount.put(coathleteId, count + 1);
                    }
                });
            }

            // Reset variables
            prevDate.set(scanDate);
            prevCourseId.set(scanCourseId);
            athleteIdsAtCourse.clear();
            didInputAthleteRun.set(false);
        }

        if(scanAthleteId == inputAthleteId)
        {
            didInputAthleteRun.set(true);
            return;
        }

        // Add athlete to temp list
        athleteIdsAtCourse.add(result.athlete.athleteId);
    }

    public List<AthleteIdCount> after()
    {
        return friendAthleteIds.stream().map(friendAthleteId -> {
            Integer countOfRunsWithFriend = athleteIdToCount.get(friendAthleteId);
            if(countOfRunsWithFriend == null)
            {
                return new AthleteIdCount(friendAthleteId, 0);
            }
            return new AthleteIdCount(friendAthleteId, countOfRunsWithFriend);
        }).collect(Collectors.toList());
//
//        return athleteIdToCount.entrySet().stream()
//                .filter(athleteIdToCount -> friendAthleteIds.contains(athleteIdToCount.getKey()))
//                .map(entry -> new AthleteIdCount(entry.getKey(), entry.getValue()))
//                .sorted(AthleteIdCount.COMPARATOR)
//                .collect(Collectors.toList());
    }

    @Override
    public String toString()
    {
        return "HowManyRunsWithOthers{" +
                "athleteIdToCount=" + athleteIdToCount +
                ", prevCourseId=" + prevCourseId +
                ", prevDate=" + prevDate +
                ", athleteIdsAtCourse=" + athleteIdsAtCourse +
                ", didInputAthleteRun=" + didInputAthleteRun +
                ", inputAthleteId=" + inputAthleteId +
                '}';
    }
}
