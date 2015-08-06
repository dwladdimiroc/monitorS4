set term postscript eps color blacktext "Helvetica" 14
set output "avgEventProcess.eps"
set title "Cantidad promedio de eventos procesados"
set key right top
set grid y
set yrange[0:650]
set xlabel 'Tiempo (s)'
set ylabel "# Eventos"
set datafile separator ","
plot "avgEventProcess-cm.csv" using 1:2 title 'Con monitor' with lines, \
	"avgEventProcess-sm.csv" using 1:2 title 'Sin monitor' with lines
exit