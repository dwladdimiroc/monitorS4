set term postscript eps color blacktext "Helvetica" 14
set output "consumeRAM.eps"
set title "System consume RAM with replication"
set key left top
set grid y
set yrange [0:450]
set xlabel 'Tiempo (s)'
set ylabel "Resident size (MB)"
set datafile separator ","
plot "consumeRAM.csv" using 5:3 title 'Consume RAM' with lines
exit