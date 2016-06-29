set term postscript eps color blacktext "Helvetica" 26 enhanced
set output "statsMem.eps"
set key left top
set xlabel 'Time (s)'
set xrange [0:600]
set xtics 100
set ylabel "Mem"
set yrange [0:100]
set datafile separator ";"
plot "statsMem.csv" using 1:2 title 'Mem (RAM)' with linespoints pi 25 lw 6
exit