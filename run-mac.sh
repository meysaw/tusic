#!/bin/bash

# Make macOS binaries executable
chmod +x bin/mac/*

# Run fat JAR
java -jar target/tusic-1.0-SNAPSHOT.jar
