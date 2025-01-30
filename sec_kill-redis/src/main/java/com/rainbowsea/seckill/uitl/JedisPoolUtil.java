package com.rainbowsea.seckill.uitl;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 使用连接池的方式来获取 Redis 连接
 */
public class JedisPoolUtil {


    // 解读 volatile 作用
    /*
    1. 线程可见性：当一个线程去修改一个共享变量时，另外一个线程可以读取这个修改的值
    2. 顺序的一致性：禁止指令重排
     */
    private static volatile JedisPool jedisPool = null;

    // 使用单例模式，将构造方法私有化

    private JedisPoolUtil() {
    }


    // 单例：保证每次调用返回的 jedisPool是单例的
    public static JedisPool getJedisPoolInstacne() {
        if (null == jedisPool) {

            synchronized (JedisPoolUtil.class) {

                if (null == jedisPool) {

                    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
                    // 对连接池进行配置
                    jedisPoolConfig.setMaxTotal(200);
                    jedisPoolConfig.setMaxIdle(32);
                    jedisPoolConfig.setMaxWaitMillis(60 * 1000); // 单位是毫秒
                    jedisPoolConfig.setBlockWhenExhausted(true);
                    jedisPoolConfig.setTestOnBorrow(true);
                    jedisPool = new JedisPool(jedisPoolConfig, "192.168.76.146", 6379, 60000, "rainbowsea");

                }
            }
        }


        return jedisPool;
    }


    /**
     * 释放连接资源
     *
     * @param jedis
     */
    public static void release(Jedis jedis) {
        if (null != jedis) {
            jedis.close(); // 如果这个jedis 是从连接池获取的，这里 jedis.close()
            // 就是将 jedis对象/连接，释放到连接池中。
        }

    }
}
