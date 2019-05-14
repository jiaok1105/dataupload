/**
 * 
 */
package fuyoubaojian.qfcode.utils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;


/**
 * 依赖的jar包有：commons-lang-2.6.jar、httpclient-4.3.2.jar、httpcore-4.3.1.jar、commons
 * -io-2.4.jar
 * 
 * @author zhaoyb
 * 
 */
public class HttpClientUtils {

	public static final int connTimeout = 10000;
	public static final int readTimeout = 10000;
	public static final String charset = "UTF-8";
	private static HttpClient client = null;
	private static final Logger log = Logger.getLogger(HttpClientUtils.class);

	// cookie信息
	private static CookieStore cookieStore = new BasicCookieStore();

	static {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(128);
		cm.setDefaultMaxPerRoute(128);
		client = HttpClients.custom().setConnectionManager(cm).build();
	}

	public static String postParameters(String url, String parameterStr)
			throws ConnectTimeoutException, SocketTimeoutException, Exception {
		return post(url, parameterStr, "application/x-www-form-urlencoded", charset, connTimeout, readTimeout);
	}

	public static String postParameters(String url, String parameterStr, String charset, Integer connTimeout,
			Integer readTimeout) throws ConnectTimeoutException, SocketTimeoutException, Exception {
		return post(url, parameterStr, "application/x-www-form-urlencoded", charset, connTimeout, readTimeout);
	}

	public static String postParameters(String url, Map<String, String> params)
			throws ConnectTimeoutException, SocketTimeoutException, Exception {
		return postForm(url, params, null, connTimeout, readTimeout);
	}

	public static String postJson(String url, String requestbody)
			throws ConnectTimeoutException, SocketTimeoutException, Exception {
		return post(url, requestbody, "application/json", charset, connTimeout, readTimeout);
	}

	public static String postParameters(String url, Map<String, String> params, Integer connTimeout,
			Integer readTimeout) throws ConnectTimeoutException, SocketTimeoutException, Exception {
		return postForm(url, params, null, connTimeout, readTimeout);
	}

	public static String get(String url) throws Exception {
		return get(url, charset, null, null);
	}

	public static String get(String url, String charset) throws Exception {
		return get(url, charset, connTimeout, readTimeout);
	}

