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

- Athlete Course Summary file/website code needs updating.

- Top 10 at course surely can be done with 1 processor
  Related: Why does 'Populating top 10 run table for Ōrākei Bay parkrun' take so long.

- Speed stats for the year 

- Remove sql from readme

- Friends table including at what venues
      - 'Most runs with other athletes' table
  Low priority
  Create a demo table.

- Need a loading icon whilst fastest times page is loading


## Ideas

- Unique runners at course / Add unique runs for runners / Add unique runs to Most Events???

## Issues Completed

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



# QUERIES

## How many athletes have finished a run (in the database)

```
select count(1) from athlete where athlete_id > 0;
```

## Runners with X runs (e.g. how many runners with just 1 run)

```
select count as total_runs_count, count(athlete_id) as athlete_count
from
( 
select distinct athlete_id, count(concat(course_name, event_number)) as count
from result 
group by athlete_id 
order by count desc
) as athlete_course_count
group by count
order by total_runs_count desc;
```

## Delete from all tables

```
delete from athlete; delete from result; delete from course_event_summary;
```

## Get course event summaries without results

```
select distinct course_name, event_number from course_event_summary left join result using (course_name, event_number) where athlete_id is null;
```

## Delete course event summaries without results

```
select distinct group_concat(concat('''', course_name, event_number, '''')) 
from course_event_summary 
left join result 
using (course_name, event_number) 
where athlete_id is null;

select * from course_event_summary
where concat(course_name, event_number) in (
'cornwall520','hamiltonpark104','lake2laketrail54','millwater344','millwater345','millwater346','millwater347','millwater351'
);
```

## Get Most Events

```
select name, athlete_id, count(concat) as count, athlete_id
from (select athlete_id, concat(athlete_id, course_name, event_number, '-', position) as concat from result) as sub1
join athlete using (athlete_id)
group by athlete_id
order by count desc, athlete_id asc 
limit 40;
```

# Get Most Volunteers
```
select name, athlete_id, count(concat) as count, athlete_id
from (select athlete_id, concat(athlete_id, '-', course_id, '-', date) as concat from event_volunteer) as sub1
join athlete using (athlete_id)
group by athlete_id
order by count desc, athlete_id asc 
limit 20;
```

## Get Most Different Events

```
select json_arrayagg(json_object('name', name, 'count', count)) as json
from (

select name, count(course_name) as count
from (select distinct athlete_id, course_name from parkrun_stats.result) as sub1
join athlete using (athlete_id)
group by athlete_id
having count > 30
order by count desc, athlete_id asc 
limit 50

) as sub\G;
```

## Get Most Different Events along With Total runs

```
select a.name, a.athlete_id, sub1.count as different_courses, sub2.count as total_runs
from athlete a
join  
(
    select athlete_id, count(course_name) as count
    from (select distinct athlete_id, course_name from parkrun_stats.result) as sub1a
    group by athlete_id
    having count > 40
    order by count desc, athlete_id asc 
) as sub1 on sub1.athlete_id = a.athlete_id
join
(
    select athlete_id, count(concat) as count
    from (select athlete_id, concat(athlete_id, course_name, event_number, '-', position) as concat from result) as sub2a
    group by athlete_id
    order by count desc, athlete_id asc 
) as sub2 on sub2.athlete_id = a.athlete_id

order by different_events desc, total_runs desc, a.athlete_id desc
limit 50;
```

## Get Most Different Events with Total NZ Events (WIP)

```
```

### Best position at course athlete history

```
select athlete_id, course_name, min(position) as best_position
from athlete
left join result using (athlete_id)
group by athlete_id, course_name
having athlete_id = 49817
order by best_position desc;
```

### Count of runs at courses

```
select json_arrayagg(json_object('course_name', course_name, 'count', count)) as json
from (

select course_long_name, count(course_name) as count
from athlete
left join result using (athlete_id)
left join course using (course_name)
group by athlete_id, course_name
having athlete_id = 7892266
order by count desc

) as sub\G;
```

