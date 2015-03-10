set term postscript eps color blacktext "Helvetica" 14
set output "stadistics.eps"
set title "Tiempo total del sistema según distintos umbrales"

#unset key
set style data histogram
set style fill solid border

set key left top
set grid y
set xlabel 'theta'
set ylabel 'time (s)'
set yrange[305:315]
set datafile separator ","
set style histogram clustered

plot 'Stadistics.csv' using 2:xticlabels(1) title columnheader

exit