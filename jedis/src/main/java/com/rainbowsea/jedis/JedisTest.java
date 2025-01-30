package com.rainbowsea.jedis;

import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JedisTest {
    // zset 有序集合操作
    @Test
    public void zset() {
        // 创建 jedis 连接对象
        Jedis jedis = new Jedis("192.168.76.145", 6379);
        // 密码身份登录
        jedis.auth("rainbowsea");

        jedis.zadd("heros", 1, "关羽");
        jedis.zadd("heros", 2, "张飞");
        jedis.zadd("heros", 3, "赵云");
        jedis.zadd("heros", 4, "马超");
        jedis.zadd("heros", 5, "黄忠");


        // 取出
        //Set<String> heros = jedis.zrange("heros", 0, -1);  // 默认是升序(根据 score 评分值升序)
        Set<String> heros = jedis.zrevrange("heros", 0, -1);  // 降序(根据 score 评分值降序)
        for (String hero : heros) {
            System.out.println("hero = " + hero);
        }
        jedis.close();
    }


    // hash操作
    @Test
    public void hash2() {
        // 创建 jedis 连接对象
        Jedis jedis = new Jedis("192.168.76.145", 6379);
        // 密码身份登录
        jedis.auth("rainbowsea");

        // 先构建一个Java的map
        Map<String, String> maps = new HashMap<>();

        maps.put("job", "Java工程师");
        maps.put("name", "李华");
        maps.put("emial", "lihua@qq.com");

        jedis.hset("hash02", maps);

        // 获取hash 的值
        List<String> person = jedis.hmget("hash02", "name", "job", "emial");
        for (String s : person) {
            System.out.println("s = >" + s);
        }

        jedis.close();


    }

    @Test
    public void hash() {
        // 创建 jedis 连接对象
        Jedis jedis = new Jedis("192.168.76.145", 6379);
        // 密码身份登录
        jedis.auth("rainbowsea");

        jedis.hset("hash01", "name", "李白");
        jedis.hset("hash01", "age", "18");

        // 获取hash 的值
        String name = jedis.hget("hash01", "name");
        System.out.println("name - >" + name);
        jedis.close();


    }


    // set 操作
    @Test
    public void set() {
        // 创建 jedis 连接对象
        Jedis jedis = new Jedis("192.168.76.145", 6379);
        // 密码身份登录
        jedis.auth("rainbowsea");

        jedis.sadd("city", "北京", "上海");
        jedis.sadd("city", "广州");
        jedis.sadd("city", "深圳");

        Set<String> smembers = jedis.smembers("city");
        for (String city : smembers) {
            System.out.println("city -->" + city);
        }

        jedis.close(); // 关闭连接
    }


    // list 操作
    @Test
    public void list() {
        // 创建 jedis 连接对象
        Jedis jedis = new Jedis("192.168.76.145", 6379);
        // 密码身份登录
        jedis.auth("rainbowsea");

        // 添加 list 数据
        jedis.lpush("name_list", "jack", "tom", "nono");

        List<String> nameList = jedis.lrange("name_list", 0, -1);
        for (String name : nameList) {
            System.out.println("name -->" + name);
        }

        jedis.close();

    }


    // string 操作
    @Test
    public void string() {
        // 创建 jedis 连接对象
        Jedis jedis = new Jedis("192.168.76.145", 6379);
        // 密码身份登录
        jedis.auth("rainbowsea");

        // 批量设置k-v
        jedis.mset("s1", "jack", "s2", "scott", "s3", "rainbow");

        // 批量获取
        List<String> mget = jedis.mget("s1", "s2");
        for (String s : mget) {
            System.out.println("s -> " + s);
        }

        jedis.close();

    }


    // key 操作
    @Test
    public void key() {
        // 创建 jedis 连接对象
        Jedis jedis = new Jedis("192.168.76.145", 6379);
        // 密码身份登录
        jedis.auth("rainbowsea");

        jedis.set("k1", "v1");
        jedis.set("k2", "v2");
        jedis.set("k3", "v3");

        // 获取key
        Set<String> keys = jedis.keys("*");
        for (String key : keys) {
            System.out.println("key=>" + key);
        }

        // 判断 key 是否存在
        System.out.println("exists:" + jedis.exists("k1")); // True
        System.out.println("exists: " + jedis.exists("k99"));
        // ttl
        System.out.println("ttl:" + jedis.ttl("k2"));

        System.out.println("k3 = " + jedis.get("k3")); // v3

        jedis.close();// 关闭当前连接

    }


    // 连接 Redis
    // 1. 如果Redis 设置了密码，则需要进行身份校验
    // 2. 因为需要连接到 redis端口，比如6379，就需要配置防火墙，放开端口
    // 3. 注意修改 bind ，支持远程连接
    // 4 注意关闭保护模式，protected-mode no ，no表示关闭
    // 5. 注意：设置了密码，需要执行 auth(密码)进行身份验证
    @Test
    public void con() {
        // 使用 ip地址 + redis的端口的构造器方法
        Jedis jedis = new Jedis("192.168.76.145", 6379);

        // 如果Redis 配置了密码，则需要进行身份校验
        jedis.auth("rainbowsea");
        String ping = jedis.ping();
        System.out.println("连接成功 ping 返回的结果 = " + ping);

        jedis.close();  // 关闭当前连接，注意并没有关闭 Redis

    }


}
