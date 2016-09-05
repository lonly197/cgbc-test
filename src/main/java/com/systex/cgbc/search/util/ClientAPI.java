package com.systex.cgbc.search.util;

import com.systex.cgbc.util.Config;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;

/**
 * @author yukaiwan
 */
public class ClientAPI {
    private Client client;
    private Node node;
    private Config config;

    /**
     *
     */
    public ClientAPI() {
        config = Config.getInstance();
    }

    public Client getClient() {
        this.close();
        String clusterName = config.getProperty("cluster_name");
        String[] addressArrays = config.getArrays("address");
        int port = config.getInt("port");

        Settings settings =
            ImmutableSettings.settingsBuilder().put("cluster.name", clusterName).build();
        client = new TransportClient(settings);
        for (String address : addressArrays) {
            ((TransportClient) client)
                .addTransportAddress(new InetSocketTransportAddress(address, port));
        }
        return client;
    }

    /**
     * 关闭node
     */
    public void close() {
        if (node != null) {
            synchronized (node) {
                node.close();
            }
        }
        if (client != null) {
            synchronized (client) {
                client.close();
            }
        }
    }
}
