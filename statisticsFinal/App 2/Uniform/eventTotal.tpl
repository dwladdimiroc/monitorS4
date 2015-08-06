set term postscript eps color blacktext "Helvetica" 14
set output "eventTotal.eps"
set title "Cantidad de eventos procesados"

set key left top
set grid y
set xlabel 'Monitor'
set ylabel 'Tiempo (s)'

set style data histogram
set style fill solid border
set datafile separator ","

plot 'eventTotal.csv' using 2:xticlabels(1) title columnheader