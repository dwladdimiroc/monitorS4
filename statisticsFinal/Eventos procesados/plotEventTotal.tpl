set term postscript eps color blacktext "Helvetica" 14 enhanced
set output "eventTotal.eps"

set key right top
set grid y
set ylabel '# Events'
set yrange [0:420000]
set format y "%.1t*10^{%3T}"

set auto x
set style data histogram
set style histogram cluster gap 1
set style fill solid border -2
set boxwidth 0.9
set xtic rotate by -15 scale 0

set datafile separator ","
plot 'eventTotal.csv' using 2:xtic(1) ti col, '' u 3 ti col, '' u 4 ti col