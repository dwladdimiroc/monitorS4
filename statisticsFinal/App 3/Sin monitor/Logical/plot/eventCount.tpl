set term postscript eps color blacktext "Helvetica" 14
set output "../graphics/eventCount.eps"
set title "Cantidad de eventos procesados"
set key right top
set grid y
set yrange [0:100000]
set xlabel 'Tiempo (s)'
set ylabel "# Eventos"
set datafile separator ","
plot "../statistics/dequeued@textInput.csv" using 1:2 title 'PE 1' with linespoints pi 10, \
	"../statistics/dequeued@processTwoStream.csv" using 1:2 title 'PE 2' with linespoints pi 10, \
	"../statistics/dequeued@mongoStream.csv" using 1:2 title 'PE 3' with linespoints pi 10
exit