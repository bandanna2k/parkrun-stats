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

## Get Most Events

```
select name, athlete_id, count(course_name) as count, athlete_id
from (select distinct athlete_id, course_name from result) as sub1
join athlete using (athlete_id)
group by athlete_id
having count > 3
order by count desc, athlete_id desc 
limit 20;
```

### Get athlete history

```
select athlete_id, course_name, min(position) as best_position
from athlete
left join result using (athlete_id)
group by athlete_id, course_name
having athlete_id = 414811;
```