# INVARIANTS

## Finishers does not match results
```
select ces.course_id, ces.date, ces.finishers, count(r.athlete_id) as result_count
from course_event_summary ces
left join result r on 
    ces.course_id = r.course_id and
    ces.date = r.date
group by ces.course_id, ces.date, ces.finishers
having 
    ces.finishers <> result_count
limit 10;
```

## Course Event Summary with finishers not entered (needs back filling)
```
select count(1) from course_event_summary where finishers is null;
```
## Course Event Summary with first finishers not entered
```
select count(1) from course_event_summary 
where 
    first_male_athlete_id = 0 and 
    first_female_athlete_id = 0;
```
## First female finishers do not match (Query not as expected)
```
select * from course_event_summary ces
left join result r 
on  ces.course_name = r.course_name and 
    ces.event_number = r.event_number and
    ces.first_female_athlete_id = r.athlete_id
where
    ces.first_female_athlete_id <> 0 
    and r.athlete_id is null;
```
# No runner with 2 results on the same day

# No runner with 2 results in the same event

# Course with no results for a given day

E.g. 2022-03-19 When only a few South Island courses were available.

```
select course_long_name, sub1.date
from course c 
left join (
    select course_name, date from course_event_summary 
    where date = '2024-03-02'
) as sub1 on sub1.course_name = c.course_name
where 
    country = 'NZ' and
    status = 'R';    
```

# Attendance records

```
select course_long_name, c.course_name, 
            max, ces.date, 
            sub3.recent_event_finishers, sub3.recent_event_date 
from course c
left join 
(
    select course_name, max(count) as max
    from
    (
        select course_name, event_number, count(position) as count
        from result
        group by course_name, event_number
    ) as sub1
    group by course_name
    order by course_name asc
) as sub2 on c.course_name = sub2.course_name

left join course_event_summary ces
on c.course_name = ces.course_name
and ces.finishers = sub2.max

left join
(
    select ces.course_name, finishers as recent_event_finishers, recent_event_date
    from course_event_summary ces
    join
    (
        select course_name, max(date) as recent_event_date
        from course_event_summary
        group by course_name
    ) as sub4 on ces.course_name = sub4.course_name and ces.date = sub4.recent_event_date
) as sub3 on c.course_name = sub3.course_name;
```

# Fastest First Finishers

```
select row_number(), min(time_seconds) 
from
(
    select ces.course_name, ces.event_number, ces.date, fa.athlete_id, fr.time_seconds
    from course_event_summary ces
    left join athlete fa 
    on ces.first_female_athlete_id = fa.athlete_id
    left join result fr
    on ces.course_name = fr.course_name and ces.event_number = fr.event_number and ces.first_female_athlete_id = fr.athlete_id    
) as sub1
group by row_number()
    
```



All event volunteers should have an athlete id.
```
select distinct athlete_id, course_id, name 
from event_volunteer
left join athlete using (athlete_id)
where name is null;
```

Count of volunteers at all courses
```
select course_id, count(athlete_id) as count
from
(
    select course_id, athlete_id, if(name is null, athlete_id, name) as identifier
    from event_volunteer
    left join athlete using (athlete_id)
    where athlete_id = 2306890
) as sub1
group by course_id
order by count;
```

# Course Event Summary without volunteers
```
select distinct ces.course_id, ces.date, ev.athlete_id 
from course_event_summary ces
left join event_volunteer ev using (course_id)
where ev.athlete_id is null;
```


