#!/bin/bash

/home/pi/zulu11.56.19-ca-jdk11.0.15-linux_aarch32hf/bin/java -Dxpi4j.library.path=local -Dorg.slf4j.simpleLogger.showDateTime=true -Dorg.slf4j.simpleLogger.dateTimeFormat="YYYYMMdd-hhmmss" -cp "/home/pi/im2/libs/*.jar" -jar /home/pi/im2/im-2.*.jar /home/pi/im2/im.properties
RESULT=$?

if [ $RESULT = "99" ]
then
  /sbin/poweroff
fi
