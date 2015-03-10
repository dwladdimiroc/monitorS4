set term postscript eps color blacktext "Helvetica" 14
set output "queue.eps"
set title "System queue size without replication"
set key left top
set grid y
set xlabel 'time (s)'
set ylabel "queue"
set datafile separator ","
plot "queue.csv" using 3:4 title 'Queue' with steps
exit