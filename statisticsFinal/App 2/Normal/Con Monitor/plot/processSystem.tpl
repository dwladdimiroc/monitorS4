set term postscript eps color blacktext "Helvetica" 26 enhanced
set output "../graphics/processSystem.eps" 
set size nosquare 1.15,1
set key left top
set xlabel 'Tiempo (s)'
set xrange [0:4200]
set xtics 600
set ylabel "# Eventos"
set yrange [0:400]
set ytics 100 nomirror
set y2label "# Replicas"
set y2range [0:24]
set y2tics 6 nomirror
set datafile separator ","
plot "../statistics/lambda@processElements.SplitPE.csv" using 1:2 title 'Flujo de eventos (eventos/s)' with linespoints pi 25 lw 6 axes x1y1, \
	"../statistics/replicationTotal.csv" using 1:2 title 'Total de replicas' with linespoints pi 25 lw 6 lc 3 axes x1y2
exit