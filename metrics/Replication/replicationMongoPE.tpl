set term postscript eps color blacktext "Helvetica" 14
set output "replicationMongoPE.eps"
set title "Mongo PE - Number of replicas"
set key left top
set grid y
set xrange[0:10]
set yrange[0:3]
set xlabel 'time (s)'
set ylabel 'number of replications'
set datafile separator ","
plot "replicationMongoPESimple.csv" using 2:3 title 'Replication' with steps
exit