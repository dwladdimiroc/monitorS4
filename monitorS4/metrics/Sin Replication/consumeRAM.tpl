set term postscript eps color blacktext "Helvetica" 14
set output "consumeRAM.eps"
set title "System consume RAM without replication"
set key left top
set grid y
set xrange [0:343]
set xlabel 'time (s)'
set ylabel "Resident size (MB)"
set datafile separator ","
plot "consumeRAM.csv" using 5:3 title 'Consume RAM' with lines
exit