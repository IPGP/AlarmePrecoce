#!/bin/bash
ALARMHOST="127.0.0.1"
ALARMPORT="4445"
message="02 1 $(date '+%Y/%m/%d %H:%M:%S') Test default true 11 test"
printf "%s" "$message" | nc -u -w1 "$ALARMHOST" "$ALARMPORT"

exit 0
