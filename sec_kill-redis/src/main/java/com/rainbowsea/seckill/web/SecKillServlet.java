package com.rainbowsea.seckill.web;

import com.rainbowsea.seckill.redis.SecKillRedis;
import com.rainbowsea.seckill.redis.SecKillRedisByLua;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;

public class SecKillServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 请求时，模拟生成一个 userId
        String userId = new Random().nextInt(10000) + "";
        // 2. 获取用户购买的票的编号
        String ticketNo = request.getParameter("ticketNo");


        // 3. 调用秒杀的方法
        //boolean isOk = SecKillRedis.doSecKill(userId, ticketNo);
        // 3.调用 Lua 脚本的完成秒杀方法
        boolean isOk = SecKillRedisByLua.doSecKill(userId, ticketNo);

        // 4. 将结果返回给前端，这个地方可以根据业务需要调整
        response.getWriter().println(isOk);
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
