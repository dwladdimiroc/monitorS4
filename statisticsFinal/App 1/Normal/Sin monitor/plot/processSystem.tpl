set term postscript eps color blacktext "Helvetica" 26 enhanced
set output "../graphics/processSystem.eps" 
set size nosquare 1.15,1
set key left top
set xlabel 'Tiempo (s)'
set xrange [0:4200]
set xtics 600
set ylabel "# Eventos"
set yrange [0:200]
set ytics 50 nomirror
set y2label "# Replicas"
set y2range [0:15]
set y2tics 3 nomirror
set datafile separator ","
plot "../statistics/lambda@processElements.StopwordPE.csv" using 1:2 title 'Tasa de entrada (eventos/s)' with linespoints pi 25 lw 6 axes x1y1, \
	"../statistics/mu@processElements.MongoPE.csv" using 1:2 title 'Tasa de salida (eventos/s)' with linespoints lc rgb "#006600" pi 25 lw 6 axes x1y1, \
	"../statistics/replicationTotal.csv" using 1:2 title 'Total de replicas' with linespoints pi 25 lw 6 axes x1y2
exit