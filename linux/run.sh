#!/bin/bash
output=$(find /opt/CustomLauncherRewrite/ -name CustomLauncherRewrite*.jar -exec java "-jar" {} \;)

case "java.lang.UnsupportedClassVersionError" in 
  *$output*)
    notify-send -i stop "Please update Java JRE to version 16 or higher!"
    ;;
esac
