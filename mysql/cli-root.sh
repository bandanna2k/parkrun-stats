
docker run -it \
	--rm \
 	--network host \
	mysql \
	mysql parkrun_stats -h127.0.0.1 -uroot -pfractal $@
