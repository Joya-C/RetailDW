#!/bin/bash

CLASSPATH=.:$HIVE_HOME/conf:$(hadoop classpath)

for i in ${HIVE_HOME}/lib/*.jar ; do
	CLASSPATH=$CLASSPATH:$i
done

for i in ~/commons-csv-1.10.0/*.jar ; do
	CLASSPATH=$CLASSPATH:$i
done

#javac -cp $CLASSPATH HiveETL.java
java -cp $CLASSPATH HiveETL /home/lapras/DataWarehouse/data/input /home/lapras/DataWarehouse/data/output
