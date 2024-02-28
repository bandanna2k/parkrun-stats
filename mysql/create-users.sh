


docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE USER 'schema'@'localhost' IDENTIFIED BY 'fractal';"

docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT CREATE, ALTER ON *.* TO 'schema'@'localhost';"

docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE USER 'dao'@'localhost' IDENTIFIED BY 'daoFractaldao';"

docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT SELECT, INSERT, DELETE ON *.* TO 'dao'@'localhost';"

