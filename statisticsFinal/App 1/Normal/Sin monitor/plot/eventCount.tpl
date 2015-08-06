set term postscript eps color blacktext "Helvetica" 14
set output "../graphics/eventCount.eps"
set title "Cantidad total de eventos procesados"
set key left top
set grid y
set yrange[0:450000]
set xlabel 'Tiempo (s)'
set ylabel "# Eventos"
set datafile separator ","
plot "../statistics/eventCount@processElements.StopwordPE.csv" using 1:2 title 'PE 1' with lines, \
	"../statistics/eventCount@processElements.LanguagePE.csv" using 1:2 title 'PE 2' with lines, \
	"../statistics/eventCount@processElements.CounterPE.csv" using 1:2 title 'PE 3' with lines, \
	"../statistics/eventCount@processElements.MongoPE.csv" using 1:2 title 'PE 4' with lines
exit