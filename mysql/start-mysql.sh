
mysql/stop-mysql-and-prune.sh

DB_DIR=~/mysql/databases/parkrun-stats

mkdir -p $DB_DIR

MYSQL_SCRIPTS_DIR=$PWD/mysql/scripts

docker run \
 	-d \
 	--network host \
	--name mysql-parkrun-stats \
  -e MYSQL_ROOT_PASSWORD=fractal \
	-v $DB_DIR:/var/lib/mysql \
 	mysql

