set term postscript eps color blacktext "Helvetica" 14
set output "avgEventProcess.eps"
set title "Tiempo promedio"
set key left top
set grid y
#set yrange[0:120]
set xlabel 'Tiempo (s)'
set ylabel "Tiempo (ms)"
set datafile separator ","
plot "avgEventProcess.csv" using 1:2 title 'Tiempo promedio de procesamiento total por evento' with lines
exit