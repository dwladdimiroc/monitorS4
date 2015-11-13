set term postscript eps color blacktext "Helvetica" 24
set output "../graphics/consumeCPU.eps"
set key right top
set grid y
set xrange[0:900]
set yrange[0:30]
set xlabel 'Tiempo (s)'
set ylabel "% CPU"
set datafile separator ","
plot "../statistics/consumeCPU.csv" using 9 notitle with linespoints pi 50 lw 6
exit