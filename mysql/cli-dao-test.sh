
docker run -it \
	--rm \
 	--network host \
	mysql \
	mysql parkrun_stats_test -h127.0.0.1 -udao -pdaoFractaldao $@
