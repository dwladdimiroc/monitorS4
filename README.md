# monitorS4
Sistema de distribución de carga para Apache S4, utilizando un algoritmo reactivo y predictivo, además de un algoritmo de administración de réplicas según los recursos disponibles en la máquina que aloja el sistema.

Instalación
---
Para la configuración del motor de procesamiento de stream S4, se deberán instalar ciertos paquetes, por lo que se deberán seguir los siguientes pasos:

Instalar OpenJDK

	$ sudo apt-get install openjdk-7-jdk

Configuración de OpenJDK

	$ sudo update-alternatives --install "/usr/bin/java" "java" "/usr/lib/jvm/java-7-openjdk-amd64/bin/java" 1
	$ sudo update-alternatives --install "/usr/bin/javac" "javac" "/usr/lib/jvm/java-7-openjdk-amd64/bin/javac" 1
	$ sudo update-alternatives --install "/usr/bin/javaws" "javaws" "/usr/lib/jvm/java-7-openjdk-amd64/bin/javaws" 1

Apache S4

	Download: http://archive.apache.org/dist/incubator/s4/s4-0.6.0-incubating/apache-s4-0.6.0-incubating-src.zip

Gradle

	Download: https://services.gradle.org/distributions/gradle-1.4-all.zip

Instalar S4

En la carpeta de S4, se deberá realizar el siguiente comando:

	$ ../gradle-1.4/bin/gradle wrapper

Luego deberá ejecutar el siguiente comando,

	$ ./gradlew install -DskipTests
	$ ./gradlew s4-tools:installApp

Create App

Para crear una nueva aplicación, se deberá ejecutar el siguiente comando:

	$ ./s4 newApp myApp -parentDir=/tmp

donde 'myApp' es el nombre de la aplicación y /tmp corresponde al directorio donde será guardado el proyecto.

Al crear el proyecto, existen ciertos archivos que podrían ser útiles modificarlos, como:

	build.gradle  --> El archivo de creación por plantilla, que tendrá que personalizar
	gradlew --> referencias del script gradlew de la instalación S4
	s4 --> Referencia el script s4 de la instalación de S4, y añade una tarea "adapter"
	src/ --> Sources (maven-like structure)

Execute App

Ejecutar en la carpeta de Apache S4, el siguiente comando:

	$ ./s4 zkServer -clusters=c=cluster1:flp=12000:nbTasks=2 -clean

Luego, se deberá levantar un nodo, el cual se deberá realizar desde la carpeta de la aplicación con el siguiente comando:

	$ ./s4 node -c=cluster1

Para poder compilar el programa, se deberán ejecutar los dos siguientes comandos:

	$ ./s4 s4r -a=hello.HelloApp -b=`pwd`/build.gradle myApp
	$ ./s4 deploy -s4r=`pwd`/build/libs/myApp.s4r -c=cluster1 -appName=myApp

Debe tomarse en consideración que 'hello.HelloApp' se deberá cambiar por el nombre de la clase con su correspondiente package que está incorporado. Así mismo 'myApp', que dependerá del nombre de la aplicación que se colocó.

Para poder verificar el estado del sistema, puede utilizarse el siguiente comando

	$ ./s4 status

Finalmente, para poder escuchar los datos entregados a cada uno de los PE, se necesita crear un adapter, el cual estará encargado de entregar los flujos de datos a cada uno de los nodos. Por lo tanto, para poder realizar esto, se deberá ejecutar los siguientes comandos:

	$ ./s4 newCluster -c=cluster2 -nbTasks=1 -flp=13000
	$ ./s4 deploy -appClass=hello.HelloInputAdapter -p=s4.adapter.output.stream=names -c=cluster2 -appName=adapter
	$ ./s4 adapter -c=cluster2

Si se desea utilizar las métricas por defecto de S4, se debe utilizar como parámetro el siguiente comando:

	$ -p=s4.metrics.config=csv:/path/to/directory:TIME:TIMEUNIT
