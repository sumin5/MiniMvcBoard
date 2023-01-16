package com.kyk.ezenPJT.naver;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.util.StringUtils;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

public class NaverLoginBO {
	private final static String CLIENT_ID = "Gaaq_t3hVRFVgCsTIPbQ";
	private final static String CLIENT_SECRET = "nAEFbpU91b";
	private final static String REDIRECT_URI = "https://localhost:8443/ezenPJT/nredirect";
	private final static String SESSION_STATE = "oauth_state";
	
	/* ������ ��ȸ API URL */
	private final static String PROFILE_API_URL = "https://openapi.naver.com/v1/nid/me";
	/* ���̹� ���̵�� ���� URL���� method */
	public String getAuthorizationUrl(HttpSession session) {
		//���� ��ȿ�� ������ ���Ͽ� ������ ����
		String state = generateRandomString();
		setSession(session,state);
		//Scribe���� �����ϴ� ���� URL��������� �̿��Ͽ� ���� url����
		OAuth20Service oauthService = new ServiceBuilder()
			.apiKey(CLIENT_ID)
			.apiSecret(CLIENT_SECRET)
			.callback(REDIRECT_URI)
			.state(state)
			.build(NaverLoginApi.instance());
		return oauthService.getAuthorizationUrl();	
	}
	
	private String generateRandomString() {
		return UUID.randomUUID().toString(); //UUID�� ���ϰ��� �����ϴ� Ŭ����, file����� currentTimeMills����� �Ͱ� ����
		
	}
	
	//http session�� ������ ����
	private void setSession(HttpSession session, String state) {
		session.setAttribute(SESSION_STATE, state);
	}
	
	//���̹����̵�� redirectó�� �� AccessTokenȹ�� Method
	public OAuth2AccessToken getAccessToken(HttpSession session, String code, String state) throws IOException {
		String sessionState = getSession(session);
		if(StringUtils.pathEquals(sessionState, state)) {
			OAuth20Service oauthService = new ServiceBuilder()
					.apiKey(CLIENT_ID)
					.apiSecret(CLIENT_SECRET)
					.callback(REDIRECT_URI)
					.state(state)
					.build(NaverLoginApi.instance());
			//scribe���� �����ϴ� AccesssToken ȹ�� ������� �׾Ʒ� Access Token�� ȹ��
			OAuth2AccessToken accessToken = oauthService.getAccessToken(code);
			return accessToken;
		}
		else {
			return null;
		}
	}
	
	//http session�� ������ ��ȯ
	private String getSession(HttpSession session) {
		return (String)session.getAttribute(SESSION_STATE);
	}
	
	//Access Token�� �̿��Ͽ� ���̹� ����� ������ API�� ȣ��
	public String getUserProfile(OAuth2AccessToken oauthToken) throws IOException {
		OAuth20Service oauthService = new ServiceBuilder()
				.apiKey(CLIENT_ID)
				.apiSecret(CLIENT_SECRET)
				.callback(REDIRECT_URI)
				.build(NaverLoginApi.instance());
		OAuthRequest request = new OAuthRequest(Verb.GET, PROFILE_API_URL, oauthService);
		oauthService.signRequest(oauthToken, request);
		Response response = request.send();
		return response.getBody();
	}
}
