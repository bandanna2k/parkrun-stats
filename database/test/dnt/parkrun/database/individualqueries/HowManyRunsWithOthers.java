package dnt.parkrun.database.individualqueries;

import dnt.parkrun.datastructures.Result;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class HowManyRunsWithOthers
{
    private final Map<Integer, Integer> athleteIdToCount = new HashMap<>();

    private final AtomicInteger prevCourseId = new AtomicInteger(-1);
    private final AtomicReference<Date> prevDate = new AtomicReference<>();

    private final List<Integer> athleteIdsAtCourse = new ArrayList<>();
    private final AtomicBoolean didInputAthleteRun = new AtomicBoolean(false);

    final int inputAthleteId;

    public HowManyRunsWithOthers(int inputAthleteId)
    {
        this.inputAthleteId = inputAthleteId;
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

    public void after()
    {
        List<AthleteIdCount> listOfCrossRuns = athleteIdToCount.entrySet().stream()
                .map(entry -> new AthleteIdCount(entry.getKey(), entry.getValue()))
                .sorted(AthleteIdCount.COMPARATOR)
                .collect(Collectors.toList());

        for (int i = 0; i < Math.min(20, listOfCrossRuns.size()); i++)
        {
            AthleteIdCount athleteIdCount = listOfCrossRuns.get(i);
            System.out.printf("%d:\t%d\t%d%n", i, athleteIdCount.athleteId, athleteIdCount.count);
        }
        System.out.println();
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
