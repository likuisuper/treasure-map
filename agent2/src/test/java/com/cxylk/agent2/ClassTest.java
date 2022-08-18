package com.cxylk.agent2;

import com.cxylk.agent2.base.Agent;
import com.cxylk.agent2.base.Collect;
import org.junit.Test;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.commons.util.ReflectionUtils;

import java.net.URISyntaxException;
import java.util.List;

/**
 * @author likui
 * @date 2022/8/16 下午4:04
 **/
public class ClassTest {
    @Test
    public void test() throws URISyntaxException {
//        String currentPackage = Agent.class.getPackage().getName();
//        List result = ReflectionUtils.findAllClassesInPackage(
//                currentPackage.substring(0,currentPackage.lastIndexOf(".")),
//                ClassFilter.of(o-> Collect.class.isAssignableFrom(o)&&!o.isInterface())
//        );
//        result.forEach(System.out::println);

        System.out.println(System.getProperty("user.dir"));
    }
}
