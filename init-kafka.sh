#!/bin/bash
set -e

echo "Waiting for Kafka to be ready..."
sleep 15

echo "Creating Kafka topics..."

kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic classification-requests --partitions 3 --replication-factor 1
kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic ttp-evaluation-tasks --partitions 3 --replication-factor 1
kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic notifications --partitions 3 --replication-factor 1

echo "✅ Kafka topics created successfully!"
kafka-topics --bootstrap-server kafka:9092 --list

echo "Kafka initialization complete!"
