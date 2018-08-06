#!/bin/bash

# THIS SCRIPT STARTS ASTERISK AND THE EARLYWARNING APPLICATION

echo "Starting Asterisk"
/usr/sbin/asterisk

# Wait a bit for Asterisk to be ready and bind the ports
echo "Waiting a bit"
sleep 1

echo "Starting EarlyWarning"
/usr/bin/java -jar EarlyWarning.jar
