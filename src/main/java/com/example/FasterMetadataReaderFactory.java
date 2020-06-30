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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.type.classreading.ConcurrentReferenceCachingMetadataReaderFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReader;

public class FasterMetadataReaderFactory extends ConcurrentReferenceCachingMetadataReaderFactory {

	private static Log logger = LogFactory.getLog(FasterMetadataReaderFactory.class);

	private Kryo kryo = new Kryo();

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
		File file = new File("cache", resource.getPath());
		if (file.exists()) {
			try (Input stream = new Input(new FileInputStream(file))) {
				logger.info("Reading metadata for: " + resource);
				MetadataReader reader = (MetadataReader) kryo.readClassAndObject(stream);
				logger.info("Annotations: " + reader.getAnnotationMetadata());
				return reader;
			}
			catch (Exception e) {
				throw new IllegalStateException("Could not deserialize", e);
			}
		}
		file.getParentFile().mkdirs();
		try (Output stream = new Output(new FileOutputStream(file))) {
			MetadataReader reader = super.createMetadataReader(resource);
			reader = new CopyMetadataReader((ClassPathResource) reader.getResource(), reader.getAnnotationMetadata());
			logger.info("Writing metadata for: " + resource);
			kryo.writeClassAndObject(stream, reader);
			return reader;
		}
		catch (Exception e) {
			throw new IllegalStateException("Could not serialize", e);
		}
	}

}