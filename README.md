# Tusic - TUI Music Player

Terminal-based music player built with Java using **Lanterna** for TUI.

## Features

- Plays audio using **mpv**
- Downloads tracks using **yt-dlp**
- **SQLite** database support
- Works on **Linux** and **macOS**

## Requirements

- Java 21+


## Linux Setup

1. Build the fat JAR (includes all dependencies):

```bash
git clone https://github.com/meysaw/tusic.git

mvn clean package
```
2. Make the Linux binaries executable:
```bash
chmod +x bin/linux/*
```
3.Run the application:
```bash
./run.sh
```


## macOS Setup
1. Build the fat JAR (includes all dependencies):

```bash
git clone https://github.com/meysaw/tusic.git

mvn clean package
```
2. Make the macOS binaries executable:
```bash
chmod +x bin/linux/*
```
3.Run the application:
```bash
   ./run-mac.sh
```
