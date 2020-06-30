package com.example;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

public class FakeMetadataReaderListener implements SmartApplicationListener {

	private static Log logger = LogFactory.getLog(FakeMetadataReaderListener.class);

	private FakeMetadataReaderFactory factory;

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return ApplicationContextInitializedEvent.class.isAssignableFrom(eventType)
				|| ApplicationReadyEvent.class.isAssignableFrom(eventType);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationContextInitializedEvent) {
			ApplicationContextInitializedEvent initialized = (ApplicationContextInitializedEvent) event;
			ConfigurableApplicationContext context = initialized.getApplicationContext();
			if (!(context instanceof GenericApplicationContext)) {
				throw new IllegalStateException("ApplicationContext must be a GenericApplicationContext");
			}
			if (!isEnabled(context.getEnvironment())) {
				return;
			}
			if (context.getBeanFactory().containsSingleton(MetadataReaderFactory.class.getName())) {
				return;
			}
			factory = new FakeMetadataReaderFactory(context);
			context.getBeanFactory().registerSingleton(MetadataReaderFactory.class.getName(), factory);
		}
		else {
			if (this.factory != null) {
				logger.info(factory.getTypes());
			}
		}
	}

	private boolean isEnabled(ConfigurableEnvironment environment) {
		return environment.getProperty("spring.functional.enabled", Boolean.class, true);
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 10;
	}

	static class FakeMetadataReaderFactory implements MetadataReaderFactory {

		private MetadataReaderFactory factory;

		private Set<String> types = new LinkedHashSet<>();

		public FakeMetadataReaderFactory(ResourceLoader resourceLoader) {
			this.factory = new FasterMetadataReaderFactory(resourceLoader.getClassLoader());
		}

		@Override
		public MetadataReader getMetadataReader(String name) throws IOException {
			types.add(name);
			return new FakeMetadataReader(factory.getMetadataReader(name));
		}

		@Override
		public MetadataReader getMetadataReader(Resource path) throws IOException {
			throw new UnsupportedOperationException();
		}

		public Set<String> getTypes() {
			return types;
		}

	}

	static class FakeMetadataReader implements MetadataReader {

		private MetadataReader delegate;

		public FakeMetadataReader(MetadataReader delegate) {
			this.delegate = delegate;
		}

		@Override
		public Resource getResource() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ClassMetadata getClassMetadata() {
			throw new UnsupportedOperationException();
		}

		@Override
		public AnnotationMetadata getAnnotationMetadata() {
			return delegate.getAnnotationMetadata();
		}

	}

}
