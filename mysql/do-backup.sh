
DATE_DIR=`date +"%Y-%m-%d_%H-%M-%S"`
BACKUP_DIR=mysql/backup/$DATE_DIR/ 

mkdir -p $BACKUP_DIR/parkrun_stats
mkdir -p $BACKUP_DIR/weekly_stats

cp /root/mysql/databases/parkrun-stats/parkrun_stats/* $BACKUP_DIR/parkrun_stats
cp /root/mysql/databases/parkrun-stats/weekly_stats/* $BACKUP_DIR/weekly_stats

(cd $BACKUP_DIR; ls -alR; pwd)
