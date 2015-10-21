set term postscript eps color blacktext "Helvetica" 30
set output "../graphics/consumeCPU.eps"
set title "CPU used"
set key right top
set grid y
set xrange[0:900]
set yrange[0:30]
set xlabel 'Time (s)'
set ylabel "% CPU"
set xtics 200
set datafile separator ","
plot "../statistics/consumeCPU.csv" using 9 notitle with lines lw 2
exit