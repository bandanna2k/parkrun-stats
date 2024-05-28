package dnt.parkrun.friends;

import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.Result;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HowManyRunsWithFriend implements ResultDao.ResultProcessor
{
    public final int inputAthleteId;
    public final int friendAthleteId;

    public final List<Object[]> runs = new ArrayList<>();

    private boolean didInputAthleteRun = false;
    private boolean didFriendAthleteRun = false;

    private int prevCourseId = -1;
    private Date prevDate = Date.from(Instant.EPOCH);
    private Date maxDate = Date.from(Instant.EPOCH);

    public HowManyRunsWithFriend(int inputAthleteId, int friendAthleteId)
    {
        this.inputAthleteId = inputAthleteId;
        this.friendAthleteId = friendAthleteId;
    }

    @Override
    public void visitInOrder(Result result)
    {
        int scanCourseId = result.courseId;
        Date scanDate = result.date;

        // Set max
        maxDate = new Date(Math.max(maxDate.getTime(), scanDate.getTime()));

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
            //int eventNumber = result.eventNumber;
            runs.add(new Object[] { courseId, date });

            didInputAthleteRun = false;
            didFriendAthleteRun = false;
        }
    }

    @Override
    public void onFinish()
    {

    }

    public Date getLatestDate() { return maxDate; }
}
