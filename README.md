# parkrun-stats

# QUERIES

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
from (select distinct athlete_id, course_name from result) as sub1
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