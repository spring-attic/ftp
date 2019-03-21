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

import java.io.File;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ftp.FTPFile;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.app.test.ftp.FtpTestSupport;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.hazelcast.metadata.HazelcastMetadataStore;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


/**
 * @author David Turanski
 * @author Marius Bogoevici
 * @author Artem Bilan
 * @author Christian Tzolov
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = {
				"debug=true",
				"ftp.remoteDir = ftpSource",
				"ftp.factory.username = foo",
				"ftp.factory.password = foo",
				"ftp.filenamePattern = *",
				"file.consumer.mode = ref",
				"ftp.factory.cacheSessions = true"
		})
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
public abstract class FtpSourceIntegrationTests extends FtpTestSupport {

	@Autowired
	SourcePollingChannelAdapter sourcePollingChannelAdapter;

	@Autowired
	MessageCollector messageCollector;

	@Autowired
	FtpSourceProperties config;

	@Autowired
	SessionFactory<FTPFile> sessionFactory;

	@Autowired
	Source ftpSource;

	@TestPropertySource(properties = "file.consumer.mode = ref")
	public static class SourceFilesAsRefTest extends FtpSourceIntegrationTests {

		@Autowired
		private ConcurrentMetadataStore metadataStore;

		@Test
		@SuppressWarnings("unchecked")
		public void sourceFilesAsRef() throws InterruptedException {
			Set<FileListFilter<?>> filters =
					TestUtils.getPropertyValue(sourcePollingChannelAdapter,
							"source.synchronizer.filter.fileFilters", Set.class);
			assertEquals("*", TestUtils.getPropertyValue(filters.iterator().next(), "path"));
			for (int i = 1; i <= 2; i++) {
				Message<?> received = messageCollector.forChannel(ftpSource.output()).poll(10, TimeUnit.SECONDS);
				assertNotNull(received);

				assertThat(new File(received.getPayload().toString().replaceAll("\"", "")),
						equalTo(new File(this.config.getLocalDir(), "ftpSource" + i + ".txt")));
			}
			assertThat(this.sessionFactory, instanceOf(CachingSessionFactory.class));
			this.sourcePollingChannelAdapter.stop();

			assertThat(this.metadataStore, instanceOf(HazelcastMetadataStore.class));

			assertNotNull(this.metadataStore.get("ftpSource/ftpSource1.txt"));
			assertNotNull(this.metadataStore.get("ftpSource/ftpSource2.txt"));
		}

	}

	// TODO: there is an interference issue between both tests
	//@TestPropertySource(properties = { "spring.cloud.stream.bindings.output.contentType=text/plain" })
	//public static class SourceRefModeWithTextContentTypeTest extends FtpSourceIntegrationTests {
	//
	//	@Test
	//	public void sourceFilesAsRef() throws InterruptedException {
	//		assertEquals("*",
	//				TestUtils.getPropertyValue(sourcePollingChannelAdapter, "source.synchronizer.filter.path"));
	//		for (int i = 1; i <= 2; i++) {
	//			Message<?> received = messageCollector.forChannel(ftpSource.output()).poll(10, TimeUnit.SECONDS);
	//			assertNotNull(received);
	//
	//			assertThat(received.getPayload(),
	//					equalTo(this.config.getLocalDir()  + File.separator + "ftpSource" + i + ".txt"));
	//		}
	//		assertThat(this.sessionFactory, instanceOf(CachingSessionFactory.class));
	//		this.sourcePollingChannelAdapter.stop();
	//	}
	//}

	@SpringBootApplication
	public static class FtpSourceApplication {

	}

}

