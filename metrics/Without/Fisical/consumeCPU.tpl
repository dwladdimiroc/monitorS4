set term postscript eps color blacktext "Helvetica" 14
set output "consumeCPU.eps"
set title "Porcentaje de CPU utilizada"
set key right top
set grid y
#set yrange[0:120]
set xlabel 'Tiempo (s)'
set ylabel "% CPU"
set datafile separator "\t"
plot "consumeCPU.csv" using 3 title '% user' with lines, \
	"consumeCPU.csv" using 5 title '% system' with lines
exit