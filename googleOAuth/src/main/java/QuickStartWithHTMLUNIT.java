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

import java.io.BufferedInputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

public class QuickStartWithHTMLUNIT {

	static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		public void checkClientTrusted(X509Certificate[] certs, String authType) {
		}

		public void checkServerTrusted(X509Certificate[] certs, String authType) {
		}
	} };

	// Ignore differences between given hostname and certificate hostname
	static HostnameVerifier hv = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	// Install the all-trusting trust manager

	private static WebClient getWebClient() {
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
		} catch (Exception e) {
		}

		System.getProperties().put(
				"org.apache.commons.logging.simplelog.defaultlog", "error");
		WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_7);
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setRedirectEnabled(true);
		webClient.waitForBackgroundJavaScript(50000);
		webClient.waitForBackgroundJavaScriptStartingBefore(1000000);

		return webClient;
	}

	private static HtmlForm getFormById(HtmlPage htmlpage, String id) {
		List<HtmlForm> forms = htmlpage.getForms();
		HtmlForm formToSubmit = null;
		for (HtmlForm htmlForm : forms) {
			System.out.println(htmlForm.getId());
			if (htmlForm.getId().equalsIgnoreCase(id))
				formToSubmit = htmlForm;
		}
		return formToSubmit;

	}

	public static void main(String[] args) throws Exception {

		WebClient wc = getWebClient();

		/* Presented the Page */
		HtmlPage leg1Page = wc
				.getPage("https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=514152706870.apps.googleusercontent.com&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile&state=%252Fprofile&redirect_uri=https%3A%2F%2Flocalhost%2Foauth2callback");

		HtmlForm formToSubmit = getFormById(leg1Page, "gaia_loginform");
		if (formToSubmit == null) {
			System.out.println("No form to submit!!");
			return;
		}

		formToSubmit.getInputByName("Email").setValueAttribute(
				"contact.me.at.vijay");
		formToSubmit.getInputByName("Passwd").setValueAttribute("leader123");

		HtmlSubmitInput button = formToSubmit.getInputByName("signIn");

		// submit the page 1
		final HtmlPage leg2page1 = button.click();

		/** Saying Allow Permission **/
		URL url = new URL(
				"https://ssl.gstatic.com/accounts/o/712712367-approval_page_lib.js");
		HttpsURLConnection urlc = (HttpsURLConnection) url.openConnection();
		BufferedInputStream buffer = null;

		buffer = new BufferedInputStream(urlc.getInputStream());

		StringBuilder builder = new StringBuilder();
		int byteRead;
		while ((byteRead = buffer.read()) != -1)
			builder.append((char) byteRead);

		buffer.close();

		ScriptResult result = leg2page1.executeJavaScript(builder.toString());

		formToSubmit = getFormById(leg2page1, "submit_access_form");
		if (formToSubmit == null) {
			System.out.println("No form to submit!!");
			return;
		}
		System.out.println("check this out");
		// System.out.println(leg2page1.getWebResponse().getContentAsString());
		System.out.println(formToSubmit.asXml());
		HtmlButton hsi = formToSubmit.getElementById("submit_approve_access");
		// Google is checking for Javascript based on this
		formToSubmit.getElementById("submit_access").setAttribute("value",
				"true");

		hsi.getPage().getWebClient().getOptions()
				.setThrowExceptionOnFailingStatusCode(false);
		hsi.getPage().getWebClient().getOptions().setRedirectEnabled(false);
		Page leg2page2 = hsi.click();

		Thread.sleep(10);
		WebRequest wr = leg2page2.getWebResponse().getWebRequest();
		System.out.println(leg2page2.getWebResponse().getWebRequest()
				.getRequestBody());
		// redirect get the location
		String redirectLocation = leg2page2.getWebResponse()
				.getResponseHeaderValue("Location");
		String code = extractCode(redirectLocation);
		WebRequest tokenRequest = new WebRequest(new URL(
				"https://accounts.google.com/o/oauth2/token"), HttpMethod.POST);
		tokenRequest.setRequestParameters(getParamsforTokenRequest(code));
		WebClient wc2 = getWebClient();
		Page page = wc2.getPage(tokenRequest);
		System.out.println(page.getWebResponse().getContentAsString());
	}

	private static List<NameValuePair> getParamsforTokenRequest(String code) {
		List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
		requestParams.add(new NameValuePair("code", code));
		requestParams.add(new NameValuePair("client_id",
				"514152706870.apps.googleusercontent.com"));
		requestParams.add(new NameValuePair("client_secret",
				"Pw5sgVmks82KkNHQkQfb8rqK"));
		requestParams
				.add(new NameValuePair("grant_type", "authorization_code"));
		requestParams.add(new NameValuePair("redirect_uri",
				"https://localhost/oauth2callback"));
		return requestParams;
	}

	private static String extractCode(String codeString) {
		int start = codeString.indexOf("code=") + "code=".length();
		return codeString.substring(start);

	}

}
