/*
 * Copyright 2015-2018 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.ftp.source;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * @author David Turanski
 * @author Gary Russell
 * @author Artem Bilan
 */
public class FtpSourcePropertiesTests {

	@Test
	public void localDirCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.localDir:local")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertThat(properties.getLocalDir(), equalTo(new File("local")));
		context.close();
	}

	@Test
	public void remoteDirCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.remoteDir:/remote")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertThat(properties.getRemoteDir(), equalTo("/remote"));
		context.close();
	}

	@Test
	public void deleteRemoteFilesCanBeEnabled() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.deleteRemoteFiles:true")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertTrue(properties.isDeleteRemoteFiles());
		context.close();
	}

	@Test
	public void autoCreateLocalDirCanBeDisabled() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.autoCreateLocalDir:false")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertTrue(!properties.isAutoCreateLocalDir());
		context.close();
	}

	@Test
	public void tmpFileSuffixCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.tmpFileSuffix:.foo")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertThat(properties.getTmpFileSuffix(), equalTo(".foo"));
		context.close();
	}

	@Test
	public void filenamePatternCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.filenamePattern:*.foo")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertThat(properties.getFilenamePattern(), equalTo("*.foo"));
		context.close();
	}

	@Test
	public void remoteFileSeparatorCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.remoteFileSeparator:\\")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertThat(properties.getRemoteFileSeparator(), equalTo("\\"));
		context.close();
	}


	@Test
	public void preserveTimestampDirCanBeDisabled() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.preserveTimestamp:false")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertTrue(!properties.isPreserveTimestamp());
		context.close();
	}

	@Configuration
	@EnableConfigurationProperties(FtpSourceProperties.class)
	static class Conf {

	}

}
