set term postscript eps color blacktext "Helvetica" 14
set output "replicationTwoPE.eps"
set title "Test Paper"
set key left top
set grid y
set xrange[50000:150000.436396]
set xlabel 'time (ms)'
set ylabel 'number of replications'
set datafile separator ","
plot "replicationTwoPE.csv" using 1:2 title 'Replication' with steps
exit