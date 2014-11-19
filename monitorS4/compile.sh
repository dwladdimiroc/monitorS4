clear
./s4 s4r -a=topology.Topology -b=`pwd`/build.gradle monitorS4
./s4 deploy -s4r=`pwd`/build/libs/monitorS4.s4r -c=cluster1 -appName=monitorS4