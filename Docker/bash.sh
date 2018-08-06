#!/bin/bash

# THIS SCRIPT RUNS AN EARLYWARNING DOCKER CONTAINER BUT DOES NOT START THE APPLICATION

docker run -it \
    -v $(pwd)/asterisk:/etc/asterisk \
    -v $(pwd)/configuration:/earlywarning/configuration \
    -v $(pwd)/asterisk/sounds:/usr/share/asterisk/sounds \
    -p 6001:6001 \
    -p 6002:6002 \
    -p 4445:4445/udp \
    -p 5060:5060/udp \
    -p 5060:5060/tcp \
    -p 10000-10100:10000-10100/tcp \
    -p 10000-10100:10000-10100/udp \
    --entrypoint /bin/bash \
    earlywarning