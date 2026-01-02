#!/bin/bash

# Make Linux binaries executable
chmod +x bin/linux/*

# Run fat JAR
java -jar target/tusic-1.0-SNAPSHOT.jar
