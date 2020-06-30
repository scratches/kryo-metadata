package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SmartApplicationListener;

public class OnceOnlyListener implements SmartApplicationListener {

	private final ApplicationListener<? super ApplicationEvent> delegate;

	public OnceOnlyListener(ApplicationListener<? super ApplicationEvent> delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return ApplicationContextInitializedEvent.class.isAssignableFrom(eventType)
				|| ApplicationEnvironmentPreparedEvent.class.isAssignableFrom(eventType);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		SpringApplication application;
		if (event instanceof ApplicationContextInitializedEvent) {
			ApplicationContextInitializedEvent initialized = (ApplicationContextInitializedEvent) event;
			application = initialized.getSpringApplication();
		}
		else {
			ApplicationEnvironmentPreparedEvent prepared = (ApplicationEnvironmentPreparedEvent) event;
			application = prepared.getSpringApplication();
		}
		for (ApplicationListener<?> listener : application.getListeners()) {
			if (listener.getClass() == delegate.getClass()) {
				return;
			}
		}
		delegate.onApplicationEvent(event);
	}

}
