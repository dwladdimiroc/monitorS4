set term postscript eps color blacktext "Helvetica" 14
set output "timeEvent.eps"
set title "Event average time without replication"
set key left top
set grid y
set xlabel 'event'
set ylabel "time (s)"
set datafile separator ","
plot "timeEvent.csv" using 1:6 title 'Average time' with lines
exit