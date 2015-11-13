set term postscript eps color blacktext "Helvetica" 22 enhanced
set output "../graphics/avgEventProcess.eps"
#set title "Cantidad promedio de eventos procesados"
set key right top
set key samplen 2 spacing 2
set grid y
set xlabel 'Tiempo (s)'
set xrange[0:900]
set xtics 150
set yrange [0:700]
set ylabel "# Eventos"
set datafile separator ","
set style line 1 lw 5 lt 2 pi 5 lc rgb "red"
plot "../statistics/avgEventProcess.csv" using 1:2 title 'Cant. promedio de eventos procesados' with lines ls 1
exit