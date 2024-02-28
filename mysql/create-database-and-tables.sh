


docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uschema -pfractal -e \
  "`cat mysql/scripts/create-database-and-tables.sql`"

