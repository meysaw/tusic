package com.tusic;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Helper class to download audio from URLs using yt-dlp.
 * Uses ProcessBuilder directly instead of the outdated youtubedl-java library
 * to avoid command-string splitting bugs with spaces in paths/titles.
 */
public class YTDLHelper {

    private static final String DOWNLOAD_DIR = "downloads";

    /**
     * Downloads audio from the given URL as MP3.
     *
     * @param url      the source URL (YouTube, SoundCloud, etc.)
     * @param onLog    callback for log messages (can be null)
     * @return the file path of the downloaded MP3, or null on failure
     */
    public static String downloadAudio(String url, Consumer<String> onLog) {
        try {
            new File(DOWNLOAD_DIR).mkdirs();

            String ytdlpPath = getExecutablePath();
            String outputTemplate = DOWNLOAD_DIR + "/%(title)s.%(ext)s";

            List<String> command = new ArrayList<>();
            command.add(ytdlpPath);
            command.add("--extract-audio");
            command.add("--audio-format");
            command.add("mp3");
            command.add("--output");
            command.add(outputTemplate);
            command.add("--print");
            command.add("after_move:filepath");
            command.add("--no-playlist");
            command.add("--quiet");
            command.add(url);

            log(onLog, "Connecting to source...");

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);

            Process process = pb.start();

            log(onLog, "Downloading audio...");

            // Capture stdout (the last line should be the filepath)
            StringBuilder stdout = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line).append("\n");
                }
            }

            // Capture stderr for diagnostics
            StringBuilder stderr = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderr.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                String errMsg = stderr.toString().trim();
                // Show only the last line of stderr (most relevant)
                String[] errLines = errMsg.split("\n");
                log(onLog, "ERROR | " + errLines[errLines.length - 1]);
                return null;
            }

            // The --print flag outputs the final filepath as the last line of stdout
            String output = stdout.toString().trim();
            if (output.isEmpty()) {
                log(onLog, "ERROR | No output from yt-dlp");
                return null;
            }

            // Take the last non-empty line as the file path
            String[] lines = output.split("\n");
            String filePath = lines[lines.length - 1].trim();

            if (!new File(filePath).exists()) {
                log(onLog, "ERROR | Downloaded file not found at: " + filePath);
                return null;
            }

            log(onLog, "Done.");
            return filePath;

        } catch (Exception e) {
            log(onLog, "ERROR | " + e.getMessage());
            return null;
        }
    }

    /**
     * Resolves the yt-dlp executable path based on the current OS.
     */
    private static String getExecutablePath() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            return "./bin/mac/yt-dlp";
        } else if (os.contains("linux")) {
            return "./bin/linux/yt-dlp";
        }

        throw new UnsupportedOperationException(
                "Unsupported OS: " + os + ". Only macOS and Linux are supported.");
    }

    private static void log(Consumer<String> onLog, String message) {
        if (onLog != null) {
            onLog.accept(message);
        }
    }
}
