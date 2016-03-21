clear
./s4 s4r -a=topology.Topology -b=`pwd`/build.gradle experimentJournal
./s4 deploy -s4r=`pwd`/build/libs/experimentJournal.s4r -c=cluster1 -appName=experimentJournal
