#!/bin/sh

. ./common.cfg
AMLIB=$CONTAINER/webapps/openam/WEB-INF/lib
TCLIB=$CONTAINER/lib
CTLIB=$BASE/../AXM62SP2/sdk/java/runtime/lib


rm -rf target/classes/org/forgerock/openam/authentication/modules/cleartrust/*.class

javac -d target/classes -classpath $AMLIB/openam-shared-$AMVER.jar:$AMLIB/openam-core-$AMVER.jar:$CTLIB/axm-runtime-api-6.2.2.jar:$TCLIB/servlet-api.jar src/main/java/org/forgerock/openam/authentication/modules/cleartrust/CTPrincipal.java src/main/java/org/forgerock/openam/authentication/modules/cleartrust/CTAuth.java 

cd target/classes
jar cvf ../openam-auth-ct-$AMVER.jar org/forgerock/openam/authentication/modules/cleartrust/*.class
