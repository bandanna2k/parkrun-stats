# parkrun-stats

# TODO

- Convert time to int (java)

- Convert time to int (db)

- Invariant test for results against attendance

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
limit 20;
```

## Get Most Different Events

```
select name, athlete_id, count(course_name) as count, athlete_id
from (select distinct athlete_id, course_name from parkrun_stats.result) as sub1
join athlete using (athlete_id)
group by athlete_id
having count > 3
order by count desc, athlete_id asc 
limit 20;
```

### Best position at course athlete history

```
select athlete_id, course_name, min(position) as best_position
from athlete
left join result using (athlete_id)
group by athlete_id, course_name
having athlete_id = 414811;
```

### Count of runs at courses

```
select athlete_id, course_name, count(course_name) as count
from athlete
left join result using (athlete_id)
group by athlete_id, course_name
having athlete_id = 414811
order by count desc;
```

# INVARIANTS

## Course Event Summary with finishers not entered
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
