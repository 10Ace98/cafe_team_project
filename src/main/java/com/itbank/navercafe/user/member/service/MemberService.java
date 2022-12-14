package com.itbank.navercafe.user.member.service;

import com.itbank.navercafe.user.member.dto.MemberDTO;



public interface MemberService {
	public int signup(MemberDTO dto);

	public int delete(String id);

	public int update(MemberDTO dto);

	public MemberDTO getU(String id);

	public boolean loginChk(MemberDTO memberDTO);

}
