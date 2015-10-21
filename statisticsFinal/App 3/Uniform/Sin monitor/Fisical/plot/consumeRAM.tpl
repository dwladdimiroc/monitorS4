set term postscript eps color blacktext "Helvetica" 14
set output "../graphics/consumeRAM.eps"
set title "Consumo de memoria RAM"
set key left top
set grid y
set xrange [0:900]
set yrange [0:300]
set xlabel 'Tiempo (s)'
set ylabel "Utilizacion de RAM (MB)"
set datafile separator "\t"
plot "../statistics/consumeRAM.csv" using 5:3 title 'Consume RAM' with lines
exit