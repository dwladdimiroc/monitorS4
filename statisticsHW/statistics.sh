while true
do
	echo $(ps -C java -o pid,pmem,pcpu) | tee -a stats.log
	sleep 2
done
