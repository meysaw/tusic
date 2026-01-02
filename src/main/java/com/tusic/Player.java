package com.tusic;

import java.io.IOException;

public class Player {

    private Process mpvProcess;

    public void play(String filePath) {
        stop();

        try {
            mpvProcess = new ProcessBuilder(
                    getMpvPath(),
                    "--no-video",
                    "--quiet",
                    filePath
            ).start();
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    public void stop() {
        if (mpvProcess != null && mpvProcess.isAlive()) {
            mpvProcess.destroy();
        }
    }

    private static String getMpvPath() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            return "bin/mac/mpv";
        } else if (os.contains("linux")) {
            return "bin/linux/mpv";
        }

        throw new RuntimeException("Unsupported OS");
    }
}
