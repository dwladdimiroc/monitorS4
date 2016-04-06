clear
./s4 s4r -a=topology.Topology -b=`pwd`/build.gradle quotesApp
./s4 deploy -s4r=`pwd`/build/libs/quotesApp.s4r -c=cluster1 -appName=quotesApp
