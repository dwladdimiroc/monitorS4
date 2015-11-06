set term postscript eps color blacktext "Helvetica" 16 enhanced
set output "../graphics/processSystem.eps" 
set key left top
set grid y
set xlabel 'Time (s)'
set xrange [0:3600]
set xtics 600
set ylabel "# Events"
set yrange [0:1000]
set ytics 200
set y2label "# Replicas"
set y2range [0:12]
set y2tics 3
set datafile separator ","
plot "../statistics/lambda@processElements.StopwordPE.csv" using 1:2 title 'Input rate (events/5s)' with linespoints pi 25 lw 3 axes x1y1, \
	"../statistics/mu@processElements.MongoPE.csv" using 1:2 title 'Throughput (events/5s)' with linespoints lc rgb "#006600" pi 25 lw 3 axes x1y1, \
	"../statistics/replicationTotal.csv" using 1:6 title 'Nums. of PE' with linespoints pi 25 lw 3 axes x1y2
exit