set term postscript eps color blacktext "Helvetica" 30
set output "../graphics/consumeRAM.eps"
set title "Memory RAM consumed"
set key right bottom
set grid y
set xrange [0:900]
set yrange [0:400]
set xlabel 'Time (s)'
set ylabel "Memory (MB)"
set xtics 200
set datafile separator "\t"
plot "../statistics/consumeRAM.csv" using 5:3 notitle with lines lw 2
exit