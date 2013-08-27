/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.api.client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import ro.nextreports.engine.queryexec.QueryParameter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

/**
 * @author Decebal Suiu
 */
public class WebServiceClient {	

	protected String httpProxy;
	protected String server;
	protected String username;
	protected String password;
	protected PasswordEncoder passwordEncoder;
//	protected WebResource resource;
	protected boolean debug;
	
	protected String keystoreFile;
	protected String keyStorePass;
	protected SSLContext sslContext;
	protected KeyStore ks;
	protected SavingTrustManager tm;
	
	private static final int UNDEFINED = -1;
	private int timeout = UNDEFINED;

	public String getHttpProxy() {
		return httpProxy;
	}

	/**
	 * Format: 192.168.16.1:128 
	 * 
	 * @param httpProxy
	 */
	public void setHttpProxy(String httpProxy) {
		this.httpProxy = httpProxy;
		
		if (httpProxy == null) {
//			throw new IllegalArgumentException("'httpProxy' cannot be null");
			return;
		}
		
		String[] tokens = httpProxy.split(":");
		int count = tokens.length; 
		if (count == 1) {
			System.setProperty("http.proxyHost", tokens[0]);
		} else if (count == 2) {
			System.setProperty("http.proxyHost", tokens[0]);
			System.setProperty("http.proxyPort", tokens[1]);
		}		
	}

	public String getServer() {
		return server;
	}

