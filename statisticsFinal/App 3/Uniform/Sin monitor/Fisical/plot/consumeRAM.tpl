set term postscript eps color blacktext "Helvetica" 24
set output "../graphics/consumeRAM.eps"
set key right bottom
set grid y
set xrange [0:900]
set yrange [0:300]
set xlabel 'Tiempo (s)'
set ylabel "Memoria (MB)"
set datafile separator "\t"
plot "../statistics/consumeRAM.csv" using 5:3 notitle with lines lw 6
exit