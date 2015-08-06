set term postscript eps color blacktext "Helvetica" 14
set output "avgTimeTotal.eps"
set title "Tiempo promedio de procesamiento de cada evento"
set key right top
set grid y
set yrange[0:4000]
set xlabel 'Tiempo (s)'
set ylabel "Tiempo de procesamiento (s)"
set datafile separator ","
plot "avgTimeTotal-cm.csv" using 1:3 title 'Con monitor' with linespoints pi 50, \
	"avgTimeTotal-sm.csv" using 1:3 title 'Sin monitor' with linespoints pi 50
exit