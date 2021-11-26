package com.codemaster.demo.im;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntryID;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;

public class Server implements Closeable {

    public static final Logger logger = LoggerFactory.getLogger(Server.class);

    private JedisManager jedisManager;

    private IdGenerator idGenerator;

    private UserRepo userRepo;

    private Undertow server;

    private Gson gson;

    private JdbcPool jdbcPool;

    public Server(int port, String redisHost, int redisPort, String jdbcUrl) {
        gson = new Gson();
        idGenerator = new IdGenerator();
        jedisManager = new JedisManager(redisHost, redisPort);
        userRepo = new UserRepo(gson);
        jdbcPool = new JdbcPool(jdbcUrl);

        HttpHandler httpHandler = Handlers.routing()
                .get("/auth", this::auth)
                .get("/rp", this::rp)
                .post("/users", this::createUser)
                .post("/group", this::createGroup)
                .put("/group", this::upgradeGroup)
                .delete("/group", this::deleteGroup)
                .post("/group/member", this::addMember)
                .post("/group/members", this::addMembers)
                .delete("/group/member", this::deleteMember);

        server = Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
                .setHandler(httpHandler)
                .build();
    }

    private void createUser(HttpServerExchange exchange) {
        Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
        int userIdFrom = getQueryParam(queryParams, "userIdFrom").map(Integer::parseInt).get();
        int userIdTo = getQueryParam(queryParams, "userIdTo").map(Integer::parseInt).get();

        try (Jedis jedis = jedisManager.get()) {

            for (int userId = userIdFrom; userId <= userIdTo; userId++) {
                String token = "access_token_" + userId;
                if (!jedis.exists(token)) {
                    int id = idGenerator.next();
                    userRepo.put(userId+"", id);
                    jedis.hmset(token, ImmutableMap.of("user_id", id + "", "app_id", 1 + ""));
                }
            }

        }

        ok(exchange, "");
    }

    public void start() {
        server.start();
    }

    @Override
    public void close() {
        idGenerator.sync();
        jedisManager.close();
        userRepo.sync();
        jdbcPool.close();
    }

    private void auth(HttpServerExchange exchange) throws Exception {
        Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
        String userId = getQueryParam(queryParams, "userId").get();

        try (Jedis jedis = jedisManager.get()) {
            String token = "access_token_" + userId;
            if (!jedis.exists(token)) {
                int id = idGenerator.next();
                userRepo.put(userId, id);
                jedis.hmset(token, ImmutableMap.of("user_id", id + "", "app_id", 1 + ""));
            }
        }

        ok(exchange, userId);
    }

    private void rp(HttpServerExchange exchange) throws Exception {
        Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
        String userId = getQueryParam(queryParams, "userId").get();
        String friendId = getQueryParam(queryParams, "friendId").get();
        int isFriend = getQueryParam(queryParams, "friend").map(Integer::parseInt).get();

        Integer imUserId = userRepo.getId(userId);
        Integer imFriendId = userRepo.getId(friendId);

        try (Jedis jedis = jedisManager.get();
             Connection conn = jdbcPool.getConnection()) {
            rp(conn, jedis, imUserId, imFriendId, isFriend);
            rp(conn, jedis, imFriendId, imUserId, isFriend);
        }

        ok(exchange, null);
    }

    private void createGroup(HttpServerExchange exchange) throws Exception {
        Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
        int master = getQueryParam(queryParams, "master").map(Integer::parseInt).get();
        String name = getQueryParam(queryParams, "name").get();
        int superGroup = getQueryParam(queryParams, "super").map(Integer::parseInt).get();

        int id = 0;
        try (Connection conn = jdbcPool.getConnection();
             Jedis jedis = jedisManager.get()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO `group`(appid, master, name, super) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, 1);
            ps.setInt(2, master);
            ps.setString(3, name);
            ps.setInt(4, superGroup);
            ps.execute();
            id = Optional.ofNullable(ps.getGeneratedKeys())
                    .filter(rs -> {
                        try {
                            return rs.next();
                        } catch (SQLException e) {
                            return false;
                        }
                    })
                    .map(rs -> {
                        try {
                            return rs.getInt(1);
                        } catch (SQLException e) {
                            return 0;
                        }
                    }).get();


            group(jedis, ImmutableMap.<String, String>builder()
                    .put("group_id", id + "")
                    .put("app_id", "1")
                    .put("super", superGroup+"")
                    .put("name", "group_create")
                    .build());
        }

