/**
 *  Copyright (C) 2011 GT-STCFed - RNP - http://gtstcfed.das.ufsc.br
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2011 GT-STCFed - RNP - http://gtstcfed.das.ufsc.br
 *
 *  Este programa é software livre; você pode redistribuí-lo e/ou
 *  modificá-lo sob os termos da Licença Pública Geral GNU, conforme
 *  publicada pela Free Software Foundation; tanto a versão 2 da
 *  Licença como (a seu critério) qualquer versão mais nova.
 *
 *  Este programa é distribuído na expectativa de ser útil, mas SEM
 *  QUALQUER GARANTIA; sem mesmo a garantia implícita de
 *  COMERCIALIZAÇÃO ou de ADEQUAÇÃO A QUALQUER PROPÓSITO EM
 *  PARTICULAR. Consulte a Licença Pública Geral GNU para obter mais
 *  detalhes.
 *
 *  Você deve ter recebido uma cópia da Licença Pública Geral GNU
 *  junto com este programa; se não, escreva para a Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307, USA.
*/
package br.ufsc.das.gtscted.shibbauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Connection{
	
	private DefaultHttpClient httpClient;
	
	public Connection() throws ClientProtocolException, IOException,KeyManagementException, NoSuchAlgorithmException,KeyStoreException, UnrecoverableKeyException {	
		SSLSocketFactory socketFactory;
		httpClient = new DefaultHttpClient();
		KeyStore trustStore;
		trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		
		// usando a nova SSLSocketFactory. Ver links abaixo:
		// http://groups.google.com/group/android-developers/browse_thread/thread/d9b914c0dca5a702
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4523989
		// http://exampledepot.com/egs/javax.net.ssl/TrustAll.html
		// http://stackoverflow.com/questions/2642777/trusting-all-certificates-using-httpclient-over-https
		// http://stackoverflow.com/questions/2899079/custom-ssl-handling-stopped-working-on-android-2-2-froyo
		socketFactory = new MySSLSocketFactory(trustStore);
		Scheme scheme = new Scheme("https", socketFactory, 443);
		httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
	}
	
	public Serializable getSerializableCookie(int index){
		return new SerializableCookie(this.httpClient.getCookieStore().getCookies().get(index));
	}
	
	public void addCookie(Cookie cookie){
		this.httpClient.getCookieStore().addCookie(cookie);
	}
		
	
	// Return array: index(0) -> endpointUrl (if redirected)
	//				 index(1) -> get response
	public String[] httpGetWithEndpoint(String url) throws IOException{
		HttpGet httpget = new HttpGet(url);
		HttpContext context = new BasicHttpContext(); 
		HttpResponse response = httpClient.execute(httpget, context); 
		String responseAsStr = readResponse(response.getEntity().getContent()).toString();
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
			throw new IOException(response.getStatusLine().toString());
		}
		HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
		HttpHost currentHost = (HttpHost)  context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
		String currentUrl = currentHost.toURI() + currentReq.getURI();

		URL endpointUrl = new URL(currentUrl);
		String endpointDomain = "https://"+endpointUrl.getHost();

		String[] returnArray = {endpointDomain,responseAsStr};
		return returnArray;
	}
	
			
	public String httpGet(String url) throws ClientProtocolException,IOException {
		HttpGet httpGet = new HttpGet(url);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = httpClient.execute(httpGet, responseHandler);
		return responseBody;
	}

	public String authenticate(String wayfLocation, String wayfActionPath, String idpUrl, String username, String password) throws ClientProtocolException, IOException {
		
		//POST para o WAYF passando o idp escolhido
		HttpPost httpPost1 = new HttpPost(wayfLocation + wayfActionPath);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_idp", idpUrl));
		httpPost1.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
		HttpResponse response1 = httpClient.execute(httpPost1);
		String strResponse1 = readResponse(response1.getEntity().getContent()).toString();
		//-----------------------------------------------
		
		//Obt�m o caminho indicado no campo "action" da p�gina do idp (/idp/Authn/UserPassword)
		Document idpDoc = Jsoup.parse(strResponse1);
		Element idpFormElement = idpDoc.select("form").get(0);
		String idpActionPath = idpFormElement.attr("action");
				
		//POST para o idp escolhido (por exemplo https://idpstcfed.sj.ifsc.edu.br/idp/Authn/UserPassword)
		// passando o usuario (j_username) e a senha (j_password)
		HttpPost httpPost2 = new HttpPost(idpUrl.replace("/idp/shibboleth", idpActionPath));
		List<NameValuePair> nameValuePairs2 = new ArrayList<NameValuePair>();
		nameValuePairs2.add(new BasicNameValuePair("j_username", username));
		nameValuePairs2.add(new BasicNameValuePair("j_password", password));
		httpPost2.setEntity(new UrlEncodedFormEntity(nameValuePairs2, HTTP.UTF_8));
		HttpResponse response2 = httpClient.execute(httpPost2);
		String strResponse2 = readResponse(response2.getEntity().getContent()).toString();
		//-----------------------------------------------
		
		// Obt�m os elementos que ser�o passados para o SP para criar o security context
		Document authResponseDoc = Jsoup.parse(strResponse2);
		Element authResponseFormElement = authResponseDoc.select("form").get(0);
		Element relayStateElement = authResponseDoc.select("input").get(0);
		Element SAMLResponseElement = authResponseDoc.select("input").get(1);
		String action = authResponseFormElement.attr("action"); 
		String relayStateValue = relayStateElement.attr("value");
		String SAMLResponseValue = SAMLResponseElement.attr("value");		
		
		// POST para o "assertion consumer" no SP, indicado no campo "action" da resposta
		// recebida ap�s a autenticacao. Este POST cont�m dois valores: RelayState e 
		// SAMLResponse.
		HttpPost httpPost3 = new HttpPost(action);
		List<NameValuePair> nameValuePairs3 = new ArrayList<NameValuePair>();
		nameValuePairs3.add(new BasicNameValuePair("RelayState", relayStateValue));
		nameValuePairs3.add(new BasicNameValuePair("SAMLResponse", SAMLResponseValue));
		httpPost3.setEntity(new UrlEncodedFormEntity(nameValuePairs3, HTTP.UTF_8));
		HttpResponse response3 = httpClient.execute(httpPost3);		
		return readResponse(response3.getEntity().getContent()).toString();
	}
		
	
	private StringBuilder readResponse(InputStream is) throws IOException {
		String line = "";
		StringBuilder total = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		while ((line = reader.readLine()) != null) {
			total.append(line);
		}
		return total;
	}	

	
	public class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");
		public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}
}