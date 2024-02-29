
DATE_DIR=`date +"%Y-%m-%d_%H-%M-%S"`
BACKUP_DIR=mysql/backup/$DATE_DIR/ 

mkdir -p $BACKUP_DIR

cp /root/mysql/databases/parkrun-stats/parkrun_stats/* $BACKUP_DIR
