package com.cxylk.config;

import com.cxylk.service.DubboUserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.Configuration;

/**
 * @author likui
 * @date 2022/8/23 下午7:26
 * 统一配置dubbo service，这样可以直接使用@Autowired使用
 **/
@EnableDubbo
@Configuration
public class DubboConfig {
    @DubboReference
    private DubboUserService userService;
}
