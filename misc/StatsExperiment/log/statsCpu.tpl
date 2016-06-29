set term postscript eps color blacktext "Helvetica" 26 enhanced
set output "statsCpu.eps"
set key left top
set xlabel 'Time (s)'
set xrange [0:600]
set xtics 100
set ylabel "Cpu"
set yrange [0:800]
set datafile separator ";"
plot "statsCpu.csv" using 1:2 title 'CPU' with linespoints pi 25 lw 6
exit