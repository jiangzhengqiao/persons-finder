#!/bin/bash

# Initialize report header with professional metrics
REPORT="| Concurrency | Throughput (RPS) | Avg Latency (ms) | P99 Latency (ms) |\n| :--- | :--- | :--- | :--- |"

echo "----------------------------------------------------------------"
echo "Initiating Performance Benchmark:"
echo "Target: http://localhost:8080"
echo "Duration per test: 30s | Warm-up: Integrated"
echo "----------------------------------------------------------------"

for threads in 10 50 100
do
    echo "Testing Load Level: $threads Concurrent Connections..."
    
    # Execute wrk with Lua script for randomized spatial queries
    OUTPUT=$(wrk -t10 -c$threads -d30s --latency -s random_search.lua http://localhost:8080)
    
    # Extract raw metrics using awk
    QPS=$(echo "$OUTPUT" | grep "Requests/sec" | awk '{print $2}')
    AVG=$(echo "$OUTPUT" | grep "Latency" | grep -v "Distribution" | awk '{print $2}')
    P99=$(echo "$OUTPUT" | grep "99%" | awk '{print $2}')
    
    # Unit conversion utility for consistent Millisecond reporting
    format_ms() {
        val=$1
        if [[ $val == *us ]]; then echo "scale=2; ${val%us}/1000" | bc;
        elif [[ $val == *ms ]]; then echo "${val%ms}";
        elif [[ $val == *s ]]; then echo "scale=2; ${val%s}*1000" | bc;
        else echo "$val"; fi
    }

    AVG_MS=$(format_ms $AVG)
    P99_MS=$(format_ms $P99)

    # Append data to the Markdown report
    REPORT="$REPORT\n| $threads | $QPS | $AVG_MS | $P99_MS |"
    
    # Cooldown period to prevent thermal throttling or socket exhaustion
    sleep 3
done

echo -e "\n================ FINAL PERFORMANCE SUMMARY ================"
echo -e "$REPORT"
echo -e "==========================================================="
