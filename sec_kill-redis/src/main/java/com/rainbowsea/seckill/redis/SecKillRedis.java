package com.rainbowsea.seckill.redis;


import com.rainbowsea.seckill.uitl.JedisPoolUtil;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * 秒杀类
 */
public class SecKillRedis {
    /**
     * 秒杀过程
     *
     * @param uid      用户ID
     * @param ticketNo 票的编号，比如 北京-成都的 ticketNo为 bj_cd
     * @return
     */
    public static boolean doSecKill(String uid, String ticketNo) {
        // -uid 和 ticketNo 进行一个非空校验
        if (uid == null || ticketNo == null) {
            return false;
        }


        // - 连接 Redis ，得到一个 jedis 对象
        //Jedis jedis = new Jedis("192.168.76.146", 6379);
        //jedis.auth("rainbowsea");  // 设置了密码，需要进行一个验证

        // 通过连接池获取 jedis对象/连接
        JedisPool jedisPoolInstacne = JedisPoolUtil.getJedisPoolInstacne();
        Jedis jedis = jedisPoolInstacne.getResource();
        System.out.println("--使用的连接池");

        // 判断 获取的jedis 是否为空
        if (jedis == null) {
            return false;
        }

        // 拼接票的库存 key
        String stockKey = "sk:" + ticketNo + ":ticket";

        // 拼接秒杀用户要存放到 set 集合对应的key，这个 set 集合可以存放多个 userId(同时set集合有着不可重复的特性，符合我们用户不够复购的特点)
        String userKey = "sk:" + ticketNo + ":user";


        // 监控库存，开启 watch 的监控
        jedis.watch(stockKey);

        // 获取到对应的票的库存
        String stock = jedis.get(stockKey);
        // 获取到对应的票的库存，判断是否为 null
        if (stock == null) {
            System.out.println("秒杀还没有开始，请等待...");
            //jedis.close(); // 关闭连接
            JedisPoolUtil.release(jedis);
            return false;
        }


        // - 判断用户是否重复秒杀/复购
        if (jedis.sismember(userKey, uid)) {
            System.out.println(uid + "不能重复秒杀...");
            //jedis.close();
            JedisPoolUtil.release(jedis);
            return false;
        }

        // 判断火车票，是否还有剩余
        if (Integer.parseInt(stock) <= 0) {
            System.out.println("票已经卖完了，秒杀结束...");
            //jedis.close();
            JedisPoolUtil.release(jedis);
            return false;
        }


        // 可以购买
  /*      // 1.将票的库存量 -1
        jedis.decr(stockKey);
        // 2. 将该用户加入到抢购成功对应 set 集合中
        jedis.sadd(userKey, uid);
*/

        // 使用事务，完成秒杀
        Transaction multi = jedis.multi();

        // 组成操作
        multi.decr(stockKey); // 减去票的库存
        multi.sadd(userKey, uid); // 将该抢到票的用户加入到抢购成功的 set 集合中
        // 执行
        List<Object> results = multi.exec();

        if (results == null || results.size() == 0) {
            System.out.println("抢票失败...");
            JedisPoolUtil.release(jedis);
            return false;
        }

        System.out.println(uid + "秒杀成功");
        //jedis.close();
        JedisPoolUtil.release(jedis);
        return true;

    }

    /**
     * 编写一个测试方法-看看是否能够连通指定的 Redis
     */

    @Test
    public void testRedis() {
        Jedis jedis = new Jedis("192.168.76.146", 6379);
        jedis.auth("rainbowsea");  // 设置了密码，需要进行一个验证
        System.out.println(jedis.ping());
        jedis.close(); // 关闭连接

    }
}
