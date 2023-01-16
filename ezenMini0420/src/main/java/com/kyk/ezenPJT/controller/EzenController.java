package com.kyk.ezenPJT.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.kyk.ezenPJT.command.AuthCommand;
import com.kyk.ezenPJT.command.CalendarInsertCommand;
import com.kyk.ezenPJT.command.CalendarListCommand;
import com.kyk.ezenPJT.command.CalendarUpdateCommand;
import com.kyk.ezenPJT.command.DashBoardCommand;
import com.kyk.ezenPJT.command.EzenAdminAuthCommand;
import com.kyk.ezenPJT.command.EzenBoardContentCommand;
import com.kyk.ezenPJT.command.EzenBoardDeleteCommand;
import com.kyk.ezenPJT.command.EzenBoardListCommand;
import com.kyk.ezenPJT.command.EzenBoardModifyCommand;
import com.kyk.ezenPJT.command.EzenBoardPageListCommand;
import com.kyk.ezenPJT.command.EzenBoardReplyCommand;
import com.kyk.ezenPJT.command.EzenBoardReplyViewCommand;
import com.kyk.ezenPJT.command.EzenBoardWriteCommand;
import com.kyk.ezenPJT.command.EzenCommand;
import com.kyk.ezenPJT.command.EzenJoinCommand;
import com.kyk.ezenPJT.command.EzenRecipeCommand;
import com.kyk.ezenPJT.command.EzenRecipeDetailsCommand;
import com.kyk.ezenPJT.command.EzenRecipeWriteCommand;
import com.kyk.ezenPJT.command.calendarDeleteCommand;
import com.kyk.ezenPJT.dao.EzenDao;
import com.kyk.ezenPJT.dto.AuthUserDto;
import com.kyk.ezenPJT.dto.DashBoardDto;
import com.kyk.ezenPJT.dto.EzenBoardDto;
import com.kyk.ezenPJT.dto.FullCalendarDto;
import com.kyk.ezenPJT.dto.RecipeDto;
import com.kyk.ezenPJT.naver.NaverLoginBO;
import com.kyk.ezenPJT.util.Constant;


@Controller
public class EzenController {
	
	private EzenCommand com;
	
	//��ȣȭ ó�� bean����(���������̹Ƿ� ���� �� �����س��� ���)
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	public void setPasswordEncoder(BCryptPasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
		Constant.passwordEncoder = passwordEncoder;
	}
	
	//EzenDaoŬ���� ����
	private EzenDao edao;
	@Autowired
	public void setEdao(EzenDao edao) {
		this.edao = edao;
		Constant.edao = edao;
	}
	
	//NaverLoginBO��ü ����
	private NaverLoginBO naverLoginBO;
	@Autowired
	public void setNaverLoginBO(NaverLoginBO naverLoginBO) {
		this.naverLoginBO = naverLoginBO;
	}
	
	//google login Bean
	@Autowired
	private GoogleConnectionFactory googleConnectionFactory;
	
	@Autowired
	private OAuth2Parameters googleOAuth2Parameters;
	
	@RequestMapping("/home")
	public String home() {
		System.out.println("home");
		return "home";
	}
	
	@RequestMapping("/mhome")
	public String mhome() {
		System.out.println("mhome");
		return "mhome";
	}
	
	//Ŭ���̾�Ʈ���� ��û�� ajax�� �ǽ�
	//�������� jsp�� �����ô� @ResponseBody���� ����
	@RequestMapping("/joinView")
	public String joinView() {
		return "joinView";
	}
	
	@RequestMapping("/loginView")
	public String loginView(HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model) {
		System.out.println("loginView");
		socialUrl(model,session);
		return "loginView";
	}
	
