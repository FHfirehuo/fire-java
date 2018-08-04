# log4j elasticsearch appender 

A log4j2 appender implementation for elasticsearch 6.X

    本项目依赖于elasticsearch 6.2.4 的 rest-high-level-client。
    实现将log4j2打印的日志自动保存在elasticsearch中。

Installation and Getting Started
----------

maven

```xml
<dependency>
    <groupId>com.fire</groupId>
    <artifactId>elasticsearch-log4j-appender</artifactId>
    <version>${revsion}</version>
</dependency>

<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>6.X</version>
</dependency>
```
log4j2.xml

```xml
<appenders>
    <Elasticsearch name="Elasticsearch" uris="http://localhost:9200" alias="log_test2" type="log" >
        <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n" />
    </Elasticsearch>
</appenders>
	
<loggers>
    <logger name="ElasticsearchLog" level="INFO">
        <AppenderRef ref="Elasticsearch" />
    </logger>
</loggers>
```
Exmple

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogTest.class);

	public static void main(String[] args) {
		LOGGER.debug("debug消息");
		LOGGER.info("info消息");
		LOGGER.warn("warn消息");
		LOGGER.error("error消息");

		System.exit(0);
	}
}
```

Note
--

本包已知和以下jar包冲突。请慎重使用。

```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>jcl-over-slf4j</artifactId>
    <version>${revsion}</version>
    <scope>runtime</scope>
</dependency>
```



java
--
应用结束时必须调用如下(或者类似代码)代码。否则应用将会被阻塞,不能正常停止。

```java
System.exit(0);
```

springboot
--
maven

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter</artifactId>
	<exclusions>
		<exclusion>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-logging</artifactId>
		</exclusion>
	</exclusions>
</dependency>

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>
```
    因为springboot默认使用的是logback。所以应该先排除logback日志，然后添加log4j2的引用。

应用结束时必须调用如下(或者类似代码)代码。否则应用将会被阻塞,不能正常停止。

```java
System.exit(SpringApplication.exit(app));
```



