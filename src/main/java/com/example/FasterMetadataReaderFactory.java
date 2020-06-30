/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.type.classreading.ConcurrentReferenceCachingMetadataReaderFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReader;

public class FasterMetadataReaderFactory extends ConcurrentReferenceCachingMetadataReaderFactory {

	private static Log logger = LogFactory.getLog(FasterMetadataReaderFactory.class);

	private ObjectMapper mapper = new ObjectMapper();

	{
		mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
		mapper.setVisibility(mapper.getDeserializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
	}

	public FasterMetadataReaderFactory() {
		super();
	}

	public FasterMetadataReaderFactory(ClassLoader classLoader) {
		super(classLoader);
	}

	public FasterMetadataReaderFactory(ResourceLoader resourceLoader) {
		super(resourceLoader);
	}

	@Override
	protected MetadataReader createMetadataReader(Resource resource) throws IOException {
		if (resource instanceof ClassPathResource) {
			return serializableReader((ClassPathResource) resource);
		}
		return super.createMetadataReader(resource);
	}

	private MetadataReader serializableReader(ClassPathResource resource) {
		File file = new File("cache", resource.getPath().replace(".class", ".json"));
		if (file.exists()) {
			try {
				logger.info("Reading metadata for: " + resource);
				MetadataReader reader = (MetadataReader) mapper.readValue(file, CopyMetadataReader.class);
				logger.info("Annotations: " + reader.getAnnotationMetadata());
				return reader;
			} catch (Exception e) {
				throw new IllegalStateException("Could not deserialize", e);
			}
		}
		file.getParentFile().mkdirs();
		try {
			MetadataReader reader = super.createMetadataReader(resource);
			reader = new CopyMetadataReader((ClassPathResource) reader.getResource(), reader.getAnnotationMetadata());
			logger.info("Writing metadata for: " + resource);
			mapper.writeValue(file, reader);
			return reader;
		} catch (Exception e) {
			throw new IllegalStateException("Could not serialize", e);
		}
	}

}