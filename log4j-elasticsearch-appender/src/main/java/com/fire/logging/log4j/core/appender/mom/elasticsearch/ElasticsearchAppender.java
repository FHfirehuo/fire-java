package com.fire.logging.log4j.core.appender.mom.elasticsearch;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;

/**
 * 
 * @author fire
 *
 */
@Plugin(name = "Elasticsearch", category = Node.CATEGORY, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class ElasticsearchAppender extends AbstractAppender {

	/**
	 * Builds ElasticsearchAppender instances.
	 * 
	 * @param <B>
	 *            The type to build
	 */
	public static class Builder<B extends Builder<B>> extends AbstractAppender.Builder<B>
			implements org.apache.logging.log4j.core.util.Builder<ElasticsearchAppender> {

		@PluginAttribute("uris")
		private String uris;

		@PluginAttribute("alias")
		private String alias;

		@PluginAttribute("type")
		private String type;

		@Override
		public ElasticsearchAppender build() {
			final Layout<? extends Serializable> layout = getLayout();
			if (layout == null) {
				AbstractLifeCycle.LOGGER.error("No layout provided for ElasticsearchAppender");
				return null;
			}

			List<String> hosts = Arrays.asList(uris.split(","));

			final ElasticsearchManager elasticsearchManager = new ElasticsearchManager(
					getConfiguration().getLoggerContext(), getName(), hosts, alias, type);
			return new ElasticsearchAppender(getName(), getFilter(), layout, isIgnoreExceptions(),
					elasticsearchManager);
		}

		public String getUris() {
			return uris;
		}

		public B setUris(String uris) {
			this.uris = uris;
			return asBuilder();
		}

		public String getAlias() {
			return alias;
		}

		public B setAlias(String alias) {
			this.alias = alias;
			return asBuilder();
		}

		public String getType() {
			return type;
		}

		public B setType(String type) {
			this.type = type;
			return asBuilder();
		}

	}

	/**
	 * Creates a builder for a ElasticsearchAppender.
	 * 
	 * @return a builder for a ElasticsearchAppender.
	 */
	@PluginBuilderFactory
	public static <B extends Builder<B>> B newBuilder() {
		return new Builder<B>().asBuilder();
	}

	private final ElasticsearchManager manager;

	private ElasticsearchAppender(String name, Filter filter, Layout<? extends Serializable> layout,
			final boolean ignoreExceptions, final ElasticsearchManager manager) {
		super(name, filter, layout, ignoreExceptions);
		this.manager = Objects.requireNonNull(manager, "manager");
	}

	@Override
	public void append(LogEvent event) {
		if (event.getLoggerName() != null && event.getLoggerName().startsWith("org.elasticsearch.client")) {
			LOGGER.warn("Recursive logging from [{}] for appender [{}].", event.getLoggerName(), getName());
		} else {
			try {
				tryAppend(event);
			} catch (final Exception e) {
				error("Unable to write to Elasticsearch in appender [" + getName() + "]", event, e);
			}
		}
	}
	
	LocalDateTime dateTime = LocalDateTime.now();
	
	private void tryAppend(LogEvent logEvent) throws IOException {

		Map<String, Object> log = new HashMap<>();

		log.put("timestamp", dateTime);
		
		try {
			log.put("host", InetAddress.getLocalHost().getCanonicalHostName());
		} catch (UnknownHostException e) {
			log.put("host", "Unknown local hostname");
		}
		
		log.put("thread", logEvent.getThreadName());
		
		log.put("level", logEvent.getLevel().toString());
		
		StackTraceElement source = logEvent.getSource();

		if (source != null) {
			log.put("className", source.getClassName());
			log.put("method", source.getMethodName());
			//log.put("line", source.getLineNumber());
		}
		
		log.put("message", logEvent.getMessage().getFormattedMessage());
		
		if (logEvent.getThrown() != null) {
			log.put("exception", logEvent.getThrown());
		}
		
		manager.send(log);
	}

	@Override
	public void start() {
		super.start();
		manager.build();
	}

	@Override
	public boolean stop(final long timeout, final TimeUnit timeUnit) {
		setStopping();
		boolean stopped = super.stop(timeout, timeUnit, false);
		stopped &= manager.stop(timeout, timeUnit);
		setStopped();
		return stopped;
	}

}
