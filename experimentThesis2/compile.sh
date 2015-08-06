clear
./s4 s4r -a=topology.Topology -b=`pwd`/build.gradle experimentThesis2
./s4 deploy -s4r=`pwd`/build/libs/experimentThesis2.s4r -c=cluster1 -appName=experimentThesis2