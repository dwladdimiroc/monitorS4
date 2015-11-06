set term postscript eps color blacktext "Helvetica" 26 enhanced
set output "../graphics/processSystem.eps" 
set size nosquare 1.15,1
set key left top
set xlabel 'Time (s)'
set xrange [0:900]
set xtics 150
set ylabel "# Events"
set yrange [0:300]
set ytics 50 nomirror
set y2label "# Replicas"
set y2range [0:10]
set y2tics 2 nomirror
set datafile separator ","
plot "../statistics/lambda@processElements.ProcessOnePE.csv" using 1:3 title 'Input rate (events/s)' with linespoints pi 25 lw 6 axes x1y1, \
	"../statistics/lambda@processElements.MongoPE.csv" using 1:3 title 'Throughput (events/s)' with linespoints lc rgb "#006600" pi 25 lw 6 axes x1y1, \
	"../statistics/replicationTotal.csv" using 1:5 title '# Replicas' with linespoints pi 25 lw 6 axes x1y2
exit