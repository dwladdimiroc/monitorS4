set term postscript eps color blacktext "Helvetica" 14
set output "avgEventProcess.eps"
set title "Cantidad promedio de eventos procesados"
set key right top
set grid y
#set yrange[0:120]
set xlabel 'Tiempo (s)'
set ylabel "# Eventos"
set datafile separator ","
plot "avgEventProcess.csv" using 1:2 title '# Eventos' with lines
exit