# Most different volunteers 
```
select a.name, a.athlete_id, sub1.count as different_region_course_count, sub2.count as total_region_volunteers
from parkrun_stats.athlete a
join  
(
    select athlete_id, count(course_id) as count
    from (select distinct athlete_id, course_id from parkrun_stats.event_volunteer) as sub1a
    group by athlete_id
    having count >= 10
    order by count desc, athlete_id asc 
) as sub1 on sub1.athlete_id = a.athlete_id
join
(
    select athlete_id, count(concat) as count
    from (select athlete_id, concat(athlete_id, '-', course_id, '-', date) as concat from event_volunteer) as sub2a
    group by athlete_id
    order by count desc, athlete_id asc 
) as sub2 on sub2.athlete_id = a.athlete_id
where a.name is not null
order by different_region_course_count desc, total_region_volunteers desc, a.athlete_id desc;
```

```
select a.name, a.athlete_id, sub1.count as different_region_course_count, sub2.count as total_region_volunteers
from parkrun_stats.athlete a
join  
(
    select athlete_id, count(course_id) as count
    from (select distinct athlete_id, course_id from parkrun_stats.event_volunteer) as sub1a
    group by athlete_id
    having count >= 10
    order by count desc, athlete_id asc 
) as sub1 on sub1.athlete_id = a.athlete_id
join
(
    select athlete_id, count(concat) as count
    from (select athlete_id, concat(athlete_id, '-', course_id, '-', date) as concat from event_volunteer) as sub2a
    group by athlete_id
    order by count desc, athlete_id asc 
) as sub2 on sub2.athlete_id = a.athlete_id
where a.name is not null
order by different_region_course_count desc, total_region_volunteers desc, a.athlete_id desc;
```

# Get all course start dates
```
select course_id, date 
from course join course_event_summary using (course_id)  
where event_number = 1 
order by date desc;
```
```
SELECT JSON_ARRAYAGG(unix_timestamp(date)), JSON_ARRAYAGG(course_id), JSON_ARRAYAGG(course_long_name)
from
(
    select * 
    from course join course_event_summary using (course_id)  
    where event_number = 1
    order by date asc
) as sub1 
```

# Get first runs of athlete
```
select course_id, first_run 
from
(
    select athlete_id, course_id, min(date) as first_run 
    from result 
    group by athlete_id, course_id 
    having athlete_id = 414811
) as sub1;
```
```
select athlete_id, json_course_ids, json_first_runs
from most_events_for_region_2024_03_23
join 
(
    select athlete_id, JSON_ARRAYAGG(course_id) as json_course_ids, JSON_ARRAYAGG(unix_timestamp(first_run)) as json_first_runs
    from
    (
        select athlete_id, course_id, min(date) as first_run 
        from parkrun_stats.result 
        group by athlete_id, course_id 
    ) as sub1
    group by athlete_id
) as sub2 using (athlete_id)\G
limit 2\G; 
```

I make it David SCOBIE (8), David DRUMMOND (8), Donna CLEARWATER (7), Hannah OLDROYD (7), Deborah CLEARWATER (7) next closest, after Mark and Shelley.


select athlete_id, count(course_id) as course_count
from 
(
    select athlete_id, course_id, count(course_id) as count
    from result
    group by athlete_id, course_id
    having count >= 3 and course_id in (4, 8, 15, 16, 19,      20, 22, 23, 25, 34,      37, 38, 46)
    order by athlete_id desc
) as sub1
group by athlete_id
having course_count >= 5
order by course_count desc;

select * 
from course
where course_id in (
4, 8, 15, 16, 19,      20, 22, 23, 25, 34,      37, 38, 46)

# Most Runs With
```
select athlete_id1, athlete_id2, count(athlete_id1) as count
from
(
    select r1.course_id, r1.date, r1.athlete_id as athlete_id1, r2.athlete_id as athlete_id2
    from result r1
    left join result r2
    on r1.course_id = r2.course_id and r1.date = r2.date
) as sub1
group by athlete_id1, athlete_id2
order by count
limit 10;
```

# Invariant - Event number must equal date.
```
select course_id, date, count(event_number) as count
from course_event_summary
group by course_id, date
having count > 1;
```

```
select course_id, event_number, count(date) as count
from course_event_summary
group by course_id, event_number
having count > 1;
```

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

