for file in ./*
do
	if [ ${file: -4} == ".tpl" ]
	then	
		gnuplot $file
	fi
done