/*
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class QuickStart {

	private static String currentUrl;
	private static NodeList inputnodes;
	private static NodeList elementsByTagName;
	private static String response2Str;

	public static void main(String[] args) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient = WebClientDevWrapper.wrapClient(httpclient);
		/*
		 * HttpParams params = new BasicHttpParams();
		 * params.setParameter(ClientPNames.HANDLE_REDIRECTS, "true")
		 */

		httpclient.setRedirectStrategy(new DefaultRedirectStrategy() {
			@Override
			public boolean isRedirected(HttpRequest request,
					HttpResponse response, HttpContext context) {
				boolean isRedirect = false;
				try {
					isRedirect = super.isRedirected(request, response, context);
				} catch (ProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!isRedirect) {
					int responseCode = response.getStatusLine().getStatusCode();
					if (responseCode == 301 || responseCode == 302) {
						return true;
					}
				}
				return isRedirect;
			}
		});

		HttpGet httpGet = new HttpGet(
				"https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=514152706870.apps.googleusercontent.com&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile&state=%252Fprofile&redirect_uri=https%3A%2F%2Flocalhost%2Foauth2callback");
		/** Sending first request from Client **/
		// HttpGet httpGet = new HttpGet("https://google.com");
		HttpContext context = new BasicHttpContext();
		HttpResponse response1 = httpclient.execute(httpGet, context);

		// The underlying HTTP connection is still held by the response object
		// to allow the response content to be streamed directly from the
		// network socket.
		// In order to ensure correct deallocation of system resources
		// the user MUST either fully consume the response content or abort
		// request
		// execution by calling HttpGet#releaseConnection().

		try {
			System.out.println(response1.getStatusLine());

			currentUrl = getFinalURL(context);
			/*
			 * Header[] hd = response1.getAllHeaders(); for (Header header : hd)
			 * { System.out.println(header.getName() + "  " +
			 * header.getValue()); }
			 */
			HttpEntity entity1 = response1.getEntity();
			String formString = getFormString(readContent(response1));
			// System.out.println(formString);
			/*
			 * SAXParserFactory factory = SAXParserFactory.newInstance();
			 * SAXParser saxParser = factory.newSAXParser();
			 * 
			 * DefaultHandler handler = new DefaultHandler();
			 * saxParser.parse((new InputSource(new StringReader(formString))),
			 * handler);
			 */
			// do something useful with the response body
			// and ensure it is fully consumed
			// EntityUtils.consume(entity1);

			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.parse(new InputSource(new StringReader(
					formString)));

			elementsByTagName = doc.getElementsByTagName("input");
			System.out.println(elementsByTagName.getLength());

			/*
			 * Document doc = docBuilder.parse( inputnodes =
			 * doc.getElementsByTagName("input");
			 */

		} finally {
			httpGet.releaseConnection();
		}
		context = new BasicHttpContext();
		/** Authentication with User Credentials **/

		System.out.println("AUTHNEITCATION");
		HttpPost httpPost = new HttpPost(
				"https://accounts.google.com/ServiceLoginAuth");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("Email", "contact.me.at.vijay"));
		nvps.add(new BasicNameValuePair("Passwd", "leader123"));
		for (int i = 0; i < elementsByTagName.getLength(); i++) {
			Node n = elementsByTagName.item(i);
			String value = "";
			String name = n.getAttributes().getNamedItem("name").getNodeValue();
			Node n1 = n.getAttributes().getNamedItem("value");
			if (n1 != null) {
				value = n1.getNodeValue();
			}
			/* System.out.println(i + "adding + name " + name + " " + value); */
			if (value == null)
				value = "";
			nvps.add(new BasicNameValuePair(name, value));

		}
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		HttpResponse response2 = httpclient.execute(httpPost, context);
		try {
			System.out.println(response2.getStatusLine());
			System.out.println(getFinalURL(context));
			response2Str = readContent(response2);

		} finally {
			httpPost.releaseConnection();
		}

		/** Allowing Permission and Clicking ALLOW **/
		// httpclient.setRedirectStrategy(new DefaultRedirectStrategy());

		System.out.println("CLIKCING ALLOW");

		String formString = getFormString(response2Str);
		System.out.println(formString);
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		Document doc = docBuilder.parse(new InputSource(new StringReader(
				formString)));
		elementsByTagName = doc.getElementsByTagName("input");

		HttpPost httpPost2 = new HttpPost(doc.getChildNodes().item(0)
				.getAttributes().getNamedItem("action").getNodeValue());
		System.out.println("POSTING TO "
				+ doc.getChildNodes().item(0).getAttributes()
						.getNamedItem("action").getNodeValue());

		List<NameValuePair> nvps2 = new ArrayList<NameValuePair>();
		for (int i = 0; i < elementsByTagName.getLength(); i++) {
			Node n = elementsByTagName.item(i);
			String value = "";
			String name = n.getAttributes().getNamedItem("name").getNodeValue();
			Node n1 = n.getAttributes().getNamedItem("value");
			if (n1 != null) {
				value = n1.getNodeValue();
			}
			if (value == null)
				value = "";
			nvps2.add(new BasicNameValuePair(name, value));

		}
		httpPost2.setEntity(new UrlEncodedFormEntity(nvps2));
		HttpResponse response3 = httpclient.execute(httpPost2, context);
		try {
			System.out.println(response3.getStatusLine());
			readContent(response3);
			System.out.println(getFinalURL(context));

		} finally {
			httpPost2.releaseConnection();
		}
	}

	private static String getFinalURL(HttpContext context) {
		HttpUriRequest currentReq = (HttpUriRequest) context
				.getAttribute(ExecutionContext.HTTP_REQUEST);

		HttpHost currentHost = (HttpHost) context
				.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
		return (currentReq.getURI().isAbsolute()) ? currentReq.getURI()
				.toString() : (currentHost.toURI() + currentReq.getURI());
	}

	private static String readContent(HttpResponse response) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));

		StringBuilder total = new StringBuilder();

		String line = null;

		while ((line = r.readLine()) != null) {
			total.append(line);
		}
		System.out.println("Web Page Content " + total);
		return total.toString();
	}

	private static String getFormString(String response) {
		int start = response.indexOf("<form");
		int end = response.indexOf("</form>");
		String sub = response.substring(start, end + 7);
		sub = sub.replaceAll("novalidate", "");
		sub = sub.replaceAll("&#9731", "&#9731;");
		// sub = sub.replaceAll(" disabled ", " disabled=\"false\" ");

		sub = format(sub);
		System.out.println(sub);
		return sub;
	}

	public static String format(String xmlNode) {
		String result = null;
		Pattern patt = Pattern.compile("(<input[^>]*)(\\s*>)");
		Matcher mattcher = patt.matcher(xmlNode);
		while (mattcher.find()) {
			result = mattcher.replaceAll("$1></input>");
			// System.out.println(result);
		}
		return result.replaceAll("/>", ">");
	}
}
