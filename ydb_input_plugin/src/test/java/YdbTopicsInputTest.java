import co.elastic.logstash.api.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.logstash.plugins.ConfigurationImpl;
import org.logstashplugins.YdbTopicsInput;
import tech.ydb.table.SessionRetryContext;
import tech.ydb.table.impl.SimpleTableClient;
import tech.ydb.table.rpc.grpc.GrpcTableRpc;
import tech.ydb.test.junit4.GrpcTransportRule;
import org.mockito.MockitoAnnotations;
import tech.ydb.test.junit5.YdbHelperExtension;

import java.util.*;
import java.util.function.Consumer;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class YdbTopicsInputTest {

    private YdbTopicsInput input;

   @RegisterExtension
    public static final YdbHelperExtension ydb = new YdbHelperExtension();

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


    private static String connectionString() {
        System.out.println(ydb.endpoint());
        StringBuilder jdbc = new StringBuilder()
                .append(ydb.useTls() ? "grpcs://" : "grpc://")
                .append(ydb.endpoint())
                .append(ydb.database());

        if (ydb.authToken() != null) {
            jdbc.append("?").append("token=").append(ydb.authToken());
        }

        return jdbc.toString();
    }

    @Before
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

    @After
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

        assertFalse(input.getIsStopped());

        input.start(consumer);
        input.stop();

        assertTrue(input.getIsStopped());
    }

}