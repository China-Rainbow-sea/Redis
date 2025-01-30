package com.rainbowsea.redis.controller;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.font.Script;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/redisTest")
public class RedisTestController {
    // 装配 RedisTemplate

    @Resource
    private RedisTemplate redisTemplate;

    // 编写方法，使用 Redis 分布式锁，完成对 key 为 num 的 + 1操作

    @GetMapping("/lock")
    public void lock() {


        // 得到一个 UUID的值，作为锁的值
        String uuid = UUID.randomUUID().toString();

        // 1. 获取锁/设置锁 key -> lock : setnx
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);

        if (lock) { // true ，说明获取锁/设置锁成功
            Object value = redisTemplate.opsForValue().get("num");

            // 判断返回的 value 是否有值
            if (value == null | !StringUtils.hasText(value.toString())) {
                return;
            }

            // 2. 有值，就将其转成 int
            int num = Integer.parseInt(value.toString());
            // 3. 将 num + 1 ，再重新设置回去
            redisTemplate.opsForValue().set("num", ++num);
            // 使用 lua 脚本来锁，控制删除原子性
            // 定义 lua 脚本
            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else " +
                    "return" +
                    " 0 end;";
            // 使用 redis执行Lua执行
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(script);
            // 设置一下返回值类型为 Long
            // 因为删除判断的时候，返回的 0 ，给其封装为数据类型。如果步封装那么默认返回 String 类型。
            // 那么返回字符串与0会有发生错误
            redisScript.setResultType(Long.class);
            // 第一个是 script 脚本，第二个需要判断的 key，第三个就是 key 所对应的值
            // 解读： Arrays.asList("lock") 会传递给 script 的keys[1],uuid 会传递给ARGV[1]
            redisTemplate.execute(redisScript, Arrays.asList("lock"), uuid);

            // 4. 释放锁-lock
            // 为了防止误删除其他用户的锁，先判断当前的锁是不是前面获取的锁，如果相同，再释放，不相同不可以释放
            /*if (uuid.equals((String) redisTemplate.opsForValue().get("lock"))) {
                redisTemplate.delete("lock");
            }*/

        } else {  // 获取锁失败，休眠 100 毫秒，再重新获取锁/设置锁
            try {
                Thread.sleep(100);
                lock();  // 递归回去，休眠结束重新发送新的请求
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    // 编写一个方法，获取所有的 key
    @GetMapping("/t4")
    public String t4() {

        String val = (String) redisTemplate.opsForValue().get("k100");

        System.out.println("k100 " + val);

        return "OK";
    }


    // 编写一个方法，获取所有的 key
    @GetMapping("/t3")
    public String t3() {
        Set keys = redisTemplate.keys("*");

        for (Object key : keys) {
            System.out.println("key -->" + key.toString());
        }

        return "OK";
    }


    // 演示如何操作 list 列表
    @GetMapping("/t2")
    public String t2() {
        // list-存
        redisTemplate.opsForList().leftPush("books", "笑傲江湖");
        redisTemplate.opsForList().leftPush("books", "hello world");

        // list - 取数据
        List books = redisTemplate.opsForList().range("books", 0, -1);
        String booksList = "";
        for (Object book : books) {
            System.out.println("book->" + book.toString());
            booksList += book.toString();
        }

        return booksList;
    }


    // 编写一个测试方法
    // 演示设置数据和获取数据
    @GetMapping("/t1")
    public String t1() {

        // 设置值到 redis 当中,opsForValue 是操作 string 字符串的
        redisTemplate.opsForValue().set("book", "天龙八部");

        // 从 redis 当中获取值
        String book = (String) redisTemplate.opsForValue().get("book");

        return book;

    }
}
