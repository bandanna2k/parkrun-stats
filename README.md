# parkrun-stats

# TODO

- Backup / Migrate time / invariant / backup / drop column

- Invariant test for results against attendance

- DB Isolation

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

## Get Most Different Events with Total NZ Events (WIP)

```
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
select json_arrayagg(json_object('course_name', course_name, 'count', count)) as json
from (

select course_long_name, count(course_name) as count
from athlete
left join result using (athlete_id)
left join course using (course_name)
group by athlete_id, course_name
having athlete_id = 414811
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
