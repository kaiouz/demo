package com.codemaster.demo.im;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisManager implements AutoCloseable{

    private JedisPool pool;

    public JedisManager(String host, int port) {
        pool = new JedisPool(host, port);
    }

    public Jedis get() {
        return pool.getResource();
    }

    @Override
    public void close() {
        pool.close();
    }
}
