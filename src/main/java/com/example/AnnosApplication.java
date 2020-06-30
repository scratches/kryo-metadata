package com.example;

import java.lang.management.ManagementFactory;
import java.util.Arrays;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.init.func.FunctionalInstallerListener;

@SpringBootApplication(proxyBeanMethods = false)
public class AnnosApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(AnnosApplication.class)
				.listeners(new FakeMetadataReaderListener(), new OnceOnlyListener(new FunctionalInstallerListener()),
						new OnceOnlyListener(new ConfigFileApplicationListener()))
				.run(args);
	}

}

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "app", havingValue = "true", value = "enabled", matchIfMissing = true)
@ConditionalOnClass({ CommandLineRunner.class })
@ConditionalOnMissingBean(CommandLineRunner.class)
class SampleConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public CommandLineRunner runner(ConfigurableListableBeanFactory beans) {
		return args -> {
			System.err.println("Class count: " + ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
			System.err.println("Bean count: " + beans.getBeanDefinitionNames().length);
			System.err.println("Bean names: " + Arrays.asList(beans.getBeanDefinitionNames()));
		};
	}

}