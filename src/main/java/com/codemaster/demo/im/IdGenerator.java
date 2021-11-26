package com.codemaster.demo.im;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {

    public static final Logger logger = LoggerFactory.getLogger(IdGenerator.class);

    private AtomicInteger id;

    public IdGenerator() {
        int i = read();
        id = new AtomicInteger(i);
    }

    public int next() {
        return id.incrementAndGet();
    }

    public void sync() {
        write(id.get());
    }

    private int read() {
        Path idPath = getPath();
        if (Files.exists(idPath)) {
            try {
                return Files.readAllLines(idPath).stream().findFirst()
                        .map(Integer::parseInt).orElse(0);
            } catch (IOException e) {
                logger.error("读取id失败", e);
                return 0;
            }
        } else {
            return 0;
        }
    }

    private Path getPath() {
        return Paths.get("id.txt");
    }

    private void write(int id) {
        Path path = getPath();
        try {
            Files.write(path, Lists.newArrayList(Integer.toString(id)));
        } catch (IOException e) {
            logger.error("写入id失败", e);
        }
    }
}
