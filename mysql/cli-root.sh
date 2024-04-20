
docker run -it \
	--rm \
 	--network host \
	mysql \
	mysql -h127.0.0.1 -uroot -pfractal --default-character-set=utf8mb4 $@
