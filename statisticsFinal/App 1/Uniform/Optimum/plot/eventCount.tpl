set term postscript eps color blacktext "Helvetica" 16 enhanced
set output "../graphics/eventCount.eps" 
set key left top
set grid y
set xlabel 'Tiempo (s)'
set xrange [0:4200]
set ylabel "# Eventos"
set yrange [0:450000]
set format y "%.1t*10^{%3T}"
set datafile separator ","
plot "../statistics/dequeued@textInput.csv" using 1:2 title 'PE 1' with linespoints pi 25 lw 3, \
	"../statistics/dequeued@languageStream.csv" using 1:2 title 'PE 2' with linespoints pi 25 lc rgb '#006600' lw 3, \
	"../statistics/dequeued@counterStream.csv" using 1:2 title 'PE 3' with linespoints pi 25 lw 3, \
	"../statistics/dequeued@mongoStream.csv" using 1:2 title 'PE 4' with linespoints pi 25 lw 3
exit