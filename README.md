# parkrun-stats

# What to run?

Run MostEvents.main (this will fill a database with all region results from active parkruns)

Run Stats.main <date> E.g. java -jar Stats.jar 25/12/2023 
(this create a Most Events table, and an Attendance table, and output to HTML)

# TODO

## Major

- Prove?? Parsers can break half way through. Collate records first then stream records.

- Retrieve result.age group and result.age grade

- Make work well on mobile

## Minor

- Bug - Top 10 in region is always empty on first run so assert kicks in.  

- Revisit pIndex downloads. I don't think the criteria for downloading the athletes its correct.

- Do I need name/most global events in most events table?

- Bug - When course not found, athlete course summary not finished.

- Invariant - region event count > global event count

- Invariants - 10 incrementing numbers, 1 number stays the same for all weekly tables.

- Change 'How are we doing test to assume next event number'

## Fixed Bugs

- Bug - Why is 1311970 not getting collected for pIndex. No bug. 1311970 has NZ pIndex of 5 (needs to be 6)

## Ideas

- Add unique runs to Most Events???


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
