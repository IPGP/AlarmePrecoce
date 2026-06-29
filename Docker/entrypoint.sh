#!/bin/sh
set -eu

OVERRIDES=/etc/asterisk-overrides

if [ -d "$OVERRIDES" ] && [ -n "$(ls -A "$OVERRIDES" 2>/dev/null)" ]; then
  cp -a "$OVERRIDES"/. /etc/asterisk/
fi

exec /usr/bin/supervisord -c /etc/supervisor/supervisord.conf