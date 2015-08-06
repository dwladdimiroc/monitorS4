set term postscript eps color blacktext "Helvetica" 14
set output "../graphics/avgTimeTotal.eps"
set title "Tiempo promedio de procesamiento de cada evento"
set key right top
set grid y
set yrange[0:3500]
set xlabel 'Tiempo (s)'
set ylabel "Tiempo de procesamiento (s)"
set datafile separator ","
plot "../statistics/avgTimeTotal.csv" using 1:3 title 'Tiempo promedio' with lines
exit