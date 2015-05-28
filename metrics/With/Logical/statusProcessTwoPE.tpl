set term postscript eps color blacktext "Helvetica" 14
set output "statusProcessTwoPE.eps"

set multiplot layout 4, 1 title "Estado del PE ProcessTwo"

set title "Tasa de llegada"
set key right top
set grid y
set xlabel 'Tiempo (s)'
set ylabel "lambda"
set ytics 1000
set datafile separator ","
plot "lambda@processElements.ProcessTwoPE.csv" using 1:2 title 'lambda' with lines

set title "Tasa de procesamiento"
set key right top
set grid y
set xlabel 'Tiempo (s)'
set ylabel "mu"
set ytics 1000
set datafile separator ","
plot "mu@processElements.ProcessTwoPE.csv" using 1:2 title 'mu' with lines

set title "Tasa de rendimiento"
set key right top
set grid y
set xlabel 'Tiempo (s)'
set ylabel "rho"
set ytics 1
set datafile separator ","
plot "rho@processElements.ProcessTwoPE.csv" using 1:2 title 'rho' with lines

set title "Cola"
set key right top
set grid y
set xlabel 'Tiempo (s)'
set ylabel "# Eventos"
set ytics 500
set datafile separator ","
plot "queue@processElements.ProcessTwoPE.csv" using 1:2 title 'queue' with lines

unset multiplot

exit