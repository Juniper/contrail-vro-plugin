/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TestSpringConfig {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);

        ConnectionRepository h1 = context.getBean(ConnectionRepository.class);
        ConnectionRepository h2 = context.getBean(ConnectionRepository.class);

        assert h1 == h2: "Not the same";
        System.out.print(h1);
    }
}
