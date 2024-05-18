# SELECT User, Host FROM mysql.user;
# SELECT * FROM mysql.db WHERE Db = 'parkrun_stats'\G;

echo "Expected error dropping old users."

# dao
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "DROP USER 'dao'@'127.0.0.1';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "DROP USER 'dao'@'*';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "DROP USER 'dao'@'%';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE USER 'dao'@'%' IDENTIFIED BY '0b851094';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT SELECT, INSERT, UPDATE, DELETE ON parkrun_stats_NZ.* TO 'dao'@'%';"

# stats
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "DROP USER 'stats'@'127.0.0.1';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "DROP USER 'stats'@'*';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "DROP USER 'stats'@'%';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE USER 'stats'@'%' IDENTIFIED BY '4b0e7ff1';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT CREATE, UPDATE, SELECT, INSERT ON weekly_stats.* TO 'stats'@'%';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT SELECT ON parkrun_stats_NZ.* TO 'stats'@'%';"

# test
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "DROP USER 'test'@'127.0.0.1';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "DROP USER 'test'@'*';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "DROP USER 'test'@'%';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE USER 'test'@'%' IDENTIFIED BY 'qa';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT CREATE, INSERT, SELECT, UPDATE, DELETE, DROP ON parkrun_stats_test.* TO 'test'@'%';"

