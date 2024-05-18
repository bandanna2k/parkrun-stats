
DATE_DIR=`date +"%Y-%m-%d_%H-%M-%S"`
BACKUP_DIR=mysql/backup/$DATE_DIR/ 

mkdir -p $BACKUP_DIR/

docker exec mysql-parkrun-stats mysqldump -h127.0.0.1 -uroot -pfractal --databases parkrun_stats_NZ > $BACKUP_DIR/mysqldump.parkrun_stats_NZ.sql
docker exec mysql-parkrun-stats mysqldump -h127.0.0.1 -uroot -pfractal --databases weekly_stats_NZ > $BACKUP_DIR/mysqldump.weekly_stats_NZ.sql

(cd $BACKUP_DIR; ls -alR; pwd)

# TO RESTORE
# sudo docker exec -i mysql-parkrun-stats sh -c 'exec mysql -uroot -pfractal' < /tmp/backup-april-30/mysqldump.weekly_stats.sql