set term postscript eps color blacktext "Helvetica" 14
set output "timeEvent.eps"
set title "Event average time with replication"
set key left top
set grid y
set yrange[0:120]
set xlabel 'event'
set ylabel "time (s)"
set datafile separator ","
plot "timeEvent.csv" using 1:5 title 'Average time' with lines
exit