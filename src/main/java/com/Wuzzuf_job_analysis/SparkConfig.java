package com.Wuzzuf_job_analysis;

import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

@Configuration
public class SparkConfig {
    @Bean
    public SparkSession sparkSession() {
        SparkSession sparkSession = SparkSession.builder().appName("Wuzzuf jobs insights")
                .master("local[2]").getOrCreate();
        sparkSession.conf().set("spark.sql.shuffle.partitions", 5);
        return sparkSession;
    }
}