	/**
	 * 发送一个 Post 请求, 使用指定的字符集编码.
	 * 
	 * @param url
	 * @param body
	 *            RequestBody
	 * @param mimeType
	 *            例如 application/xml "application/x-www-form-urlencoded" a=1&b=2&c=3
	 * @param charset
	 *            编码
	 * @param connTimeout
	 *            建立链接超时时间,毫秒.
	 * @param readTimeout
	 *            响应超时时间,毫秒.
	 * @return ResponseBody, 使用指定的字符集编码.
	 * @throws ConnectTimeoutException
	 *             建立链接超时异常
	 * @throws SocketTimeoutException
	 *             响应超时
	 * @throws Exception
	 */
	public static String post(String url, String body, String mimeType, String charset, Integer connTimeout,
			Integer readTimeout) throws ConnectTimeoutException, SocketTimeoutException, Exception {
		HttpClient client = null;
		HttpPost post = new HttpPost(url);
		String result = "";
		try {
			if (StringUtils.isNotBlank(body)) {
				HttpEntity entity = new StringEntity(body, ContentType.create(mimeType, charset));
				post.setEntity(entity);
			}
			// 设置参数
			Builder customReqConf = RequestConfig.custom();
			if (connTimeout != null) {
				customReqConf.setConnectTimeout(connTimeout);
			}
			if (readTimeout != null) {
				customReqConf.setSocketTimeout(readTimeout);
			}
			post.setConfig(customReqConf.build());

			HttpResponse res;
			if (url.startsWith("https")) {
				// 执行 Https 请求.
				client = createSSLInsecureClient();
				res = client.execute(post);
			} else {
				// 执行 Http 请求.
				client = HttpClientUtils.client;
				res = client.execute(post);
			}
			result = IOUtils.toString(res.getEntity().getContent(), charset);
		} finally {
			post.releaseConnection();
			if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
				((CloseableHttpClient) client).close();
			}
		}
		return result;
	}

	/**
	 * 提交form表单
	 * 
	 * @param url
	 * @param params
	 * @param connTimeout
	 * @param readTimeout
	 * @return
	 * @throws ConnectTimeoutException
	 * @throws SocketTimeoutException
	 * @throws Exception
	 */
	public static String postForm(String url, Map<String, String> params, Map<String, String> headers,
			Integer connTimeout, Integer readTimeout)
			throws ConnectTimeoutException, SocketTimeoutException, Exception {

		HttpClient client = null;
		HttpPost post = new HttpPost(url);
		try {
			if (params != null && !params.isEmpty()) {
				List<NameValuePair> formParams = new ArrayList<org.apache.http.NameValuePair>();
				Set<Entry<String, String>> entrySet = params.entrySet();
				for (Entry<String, String> entry : entrySet) {
					formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
				post.setEntity(entity);
			}

			if (headers != null && !headers.isEmpty()) {
				for (Entry<String, String> entry : headers.entrySet()) {
					post.addHeader(entry.getKey(), entry.getValue());
				}
			}
			// 设置参数
			Builder customReqConf = RequestConfig.custom();
			if (connTimeout != null) {
				customReqConf.setConnectTimeout(connTimeout);
			}
			if (readTimeout != null) {
				customReqConf.setSocketTimeout(readTimeout);
			}
			post.setConfig(customReqConf.build());
			HttpResponse res = null;
			if (url.startsWith("https")) {
				// 执行 Https 请求.
				client = createSSLInsecureClient();
				res = client.execute(post);
			} else {
				// 执行 Http 请求.
				client = HttpClientUtils.client;
				res = client.execute(post);
			}
			return IOUtils.toString(res.getEntity().getContent(), "UTF-8");
		} finally {
			post.releaseConnection();
			if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
				((CloseableHttpClient) client).close();
			}
		}
	}

	/**
	 * 发送一个 GET 请求
	 * 
	 * @param url
	 * @param charset
	 * @param connTimeout
	 *            建立链接超时时间,毫秒.
	 * @param readTimeout
	 *            响应超时时间,毫秒.
	 * @return
	 * @throws ConnectTimeoutException
	 *             建立链接超时
	 * @throws SocketTimeoutException
	 *             响应超时
	 * @throws Exception
	 */
	public static String get(String url, String charset, Integer connTimeout, Integer readTimeout)
			throws ConnectTimeoutException, SocketTimeoutException, Exception {

		HttpClient client = null;
		HttpGet get = new HttpGet(url);
		String result = "";
		try {
			// 设置参数
			Builder customReqConf = RequestConfig.custom();
			if (connTimeout != null) {
				customReqConf.setConnectTimeout(connTimeout);
			}
			if (readTimeout != null) {
				customReqConf.setSocketTimeout(readTimeout);
			}
			get.setConfig(customReqConf.build());

			HttpResponse res = null;

			if (url.startsWith("https")) {
				// 执行 Https 请求.
				client = createSSLInsecureClient();
				res = client.execute(get);
			} else {
				// 执行 Http 请求.
				client = HttpClientUtils.client;
				res = client.execute(get);
			}

			result = IOUtils.toString(res.getEntity().getContent(), charset);
		} finally {
			get.releaseConnection();
			if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
				((CloseableHttpClient) client).close();
			}
		}
		return result;
	}

	/**
	 * 从 response 里获取 charset
	 * 
	 * @param ressponse
	 * @return
	 */
	@SuppressWarnings("unused")
	private static String getCharsetFromResponse(HttpResponse ressponse) {
		// Content-Type:text/html; charset=GBK
		if (ressponse.getEntity() != null && ressponse.getEntity().getContentType() != null
				&& ressponse.getEntity().getContentType().getValue() != null) {
			String contentType = ressponse.getEntity().getContentType().getValue();
			if (contentType.contains("charset=")) {
				return contentType.substring(contentType.indexOf("charset=") + 8);
			}
		}
		return null;
	}

	/**
	 * 创建 SSL连接
	 * 
	 * @return
	 * @throws GeneralSecurityException
	 */
	private static CloseableHttpClient createSSLInsecureClient() throws GeneralSecurityException {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();

			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {

				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}

				@Override
				public void verify(String host, SSLSocket ssl) throws IOException {
				}

				@Override
				public void verify(String host, X509Certificate cert) throws SSLException {
				}

				@Override
				public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
				}

			});

			// return HttpClients.custom().setSSLSocketFactory(sslsf).build();

			return HttpClients.custom().setDefaultCookieStore(cookieStore).setSSLSocketFactory(sslsf).build();

		} catch (GeneralSecurityException e) {
			throw e;
		}
	}

	/**
	 * 
	 * @param url
	 * @return
	 * @throws ConnectTimeoutException
	 * @throws SocketTimeoutException
	 * @throws Exception
	 */
	public static byte[] getByTeFromURL(String url) throws ConnectTimeoutException, SocketTimeoutException, Exception {
		byte[] result = null;

		HttpClient client = null;
		HttpPost post = new HttpPost(url);
		try {

			// 设置参数
			Builder customReqConf = RequestConfig.custom();

			customReqConf.setConnectTimeout(connTimeout);

			customReqConf.setSocketTimeout(readTimeout);

			post.setConfig(customReqConf.build());

			HttpResponse res;
			if (url.startsWith("https")) {
				// 执行 Https 请求.
				client = createSSLInsecureClient();
				res = client.execute(post);
			} else {
				// 执行 Http 请求.
				client = HttpClientUtils.client;
				res = client.execute(post);
			}

			result = IOUtils.toByteArray(res.getEntity().getContent());

		} finally {
			post.releaseConnection();
			if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
				((CloseableHttpClient) client).close();
			}
		}

		return result;
	}

	/**
	 * 不能修改，公共方法
	 * @param jsondata
	 * @return
	 */
	public static Map<String,Object> getJson(String jsondata) {
		String result = "";
		try {
			result =postJson("http://115.28.201.228/jnyl/dwjk/TransactionDispatch.htm",jsondata);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("返回jSONString" + result);
		JSONObject jSONObject = JSONObject.fromObject(result);
		log.info("返回jSONObject" + jSONObject);
		Map<String,Object> resuletMap = (Map) jSONObject;
		return resuletMap;
	}

	public static void main(String[] args) {
		/*Object o = 90.1;
		String s = AddressUtil.toStringByIn(o);
		System.out.print("============="+s);*/




		try {
			String jsondata;
			Map<String,Object > data = new HashMap<String,Object>();
			Map<String, Object> dataInJson = new HashMap<String, Object>();
			Map<String, Object> itemInJson = new HashMap<String, Object>();
			String result;
			/*String date1 = DateFormatUtils.format(TimeUtils.getDateToNow(-1, 3), "yyyy-MM-dd");
			String date2 = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
			log.info("date1" + date1);
			log.info("date2" + date2);*/
			//===========================================select
			/*data.put("AppCode", "1001");
			data.put("InJsonString", dataInJson);

			dataInJson.put("name", "测试001");
			dataInJson.put("idcard", "370800201801010215");
			jsondata = JSON.toJSONString(data);
			System.out.println(jsondata);
			result =
					postJson("http://115.28.201.228/jnyl/dwjk/TransactionDispatch.htm",
							jsondata);
			System.out.println("返回文本：" + result); // 返回值： {"state":"2","message":"未建管理卡"}
			JSONObject jSONObject = JSONObject.fromObject(result);
			Map<String,Object> resuletMap = (Map) jSONObject;
			System.out.println("state：" + resuletMap.get("state")); // 返回值：
			System.out.println("message：" + resuletMap.get("message")); // 返回值：
			data.clear();*/
           //=========================================== end

			//===========================================upload  start
			/*data.put("AppCode", "1101");
			data.put("InJsonString", dataInJson);

			dataInJson.put("name", "测试001");
			dataInJson.put("idcard", "370800201801010215");
			dataInJson.put("machineId", "WL10001");
			dataInJson.put("item", itemInJson);

			itemInJson.put("areacode", "370812");
			itemInJson.put("bi", "0");
			itemInJson.put("cfyzt", "5");
			itemInJson.put("createDate", "2017-09-21");
			itemInJson.put("csqk", "0");
			itemInJson.put("csrq", "2017-09-21");
			itemInJson.put("cssc", "5");
			itemInJson.put("cstz", "5");
			itemInJson.put("csyz", "5");
			itemInJson.put("cun", "5");
			itemInJson.put("cunName", "5");
			itemInJson.put("czy", "5");
			itemInJson.put("db", "0");
			itemInJson.put("er", "0");
			itemInJson.put("fb", "0");
			itemInJson.put("fqcsrq", "2017-09-21");
			itemInJson.put("fqlxdh", "5");
			itemInJson.put("fqsfzh", "5");
			itemInJson.put("fqzy", "5");
			itemInJson.put("gm", "0");
			itemInJson.put("grdabh", "5");
			itemInJson.put("hdbw", "0");
			itemInJson.put("hxpl", "5");
			itemInJson.put("jbbk", "0");
			itemInJson.put("jbqk", "5");
			itemInJson.put("jbsc", "0");
			itemInJson.put("jbscyx", "5");
			itemInJson.put("jcjg", "5");
			itemInJson.put("jddw", "5");
			itemInJson.put("jddwdh", "5");
			itemInJson.put("jtdh", "5");
			itemInJson.put("jtzz", "5");
			itemInJson.put("jz", "0");
			itemInJson.put("kh", "5");
			itemInJson.put("kq", "0");
			itemInJson.put("lrrName", "5");
			itemInJson.put("lxr", "5");
			itemInJson.put("lxrdh", "5");
			itemInJson.put("maibo", "5");
			itemInJson.put("mianse", "0");
			itemInJson.put("mqcsrq", "2017-09-21");
			itemInJson.put("mqlxdh", "5");
			itemInJson.put("mqname", "5");
			itemInJson.put("mqsfzh", "5");
			itemInJson.put("mqtz", "5");
			itemInJson.put("mqzy", "5");
			itemInJson.put("name", "5");
			itemInJson.put("ot", "0");
			itemInJson.put("pf", "0");
			itemInJson.put("qd", "2,1,1,1");
			itemInJson.put("qx", "5");
			itemInJson.put("qx1", "5");
			itemInJson.put("qxqk", "0");
			itemInJson.put("ryxm", "5");
			itemInJson.put("sfjx", "0");
			itemInJson.put("sfrq", "2017-09-21");
			itemInJson.put("sfys", "5");
			itemInJson.put("sfyscode", "5");
			itemInJson.put("ssdwName", "5");
			itemInJson.put("sssq", "5");
			itemInJson.put("szhdd", "0");
			itemInJson.put("tiwen", "5");
			itemInJson.put("tlsc", "0");
			itemInJson.put("updateid", "5");
			itemInJson.put("updatetime", "2017-09-21");
			itemInJson.put("wszq", "0");
			itemInJson.put("wyfs", "0");
			itemInJson.put("wzd", "5");
			itemInJson.put("xcsfdd", "5");
			itemInJson.put("xcsfrq", "2017-09-21");
			itemInJson.put("xf", "0");
			itemInJson.put("xingbie", "0");
			itemInJson.put("xsezx", "0");
			itemInJson.put("xxdz", "5");
			itemInJson.put("yan", "0");
			itemInJson.put("zhidao", "0");
			itemInJson.put("zhuanzhen", "0");
			itemInJson.put("zxbzw", "0");


			jsondata = JSON.toJSONString(data);
			 System.out.println(jsondata);
			result =
			postJson("http://115.28.201.228/jnyl/dwjk/TransactionDispatch.htm",
			jsondata);
			System.out.println("返回文本：" + result); // 返回值： {"state":"2","message":"未建管理卡"}
			JSONObject jSONObject = JSONObject.fromObject(result);
			Map<String,Object> resuletMap = (Map) jSONObject;
			System.out.println("state：" + resuletMap.get("state")); // 返回值：
			System.out.println("message：" + resuletMap.get("message")); // 返回值：
			data.clear();*/
			//==============================================end


			// {"result":"0","archiveCode":"320003601800002","archiveId":"078cb96b576b4bb4ab18f8f951d3adf4"}
			// 返回文本：{"result":"1","errorMsg":"填写日期时间格式不合法"} JSONObject json =

			// 先得获取系统时间？
			// String currentDate =
			// get("https://pc.e-health.org.cn/nfpc-web-ui/common/getCurrentDate");
			// System.out.println("currentDate:" + currentDate);
			// JSONObject cd_json = JSON.parseObject(currentDate);
			// System.out.println("cd_json：" + cd_json.get("currentDate"));
			/*result = postParameters("https://pc.e-health.org.cn/nfpc-web-ui/archive/reproductive/indexMaleOrWomen?param=1bc3b836d39e4a9c95df4a2724ab656d,2,,0,0","");
			System.out.println(result);
			String checkId = result.substring(result.indexOf("id=\"id\"") + 70, result.indexOf("id=\"id\"") + 102);
			System.out.println(checkId);*/
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


}
