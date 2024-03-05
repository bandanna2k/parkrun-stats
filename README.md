# parkrun-stats

# TODO

- Backup DONE / Migrate time DONE / invariant DONE / backup / drop column

- Invariant test for results against attendance NEEDS TESTING

- DB Isolation

- Nicer way of retrying for a URL

- Backfill finsihers in CES

- Attendance records

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

## Finishers does not match results (needs testing)
```
select ces.course_name, ces.event_number, ces.finishers, count(r.athlete_id) as result_count
from course_event_summary ces
left join result r on 
    ces.course_name = r.course_name and
    ces.event_number = r.event_number
group by ces.course_name, ces.event_number
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
select course_long_name, max, ces.date 
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
and ces.finishers = sub2.max;
```




update result
set
    time_seconds = 
(
    select new_time from 
    (
        select from result
        convert(substring(time, 1, 2), unsigned integer) as mins,
        convert(substring(time, 4, 2), unsigned integer) as seconds,
        (((select mins) * 60) + (select seconds)) as new_time
    )
)
where
    length(time) = 5
    time_seconds is null 

select 
    *,
    convert(substring(time, 1, 2), unsigned integer) as mins,
    convert(substring(time, 4, 2), unsigned integer) as seconds,
    (((select mins) * 60) + (select seconds)) as new_time
from result 
where 
    length(time) = 5 and
    time_seconds is null 
limit 10;






update result
set
    time_seconds =
    (
        (  
            convert(substring(time, 1, 1), unsigned integer)  
        ) * 3600
    ) +
    (
        (  
            convert(substring(time, 3, 2), unsigned integer)  
        ) * 60
    ) +
    (
        convert(substring(time, 6, 2), unsigned integer)
    )
where
    length(time) = 7 and
    time_seconds is null;


    course_name = 'anderson' and
    event_number = 1 and
    position = 97;



JUNK

select name, course_name as count
from (select distinct athlete_id, course_name from parkrun_stats.result) as sub1
join athlete using (athlete_id)
limit 10;

select name, count(course_name) as count
from (select distinct athlete_id, course_name from parkrun_stats.result) as sub1
join athlete using (athlete_id)
group by athlete_id
having count > 30
order by count desc, athlete_id asc
limit 50

select athlete_id, course_name, event_number, time from
(
select athlete_id, course_name, event_number, time from result
union
select athlete_id, course_name, event_number, time from result2
) as sub1
where athlete_id = 414811 and course_name = 'wanaka';