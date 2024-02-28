

docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE USER 'schema'@'127.0.0.1' IDENTIFIED BY 'fractal';"

docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT CREATE, ALTER ON *.* TO 'schema'@'127.0.0.1';"

docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "CREATE USER 'dao'@'127.0.0.1' IDENTIFIED BY 'daoFractaldao';"

docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
  "GRANT SELECT, INSERT, DELETE ON *.* TO 'dao'@'127.0.0.1';"

