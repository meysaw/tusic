# Tusic - TUI Music Player

Terminal-based music player in Java built using Lanterna.

## Features

- Plays audio using mpv
- Downloads tracks using yt-dlp
- SQLite database support
- Works on Linux and macOS

## Requirements

- Java 21+
- mpv (or bundled binary)
- yt-dlp (or bundled binary)

## Linux Setup

1. Build the JAR:
   bash:

mvn clean package

chmod +x bin/linux/\*

run ./run.sh

## Mac Setup

2. Build the JAR:

mvn clean package

chmod +x bin/mac/\*

run ./run-mac.sh
