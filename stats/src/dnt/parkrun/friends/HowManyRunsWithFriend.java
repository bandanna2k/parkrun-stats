package dnt.parkrun.friends;

import dnt.parkrun.datastructures.Result;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class HowManyRunsWithFriend
{
    public final int inputAthleteId;
    public final int friendAthleteId;

    public final Set<Object[]> runs = new HashSet<>();

    private boolean didInputAthleteRun = false;
    private boolean didFriendAthleteRun = false;

    private int prevCourseId = -1;
    private Date prevDate = Date.from(Instant.EPOCH);

    public HowManyRunsWithFriend(int inputAthleteId, int friendAthleteId)
    {
        this.inputAthleteId = inputAthleteId;
        this.friendAthleteId = friendAthleteId;
    }

    public void visitInOrder(Result result)
    {
        int scanCourseId = result.courseId;
        Date scanDate = result.date;

        if(prevCourseId != scanCourseId || !scanDate.equals(prevDate))
        {
            // Next course
            didInputAthleteRun = false;
            didFriendAthleteRun = false;
            prevCourseId = scanCourseId;
            prevDate = scanDate;
        }

        if(result.athlete.athleteId == inputAthleteId)
        {
            didInputAthleteRun = true;
        }
        else if(result.athlete.athleteId == friendAthleteId)
        {
            didFriendAthleteRun = true;
        }

        if(didFriendAthleteRun && didInputAthleteRun)
        {
            int courseId = result.courseId;
            Date date = result.date;
            runs.add(new Object[] { courseId, date });

//            if (friendAthleteId == 4072508 && inputAthleteId == 291411)
//            {
//                System.out.println("Marty/Zoe " +  date + "\t" + courseId);
//            }
//            if (friendAthleteId == 414811 && inputAthleteId == 291411)
//            {
//                System.out.println("Marty/David" + date + "\t" + courseId);
//            }

            didInputAthleteRun = false;
            didFriendAthleteRun = false;
        }
    }

//    public List<AthleteIdCount> after()
//    {
//        return friendAthleteIds.stream().map(friendAthleteId -> {
//            Integer countOfRunsWithFriend = athleteIdToCount.get(friendAthleteId);
//            if(countOfRunsWithFriend == null)
//            {
//                return new AthleteIdCount(friendAthleteId, 0);
//            }
//            return new AthleteIdCount(friendAthleteId, countOfRunsWithFriend);
//        }).collect(Collectors.toList());


//
//        return athleteIdToCount.entrySet().stream()
//                .filter(athleteIdToCount -> friendAthleteIds.contains(athleteIdToCount.getKey()))
//                .map(entry -> new AthleteIdCount(entry.getKey(), entry.getValue()))
//                .sorted(AthleteIdCount.COMPARATOR)
//                .collect(Collectors.toList());
//    }
}
