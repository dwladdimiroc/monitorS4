set term postscript eps 11 color blacktext "Helvetica" enhanced
set output "../graphics/statusStopwordPE.eps"

set multiplot layout 5, 1 title "Estadisticas del PE Stopword"

set key right top
set key samplen 1
set grid y
set xlabel 'Tiempo (s)'
set xrange [0:4200]
set ylabel "{/Symbol l}" rotate by 0
set ytics 100
set lmargin 9
set datafile separator ","
plot "../statistics/lambda@processElements.StopwordPE.csv" using 1:2 title '{/Symbol l} (eventos/seg)' with lines lw 3

set key right bottom
set grid y
set xlabel 'Tiempo (s)'
set xrange [0:4200]
set ylabel "{/Symbol m}"
set ytics 100
set datafile separator ","
plot "../statistics/mu@processElements.StopwordPE.csv" using 1:2 title '{/Symbol m} (eventos/seg)' with lines lw 3

set key right top
set grid y
set xlabel 'Tiempo (s)'
set xrange [0:4200]
set ylabel "{/Symbol r}"
set ytics 2
set datafile separator ","
plot "../statistics/rho@processElements.StopwordPE.csv" using 1:2 title '{/Symbol r} = {/Symbol l}/(s{\267}{/Symbol m})' with lines lw 3
unset lmargin

set key right top
set grid y
set xlabel 'Tiempo (s)'
set xrange [0:4200]
set ylabel "# Eventos" rotate by 90
set ytics 200
set datafile separator ","
plot "../statistics/queue@processElements.StopwordPE.csv" using 1:2 title 'Tam. de la cola' with lines lw 3

set key right bottom
set grid y
set ytics 1
set yrange[0:3]
set lmargin 9
set xlabel 'Tiempo (s)'
set xrange [0:4200]
set ylabel "# Replicas"
set datafile separator ","
plot "../statistics/replication@processElements.StopwordPE.csv" using 1:2 title 'Cant. de replicas' with lines lw 3

unset multiplot

exit