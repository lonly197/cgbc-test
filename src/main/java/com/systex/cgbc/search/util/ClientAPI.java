package com.systex.cgbc.search.util;

import com.systex.cgbc.util.Config;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author yukaiwan
 *
 */
public class ClientAPI {
	private Client client;
    /*private Node node;*/
    private Config config;

	/**
	 *
	 */
	public ClientAPI() {
		config = Config.getInstance();
	}

	/**
	 * 取得实例
	 *
	 * @return
	 */
	public Client getClient() {
		this.close();
		// 实例名称
		String clusterName = config.getProperty("cluster_name");
		String[] addressArrays = config.getArrays("address");
		// 端口
		int port = config.getInt("port");

		// 设置集群名称：默认是elasticsearch，并设置client.transport.sniff为true，使客户端嗅探整个集群状态，把集群中的其他机器IP加入到客户端中
		Settings settings = Settings.settingsBuilder()
			.put("cluster.name", clusterName).put("client.transport.sniff", true).build();
		try {
			client = TransportClient.builder().settings(settings).build();
			for (String address : addressArrays) {
				((TransportClient) client)
					.addTransportAddress(
						new InetSocketTransportAddress(InetAddress.getByName(address), port));
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return client;
	}

	/**
	 * 关闭node
	 */
	public void close() {
    /*if (node != null) {
      synchronized (node) {
				node.close();
			}
		}*/
		if (client != null) {
			synchronized (client) {
				client.close();
			}
		}
	}
}
