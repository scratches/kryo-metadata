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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotation.Adapt;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReader;

/**
 * @author Dave Syer
 *
 */
public class CopyMetadataReader implements MetadataReader {

	private String path;

	private CopyAnnotationMetadata metadata;

	@SuppressWarnings("unused")
	private CopyMetadataReader() {
	}

	public CopyMetadataReader(MetadataReader copy) {
	}

	public CopyMetadataReader(ClassPathResource resource, AnnotationMetadata annotationMetadata) {
		path = resource.getPath();
		metadata = new CopyAnnotationMetadata(annotationMetadata);
	}

	@Override
	public Resource getResource() {
		return new ClassPathResource(path);
	}

	@Override
	public ClassMetadata getClassMetadata() {
		return metadata;
	}

	@Override
	public AnnotationMetadata getAnnotationMetadata() {
		return metadata;
	}

	static class CopyAnnotationMetadata implements AnnotationMetadata {

		private String className;

		private String enclosingClassName;

		private String[] interfaceNames;

		private String[] memberClassNames;

		private String superClassName;

		private boolean enclosingClass;

		private boolean superClass;

		private boolean abstractness;

		private boolean annotation;

		private boolean concrete;

		private boolean finalness;

		private boolean independenct;

		private boolean interfaceness;

		private Set<CopyMethodMetadata> methodMetadataSet;

		private Collection<MergedAnnotationSource> annotations;

		@SuppressWarnings("unused")
		private CopyAnnotationMetadata() {
		}

		CopyAnnotationMetadata(AnnotationMetadata annotationMetadata) {
			className = annotationMetadata.getClassName();
			enclosingClassName = annotationMetadata.getEnclosingClassName();
			interfaceNames = annotationMetadata.getInterfaceNames();
			memberClassNames = annotationMetadata.getMemberClassNames();
			superClassName = annotationMetadata.getSuperClassName();
			enclosingClass = annotationMetadata.hasEnclosingClass();
			superClass = annotationMetadata.hasSuperClass();
			abstractness = annotationMetadata.isAbstract();
			annotation = annotationMetadata.isAnnotation();
			concrete = annotationMetadata.isConcrete();
			finalness = annotationMetadata.isFinal();
			independenct = annotationMetadata.isIndependent();
			interfaceness = annotationMetadata.isInterface();
			methodMetadataSet = annotationMetadata.getAnnotatedMethods(null).stream()
					.map(anno -> new CopyMethodMetadata(anno)).collect(Collectors.toSet());
			annotations = annotationMetadata.getAnnotations().stream()
					.filter(anno -> anno.isDirectlyPresent() && anno.getAggregateIndex() == 0)
					.map(anno -> new MergedAnnotationSource(anno)).collect(Collectors.toSet());

		}

		@Override
		public Set<MethodMetadata> getAnnotatedMethods(String annotationName) {
			Set<MethodMetadata> annotatedMethods = new LinkedHashSet<>(4);
			for (MethodMetadata methodMetadata : this.methodMetadataSet) {
				if (methodMetadata.isAnnotated(annotationName)) {
					annotatedMethods.add(methodMetadata);
				}
			}
			return annotatedMethods;
		}

		@Override
		public String getClassName() {
			return className;
		}

		@Override
		public boolean isInterface() {
			return interfaceness;
		}

		@Override
		public boolean isAnnotation() {
			return annotation;
		}

		@Override
		public boolean isAbstract() {
			return abstractness;
		}

		@Override
		public boolean isConcrete() {
			return concrete;
		}

		@Override
		public boolean isFinal() {
			return finalness;
		}

		@Override
		public boolean isIndependent() {
			return independenct;
		}

		@Override
		public boolean hasEnclosingClass() {
			return enclosingClass;
		}

		@Override
		public String getEnclosingClassName() {
			return enclosingClassName;
		}

		@Override
		public boolean hasSuperClass() {
			return superClass;
		}

		@Override
		public String getSuperClassName() {
			return superClassName;
		}

		@Override
		public String[] getInterfaceNames() {
			return interfaceNames;
		}

		@Override
		public String[] getMemberClassNames() {
			return memberClassNames;
		}

		@Override
		public MergedAnnotations getAnnotations() {
			return MergedAnnotations.of(annotations.stream().map(anno -> anno.anno()).collect(Collectors.toSet()));
		}

	}

	static class CopyMethodMetadata implements MethodMetadata {

		private String methodName;

		private boolean abstractness;

		private boolean finalness;

		private boolean overridable;

		private boolean staticness;

		private String declaringClassName;

		private String returnTypeName;

		private Collection<MergedAnnotationSource> annotations;

		@SuppressWarnings("unused")
		private CopyMethodMetadata() {
		}

		public CopyMethodMetadata(MethodMetadata methodMetadata) {
			methodName = methodMetadata.getMethodName();
			abstractness = methodMetadata.isAbstract();
			finalness = methodMetadata.isFinal();
			overridable = methodMetadata.isOverridable();
			staticness = methodMetadata.isStatic();
			declaringClassName = methodMetadata.getDeclaringClassName();
			returnTypeName = methodMetadata.getReturnTypeName();
			annotations = methodMetadata.getAnnotations().stream()
					.filter(anno -> anno.isDirectlyPresent() && anno.getAggregateIndex() == 0)
					.map(anno -> new MergedAnnotationSource(anno)).collect(Collectors.toList());
		}

		@Override
		public String getMethodName() {
			return this.methodName;
		}

		@Override
		public boolean isAbstract() {
			return this.abstractness;
		}

		@Override
		public boolean isStatic() {
			return this.staticness;
		}

		@Override
		public boolean isFinal() {
			return this.finalness;
		}

		@Override
		public boolean isOverridable() {
			return this.overridable;
		}

		@Override
		public String getDeclaringClassName() {
			return this.declaringClassName;
		}

		@Override
		public String getReturnTypeName() {
			return this.returnTypeName;
		}

		@Override
		public MergedAnnotations getAnnotations() {
			return MergedAnnotations.of(annotations.stream().map(anno -> anno.anno()).collect(Collectors.toSet()));
		}

	}

	static class MergedAnnotationSource {

		private Class<? extends Annotation> type;

		@JsonTypeInfo(use = Id.CLASS)
		private Map<String, Object> attributes;

		public MergedAnnotationSource() {
		}

		public MergedAnnotationSource(MergedAnnotation<?> anno) {
			this.type = anno.getType();
			this.attributes = new HashMap<>(anno.asMap(Adapt.CLASS_TO_STRING, Adapt.ANNOTATION_TO_MAP));
		}

		public MergedAnnotation<?> anno() {
			return MergedAnnotation.of(type, attributes);
		}

	}

}