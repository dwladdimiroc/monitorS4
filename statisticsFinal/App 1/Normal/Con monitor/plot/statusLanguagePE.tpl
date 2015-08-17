set term postscript eps 11 color blacktext "Helvetica"
set output "../graphics/statusLanguagePE.eps"

set multiplot layout 5, 1 title "Estado del PE Language"

set title "Tasa de llegada"
set key right top
set grid y
set xlabel 'Tiempo (s)'
set ylabel "# Eventos"
set ytics 500
set datafile separator ","
plot "../statistics/lambda@processElements.LanguagePE.csv" using 1:2 title 'lambda' with lines

set title "Tasa de procesamiento"
set key right top
set grid y
set xlabel 'Tiempo (s)'
set ylabel "# Eventos"
set ytics 500
set datafile separator ","
plot "../statistics/mu@processElements.LanguagePE.csv" using 1:2 title 'mu' with lines

set title "Tasa de rendimiento"
set key right top
set grid y
set xlabel 'Tiempo (s)'
set ylabel "rho"
set yrange[0:2]
set ytics 1
set datafile separator ","
plot "../statistics/rho@processElements.LanguagePE.csv" using 1:2 title 'rho' with lines

set title "Cola"
set key right top
set grid y
set xlabel 'Tiempo (s)'
set ylabel "# Eventos"
set yrange[0:3]
set ytics 1
set datafile separator ","
plot "../statistics/queue@processElements.LanguagePE.csv" using 1:2 title 'queue' with lines

set title "Cantidad de replicas"
set key right top
set grid y
set ytics 1
set yrange[0:2]
set xlabel 'Tiempo (s)'
set ylabel "# Replicas"
set datafile separator ","
plot "../statistics/replication@processElements.LanguagePE.csv" using 1:2 title 'replicas' with lines

unset multiplot

exit