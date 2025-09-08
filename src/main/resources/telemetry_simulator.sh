#!/bin/bash

# --- Configuration ---
BASE_URL="http://localhost:8080"

# An array of sensor IDs that are expected to be online and sending measurements.
SENSORS=(
  "PLANET-EXPRESS-HQ-TEMP-HUMIDITY"
  "MOMCORP-FACTORY-NOISE"
  "HEAD-MUSEUM-LAWN-UV"
  "HEAD-MUSEUM-ROOF-RAIN-GAUGE"
  "PLANET-EXPRESS-SMELL-O-SCOPE"
)

cleanup() {
    echo ""
    echo "Signal received, stopping measurement simulator..."
    exit 0
}

trap cleanup SIGINT SIGTERM EXIT

echo "Starting RANDOM measurement simulator..."
echo "Press [CTRL+C] to stop."

while true; do
  SENSOR_ID=${SENSORS[$((RANDOM % ${#SENSORS[@]}))]}
  TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
  JSON_PAYLOAD=""

  case $SENSOR_ID in
    "PLANET-EXPRESS-HQ-TEMP-HUMIDITY")
      TEMP=$(echo "scale=1; 15 + $RANDOM / 32767 * 15" | bc)
      HUMIDITY=$(echo "scale=1; 40 + $RANDOM / 32767 * 30" | bc)
      JSON_PAYLOAD=$(printf '{"sensorId":"%s","eventTimestamp":"%s","measurements":[{"metricType":"TEMPERATURE","measurementValue":%s},{"metricType":"HUMIDITY","measurementValue":%s}]}' "$SENSOR_ID" "$TIMESTAMP" "$TEMP" "$HUMIDITY")
      ;;
    "MOMCORP-FACTORY-NOISE")
      NOISE=$(echo "scale=1; 85 + $RANDOM / 32767 * 25" | bc)
      JSON_PAYLOAD=$(printf '{"sensorId":"%s","eventTimestamp":"%s","measurements":[{"metricType":"NOISE_LEVEL","measurementValue":%s}]}' "$SENSOR_ID" "$TIMESTAMP" "$NOISE")
      ;;
    "PLANET-EXPRESS-SMELL-O-SCOPE")
      FUNK=$(echo "scale=1; 5 + $RANDOM / 32767 * 6" | bc)
      WIND_SPEED=$(echo "scale=1; 1 + $RANDOM / 32767 * 10" | bc)
      WIND_DIRECTION=$((RANDOM % 360))
      JSON_PAYLOAD=$(printf '{"sensorId":"%s","eventTimestamp":"%s","measurements":[{"metricType":"FUNK_LEVEL","measurementValue":%s},{"metricType":"WIND_SPEED","measurementValue":%s},{"metricType":"WIND_DIRECTION","measurementValue":%s}]}' "$SENSOR_ID" "$TIMESTAMP" "$FUNK" "$WIND_SPEED" "$WIND_DIRECTION")
      ;;
    "HEAD-MUSEUM-ROOF-RAIN-GAUGE")
      RAINFALL=$(echo "scale=2; $RANDOM / 32767 * 2.5" | bc)
      JSON_PAYLOAD=$(printf '{"sensorId":"%s","eventTimestamp":"%s","measurements":[{"metricType":"RAINFALL","measurementValue":%s}]}' "$SENSOR_ID" "$TIMESTAMP" "$RAINFALL")
      ;;
    "HEAD-MUSEUM-LAWN-UV")
      UV=$(echo "scale=1; 1 + $RANDOM / 32767 * 10" | bc)
      JSON_PAYLOAD=$(printf '{"sensorId":"%s","eventTimestamp":"%s","measurements":[{"metricType":"UV_RADIATION","measurementValue":%s}]}' "$SENSOR_ID" "$TIMESTAMP" "$UV")
      ;;
  esac

  if [ -n "$JSON_PAYLOAD" ]; then
    echo "[$(date +'%T')] [Measurement] Posting for $SENSOR_ID"
    curl -s -o /dev/null -w "    [Measurement] Response: %{http_code}\n" --connect-timeout 5 -X POST -H "Content-Type: application/json" -d "$JSON_PAYLOAD" "$BASE_URL/measurements"
  fi

  SLEEP_INTERVAL=$((RANDOM % 6 + 5))
  echo "    ...sleeping for $SLEEP_INTERVAL seconds."
  sleep $SLEEP_INTERVAL
done
