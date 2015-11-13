set term postscript eps color blacktext "Helvetica" 26 enhanced
set output "../graphics/processSystem.eps" 
set size nosquare 1.15,1
set key left top
set xlabel 'Tiempo (s)'
set xrange [0:900]
set xtics 150
set ylabel "# Eventos"
set yrange [0:200]
set ytics 50 nomirror
set y2label "# Replicas"
set y2range [0:10]
set y2tics 2 nomirror
set datafile separator ","
plot "../statistics/lambda@processElements.ProcessOnePE.csv" using 1:2 title 'Tasa de entrada (eventos/s)' with linespoints pi 25 lw 6 axes x1y1, \
	"../statistics/lambda@processElements.MongoPE.csv" using 1:2 title 'Tasa de salida (eventos/s)' with linespoints lc rgb "#006600" pi 25 lw 6 axes x1y1, \
	"../statistics/replicasTotal.csv" using 1:2 title 'Total de replicas' with linespoints pi 25 lw 6 axes x1y2
exit