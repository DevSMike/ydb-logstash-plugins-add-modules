package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.logstash.plugins.ConfigurationImpl;
import org.logstashplugins.YdbTopicsInput;
import org.mockito.MockitoAnnotations;
import tech.ydb.test.junit5.YdbHelperExtension;

import java.util.*;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;


public class YdbTopicsInputTest {

    @RegisterExtension
    private static final YdbHelperExtension ydb = new YdbHelperExtension();

//    @ClassRule
//   public final static GrpcTransportRule ydbTransport = new GrpcTransportRule();
//
//    private final String TABLE_NAME = "test1_table";
//
//    private final SimpleTableClient tableClient = SimpleTableClient.newClient(
//            GrpcTableRpc.useTransport(ydbTransport)
//    ).build();
//
//    private final SessionRetryContext ctx = SessionRetryContext.create(tableClient).build();
//
//    private final String tablePath = ydbTransport.getDatabase() + "/" + TABLE_NAME;

    private YdbTopicsInput input;

    private static String connectionString() {

        StringBuilder jdbc = new StringBuilder()
                .append(ydb.useTls() ? "grpcs://" : "grpc://")
                .append(ydb.endpoint())
                .append(ydb.database());

//        if (ydb.authToken() != null) {
//            jdbc.append("?").append("token=").append(ydb.authToken());
//        }

        return jdbc.toString();
    }

    @BeforeEach
    public void setUp() {

        String connectionString = connectionString();

        Map<String, Object> configValues = new HashMap<>();
        configValues.put(YdbTopicsInput.PREFIX_CONFIG.name(), "message");
        configValues.put(YdbTopicsInput.EVENT_COUNT_CONFIG.name(), 1L);
        configValues.put("topic_path", "fake_topic_path");
        configValues.put("connection_string", connectionString);
        configValues.put("consumer_name", "consumer");
        configValues.put("schema", "JSON");
        Configuration config = new ConfigurationImpl(configValues);
        input = new YdbTopicsInput("test-input", config, null);
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() {
        input.stop();
    }

    @Test
    public void testStart() {
        Map<String, Object> resultMap = new HashMap<>();

        Consumer<Map<String, Object>> consumer = stringObjectMap -> {
            for (String key : stringObjectMap.keySet()) {
                resultMap.put(key, stringObjectMap.get(key));
            }
        };

        Assertions.assertFalse(input.getIsStopped());

        input.start(consumer);
        input.stop();

        Assertions.assertTrue(input.getIsStopped());
    }

}