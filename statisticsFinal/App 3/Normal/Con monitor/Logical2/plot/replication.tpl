set term postscript eps color blacktext "Helvetica" 30
set output "../graphics/replication.eps"
set title "Number of replies"
set key right bottom
set grid y
set yrange[0:4]
set ytics 1
set xtics 200
set xlabel 'Time (s)'
set ylabel "# Replies"
set datafile separator ","
plot "../statistics/replication@processElements.ProcessOnePE.csv" using 1:2 title 'Operator 1' with lines lw 3, \
	"../statistics/replication@processElements.ProcessTwoPE.csv" using 1:2 title 'Operator 2' with lines lw 3, \
	"../statistics/replication@processElements.MongoPE.csv" using 1:2 title 'Operator 3' with lines lw 3
exit