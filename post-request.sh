#!/usr/bin/env bash

curl --header "Content-Type: application/json"\
  --request POST \
  --data '{ "paintBuckets": [ { "blueShade": {"name": "All Blues", "percentBlue": 100 }, "volume": 20 }, { "blueShade": {"name": "No Blues", "percentBlue": 0 }, "volume": 10 }, { "blueShade": {"name": "Deep Blue", "percentBlue": 71.5 }, "volume": 20 }, { "blueShade": {"name": "Light Blue", "percentBlue": 16 }, "volume": 10 } ], "paintVats": [ { "desiredBlueShade": {"name": "Light Blue", "percentBlue": 50 }, "volume": 50 } ] }' \
  localhost:8080/paintmix/solve
