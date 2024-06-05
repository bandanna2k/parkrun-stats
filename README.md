# parkrun-stats

# What to run?

Run InvariantTest package, this makes sure 
    results are in, 
    course are up to date (can causes problems if new courses are not added),
    parser are working (just encase the webpages have changed)

Run WeeklyResults.main, this will
    fill a database with all region results from active parkruns
    fill a database with all region volunteers from active parkruns

Run Stats.main <date> E.g. java -jar Stats.jar 25/12/2023, this creates
   a Most Events table,
   an Attendance table,
   and output to HTML

# TODO

## Major

## Minor

page 1
Random question from seed.
Calc answer
Write to DB
Write answer to DB
Write athlete to DB

Friend Session
-------
FriendSessionId  Seed        Expectation   AthleteId   Given
1                1717479069  101           414811      101

Friend Counts
-------
FriendSessionId  Row  Athlete  Date
1                1    116049   278
1                1    54321    52
1                2    10101    1



- Questions
How many people ran at <course> on your <x'th> visit? (slow due to results)

select *
from parkrun_stats_NZ.course_event_summary ces
right join (
    select * 
    from parkrun_stats_NZ.result
    where athlete_id = 414811
    order by rand()
    limit 1
) as sub1 using (course_id, date);

When you volunteered at <x> on <date>, how many finishers where there?

select *
from parkrun_stats_NZ.course_event_summary ces
right join (
select *
from parkrun_stats_NZ.event_volunteer
where athlete_id = 414811
order by rand()
limit 1
) as sub1 using (course_id, date);

What position did you finish at <course> on your <x.th> visit? (slow due to results)

What <event number> was your first/second/third volunteer at <course>?


- Auto-friend calculator

- Don't download already downloaded. Weekly Results. More info please?

- Why do I have weekly WeeklyStatsNz and WeeklyStats backing up
  (weekly_stats not needed anymore, weekly_stats_NZ)

- Add deltas to attendance processor
(Stalled until we can confirm attendance records)

- Need a loading icon whilst fastest times page is loading


## Ideas

- Unique runners at course / Add unique runs for runners / Add unique runs to Most Events???

## Issues Completed

- 2/6/2024 Max attendance deltas are appearing randomly. 
Attendance links are -1

