package com.rainbowsea.seckill.redis;


import com.rainbowsea.seckill.uitl.JedisPoolUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 使用 LUA脚本进行编写完成秒杀
 */
public class SecKillRedisByLua {

    /**
     * 说明
     * 1. 这个脚本字符串是在lua脚本上修改的, 但是要注意不完全是字符串处理
     * 2. 比如 : 这里我就使用了 \" , 还有换行使用了 \r\n
     * 3. 这些都是细节，如果你直接把lua脚本粘贴过来，不好使,一定要注意细节
     * 4. 如果写的不成功，就在这个代码上修改即可
     */
    static String secKillScript = "local userid=KEYS[1];\r\n" +
            "local ticketno=KEYS[2];\r\n" +
            "local stockKey='sk:'..ticketno..\":ticket\";\r\n" +
            "local usersKey='sk:'..ticketno..\":user\";\r\n" +
            "local userExists=redis.call(\"sismember\",usersKey,userid);\r\n" +
            "if tonumber(userExists)==1 then \r\n" +
            "   return 2;\r\n" +
            "end\r\n" +
            "local num= redis.call(\"get\" ,stockKey);\r\n" +
            "if tonumber(num)<=0 then \r\n" +
            "   return 0;\r\n" +
            "else \r\n" +
            "   redis.call(\"decr\",stockKey);\r\n" +
            "   redis.call(\"sadd\",usersKey,userid);\r\n" +
            "end\r\n" +
            "return 1";

    //使用lua脚本完成秒杀的核心方法
    public static boolean doSecKill(String uid, String ticketNo) {
        //先从redis连接池，获取连接
        JedisPool jedisPoolInstance = JedisPoolUtil.getJedisPoolInstacne();
        Jedis jedis = jedisPoolInstance.getResource();
        //就是将lua脚本进行加载
        String sha1 = jedis.scriptLoad(secKillScript);
        //evalsha是根据指定的 sha1校验码, 执行缓存在服务器的脚本
        Object result = jedis.evalsha(sha1, 2, uid, ticketNo);
        String resString = String.valueOf(result);

        //根据lua脚本执行返回的结果，做相应的处理
        if ("0".equals(resString)) {
            System.out.println("票已经卖光了..");
            JedisPoolUtil.release(jedis);  // 归还连接给,redis 连接池
            return false;
        }

        if ("2".equals(resString)) {
            System.out.println("不能重复购买..");
            JedisPoolUtil.release(jedis);  // 归还连接给,redis 连接池
            return false;
        }

        if ("1".equals(resString)) {
            System.out.println("抢购成功");
            JedisPoolUtil.release(jedis);  // 归还连接给,redis 连接池
            return true;
        } else {
            System.out.println("购票失败..");
            JedisPoolUtil.release(jedis);  // 归还连接给,redis 连接池
            return false;
        }

    }
}
