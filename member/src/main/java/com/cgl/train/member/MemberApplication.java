package com.cgl.train.member;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;


@SpringBootApplication
@ComponentScan("com.cgl")
@MapperScan("com.cgl.train.*.mapper")
public class MemberApplication {
    private static final Logger LOG= LoggerFactory.getLogger(MemberApplication.class);

    public static void main(String[] args) {
        SpringApplication application=new SpringApplication(MemberApplication.class);

        Environment environment=application.run(args).getEnvironment();
        LOG.info("启动成功!");
        LOG.info("地址:\thttp://127.0.0.1:{}{}",environment.getProperty("server.port"),environment.getProperty("server.servlet.context-path"));
    }


}
