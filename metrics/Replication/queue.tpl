set term postscript eps color blacktext "Helvetica" 14
set output "queue.eps"
set title "System queue size with replication"
set key right top
set grid y
set yrange[0:120]
set xlabel 'time (s)'
set ylabel "queue"
set datafile separator ","
plot "queue.csv" using 5:3 title 'Queue' with steps
exit