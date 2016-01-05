#!/bin/sh

. ./common.cfg
DDIR=$CONTAINER/webapps/openam

cp -v ./src/main/resources/CTAuth.xml $DDIR/config/auth/default/
cp -v ./src/main/resources/CTAuth.xml $DDIR/config/auth/default_en/
cp -v ./src/main/resources/CTAuthService.properties $DDIR/WEB-INF/classes/
cp -v ./src/main/resources/CTAuthService.xml $DDIR/WEB-INF/classes/
cp -v ./target/openam-auth-ct-$AMVER.jar $DDIR/WEB-INF/lib
