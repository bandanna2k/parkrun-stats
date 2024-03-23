
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE DATABASE IF NOT EXISTS parkrun_stats"

docker exec mysql-parkrun-stats mysql parkrun_stats -h127.0.0.1 -uroot -pfractal -e \
  "`cat mysql/scripts/create-database-and-tables.sql`"


docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE DATABASE IF NOT EXISTS parkrun_stats_test"

docker exec mysql-parkrun-stats mysql parkrun_stats_test -h127.0.0.1 -uroot -pfractal -e \
  "`cat mysql/scripts/create-database-and-tables.sql`"

## Adjust/hack data
#docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
#  "INSERT INTO parkrun_stats.course (course_id, course_name, course_long_name,   country_code, country, status) VALUES (2, 'cornwall', 'Cornwall parkrun', 65, 'NZ', 'R')"
#docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
#  "UPDATE parkrun_stats.course SET course_long_name = 'Cornwall Park parkrun' WHERE course_name = 'cornwall'"
