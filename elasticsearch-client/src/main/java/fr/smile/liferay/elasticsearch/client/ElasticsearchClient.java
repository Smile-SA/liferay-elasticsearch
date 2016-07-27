package fr.smile.liferay.elasticsearch.client;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author marem
 * @since 30/12/15.
 */
public class ElasticsearchClient {

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(ElasticsearchClient.class);

    /** Elasticsearch build settings. */
    public static final String ES_SETTING_PATH_HOME = "path.home";

    /**
     * es cluster name property.
     */
    public static final String ES_SETTING_CLUSTERNAME = "cluster.name";

    /**
     * Es property to sniff nodes.
     */
    public static final String ES_SETTING_CLIENT_SNIFF = "client.transport.sniff";

    /** Es client connexion settings. */
    @Autowired
    private ConnexionSettings connexionSettings;

    /** Client. */
    private TransportClient client;

    /**
     * Create a new elasticsearch cluster client.
     * @throws UnknownHostException if host is unknown
     */
    public ElasticsearchClient() throws UnknownHostException {

        /** Create a settings object with custom attributes and build */
        Settings.Builder settingsBuilder = Settings.settingsBuilder()
                .put(ES_SETTING_PATH_HOME, connexionSettings.getServerHome())
                .put(ES_SETTING_CLIENT_SNIFF, true)
                .put(ES_SETTING_CLUSTERNAME, connexionSettings.getClusterName());

        client = TransportClient.builder().settings(
                settingsBuilder.build()
        ).build();

        /** Prepare a list of Hosts */
        for (String node : connexionSettings.getNodes()) {
            String[] host = node.split(StringPool.COLON);
            InetSocketTransportAddress transportAddress = new InetSocketTransportAddress(
                    InetAddress.getByName(host[0]),
                    Integer.parseInt(host[1])
            );
            client.addTransportAddress(transportAddress);
        }
    }

    @PostConstruct
    public final void init() {

    }

    /**
     * The Close is run during destroying the spring context. The client object
     * need to be closed to avoid overlock exceptions
     */
    @PreDestroy
    public final void close() {
        LOGGER.info("About to close Client........");
        if (client != null) {
            client.close();
        }
        LOGGER.info("Successfully closed Client........");
    }

    /**
     * Get client.
     * @return client.
     */
    public final TransportClient getClient() {
        return client;
    }
}
