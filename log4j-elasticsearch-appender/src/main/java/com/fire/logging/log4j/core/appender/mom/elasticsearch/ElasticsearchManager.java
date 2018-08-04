package com.fire.logging.log4j.core.appender.mom.elasticsearch;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * 
 * @author fire
 *
 */
public class ElasticsearchManager extends AbstractManager {

	private RestHighLevelClient restHighLevelClient;
	private final String alias;
	private final String type;
	private final List<String> uris;

	public ElasticsearchManager(LoggerContext loggerContext, String name, final List<String> uris, final String alias,
			final String type) {
		super(loggerContext, name);

		this.alias = Objects.requireNonNull(alias, "alias");
		this.type = Objects.requireNonNull(type, "type");
		this.uris = Objects.requireNonNull(uris, "uris");
	}

	@Override
	public boolean releaseSub(final long timeout, final TimeUnit timeUnit) {
		if (restHighLevelClient != null) {
			try {
				restHighLevelClient.close();
			} catch (IOException e) {
				LOGGER.error("停止ElasticsearchAppender出错");
			}
		}
		return true;
	}

	public void send(Map<String, Object> log) throws IOException {

		IndexRequest indexRequest = new IndexRequest(alias, type).source(log);
		restHighLevelClient.index(indexRequest);
	}

	public void build() {
		HttpHost[] hosts = this.uris.stream().map(HttpHost::create).toArray(HttpHost[]::new);
		RestClientBuilder builder = RestClient.builder(hosts);
		restHighLevelClient = new RestHighLevelClient(builder);
	}

}
