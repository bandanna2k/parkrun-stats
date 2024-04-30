
# schema
#docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
#  "DROP USER 'schema'@'127.0.0.1';"
#docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
#  "CREATE USER 'schema'@'127.0.0.1' IDENTIFIED BY 'fractal';"
#docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
#  "GRANT CREATE, ALTER ON *.* TO 'schema'@'127.0.0.1';"

# dao
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "DROP USER 'dao'@'*';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE USER 'dao'@'*' IDENTIFIED BY 'daoFractaldao';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT SELECT, INSERT, UPDATE, DELETE ON parkrun_stats.* TO 'dao'@'*';"

# stats
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "DROP USER 'stats'@'*';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE USER 'stats'@'*' IDENTIFIED BY 'statsfractalstats';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT CREATE, UPDATE, SELECT, INSERT ON weekly_stats.* TO 'stats'@'*';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT SELECT ON parkrun_stats.* TO 'stats'@'*';"

# test
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "DROP USER 'test'@'*';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE USER 'test'@'*' IDENTIFIED BY 'qa';"
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT CREATE, INSERT, SELECT, UPDATE, DELETE, DROP ON parkrun_stats_test.* TO 'test'@'*';"