	/**
	 * Format: "http://localhost:8081/api"
	 * 
	 * @param server
	 */
	public void setServer(String server) {
		this.server = server;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public PasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	
	public void setKeystoreFile(String keystoreFile) {
		this.keystoreFile = keystoreFile;
	}

	public void setKeyStorePass(String keyStorePass) {
		this.keyStorePass = keyStorePass;
	}

	public void publishReport(ReportMetaData reportMetaData) throws WebServiceException {
		if (reportMetaData.getMainFile() == null) {
			throw new WebServiceException("No file content");
		}
				
		ClientResponse response = createRootResource().path("storage/publishReport")
			.post(ClientResponse.class, reportMetaData);

		checkForException(response);
	}

    public void publishChart(ChartMetaData chartMetaData) throws WebServiceException {
		if (chartMetaData.getMainFile() == null) {
			throw new WebServiceException("No file content");
		}

		ClientResponse response = createRootResource().path("storage/publishChart")
			.post(ClientResponse.class, chartMetaData);

		checkForException(response);
	}

    @SuppressWarnings("unchecked")
	public List<EntityMetaData> getEntities(String path) throws WebServiceException {
		ClientResponse response = createRootResource().path("storage/getEntities")
			.queryParam("path", path)
			.get(ClientResponse.class);
	
		checkForException(response);
		
		return response.getEntity(List.class);
	}
	
	public void publishDataSource(DataSourceMetaData dataSourceMetaData) throws WebServiceException {
		ClientResponse response = createRootResource().path("storage/publishDataSource")
			.post(ClientResponse.class, dataSourceMetaData);

		checkForException(response);
	}
	
	public boolean isAuthorized() throws WebServiceException {
		ClientResponse response = createRootResource().path("storage").get(ClientResponse.class);
		
		if (response.getStatus() == 401) {
			return false;
		}
		
		checkForException(response);
		
		return true;
	}
	
	public boolean isAuthorized(int timeout) throws WebServiceException {
		
		this.timeout = timeout;  
		ClientResponse response = createRootResource().path("storage").get(ClientResponse.class);
		// use timeout just for isAuthorized
		this.timeout = UNDEFINED;
		
		if (response.getStatus() == 401) {
			return false;
		}
		
		checkForException(response);
		
		return true;
	}
	
	public byte getVersionStatus(String localVersion) throws WebServiceException {
		ClientResponse response = createRootResource().path("storage/version")
				.queryParam("localVersion", localVersion)
				.get(ClientResponse.class);

		checkForException(response);

		return response.getEntity(Byte.class);
	}
	
	public ReportMetaData getReport(String path) throws WebServiceException {
		ClientResponse response = createRootResource().path("storage/getReport")
			.queryParam("path", path)
			.get(ClientResponse.class);

		checkForException(response);
	
		return response.getEntity(ReportMetaData.class);		
	}

    public ChartMetaData getChart(String path) throws WebServiceException {
		ClientResponse response = createRootResource().path("storage/getChart")
			.queryParam("path", path)
			.get(ClientResponse.class);

		checkForException(response);

		return response.getEntity(ChartMetaData.class);		
	}
    
    public DataSourceMetaData getDataSource(String path) throws WebServiceException {
		ClientResponse response = createRootResource().path("storage/getDataSource")
			.queryParam("path", path)
			.get(ClientResponse.class);

		checkForException(response);

		return response.getEntity(DataSourceMetaData.class);		
	}

    public int entityExists(String path) throws WebServiceException {
		ClientResponse response = createRootResource().path("storage/entityExists")
			.queryParam("path", path)
			.get(ClientResponse.class);

		checkForException(response);

		return response.getEntity(Integer.class);				
	}
	
	public void createFolder(String path) throws WebServiceException {
		ClientResponse response = createRootResource().path("storage/createFolder")
			.post(ClientResponse.class, path);

		checkForException(response);
	}
	
	public String runReport(RunReportMetaData runReportMetaData) throws WebServiceException {
		ClientResponse response = createRootResource().path("report/runReport")
			.post(ClientResponse.class, runReportMetaData);

		checkForException(response);
	
		return response.getEntity(String.class);
	}
	
	public List<QueryParameter> getWidgetParameters(String widgetId) throws WebServiceException {
		ClientResponse response = createRootResource().path("dashboard/getWidgetParameters")
			.queryParam("widgetId", widgetId)
			.get(ClientResponse.class);
	
		checkForException(response);
		
		return response.getEntity(List.class);
	}
	
	public List<DashboardMetaData> getDashboards(String user) throws WebServiceException {
		ClientResponse response = createRootResource().path("dashboard/getDashboards").queryParam("user", user)
				.get(ClientResponse.class);

		checkForException(response);
		return response.getEntity(List.class);
	}
	
	public List<WidgetMetaData> getWidgets(String dashboardPath) throws WebServiceException {
		ClientResponse response = createRootResource().path("dashboard/getWidgets").queryParam("dashboardPath", dashboardPath)
				.get(ClientResponse.class);

		checkForException(response);
		return response.getEntity(List.class);
	}
	
	protected Client createJerseyClient() {
		ClientConfig config = new DefaultClientConfig();
//		DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();  
		config.getClasses().add(XStreamXmlProvider.class);
//		ApacheHttpClient client = ApacheHttpClient.create(config);
		
		if (server.startsWith("https")) {
			log("* Use https protocol");			
			try {				
				initSSL(config);								
			} catch (Exception ex) {	
				if (tm != null) {
					try {
					    installCertificates();					    
					    initSSL(config);	
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				} else {				
					throw new RuntimeException(ex);
				}
			}
		}
		
		Client client = Client.create(config);
		
		if (isDebug()) {
			client.addFilter(new LoggingFilter());
		}
		if ((username != null) && (password != null)) {
//			client.addFilter(new HTTPBasicAuthFilter(username, password));
			String encodedPassword = password;
			if (passwordEncoder != null) {
				encodedPassword = passwordEncoder.encode(password);
			} 
			client.addFilter(new HttpBasicAuthenticationFilter(username, encodedPassword));
//			config.getState().setCredentials(null, null, -1, username, encodedPassword);
		}
		
		if (timeout != UNDEFINED) {
			client.setConnectTimeout(timeout);
		}
		
		return client;
	}
	
	protected WebResource createRootResource() {
		return createJerseyClient().resource(server);
		/*
		if (resource == null) {
			resource = createJerseyClient().resource(server);
		}
		
		return resource;
		*/
	}
	
	protected void checkForException(ClientResponse response) throws WebServiceException {
		int statusCode = response.getStatus();		
		if (!((statusCode >=200) && (statusCode < 300))) { // 2xx is OK
			log("statusCode = " + statusCode);
			throw new WebServiceException(response);
		}
	}
	
	private class NullHostnameVerifier implements HostnameVerifier {
	    public boolean verify(String hostname, SSLSession session) {
	        return true;
	    }
	}
	
	private class SavingTrustManager implements X509TrustManager {

		private final X509TrustManager tm;		
		private X509Certificate[] chain;

		SavingTrustManager(X509TrustManager tm) {
			this.tm = tm;
		}
		
		public X509Certificate[] getAcceptedIssuers() {
			throw new UnsupportedOperationException();
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			throw new UnsupportedOperationException();
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {	
			this.chain = chain;
			tm.checkServerTrusted(chain, authType);
		}
	}
	
	protected HostnameVerifier createHostnameVerifier() {
		return new NullHostnameVerifier();
	}
	
	protected SavingTrustManager createTrustManager() throws Exception {
		InputStream in = new FileInputStream(keystoreFile);
		ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(in, keyStorePass.toCharArray());
		in.close();				
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		X509TrustManager defaultTrustManager = (X509TrustManager)tmf.getTrustManagers()[0];
		SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);		
		return tm;
	}
	
	private void initSSL(ClientConfig config) throws Exception {
		
		log("* Init SSL connection ...");		
		sslContext = SSLContext.getInstance("SSL");
		tm = createTrustManager();
		sslContext.init(null, new TrustManager[] {tm}, null);
		config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
				new HTTPSProperties(createHostnameVerifier(), sslContext));		
		
		SSLSocketFactory factory = sslContext.getSocketFactory();

		URL url = new URL(server);
		String host = url.getHost();
		int port = url.getPort();
		log("  -> Opening connection to " + host + ":" + port + "...");
		SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
		socket.setSoTimeout(10000);

		log("  -> Starting SSL handshake...");
		socket.startHandshake();
		socket.close();		
		log("  -> No errors, certificate is already trusted");		
	}
	
	private void installCertificates() throws Exception {

		log("  -> Error, certificate is not trusted");	
		log("* Install SSL certificates ...");
		URL url = new URL(server);
		String host = url.getHost();		
		X509Certificate[] chain = tm.chain;
		if (chain == null) {
			throw new Exception("Could not obtain server certificate chain.");
		}
		
		log("  -> Server sent " + chain.length + " certificate(s):");		
		OutputStream out = new FileOutputStream(keystoreFile);
		for (int i = 0; i < chain.length; i++) {
			X509Certificate cert = chain[i];
			log("       " + (i + 1) + " Subject " + cert.getSubjectDN());
			log("       Issuer  " + cert.getIssuerDN());			

			String alias = host + "-" + (i + 1);
			ks.setCertificateEntry(alias, cert);
			ks.store(out, keyStorePass.toCharArray());
			log("  -> Added certificate : " + alias);
		}
		out.close();		
	}
	
	private void log(String message) {
		System.out.println(message);
	}
		
}
