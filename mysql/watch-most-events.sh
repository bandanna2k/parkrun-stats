
watch docker exec mysql-parkrun-stats mysql -h127.0.0.1 -uroot -pfractal -e \
        "select * from (select table_name from information_schema.tables where table_name like '%most%' order by table_name desc limit 1) as sub1;"
