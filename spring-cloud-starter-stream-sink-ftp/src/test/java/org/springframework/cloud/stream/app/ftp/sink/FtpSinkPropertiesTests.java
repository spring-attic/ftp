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

package org.springframework.cloud.stream.app.ftp.sink;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.file.support.FileExistsMode;

/**
 * @author David Turanski
 * @author Gary Russell
 * @author Artem Bilan
 */
public class FtpSinkPropertiesTests {

	@Test
	public void remoteDirCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.remoteDir:/remote")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSinkProperties properties = context.getBean(FtpSinkProperties.class);
		assertThat(properties.getRemoteDir(), equalTo("/remote"));
		context.close();
	}

	@Test
	public void autoCreateDirCanBeDisabled() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.autoCreateDir:false")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSinkProperties properties = context.getBean(FtpSinkProperties.class);
		assertTrue(!properties.isAutoCreateDir());
		context.close();
	}

	@Test
	public void tmpFileSuffixCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.tmpFileSuffix:.foo")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSinkProperties properties = context.getBean(FtpSinkProperties.class);
		assertThat(properties.getTmpFileSuffix(), equalTo(".foo"));
		context.close();
	}

	@Test
	public void tmpFileRemoteDirCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.temporaryRemoteDir:/foo")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSinkProperties properties = context.getBean(FtpSinkProperties.class);
		assertThat(properties.getTemporaryRemoteDir(), equalTo("/foo"));
		context.close();
	}

	@Test
	public void remoteFileSeparatorCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.remoteFileSeparator:\\")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSinkProperties properties = context.getBean(FtpSinkProperties.class);
		assertThat(properties.getRemoteFileSeparator(), equalTo("\\"));
		context.close();
	}

	@Test
	public void useTemporaryFileNameCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.useTemporaryFilename:false")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSinkProperties properties = context.getBean(FtpSinkProperties.class);
		assertFalse(properties.isUseTemporaryFilename());
		context.close();
	}

	@Test
	public void fileExistsModeCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("ftp.mode:FAIL")
				.applyTo(context);
		context.register(Conf.class);
		context.refresh();
		FtpSinkProperties properties = context.getBean(FtpSinkProperties.class);
		assertThat(properties.getMode(), equalTo(FileExistsMode.FAIL));
		context.close();
	}

	@Configuration
	@EnableConfigurationProperties(FtpSinkProperties.class)
	static class Conf {

	}

}
