import java.io.Console;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class PAN {
	private static TrustManager trustAllManager = new X509TrustManager() {
		public void checkClientTrusted(
				java.security.cert.X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		public void checkServerTrusted(
				java.security.cert.X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

	};

	public static void main(String[] args) {
		// post
		String host = "202.69.19.85:8443";
		String ip = "";
		String mac = "";
		try {
			InetAddress lia = InetAddress.getLocalHost();
			InetAddress[] ias = InetAddress.getAllByName(lia.getHostName());
			for (InetAddress ia : ias) {
				if (!ia.equals(lia)) {
					// ip
					ip = ia.toString().split("/")[1];
					// mac
					byte[] macb = NetworkInterface.getByInetAddress(ia)
							.getHardwareAddress();
					StringBuilder sb = new StringBuilder();
					for (byte b : macb) {
						sb.append(String.format("%02x", b));
						sb.append(":");
					}
					mac = sb.deleteCharAt(sb.length() - 1).toString();
					break;
				}
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		String url = String
				.format("https://%s/portal.do?wlanacname=bgl-01&wlanuserip=%s&mac=%s&vlan=1",
						host, ip, mac);
		// System.out.println(url);
		HttpPost post = new HttpPost(url);

		// header
		post.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		post.setHeader("Accept-Encoding", "gzip, deflate");
		post.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
		post.setHeader("Connection", "keep-alive");
		post.setHeader("Host", host);
		post.setHeader("Upgrade-Insecure-Requests", "1");
		post.setHeader("Cache-Control", "max-age=0");
		post.setHeader("Origin", "https://" + host);
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
		post.setHeader("Referer", url);
		post.setHeader(
				"User-Agent",
				"Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1");

		// params
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("loginType", ""));
		params.add(new BasicNameValuePair("auth_type", "0"));
		params.add(new BasicNameValuePair("isBindMac", "0"));
		params.add(new BasicNameValuePair("pageid", "1201"));
		params.add(new BasicNameValuePair("templatetype", "2"));
		params.add(new BasicNameValuePair("listbindmac", "0"));
		params.add(new BasicNameValuePair("loginTimes", ""));
		params.add(new BasicNameValuePair("groupId", ""));
		params.add(new BasicNameValuePair("url", ""));
		Console cons = System.console();
		if (cons == null) {
			System.out.println("There is no console!");
			return;
		}
		String userId = cons.readLine("%s", "userId: ");
		params.add(new BasicNameValuePair("userId", userId));
		char[] pwd = cons.readPassword("%s", "password: ");
		String passwd = new String(pwd);
		params.add(new BasicNameValuePair("passwd", passwd));
		try {
			post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		// execute
		// HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		// CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		CloseableHttpClient closeableHttpClient = null;
		CloseableHttpResponse resp = null;
		try {
			SSLContext sslContext = SSLContexts.custom().useTLS().build();
			sslContext.init(null, new TrustManager[] { trustAllManager }, null);
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
					.<ConnectionSocketFactory> create()
					.register("http", PlainConnectionSocketFactory.INSTANCE)
					.register(
							"https",
							new SSLConnectionSocketFactory(
									sslContext,
									SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER))
					.build();
			PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
					socketFactoryRegistry);
			closeableHttpClient = HttpClients.custom()
					.setConnectionManager(connManager).build();

			System.out.println("[LOGIN] loading...");
			// HttpResponse resp = closeableHttpClient.execute(post);
			resp = closeableHttpClient.execute(post);
			HttpEntity ent = resp.getEntity();
			String content = EntityUtils.toString(ent, "utf-8");
			if (!content.contains("上网期间请不要关闭")) {
				System.out.println("[LOGIN] failed.");
				System.out.println("[LOGIN] retry again...");
				if (resp != null) {
					resp.close();
				}
				resp = closeableHttpClient.execute(post);
				ent = resp.getEntity();
				content = EntityUtils.toString(ent, "utf-8");
				if (!content.contains("上网期间请不要关闭")) {
					System.out.println("[LOGIN] failed.");
					System.out.println(content);
				} else {
					System.out.println("[LOGIN] successful~");
				}
			} else {
				System.out.println("[LOGIN] successful~");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (closeableHttpClient != null) {
					closeableHttpClient.close();
				}
				if (resp != null) {
					resp.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
