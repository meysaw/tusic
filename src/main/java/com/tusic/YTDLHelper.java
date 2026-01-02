package com.tusic;

import java.io.File;

import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;

public class YTDLHelper {

    private static final String DOWNLOAD_DIR = "downloads";

    public static String downloadAudio(String link) {
        try {
            new File(DOWNLOAD_DIR).mkdirs();

            YoutubeDL.setExecutablePath(getytdlPath());

            YoutubeDLRequest request = new YoutubeDLRequest(link);
            request.setOption("extract-audio");
            request.setOption("audio-format", "mp3");
            request.setOption("output", DOWNLOAD_DIR + "/%(title)s.%(ext)s");

            request.setOption("print", "after_move:filepath");

            YoutubeDLResponse response = YoutubeDL.execute(request);

            String out = response.getOut().trim();
            return out.isEmpty() ? null : out;

        } catch (YoutubeDLException e) {
            //e.printStackTrace();
            return null;
        }
    }

    private static String getytdlPath() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            return "./bin/mac/yt-dlp";
        } else if (os.contains("linux")) {
            return "./bin/linux/yt-dlp";
        }

        throw new RuntimeException("Unsupported OS");
    }
}
