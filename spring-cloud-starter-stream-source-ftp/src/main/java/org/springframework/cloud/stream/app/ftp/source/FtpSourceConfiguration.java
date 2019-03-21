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

import org.apache.commons.net.ftp.FTPFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.file.FileConsumerProperties;
import org.springframework.cloud.stream.app.file.FileReadingMode;
import org.springframework.cloud.stream.app.file.FileUtils;
import org.springframework.cloud.stream.app.ftp.FtpSessionFactoryConfiguration;
import org.springframework.cloud.stream.app.trigger.TriggerConfiguration;
import org.springframework.cloud.stream.app.trigger.TriggerPropertiesMaxMessagesDefaultUnlimited;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.filters.ChainFileListFilter;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.dsl.Ftp;
import org.springframework.integration.ftp.dsl.FtpInboundChannelAdapterSpec;
import org.springframework.integration.ftp.filters.FtpPersistentAcceptOnceFileListFilter;
import org.springframework.integration.ftp.filters.FtpRegexPatternFileListFilter;
import org.springframework.integration.ftp.filters.FtpSimplePatternFileListFilter;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.metadata.MetadataStore;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.util.StringUtils;

/**
 * @author David Turanski
 * @author Marius Bogoevici
 * @author Gary Russell
 * @author Artem Bilan
 * @author Christian Tzolov
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({ FtpSourceProperties.class, FileConsumerProperties.class,
		TriggerPropertiesMaxMessagesDefaultUnlimited.class })
@Import({ TriggerConfiguration.class, FtpSessionFactoryConfiguration.class })
public class FtpSourceConfiguration {

	@Autowired
	@Qualifier(PollerMetadata.DEFAULT_POLLER)
	private PollerMetadata defaultPoller;

	@Autowired
	private Source source;

	@Autowired
	private ConcurrentMetadataStore metadataStore;

	@Bean
	public IntegrationFlow ftpInboundFlow(SessionFactory<FTPFile> ftpSessionFactory, FtpSourceProperties properties,
			FileConsumerProperties fileConsumerProperties) {

		FtpInboundChannelAdapterSpec messageSourceBuilder = Ftp.inboundAdapter(ftpSessionFactory)
				.preserveTimestamp(properties.isPreserveTimestamp())
				.remoteDirectory(properties.getRemoteDir())
				.remoteFileSeparator(properties.getRemoteFileSeparator())
				.localDirectory(properties.getLocalDir())
				.autoCreateLocalDirectory(properties.isAutoCreateLocalDir())
				.temporaryFileSuffix(properties.getTmpFileSuffix())
				.deleteRemoteFiles(properties.isDeleteRemoteFiles());

		ChainFileListFilter<FTPFile> chainFileListFilter = new ChainFileListFilter<>();

		if (StringUtils.hasText(properties.getFilenamePattern())) {
			chainFileListFilter.addFilter(new FtpSimplePatternFileListFilter(properties.getFilenamePattern()));
		}
		else if (properties.getFilenameRegex() != null) {
			chainFileListFilter.addFilter(new FtpRegexPatternFileListFilter(properties.getFilenameRegex()));
		}

		chainFileListFilter.addFilter(new FtpPersistentAcceptOnceFileListFilter(this.metadataStore, "ftpSource/"));

		messageSourceBuilder.filter(chainFileListFilter);

		IntegrationFlowBuilder flowBuilder =
				IntegrationFlows.from(messageSourceBuilder, e -> e.poller(this.defaultPoller));

		if (fileConsumerProperties.getMode() != FileReadingMode.ref) {
			flowBuilder = FileUtils.enhanceFlowForReadingMode(flowBuilder, fileConsumerProperties);
		}

		return flowBuilder.channel(this.source.output()).get();
	}

}
