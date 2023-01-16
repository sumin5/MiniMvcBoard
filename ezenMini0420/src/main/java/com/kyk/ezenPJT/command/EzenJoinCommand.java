package com.kyk.ezenPJT.command;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;

import com.kyk.ezenPJT.dao.EzenDao;
import com.kyk.ezenPJT.dto.EzenJoinDto;
import com.kyk.ezenPJT.util.Constant;

public class EzenJoinCommand implements EzenCommand {

	@Override
	public void execute(HttpServletRequest request, Model model) {
		//��ȣȭ ��ü
			BCryptPasswordEncoder passwordEncoder = Constant.passwordEncoder;
		
		//form�� �Է� ���Ұ��� ����
			String bid = request.getParameter("pid");
			String bpw = request.getParameter("ppw"); //��ȣȭ ��
			String baddress = request.getParameter("paddress");
			String bhobby = request.getParameter("phobby");
			String bprofile = request.getParameter("pprofile");
		
		//��ȣȭ �� bpw�� bpw_org�� ����
			String bpw_org = bpw; //bpw_org�� ��ȣȭ �� pw
			bpw = passwordEncoder.encode(bpw_org); //encode�޼���� ��ȣȭ
			
			EzenJoinDto dto = new EzenJoinDto(bid, bpw, baddress, bhobby, bprofile, null, null);
			
			EzenDao edao = Constant.edao;
			String result = edao.join(dto);
			
			
			request.setAttribute("result", result);
	}
	
}
