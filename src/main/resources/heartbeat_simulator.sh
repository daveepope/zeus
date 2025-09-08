#!/bin/bash

BASE_URL="http://localhost:8080"

SENSORS=(
  "PLANET-EXPRESS-HQ-TEMP-HUMIDITY"
  "MOMCORP-FACTORY-NOISE"
  "HEAD-MUSEUM-LAWN-UV"
  "HEAD-MUSEUM-ROOF-RAIN-GAUGE"
  "PLANET-EXPRESS-SMELL-O-SCOPE"
)

FLAKY_SENSOR="PLANET-EXPRESS-SMELL-O-SCOPE"

HEARTBEAT_PAYLOAD='{"status": "CONNECTED"}'
CHANCE_TO_GO_OFFLINE=20
OFFLINE_DURATION=10
CYCLE_INTERVAL=2

declare -A OFFLINE_UNTIL

cleanup() {
    echo ""
    echo "Signal received, stopping heartbeat simulator..."
    exit 0
}

trap cleanup SIGINT SIGTERM EXIT

echo "Starting STATEFUL heartbeat simulator..."
echo "Only sensor '$FLAKY_SENSOR' will be flaky."
echo "Press [CTRL+C] to stop."

while true; do
  echo ""
  echo "--- [Heartbeat] Starting new cycle at $(date +'%T') ---"
  CURRENT_TIME=$(date +%s)

  for SENSOR_ID in "${SENSORS[@]}"; do

    if [ "$SENSOR_ID" == "$FLAKY_SENSOR" ]; then
        OFFLINE_TIMESTAMP=${OFFLINE_UNTIL[$SENSOR_ID]:-0}

        if (( CURRENT_TIME < OFFLINE_TIMESTAMP )); then
          echo "--> [Heartbeat] SIMULATING OUTAGE: $SENSOR_ID is offline for $((OFFLINE_TIMESTAMP - CURRENT_TIME)) more seconds."
          continue
        fi

        if (( RANDOM % 100 < CHANCE_TO_GO_OFFLINE )); then
          NEW_OFFLINE_TIMESTAMP=$((CURRENT_TIME + OFFLINE_DURATION))
          OFFLINE_UNTIL[$SENSOR_ID]=$NEW_OFFLINE_TIMESTAMP
          echo "--> [Heartbeat] SIMULATING OUTAGE: $SENSOR_ID is now going offline for $OFFLINE_DURATION seconds!"
          continue
        fi
    fi

    URL="$BASE_URL/sensors/$SENSOR_ID/heartbeat"
    echo "[Heartbeat] Sending CONNECTED heartbeat to: $SENSOR_ID"

    curl -s -o /dev/null -w "    Response Status: %{http_code}\n" \
         --connect-timeout 5 \
         -X POST \
         -H "Content-Type: application/json" \
         -d "$HEARTBEAT_PAYLOAD" \
         "$URL"
  done

  echo "--- [Heartbeat] Cycle complete, sleeping for $CYCLE_INTERVAL seconds ---"
  sleep $CYCLE_INTERVAL
done

