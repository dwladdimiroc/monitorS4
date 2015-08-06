set term postscript eps color blacktext "Helvetica" 14
set output "../graphics/replication.eps"
set title "Cantidad de replicas en los PEs"
set key right top
set grid y
set yrange[0:6]
set xlabel 'Tiempo (s)'
set ylabel "# Replicas"
set datafile separator ","
plot "../statistics/replication@processElements.StopwordPE.csv" using 1:2 title 'PE 1' with lines, \
	"../statistics/replication@processElements.LanguagePE.csv" using 1:2 title 'PE 2' with lines, \
	"../statistics/replication@processElements.CounterPE.csv" using 1:2 title 'PE 3' with lines, \
	"../statistics/replication@processElements.MongoPE.csv" using 1:2 title 'PE 4' with lines
exit