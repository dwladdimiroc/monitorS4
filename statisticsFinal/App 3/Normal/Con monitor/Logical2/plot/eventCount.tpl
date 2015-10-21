set term postscript eps color blacktext "Helvetica" 30
set output "../graphics/eventCount.eps"
set title "Total number of processed events"
set key left top
set grid y
set xlabel 'Time (s)'
set ylabel "# Events"
set yrange [0:100000]
set xtics 200
set datafile separator ","
plot "../statistics/dequeued@textInput.csv" using 1:2 title 'Operator 1' with linespoints pi 10, \
	"../statistics/dequeued@processTwoStream.csv" using 1:2 title 'Operator 2' with linespoints pi 10, \
	"../statistics/dequeued@mongoStream.csv" using 1:2 title 'Operator 3' with linespoints pi 10
exit