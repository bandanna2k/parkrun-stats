


docker exec mysql-parkrun-stats mysql -hlocalhost -uschema -pfractal -e \
  "`cat mysql/scripts/create-database-and-tables.sql`"

