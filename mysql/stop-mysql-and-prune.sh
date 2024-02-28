
echo "Stopping"
docker stop mysql-parkrun-stats

echo "Removing"
docker container rm mysql-parkrun-stats 
