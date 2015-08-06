set term postscript eps color blacktext "Helvetica" 14
set output "../graphics/consumeCPU.eps"
set title "Porcentaje de CPU utilizada"
set key right top
set grid y
set xrange[0:900]
set yrange[0:30]
set xlabel 'Tiempo (s)'
set ylabel "% Utilizacion de CPU"
set datafile separator ","
plot "../statistics/consumeCPU.csv" using 9 title '% cpu' with linespoints pi 50
exit