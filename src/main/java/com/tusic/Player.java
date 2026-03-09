package com.tusic;

import java.io.IOException;

/**
 * Audio player that uses mpv in audio-only mode.
 */
public class Player {

    private Process mpvProcess;

    /**
     * Play the given audio file. Stops any currently playing track first.
     *
     * @param filePath path to the audio file
     */
    public void play(String filePath) {
        stop();

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    getMpvPath(),
                    "--no-video",
                    "--quiet",
                    filePath
            );
            pb.redirectErrorStream(true);
            mpvProcess = pb.start();
        } catch (IOException e) {
            System.err.println("Failed to start mpv: " + e.getMessage());
        }
    }

    /**
     * Stop the currently playing track, if any.
     */
    public void stop() {
        if (mpvProcess != null && mpvProcess.isAlive()) {
            mpvProcess.destroy();
            try {
                mpvProcess.waitFor();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        mpvProcess = null;
    }

    /**
     * Resolves the mpv executable path based on the current OS.
     */
    private static String getMpvPath() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            return "./bin/mac/mpv";
        } else if (os.contains("linux")) {
            return "./bin/linux/mpv";
        }

        throw new UnsupportedOperationException(
                "Unsupported OS: " + os + ". Only macOS and Linux are supported.");
    }
}