        ok(exchange, id);
    }

    private void upgradeGroup(HttpServerExchange exchange) throws SQLException {
        Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
        int groupId = getQueryParam(queryParams, "groupId").map(Integer::parseInt).get();
        int superGroup = getQueryParam(queryParams, "super").map(Integer::parseInt).get();

        try (Connection conn = jdbcPool.getConnection();
             Jedis jedis = jedisManager.get()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE `group` SET super=? WHERE id=?");
            ps.setInt(1, superGroup);
            ps.setInt(2, groupId);
            ps.execute();

            group(jedis, ImmutableMap.<String, String>builder()
                    .put("group_id", groupId+"")
                    .put("app_id", "1")
                    .put("super", superGroup+"")
                    .put("name", "group_upgrade")
                    .build());
        }

        ok(exchange, null);
    }


    private void deleteGroup(HttpServerExchange exchange) throws Exception {
        Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
        int groupId = getQueryParam(queryParams, "groupId").map(Integer::parseInt).get();

        try (Connection conn = jdbcPool.getConnection();
             Jedis jedis = jedisManager.get()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM `group` WHERE id=?");
            ps.setInt(1, groupId);
            ps.execute();

            ps = conn.prepareStatement("DELETE FROM group_member WHERE group_id=?");
            ps.setInt(1, groupId);
            ps.execute();

            group(jedis, ImmutableMap.<String, String>builder()
                    .put("group_id", groupId+"")
                    .put("name", "group_disband")
                    .build());
        }

        ok(exchange, null);
    }

    private void addMember(HttpServerExchange exchange) throws Exception {
        Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
        int groupId = getQueryParam(queryParams, "groupId").map(Integer::parseInt).get();
        int userId = getQueryParam(queryParams, "userId").map(Integer::parseInt).get();

        try (Connection conn = jdbcPool.getConnection();
             Jedis jedis = jedisManager.get()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO group_member(group_id, uid) VALUES(?, ?)");
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.execute();

            group(jedis, ImmutableMap.<String, String>builder()
                    .put("group_id", groupId + "")
                    .put("member_id", userId + "")
                    .put("name", "group_member_add")
                    .build());
        }

        ok(exchange, null);
    }

    private void addMembers(HttpServerExchange exchange) throws Exception {
        Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
        int groupId = getQueryParam(queryParams, "groupId").map(Integer::parseInt).get();
        int userIdFrom = getQueryParam(queryParams, "userIdFrom").map(Integer::parseInt).get();
        int userIdTo = getQueryParam(queryParams, "userIdTo").map(Integer::parseInt).get();


        try (Connection conn = jdbcPool.getConnection();
             Jedis jedis = jedisManager.get()) {

            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement("INSERT INTO group_member(group_id, uid) VALUES(?, ?)");

            for (int userId = userIdFrom; userId <= userIdTo; userId++) {
                ps.setInt(1, groupId);
                ps.setInt(2, userId);
                ps.execute();

                group(jedis, ImmutableMap.<String, String>builder()
                        .put("group_id", groupId + "")
                        .put("member_id", userId + "")
                        .put("name", "group_member_add")
                        .build());
            }

            conn.commit();
        }

        ok(exchange, null);
    }

    private void deleteMember(HttpServerExchange exchange) throws Exception {
        Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
        int groupId = getQueryParam(queryParams, "groupId").map(Integer::parseInt).get();
        int userId = getQueryParam(queryParams, "userId").map(Integer::parseInt).get();

        try (Connection conn = jdbcPool.getConnection();
             Jedis jedis = jedisManager.get()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM group_member WHERE group_id=? AND uid=?");
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.execute();

            group(jedis, ImmutableMap.<String, String>builder()
                    .put("group_id", groupId + "")
                    .put("member_id", userId + "")
                    .put("name", "group_member_remove")
                    .build());
        }

        ok(exchange, null);
    }


    private void ok(HttpServerExchange exchange, Object data) {
        exchange.getResponseHeaders().put(HttpString.tryFromString("Content-Type"), "application/json");
        exchange.getResponseSender().send(gson.toJson(ResultCode.ok(data)));
    }

    private void rp(Connection conn, Jedis jedis, Integer imUserId, Integer imFriendId, int isFriend) throws SQLException {
        try {
            if (isFriend == 1) {
                PreparedStatement ps = conn.prepareStatement("insert into friend(appid,uid,friend_uid,timestamp) values(?,?,?,?)");
                ps.setInt(1, 1);
                ps.setInt(2, imUserId);
                ps.setInt(3, imFriendId);
                ps.setLong(4, new Date().getTime() / 1000);
                ps.execute();
            } else {
                PreparedStatement ps = conn.prepareStatement("delete from friend where appid=? and uid=? and friend_uid=?");
                ps.setInt(1, 1);
                ps.setInt(2, imUserId);
                ps.setInt(3, imFriendId);
                ps.execute();
            }
        } catch (SQLException e) {
            logger.error("", e);
            throw e;
        }
        publish(jedis, "friends_actions_id", "friends_actions", "relationship_stream",
                ImmutableMap.<String, String>builder()
                        .put("app_id", 1 + "")
                        .put("uid", imUserId.toString())
                        .put("friend_uid", imFriendId.toString())
                        .put("friend", isFriend + "")
                        .put("name", "friend")
                        .build());
    }

    private void group(Jedis jedis, Map<String, String> hash) {
        publish(jedis, "groups_actions_id", "groups_actions", "group_manager_stream", hash);
    }

    private void publish(Jedis jedis, String actionIdKey, String actionsKey, String streamKey, Map<String, String> hash) {
        Long actionId = jedis.incr(actionIdKey);
        String groupActions = jedis.get(actionsKey);
        String prevId = Optional.ofNullable(groupActions).map(a -> a.split(":")[1]).orElse("0");
        jedis.set(actionsKey, prevId + ":" + actionId);
        jedis.xadd(streamKey, StreamEntryID.NEW_ENTRY, ImmutableMap.<String, String>builder()
                .put("previous_action_id", prevId)
                .put("action_id", actionId.toString())
                .putAll(hash)
                .build());
    }

    private Optional<String> getQueryParam(Map<String, Deque<String>> queryParams, String name) {
        return Optional.ofNullable(queryParams.get(name)).map(Deque::getFirst);
    }

    public static void main(String[] args) {
        Server server = new Server(8888, "127.0.0.1", 6379, "jdbc:mariadb://127.0.0.1:3306/gobelieve?user=root&password=root");
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.close();
        }));
    }
}
