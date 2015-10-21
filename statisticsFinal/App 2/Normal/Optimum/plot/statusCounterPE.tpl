set term postscript eps 11 color blacktext "Helvetica"
set output "../graphics/statusCounterPE.eps"

set multiplot layout 5, 1 title "Estadisticas del PE Counter"

set title "Tasa de llegada"
set key right top
set grid y
set xlabel 'Tiempo (s)'
set ylabel "# Eventos"
set ytics 500
set yrange [0:1000]
set datafile separator ","
plot "../statistics/lambda@processElements.CounterPE.csv" using 1:2 title 'lambda' with lines

set title "Tasa de procesamiento"
set key right top
set grid y
set xlabel 'Tiempo (s)'
set ylabel "# Eventos"
set ytics 500
set yrange [0:1000]
set datafile separator ","
plot "../statistics/mu@processElements.CounterPE.csv" using 1:2 title 'mu' with lines

set title "Tasa de rendimiento"
set key right top
set grid y
set xlabel 'Tiempo (s)'
set ylabel "rho"
set yrange [0:8]
set ytics 4
set datafile separator ","
plot "../statistics/rho@processElements.CounterPE.csv" using 1:2 title 'rho' with lines

set title "Cola"
set key right top
set grid y
set xlabel 'Tiempo (s)'
set ylabel "# Eventos"
set yrange [0:200000]
set ytics 100000
set datafile separator ","
plot "../statistics/queue@processElements.CounterPE.csv" using 1:2 title 'queue' with lines

set title "Cantidad de replicas"
set key right top
set grid y
set ytics 5
set yrange[0:15]
set xlabel 'Tiempo (s)'
set ylabel "# Replicas"
set datafile separator ","
plot "../statistics/replication@processElements.CounterPE.csv" using 1:2 title 'replicas' with lines

unset multiplot

exit