	private void socialUrl(Model model, HttpSession session) {
		//kakao�� Oauth2(������ �α���) url���ϱ�
		//kakao code kakao developer�������� ���� ���
		String kakao_url = "https://kauth.kakao.com/oauth/authorize"
							+"?client_id=bdb56a47b2161398cebac27f99924b92" //īī�� ���� ���� Ű��
							+"&redirect_uri=https://localhost:8443/ezenPJT/kredirect" //������ ����� �뺸�� ��û ���
							+"&response_type=code";
		model.addAttribute("kakao_url", kakao_url);
		
		//naver�� url���ϱ�
		//���̹����̵�� ���� URL�� �����ϱ� ���Ͽ� NaverLoginBOŬ������ getAuthorizationUrl�޼ҵ� ȣ��
		String naverAuthUrl = naverLoginBO.getAuthorizationUrl(session);
		System.out.println("���̹�" + naverAuthUrl);
		model.addAttribute("naver_url",naverAuthUrl);
		
		//google url���ϱ�
		//����code����, OAuth2�� ó���ϴ� ��ü
		OAuth2Operations oauthOperations = googleConnectionFactory.getOAuthOperations();
		String url = oauthOperations.buildAuthorizeUrl(GrantType.AUTHORIZATION_CODE, googleOAuth2Parameters);
		//GrantType�� Oauth2ó����� AUTORIZATION_CODE�� ��������Ʈ ����, googleOAuth2Parameter��
		//�� ������ scope�� redirect������ ���� ��ü
		System.out.println("����: " + url);
		model.addAttribute("google_url", url);
	}
	
	//social login redirect
	//kakao
	@RequestMapping(value="/kredirect", produces="application/json; charset=UTF-8")
	public String kredirect(@RequestParam String code, HttpServletResponse response, Model model, HttpServletRequest request) throws Exception {
		System.out.println("#########" + code);
		String access_Token = getKakaoAccessToken(code,response);
		System.out.println("###access_Token#### : " + access_Token);
		//�� access_Token�� �̿��Ͽ� kakao�� ����� ������ ��
		HashMap<String, Object> userInfo = getKakaoUserInfo(access_Token);
		String email = (String)userInfo.get("email");
		String authUsername = "kakao_" + email;
		String authPw = (String)userInfo.get("nickname"); //��ȣȭ�Ǳ� ��
		String cryptPw = passwordEncoder.encode(authPw);
		
		AuthUserDto dto = new AuthUserDto(authUsername,cryptPw,null);
		authDB(request,model,dto);
		
		model.addAttribute("authUser", authUsername);
		model.addAttribute("authPw", authPw);
		
		return "socialLogin";
	}
	
	private void authDB(HttpServletRequest request, Model model, AuthUserDto dto) {
		com = new AuthCommand();
		request.setAttribute("dto", dto);
		com.execute(request, model);
	}

	//naver
	@RequestMapping(value="/nredirect", produces="application/json; cahrset=UTF8")
	public String nredirect(@RequestParam String code, @RequestParam String state, HttpSession session, Model model, HttpServletRequest request) throws Exception {
		System.out.println("nredirect");
		System.out.println("state : " + state);
		OAuth2AccessToken oauthToken = naverLoginBO.getAccessToken(session, code, state);
		String apiResult = naverLoginBO.getUserProfile(oauthToken);
		System.out.println(apiResult);
		//String������ apiResult�� json���·� ����
		JSONParser parser = new JSONParser(); //�ڹ��� ���ڿ��� Object��üȭ �ϴ� Ŭ������ ��ü
		Object obj = parser.parse(apiResult); //�ڹ��� ���ڿ��� �ڹ��� Object
		JSONObject jsonObj = (JSONObject) obj; //�ڹ� Object�� �ڽ� Ŭ������ JSONObject�� ����ȯ
		JSONObject responseObj = (JSONObject)jsonObj.get("response"); //apiResult�� response
		System.out.println("naver user���� : " + responseObj);
		
		//response�� email�� ��ȯ
		String authUsername = "naver_" + (String)responseObj.get("email");
		String authPw = (String)responseObj.get("name");
		String id = (String)responseObj.get("id");
		
		System.out.println("email " + authUsername);
		System.out.println("nickname " + authPw);
		System.out.println("id " + id);
		
		String cryptPw = passwordEncoder.encode(authPw);
		
		AuthUserDto dto = new AuthUserDto(authUsername,cryptPw,null);
		authDB(request,model,dto);
		model.addAttribute("authUser", authUsername);
		model.addAttribute("authPw", authPw);
		
		return "socialLogin";
	}
	
