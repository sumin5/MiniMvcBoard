package com.kyk.ezenPJT.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.kyk.ezenPJT.dao.EzenDao;
import com.kyk.ezenPJT.dto.AuthUserDto;
import com.kyk.ezenPJT.dto.EzenJoinDto;
import com.kyk.ezenPJT.util.Constant;

public class CustomUserDetailsService implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		EzenDao edao = Constant.edao;
		AuthUserDto adto = null;
		EzenJoinDto fdto = null;
		Boolean flag = false;
		//EzenJoinDto�� DB EZENUSER�� ���ڵ�� ���ε�
		if(username.startsWith("kakao_") || username.startsWith("naver_") || username.startsWith("google_")) {
			adto = edao.authLogin(username);
		}
		else {
			fdto = edao.login(username);
			flag = true;
			if(fdto == null) {
				System.out.println("security���� dto null�� �α��� ����");
				throw new UsernameNotFoundException("No user found with username");
				//������ ��ť��Ƽ���� ���ܸ� ó���Ͽ� �α��ν���ó��
			}
		}
		
		System.out.println("fdto" + fdto);
		System.out.println("adto" + adto);
		
		if(flag) {
			String pw = fdto.getPpw();
			String auth = fdto.getPauth();
			System.out.println("pw : " + pw + " auth : " + auth);
			Collection<SimpleGrantedAuthority> roles = new ArrayList<SimpleGrantedAuthority>();
			//roles�� ���� ROLE�� �����ϴ� ��ü
			//role��(���� ���а�, ROLE_USER, ROLE_ADMIN, ROLE_MANAGE ��)�� �����ϴ� ����Ʈ ��ü
			//ROLE_TEMPORARY_USER (�ӽ� �����)
			roles.add(new SimpleGrantedAuthority(auth));
			
			UserDetails user = new User(username, pw, roles);
			
			return user;
		}
		
		else {
			String pw = adto.getAuthPw();
			String auth = adto.getAuth();
			System.out.println("pw : " + pw + " auth : " + auth);
			Collection<SimpleGrantedAuthority> roles = new ArrayList<SimpleGrantedAuthority>();
			//roles�� ���� ROLE�� �����ϴ� ��ü
			//role��(���� ���а�, ROLE_USER, ROLE_ADMIN, ROLE_MANAGE ��)�� �����ϴ� ����Ʈ ��ü
			//ROLE_TEMPORARY_USER (�ӽ� �����)
			roles.add(new SimpleGrantedAuthority(auth));
			
			UserDetails user = new User(username, pw, roles);
			
			return user;
		}
		
	}

}