- 2/6/2024 Top 10 at course surely can be done with 1 processor
  Related: Why does 'Populating top 10 run table for Ōrākei Bay parkrun' take so long.
  (Stalled until we can confirm this doesn't take that long, waiting until June 2nd, top 10 DAO might not be needed anymore.

- 30/9/2024 Speed stats for the year
  Generate files DONE
  Upload html

- 29/5/2024 Friends table including at what venues
'Most runs with other athletes' table
Low priority
Create a demo table.

- 27/5/2024 Result 1716791313481, 1716794154811

- 26/5/2024 Bad results 1716697342205

- 26/5/2024 PNG for chart

- 26/5/2024 Use processors for Top 10 
Use processor for Most Runs at Courses (need AthleteId and Count) athlete -> course -> count 
Use processor for Most Volunteers at Courses (need AthleteId and Count)

- 26/5/2024 90% Club ordered by score

- 23/5/2024 Remove sql from readme

- 23/5/2024 Athlete Course Summary file/website code needs updating.

- 22/5/2024 Download age category from Athlete Course Summary

- 21/5/2024 Move course/athlete table to global DB

- 20/5/2024 Refactor findAndReplace

- 20/5/2024 Refactor getTextFromFile

- 19/5/2024 Bring out rewrite event into its own class

- 19/5/2024 Change databases to country specific

- 18/5/24 Get rid of NZ from codebase. Should use the parameter.

- 18/5/2024 Make menu country non-specific

- 12/5/2024 Invariants - 10 incrementing numbers, 1 number stays the same for all weekly tables. ???
  Low priority

- 11/5/2024 DB Invariant for null volunteer at event

- 10/5/2024 - CourseCheckInvariant
  1715331666003 Moana Point Reserve 1
  1715335259867 Barry Curtis 516

- 10/5/2024 Meta data needs importing from constructor as speed keywords different to most events keywords.

- 10/5/2024 Average attendance to attendance records

- 8/5/2024 Run all tests in that package
  To hard basket for now.

- 8/5/2024 Invariant - Event number must equal date.

- 7/5/2024 Import CSS at the end

- 7/5/2024 Invariants need to rewrite volunteer data

- 7/5/2024 Quick invariants fail

- 5/52024 CourseEventChecker failed - Unknown athlete now filled in (seeds 1714883664994, 1714893450640, 1714904703501)
need to make seeds work the next week as well DONE
delete from results first DONE
delete from course event summary DONE

- 5/5/2024 Add META data
  Most Events parkrun NZ New Zealand
  Attendance Records
  pIndex
  Most Volunteer Volunteering
  90% Club

- 5/5/2024 Not run this week not working

- 5/5/2024 New Zealand to only occur once in the code.

- 5/5/2024 Column definition to attendance records

- 5/5/2024 Last records shown on menu

- 5/5/2024 - pIndex, where did Helen Watson come from?
Stats are correct. Running at Cornwall Park gave here pIndex of 6 in NZ which is the requirement for pIndex table.
But total Global pIndex of 7. She has run lots of runs at Delamere nr Liverpool and Northwich nr Liverpool

- 5/5/2024 - Menu (Uber app) for 3 programs
Invariants (Quick) DONE
Invariants DONE
Weekly Results (needs nothing and 10 minutes) DONE
Speed Stats (needs weekly results) DONE
Most Events (needs weekly results and 1 hour) DONE

- 5/5/2024 Batch write results/volunteers. Comms over the network is sloooooow.

- 4/5/2024 Weekly Results should use database, not events json. Invariants do this work now.
  Doing it this way causes problems.

- 3/5/2024 - Average attendance records needs to passed through to attendance html writer

- 2/9/2024 - Socks
  Try 10 docker containers running tor (confirm different IPs)
  Proxychain between containers
  Stop for now. parkrun blocking IPs

- 25/4/2024 - Bug - Change key on course_event_summary.

- 24/4/2024 - Weekly results - Backfill each course at a time. Just encase a course fails to get event history.

- 24/4/2024 - Split results page (Not doing, the no benefit)

- 24/4/2024 - Finish max regionnaire (Needs rethink, harder calculation than thought.)

- 22/4/2024 - Fix Regionnaire count (idea, use a set and match set of course_ids)
Regionnaire count to include stopped events.

- 22/4/2024 - We can assume event numbers for getting Weekly Results.
Hopefully this is the final time. We can't assume. Remember this back-fills all missing results.

- 21/4/2024 - pIndex check needed.
Dan JOE	11	14 more to P12	0.682
pIndex correct. Needs all of P12 and 1 each of 3 more courses

- 20/4/2024 - Empty tables on Age Categories.

- 16/4/2024 Low priority - Add stop dates as java code

- 16/4/2024 Prove?? Parsers can break half way through. Collate records first then stream records.
Abandoning. Course event summary invariant is just as likely to catch errors.

- 16/4/2024 Invariant - region event count < global event count for volunteers

- 16/4/2024 Mobile - put up/down arrows into stats
  Looks OK for attendance records.
  Looks bad for most events, so didn't do it.

- 15/4/2024 Do I need name/most global events in most events table?
  Attendance records has unnecessary fields. DONE
  Most events for region has unnecessary fields. DONE

- 15/4/2024 - Bug - When course not found, athlete course summary not finished.
Mitigated with invariant test that checks for new courses.

- 15/4/2024 - Can we optimise How are we doing test? 'How are we doing test to assume next event number' -
  No, we are checking the Course Event summary for each cousre which has the finishers on it. Needs to be greater than 0.

- 15/4/2024 Bug - Why is the deltas on the pIndex a bit weird.
  No bug.
  Phil O'Keefe went down -1 because Robert Gunther jumped up above him.
  Tony Collins down -2 because he ran in Canada

- 1/4/2024 - Question - Revisit pIndex downloads.
  I don't think the criteria for downloading the athletes its correct.
  Criteria is NZ regional pIndex > 5 (so 6 onwards) - This is correct.

- 1/4/2024 - Bug - Why is 1311970 not getting collected for pIndex. No bug. 1311970 has NZ pIndex of 5 (needs to be 6)

- 1/4/2024 - Issue - Can Weekly Results be optimised - Assume next event number (Can't assume, this downloads all missing events)


# The Story

## Background and Motivation 

I had a goal of completing all parkruns in New Zealand. I have a family to look after, and don't have 
to much money to spend on treats, so I cannot just leave the house and go to all parkruns as soon as they
materialise, I need to plan ahead, get cheap flights, earn brownie points. Any after my run in Christchurch,
only do tourism in summer as that was a cold, cold parkrun.

Anyhow, its taken me 10 years to get 1 run away from becoming a Regionnaire, the title given to someone that
has completed all parkruns in NZ. 2 weeks before running my last parkrun to become regionnaire, parkrun HQ 
decided to remove all stats from their website. Not cool.

In idol conversation with my neighbour Alan, I mentioned that we should not worry, and that all the information
out there is public (as of May 2024) and someone will bring the stats back. The database is small in comparison
to what I have to deal with on a daily basis at work. The database I deal with in 1 day, is much larger than
the global parkrun database. It is a small database.
Turns out that person to bring the stats back to New Zealand would be me. I am a software developer by trade, 
so I have all the tools to do this. During I couple of days of sick leave, 
I had nothing better to do than prove that it was possible. And after a few lines of code, I had downloaded 
data and stored it in a database.

## Technical Stuff

First there is 'events.json' from the URL link given 'https://images.parkrun.com/events.json'. 
Why parkrun make this link available I don't know? If they remove this, some of this code would have to 
change. This file gives a complete parkrun course table.

With the above table of courses, we can now go to each course and download the event history from URLs
of the format https://www.parkrun.co.nz/cornwallpark/results/eventhistory/, in this example 'cornwallpark' 
came from events.json
The event history gives us a start event number, and a last event number.

With the above event history table, we can now download each events results from URLs of the following
format https://www.parkrun.co.nz/cornwallpark/results/571/ where 571 is the event number in this case.
Also, from this URL, we download the volunteer data at the bottom.

All of this data I have download for New Zealand which is on the small side. parkrun rate limit the data
you can download. I have found out that you can download around 1 webpage every 5 seconds without getting
temporarily banned.

For the most events table, particular the global parkrun count for an athlete we need an extra page 
downloaded that contains an athletes individual statistics. This URL comes in the format of
https://www.parkrun.co.nz/parkrunner/6297704/. 
This URL I envision parkrun will soon hide behind authentication, so we this code will lose the ability to
download global athlete data. Hopefully not.