	public String getKakaoAccessToken (String authorize_code,HttpServletResponse response)  {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8"); 
		String access_Token = "";
		String refresh_Token = "";
		String reqURL = "https://kauth.kakao.com/oauth/token"; //��ū ������ �ޱ� ���� ��û ���
		try {
			URL url = new URL(reqURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// URL������ ����¿� ��� �� �� �ְ�, POST Ȥ�� PUT ��û�� �Ϸ��� setDoOutput�� true�� �����ؾ���.
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			//kakao�� �������ִ� ��
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			StringBuilder sb = new StringBuilder();
			sb.append("grant_type=authorization_code");
			sb.append("&client_id=bdb56a47b2161398cebac27f99924b92");  //������ �߱޹��� key
			sb.append("&redirect_uri=https://localhost:8443/ezenPJT/kredirect");
			sb.append("&code=" + authorize_code);
			bw.write(sb.toString());
			bw.flush();
			//��� �ڵ尡 200�̶�� ����
			int responseCode = conn.getResponseCode();
			System.out.println("responseCode : " + responseCode);
			// ��û�� ���� ���� JSONŸ���� Response �޼��� �о����
			 BufferedReader br = 
		            	new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
			 String line = "";
	         String result = "";
	         while ((line = br.readLine()) != null) {
	        	result += line;
	         }
	         System.out.println("response body : " + result);
	         JSONParser parser = new JSONParser();
	         Object obj = parser.parse(result); //parse�޼���� Object��ȯ
	         JSONObject jsonObj = (JSONObject) obj;
	         access_Token = (String)jsonObj.get("access_token");
	         refresh_Token = (String)jsonObj.get("refresh_token");
			 System.out.println("access_token : " + access_Token);
			 System.out.println("refresh_token : " + refresh_Token);
			 br.close();
			 bw.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return access_Token; //kakao���� �������ִ� �̿��� ����
	}
	
	//kakao access-token���� ����� ���� ���
	public HashMap<String,Object> getKakaoUserInfo (String access_Token) {
		HashMap<String, Object> userInfo = new HashMap<String, Object>();
		String reqURL = "https://kapi.kakao.com/v2/user/me";
		try {
			URL url = new URL(reqURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			//��û�� �ʿ��� Header�� ���Ե� ����
			conn.setRequestProperty("Authorization", "Bearer " + access_Token);
			int responseCode = conn.getResponseCode(); //200�̸� ����
			System.out.println("responseCode : " + responseCode);
			BufferedReader br = 
					new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
			String line = "";
	        String result = "";
	        while ((line = br.readLine()) != null)  {
	        	 result += line;
	        }
	        
	        System.out.println("response body : " + result);
	        
	        JSONParser parser = new JSONParser();
	        Object obj = parser.parse(result);
	        JSONObject jsonObj = (JSONObject) obj;
	        String id = jsonObj.get("id").toString(); //kakao�� ���� ��ȣ(���� ������ ��ȣ)
	        JSONObject properties = (JSONObject)jsonObj.get("properties");
	        JSONObject kakao_account = (JSONObject)jsonObj.get("kakao_account"); //�˼��Ŀ� ����
	        String accessToken = (String)properties.get("access_token");
	        String nickname = (String)properties.get("nickname");
	        String email = (String)kakao_account.get("email"); //�˼��Ŀ� ����
	        userInfo.put("accessToken", access_Token);
            userInfo.put("nickname", nickname);
            userInfo.put("email", email);
            userInfo.put("id", id);
            System.out.println("=============");
            System.out.println("acces token  " + accessToken);
            System.out.println("nickname  " + nickname);
            System.out.println("email  " + email); 
            System.out.println("id  " + id);
		}
		catch(Exception e1) {
			e1.getMessage();
		}
		
		return userInfo;
	}
	
	//���ۻ�������� ��� �޼���
	public HashMap<String, Object> getGoogleUserInfo(String access_Token, HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		HashMap<String, Object> gUserInfo = new HashMap<String, Object>();
		String reqURL = "https://www.googleapis.com/userinfo/v2/me?access_token=" + access_Token;
		   try {
	         URL url = new URL(reqURL); 
	         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	         conn.setRequestProperty("Authorization", "Bearer " + access_Token);
	         int responseCode = conn.getResponseCode(); 
	         System.out.println("responseCode : "+responseCode);
	         if(responseCode == 200) { //200�� ���� ����
	            BufferedReader br = 
	               new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8")); 
	            String line = ""; 
	            String result = ""; 
	            while ((line = br.readLine()) != null) {
	               result += line;
	            }
	            JSONParser parser = new JSONParser(); //���ڿ��� json��üȭ�ϴ� ��ü
	            Object obj = parser.parse(result);
	            JSONObject jsonObj = (JSONObject) obj;
	            String name_obj = (String)jsonObj.get("name");
	            String email_obj = (String)jsonObj.get("email");
	            String id_obj = "GOOGLE_" + (String)jsonObj.get("id");
	            
	            gUserInfo.put("name", name_obj); 
	            gUserInfo.put("email", email_obj); 
	            gUserInfo.put("id", id_obj);
	            
	            System.out.println("gUserInfo : " + gUserInfo); 
	         }
	      }
	      catch(Exception e) {
	         e.printStackTrace();
	      }
	      
	      return gUserInfo;
	}
	
	@RequestMapping(value="/gredirect", produces="application/text; charset=UTF8")
	//���ۿ��� ��û�ϴ� ���
	public String googleCallback(Model model, @RequestParam String code, HttpServletResponse response, HttpServletRequest request) throws IOException {
		System.out.println("google redirect");
		//���� code ����, OAuth�� ó���ϴ� ��ü
		OAuth2Operations oauthOperations = googleConnectionFactory.getOAuthOperations();
		AccessGrant accessGrant = oauthOperations.exchangeForAccess(code, googleOAuth2Parameters.getRedirectUri(), null);
		String accessToken = accessGrant.getAccessToken();
		HashMap<String, Object> map = getGoogleUserInfo(accessToken, response);
		
		String email = (String)map.get("email");
		String authUsername = "google_" + email;
		String authPw = (String)map.get("name");
		String id = (String)map.get("id");
		
		System.out.println("email : " + email);
		System.out.println("name : " + authPw);
		System.out.println("id : " + id);
		
		String cryptPw = passwordEncoder.encode(authPw);
		
		AuthUserDto dto = new AuthUserDto(authUsername,cryptPw,null);
		authDB(request,model,dto);
		
		model.addAttribute("authUser", authUsername);
		model.addAttribute("authPw", authPw);
		
		return "socialLogin";
	}

	//ȸ������ó�� ��û
	@RequestMapping(value="/join", produces="application/text; charset=UTF-8") //ajax�� ��û�� �ѱ�ó��
	@ResponseBody //ajax�� ��û�� ���� jsp�ƴ� �Ϲ� ���ڿ�, ��ü map, list ������ ��ȯ��
	public String join(HttpServletRequest request, HttpServletResponse response, Model model) {
		System.out.println("join");
		com = new EzenJoinCommand();
		com.execute(request, model);
		String result = (String)request.getAttribute("result");
		if(result.equals("success")) {
			return "join-success";
		}
		else {
			return "join-failed";
		}
	}
	
	//�α��� ����ó��, ������ ó��
	@RequestMapping("/main")
	public String main(HttpServletRequest request, Model model, Authentication authentication) {
		System.out.println("main");
		getUsername(authentication, request);
		String username = (String)request.getAttribute("username");
		String auth = (String)request.getAttribute("auth");
		com = new EzenRecipeCommand();
		com.execute(request, model);
		return "mainView";
	}
	
	//������ ���â
	@RequestMapping("/rwriteView")
	public String rwriteView() {
		System.out.println("rwriteView");
		return "rwriteView";
	}
	
	//������ DB�� ���
	@RequestMapping("/recipeWrite")
	public String recipeWrite(MultipartHttpServletRequest mphr, Model model) {
		System.out.println("recipeWrite");
		String rClass = mphr.getParameter("rClass");
		String rtrName = mphr.getParameter("rtrName");
		String rTitle = mphr.getParameter("rTitle");
		String rContent = mphr.getParameter("rContent");
		int rId = 0; //rId�� ���Ƿ� ����(DB���� seq�ѹ��� �����ϴϱ� 
		String rPhoto = null; //DB�� ������ �����̸�
		//��ȯ�Ǵ� ���� �����ʹ� MultipartFile���̰� getFile(���ϼӼ���)�� ���Ѵ�
		MultipartFile mf = mphr.getFile("rPhoto");
		
		//���ε� �Ǵ� ���� ������ġ (������Ʈ��, ��Ĺ������)
		//�ʱ� �ż� �������� �ٷ� �����ֱ����� ��Ĺ���� ����(war���Ϸ� ��Ĺ������ �����ô� ���ʿ�)
		String path = "D:/KYK/workspace/ezenMini0420/src/main/webapp/resources/upimage/";
		String path1 = "D:/KYK/util/apache-tomcat-9.0.63/wtpwebapps/ezenMini0420/resources/upimage/";
		//���ε�� ���� �̸�
		String originFileName = mf.getOriginalFilename();
		long prename = System.currentTimeMillis();
		long fileSize = mf.getSize();
		System.out.println("originFileName : " + originFileName);
		System.out.println("fileSize : " + fileSize);
		//�����ϱ����� �ߺ��� �Ǵ� ���� ���ϸ��� ���ϱ� ���� ����� ���ϸ�
		String safeFile = path + prename + originFileName;
		String safeFiel1 = path1 + prename + originFileName;
		//DB�� ������ ���� �̸�
		rPhoto = prename + originFileName;
		RecipeDto rdto = new RecipeDto(rId, rClass, rtrName, rTitle, rPhoto, rContent);
		mphr.setAttribute("rdto", rdto);
		com = new EzenRecipeWriteCommand();
		com.execute(mphr, model); //DB�� ����ϰ� �������� result�� ��ȯ. ������ model�� �߰�
		
		//model��ü�� ���� �����Ϸ��� asMap()�޼��带 ���
		Map<String, Object> map = model.asMap();
		String res = (String)map.get("result");
		System.out.println("res : " + res);
		if(res.equals("success")) {
			try {
				mf.transferTo(new File(safeFile));
				mf.transferTo(new File(safeFiel1));
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return "redirect:main";
		}
		else {
			return "main";
		}
	}
	
	/* ������ ������ */
	@RequestMapping("/recipeDetails")
	public String recipeDetails(HttpServletRequest request, Model model) {
		System.out.println("recipeDetails�Դϴ�");
		com = new EzenRecipeDetailsCommand();
		com.execute(request, model);
		
		if(model.containsAttribute("rDetails")) {
			System.out.println("success");
			return "recipeDetailsView";
		}
		else {
			return "redirect:main";
		}
	}
	
	@RequestMapping("/processLogin")
	public ModelAndView processLogin(
			@RequestParam(value = "log", required = false) String log,
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout,
			HttpSession session, Model pmodel
			) {
		System.out.println("processLogin");
		ModelAndView model = new ModelAndView();
		if(log != null && log != "") {
			model.addObject("log", "before login!");
		}
		if(error != null && error != "") { //�α��� ���нÿ� security���� processLogin?error=1
			model.addObject("error", "Invalid username or passoword");
		}
		if(logout != null && logout != "") { //processLogin?logout=1
			model.addObject("logout", "You've been logged out successfully.");
		}
		socialUrl(pmodel,session);
		model.setViewName("loginView");
		return model;
	}
	
	//logout
	@RequestMapping("/logoutView")
	public String logoutView() {
		System.out.println("logoutView");
		return "logoutView";
	}
	
	//������������
	@RequestMapping("/adminView")
	public String adminView() {
		System.out.println("adminView");
		return "adminView";
	}
	
	@RequestMapping("/authAdmin")
	@ResponseBody //ajax�� ������ jsp�� �ƴ� ������ ��ȯ(���⼭�� ���ڿ� ��ȯ)
	public String authAdmin(HttpServletRequest request, Model model) {
		System.out.println("authAdmin");
		com = new EzenAdminAuthCommand();
		com.execute(request, model);
		
		Map<String, Object> map = model.asMap();
		String res = (String)map.get("result");
		if(res.equals("success")) {
			return "success";
		}
		else
			return "failed";
	}
	
	//eBoard�κ�
	@RequestMapping("/eBoard")
	public String eBoard(HttpServletRequest request, Model model) {
		System.out.println("eBoard��û");
		com = new EzenBoardListCommand();
		com.execute(request, model);
		return "eBoardView";
	}
	
	
	@RequestMapping("/plist")
	public String purl(HttpServletRequest request, Model model) {
		System.out.println("plist��û");
		System.out.println(request.getParameter("pageNo"));
		com = new EzenBoardPageListCommand();
		com.execute(request, model);
		return "pageBoard";
	}
	
	@RequestMapping("/searchBoard")
	public String searchBoard(HttpServletRequest request, Model model) {
		System.out.println("searchBoard��û");
		
		com.execute(request, model);
		return "searchBoard";
	}
	
	@RequestMapping("/writeView")
	public String writeView() {
		System.out.println("writeView��û");
		return "writeView";
	}
	
	@RequestMapping(value="/bWrite", produces="application/text; charset=UTF-8")
	public String bWrite(HttpServletRequest request, Model model) {
		System.out.println("bWrite��û");
		com = new EzenBoardWriteCommand();
		com.execute(request, model);
		com = new EzenBoardListCommand();
		com.execute(request, model);
		return "eBoardView";
	}
	
	@RequestMapping("/contentView")
	public String contentView(HttpServletRequest request, Model model) {
		System.out.println("contentView��û");
		com = new EzenBoardContentCommand();
		com.execute(request, model);
		return "contentView";
	}
	
	@RequestMapping(value="/modify", produces="application/text; charset=UTF-8")
	public String modify(HttpServletRequest request, Model model) {
		System.out.println("modify��û");
		com = new EzenBoardModifyCommand();
		com.execute(request, model);
		com = new EzenBoardListCommand();
		com.execute(request, model);
		return "eBoardView";
	}
	
	@RequestMapping("/delete")
	public String delete(HttpServletRequest request, Model model) {
		System.out.println("delete��û");
		com = new EzenBoardDeleteCommand();
		com.execute(request, model);
		com = new EzenBoardListCommand();
		com.execute(request, model);
		return "eBoardView";
	}
	
	@RequestMapping("/replyView")
	public String replyView(HttpServletRequest request, Model model) {
		System.out.println("replyView��û");
		com = new EzenBoardReplyViewCommand();
		com.execute(request, model);
		return "replyWrite";
	}
	
	@RequestMapping(value="/reply", produces="application/text; charset=UTF-8")
	public String reply(EzenBoardDto dto, HttpServletRequest request, Model model) {
		System.out.println("reply��û");
		com = new EzenBoardReplyCommand();
		request.setAttribute("formDto", dto);
		com.execute(request, model);
		com = new EzenBoardListCommand();
		com.execute(request, model);
		return "eBoardView";
	}
	
	@RequestMapping("/util")
	public String util() {
		System.out.println("util��û");
		return "utils";
	}
	
	@RequestMapping("/fCalendar")
	public String fCalendar() {
		System.out.println("fCalendar��û");
		return "fCalendar";
	}
	
	@RequestMapping("/sse")
	public String sse() {
		System.out.println("sse��û");
		return "sseView";
	}
	
	@RequestMapping("/seventEx")
	public void seventEx(HttpServletRequest request, HttpServletResponse response) throws Exception {
		System.out.println("single event");
		response.setContentType("text/event-stream"); //event-stream�� �ƴϸ� sse�ȵ�
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();
		for(int i = 0; i < 20; i++) {
			writer.write("data: " + System.currentTimeMillis() + "/n/n");
			//������ ���� System.currentTimeMillis() + "/n/n"�̸�
			//"data: "�� �����Ͷ�� �����ڷ� event��ü�� ������ ���� �Ӽ�
			writer.flush();
			try {
				Thread.sleep(1000);
			}
			catch(Exception e) {
				e.getMessage();
			}
		}
		
		writer.close();
	}
	
	@RequestMapping(value="/calendar", produces="application/json; charset=UTF-8")
	@ResponseBody
	public ArrayList<HashMap<String,Object>> calendar(HttpServletRequest request, Model model) {
		//JS���� ArrayList�� �迭�̸� HashMap<String,Object>�� Object
		System.out.println("calendar��û");
		System.out.println("cId : " + request.getParameter("cId"));
		
		com = new CalendarListCommand();
		com.execute(request, model);
		HashMap<String, Object> map = (HashMap)model.asMap();
		ArrayList<FullCalendarDto> calendarList = (ArrayList)map.get("calendarList");
		
		ArrayList<HashMap<String,Object>> clistArr = new ArrayList<HashMap<String,Object>>();
		for(FullCalendarDto dto : calendarList) {
			HashMap<String,Object> clistMap = new HashMap<String, Object>();
			String cAllDay = dto.getcAllDay();
			boolean allDay;
			if(cAllDay.equals("true")) {
				allDay = true;
			}
			else {
				allDay = false;
			}
			
			clistMap.put("cNo",dto.getcNo());
			clistMap.put("cId",dto.getcId());
			clistMap.put("title",dto.getcTitle());
			clistMap.put("start",dto.getcStart());
			clistMap.put("end",dto.getcEnd());
			clistMap.put("allDay",allDay);
			
			clistArr.add(clistMap);
		}
	
		return clistArr;
	}
	
	@RequestMapping(value="/calendarInsert", produces="application/json; charset=UTF-8")
	@ResponseBody
	public String calendarInsert(@RequestBody FullCalendarDto fullCalendarDto, HttpServletRequest request, Model model) {
		//@RequestBody�� json���� �Ķ���͸� ������
		System.out.println("calendarInsert��û");
		
		String start = fullCalendarDto.getcStart();
		String end = fullCalendarDto.getcEnd();
		
		//���ڿ������� �ð��� LocalDateTime�������� ��ȯ
		//DateTimeFormatter formatDateTime = DateTimeFormatter.ofPattern("yyyy-MM-DD'T'HH:mm");
		//LocalDateTime localDateTime = LocalDateTime.from(formatDataTime.parse(start));
		
		//���ǰ��� �ȵ�� ���(�ð�ǥ�ÿ� z�� ���� ���)
		DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_DATE_TIME;
		LocalDateTime localDateStart = LocalDateTime.from(ISO_DATE_TIME.parse(start));
		
		//LocalDateTime�� Timestamp�� ��ȯ
		Timestamp stampStart = Timestamp.valueOf(localDateStart);
		
		LocalDateTime localDateEnd = LocalDateTime.from(ISO_DATE_TIME.parse(end));
		Timestamp stampEnd = Timestamp.valueOf(localDateEnd);
		
		System.out.println("stampStart : " + stampStart);
		System.out.println("stampEnd : " + stampEnd);
		
		//�ð��� ����ϱ� ���ؼ��� �и�������� �� posix�������� ��ȯ(1970.1.1 0�ú��� ����� �и�������)
		Long posixStart = stampStart.getTime();
		Long posixEnd = stampEnd.getTime();
		
		System.out.println("posixStart : " + posixStart);
		System.out.println("posixEnd : " + posixEnd);
		
		fullCalendarDto.settStart(stampStart);
		fullCalendarDto.settEnd(stampEnd);
		
		request.setAttribute("dto", fullCalendarDto);
		
		com = new CalendarInsertCommand();
		com.execute(request, model);
		
		return "success";
	}
	
	@RequestMapping(value="/calendarUpdate", produces="application/json; charset=UTF-8")
	@ResponseBody
	public String calendarUpdate(@RequestBody FullCalendarDto fullCalendarDto, HttpServletRequest request, Model model) {
		System.out.println("calendarUpdate��û");
		System.out.println("calendarUpdateNo : " + fullCalendarDto.getcNo());
		
		request.setAttribute("dto", fullCalendarDto);
		
		com = new CalendarUpdateCommand();
		com.execute(request, model);
		return "success";
	}
	
	@RequestMapping(value="/calendarDelete", produces="application/json; charset=UTF-8")
	@ResponseBody
	public String calendarDelete(@RequestBody FullCalendarDto fullCalendarDto, HttpServletRequest request, Model model) {
		System.out.println("calendarDelete��û");
		System.out.println("calendarDeleteNo : " + fullCalendarDto.getcNo());
		
		request.setAttribute("dto", fullCalendarDto);
		
		com = new calendarDeleteCommand();
		com.execute(request, model);
		
		return "success";
	}
	
	@RequestMapping("/about")
	public String about() {
		System.out.println("about��û");
		return "about";
	}
	
	//DashBoard
	@RequestMapping("/dash")		
	public String dash() {
		return "dashBoard";
	}
		
	@RequestMapping(value= "/dashView", produces = "application/json; charset=UTF8") 
	@ResponseBody
	public JSONObject dashView(HttpServletRequest request, Model model) {
		String subcmd = request.getParameter("subcmd");
		System.out.println(subcmd);
			
		com = new DashBoardCommand();
		com.execute(request, model);
			
		JSONObject result = getDashData(request,model);
			
		return result;
	}
	
	//bar
	@RequestMapping("/bar")
	public String bar() {
		System.out.println("bar��û");
		return "bar";
	}
	
	//pie
	@RequestMapping("/pie")
	public String pie() {
		System.out.println("pie��û");
		return "pie";
	}
	
	//�Ϲݸ޼���
	private void getUsername(Authentication authentication, HttpServletRequest request) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String username = userDetails.getUsername();
		System.out.println(userDetails.getUsername()); //�α����� ���̵�
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		String auth = authorities.toString(); //role�� �� ���ڿ��� ��ȯ
		System.out.println(auth); //[ROLE_USER]����
		request.setAttribute("username", username);
		request.setAttribute("auth", auth);
	}
	
	private JSONObject getDashData(HttpServletRequest request, Model model) {
		
		HashMap<String,Object> dashMap = (HashMap)model.asMap();
		ArrayList<DashBoardDto> dashList = (ArrayList<DashBoardDto>)dashMap.get("dashArray");
		
		JSONArray dashArr = new JSONArray();
		
		for(DashBoardDto dto:dashList ) {
			JSONObject data = new JSONObject(); //map�������� �� JSON��ü
			data.put("month", dto.getMonth());
			data.put("pc", dto.getPcQty());
			data.put("monitor", dto.getMonitorQty());
			
			dashArr.add(data);
		}
		
		 JSONObject result = new JSONObject(); //JSONObject�̹Ƿ� map�����̹Ƿ� key/value�� ���� ����
		 result.put("datas", dashArr);
		 
		 return result;
		
	}


}
