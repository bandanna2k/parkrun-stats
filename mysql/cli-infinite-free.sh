
docker run -it \
	--rm \
 	--network host \
	mysql \
	mysql -hsql202.infinityfree.com -uif0_36636281 -pQMcPuezZurVFZ --default-character-set=utf8mb4 $@
