#start job manager
./docker-entrypoint.sh jobmanager &

sleep 10

#submit job
flink run /usr/share/flink-job.jar