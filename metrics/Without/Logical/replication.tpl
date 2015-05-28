set term postscript eps color blacktext "Helvetica" 14
set output "replication.eps"
set title "Cantidad de replicas en los PEs"
set key right top
set grid y
set yrange[0:10]
set xlabel 'Tiempo (s)'
set ylabel "# Replicas"
set datafile separator ","
plot "replication@processElements.ProcessOnePE.csv" using 1:2 title 'PE 1' with lines, \
	"replication@processElements.ProcessTwoPE.csv" using 1:2 title 'PE 2' with lines, \
	"replication@processElements.MongoPE.csv" using 1:2 title 'PE 3' with lines
exit