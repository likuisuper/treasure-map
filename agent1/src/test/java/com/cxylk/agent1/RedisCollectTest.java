package com.cxylk.agent1;

import com.cxylk.agent1.redis.RedisAgent;
import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.lang.instrument.IllegalClassFormatException;

/**
 * @author likui
 * @date 2022/8/11 下午5:34
 **/
public class RedisCollectTest {
    @Before
    public void init() throws Exception {
        String name="redis.clients.jedis.Protocol";
        byte[] bytes = new RedisAgent().transform(RedisCollectTest.class.getClassLoader(), name.replaceAll("\\.","/"), null, null, null);
        ClassPool classPool=new ClassPool();
        classPool.insertClassPath(new ByteArrayClassPath(name,bytes));
        classPool.get(name).toClass();
        Class.forName(name);
    }

    @Test
    public void test(){
        Jedis jedis = new Jedis("127.0.0.1", 6379);

        jedis.set("name","lk");
        System.out.println(jedis.get("name"));
    }
}
