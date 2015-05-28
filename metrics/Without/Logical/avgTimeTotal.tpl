set term postscript eps color blacktext "Helvetica" 14
set output "avgTimeTotal.eps"
set title "Tiempo promedio de procesamiento de cada evento"
set key right top
set grid y
#set yrange[0:120]
set xlabel 'Tiempo (s)'
set ylabel "Tiempo de procesamiento (ms)"
set datafile separator ","
plot "avgTimeTotal.csv" using 1:2 title 'Tiempo promedio' with lines
exit