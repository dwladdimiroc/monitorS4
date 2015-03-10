set term postscript eps color blacktext "Helvetica" 14
set output "replicationTwoPE.eps"
set title "Two PE - Number of replicas"
set key left top
set grid y
set xrange[50:150.000436396]
set xlabel 'time (s)'
set ylabel 'number of replications'
set datafile separator ","
plot "replicationTwoPESimple.csv" using 2:3 title 'Replication' with steps
exit