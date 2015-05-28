set term postscript eps color blacktext "Helvetica" 14
set output "eventCount.eps"
set title "Cantidad de eventos procesados"
set key right top
set grid y
#set yrange[0:120]
set xlabel 'Tiempo (s)'
set ylabel "# Eventos"
set datafile separator ","
plot "eventCount@processElements.ProcessOnePE.csv" using 1:2 title 'PE 1' with lines, \
	"eventCount@processElements.ProcessTwoPE.csv" using 1:2 title 'PE 2' with lines, \
	"eventCount@processElements.MongoPE.csv" using 1:2 title 'PE 3' with lines
exit