package com.rainbowsea.jedis;

import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

public class JedisCluster_ {
    public static void main(String[] args) {
        Set<HostAndPort> set = new HashSet<>();
        set.add(new HostAndPort("192.168.76.147", 6379));

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 对连接池进行配置
        jedisPoolConfig.setMaxTotal(200);
        jedisPoolConfig.setMaxIdle(32);
        jedisPoolConfig.setMaxWaitMillis(60 * 1000); // 单位是毫秒
        jedisPoolConfig.setBlockWhenExhausted(true);
        jedisPoolConfig.setTestOnBorrow(true);

        JedisCluster jedisCluster = new JedisCluster(set,5000,5000,5,"rainbowsea",jedisPoolConfig );
        jedisCluster.set("address", "上海");
        String address = jedisCluster.get("address");
        System.out.println("address=>" + address);
        jedisCluster.close();

    }


    @Test
    public void con() {
        // 使用 ip地址 + redis的端口的构造器方法
        Jedis jedis = new Jedis("192.168.76.147", 6379);

        // 如果Redis 配置了密码，则需要进行身份校验
        jedis.auth("rainbowsea");
        String ping = jedis.ping();
        System.out.println("连接成功 ping 返回的结果 = " + ping);

        jedis.close();  // 关闭当前连接，注意并没有关闭 Redis

    }

}
