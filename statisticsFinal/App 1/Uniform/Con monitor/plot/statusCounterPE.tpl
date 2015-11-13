set term postscript eps 18 color blacktext "Helvetica" enhanced
set output "../graphics/statusCounterPE.eps"

set multiplot layout 4, 1

set key right top
set key samplen 1
set grid y
set xlabel 'Tiempo (s)'
set xrange [0:4200]
set xtics 600
set ylabel "{/Symbol l}" rotate by 0
set yrange [0:200]
set ytics 100
set lmargin 11
set datafile separator ","
plot "../statistics/lambda@processElements.CounterPE.csv" using 1:2 title '{/Symbol l} (eventos/seg)' with lines lw 5

set key right bottom
set grid y
set ylabel "{/Symbol m}"
set ytics 100
set datafile separator ","
plot "../statistics/mu@processElements.CounterPE.csv" using 1:2 title '{/Symbol m} (eventos/seg)' with lines lw 5

set key right top
set grid y
set ylabel "{/Symbol r}"
set yrange [0:6]
set ytics 2
set datafile separator ","
plot "../statistics/rho@processElements.CounterPE.csv" using 1:2 title '{/Symbol r} = {/Symbol l}/(s{\267}{/Symbol m})' with lines lw 5
unset lmargin

set key right bottom
set grid y
set ylabel "# Eventos" rotate by 90
set yrange [0:125000]
set ytics 50000
set datafile separator ","
plot "../statistics/queue@processElements.CounterPE.csv" using 1:2 title 'Tam. de la cola' with lines lw 5

unset multiplot

exit