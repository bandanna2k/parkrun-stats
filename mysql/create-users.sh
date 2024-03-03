
# schema
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE USER 'schema'@'127.0.0.1' IDENTIFIED BY 'fractal';"

docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT CREATE, ALTER ON *.* TO 'schema'@'127.0.0.1';"

# dao
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE USER 'dao'@'127.0.0.1' IDENTIFIED BY 'daoFractaldao';"

docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT SELECT, INSERT, DELETE ON *.* TO 'dao'@'127.0.0.1';"

# stats
docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE USER 'stats'@'127.0.0.1' IDENTIFIED BY 'statsfractalstats';"

docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT CREATE, UPDATE, SELECT, INSERT ON *.* TO 'stats'@'127.0.0.1';"

