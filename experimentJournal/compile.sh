clear
./s4 s4r -a=topology.Topology -b=`pwd`/build.gradle experimentThesis
./s4 deploy -s4r=`pwd`/build/libs/experimentThesis.s4r -c=cluster1 -appName=experimentThesis
