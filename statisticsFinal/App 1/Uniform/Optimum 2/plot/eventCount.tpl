set term postscript eps color blacktext "Helvetica" 14
set output "../graphics/eventCount.eps"
set title "Cantidad total de eventos procesados"
set key left top
set grid y
#set yrange[0:120]
set xlabel 'Tiempo (s)'
set ylabel "# Eventos"
set datafile separator ","
plot "../statistics/dequeued@textInput.csv" using 1:2 title 'PE 1' with linespoints pi 100, \
	"../statistics/dequeued@languageStream.csv" using 1:2 title 'PE 2' with linespoints pi 50, \
	"../statistics/dequeued@counterStream.csv" using 1:2 title 'PE 3' with linespoints pi 100, \
	"../statistics/dequeued@mongoStream.csv" using 1:2 title 'PE 4' with linespoints pi 50
exit