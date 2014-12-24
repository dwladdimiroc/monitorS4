set term postscript eps color blacktext "Helvetica" 14
set output "replicationMongoPE.eps"
set title "Mongo PE - Number of replicas"
set key left top
set grid y
set xrange[0:10000]
set yrange[0:3]
set xlabel 'time (ms)'
set ylabel 'number of replications'
set datafile separator ","
plot "replicationMongoPE.csv" using 1:2 title 'Replication' with steps
exit