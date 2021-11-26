package com.codemaster.demo.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Download {

    public static void main(String[] args) throws IOException {
        URLConnection connection = new URL("https://api.cjrhw.com/dfs/api/v2/file/ac130005-6b0c-111c-816b-0d0265df0010")
                .openConnection();
        long total = connection.getContentLength();
        long write = 0;
        byte[] buffer = new byte[2048];
        int read = 0;
        try (InputStream is = connection.getInputStream();
             OutputStream os = Files.newOutputStream(Paths.get("/Users/zoukai/Downloads/ssss.mp4"))) {

            while ((read = is.read(buffer)) > 0) {
                os.write(buffer, 0, read);
                write += read;
                System.out.printf("\r %d/%d", write, total);
            }

        }
    }
}
