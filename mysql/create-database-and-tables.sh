
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE DATABASE IF NOT EXISTS parkrun_stats"

docker exec mysql-parkrun-stats mysql parkrun_stats -h127.0.0.1 -uroot -pfractal -e \
  "`cat mysql/scripts/create-database-and-tables.sql`"


docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE DATABASE IF NOT EXISTS parkrun_stats_test"

docker exec mysql-parkrun-stats mysql parkrun_stats_test -h127.0.0.1 -uroot -pfractal -e \
  "`cat mysql/scripts/create-database-and-tables.sql`"

