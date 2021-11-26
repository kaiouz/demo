package com.codemaster.demo.im;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class UserRepo {

    public static final Logger logger = LoggerFactory.getLogger(UserRepo.class);

    private Gson gson;

    private Map<String, Integer> id;

    public UserRepo(Gson gson) {
        this.gson = gson;
        id = read();
    }

    public Integer getId(String userid) {
        return id.get(userid);
    }

    public void put(String userId, Integer id) {
        this.id.put(userId, id);
    }

    public void sync() {
        write(id);
    }

    private Map<String, Integer> read() {
        Path path = getPath();
        if (Files.exists(path)) {
            try {
                return gson.fromJson(gson.newJsonReader(Files.newBufferedReader(path)),
                        TypeToken.getParameterized(Map.class, String.class, Integer.class).getType());
            } catch (IOException e) {
                return Maps.newHashMap();
            }
        } else {
            return Maps.newHashMap();
        }
    }

    private void write(Map<String, Integer> id) {
        Path path = getPath();
        try (JsonWriter w = gson.newJsonWriter(Files.newBufferedWriter(path))) {
            gson.toJson(id, id.getClass(), w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path getPath() {
        return Paths.get("id.json");
    }
}
