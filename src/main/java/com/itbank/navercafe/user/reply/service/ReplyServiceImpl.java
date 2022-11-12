package com.itbank.navercafe.user.reply.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.itbank.navercafe.common.file.FileUtils;
import com.itbank.navercafe.common.file.dto.FileDTO;
import com.itbank.navercafe.common.file.dto.FileResult;
import com.itbank.navercafe.common.file.service.FileService;
import com.itbank.navercafe.user.cafemember.mapper.CafeMemberMapper;
import com.itbank.navercafe.user.reply.dto.ReplyDTO;
import com.itbank.navercafe.user.reply.mapper.ReplyMapper;



@Service
public class ReplyServiceImpl implements ReplyService{

	@Autowired ReplyMapper rm;
	@Autowired CafeMemberMapper cafeMemberMap;
	@Autowired FileService fs;
	@Autowired
	private FileUtils fileUtils;
	
	
	@Override
	public Integer getReplyCount(int boardNum) {
		//System.out.println("board에서 넘겨준 : "+boardNum);
		int result=rm.getReplyCount(boardNum);
		//System.out.println("그래서 갯수는? : "+result);
		return result;
	}

	@Override
	public List<HashMap<String, Object>> getReplyList(int boardNum,String cafeId) {
		return rm.getReplyList(boardNum,cafeId);
	}


	
	@Override
	public void saveReply(MultipartHttpServletRequest mul,int step) {
		MultipartFile multipartFile = mul.getFile("replyImgName");
		int result = 0;
		try {
			//생성할 디렉토리
			String directory = "boardReply";
			String userId =  mul.getParameter("userId");
			
			//System.out.println("userId : " + userId);
			//System.out.println("replyImgName : " + multipartFile.getOriginalFilename());
			
			if(userId != null && userId.length() > 0) {
				directory += "/" + userId;
			}
			
			//reply_num 빼오기
			int seq=rm.getReplyNum();


			ReplyDTO redto=new ReplyDTO();
			redto.setBoardNum(Integer.parseInt(mul.getParameter("boardNum")));
			redto.setUserId(mul.getParameter("userId"));
			redto.setReplyContent(mul.getParameter("replyContent"));
			redto.setReplyStep(step);
			redto.setReplyNum(seq);
//			System.out.println("들어갈 보드넘 :"+redto.getBoardNum());
//			System.out.println("들어갈 유저아이디 :"+redto.getUserId());
//			System.out.println("들어갈 내용 :"+redto.getReplyContent());
//			System.out.println("들어갈 스탭 :"+redto.getReplyStep());
//			System.out.println("들어갈 댓글넘버 :"+redto.getReplyNum());
//			System.out.println("directory >>>> " + directory);
//			System.out.println("설마 너?:"+multipartFile.getSize());
			if(multipartFile.getSize()!=0) {
				FileDTO fileDTO = null;
				FileResult fileResult = fileUtils.uploadFile(multipartFile, directory);
				fileDTO = fileResult.getFileDTO();
				System.out.println("넣어줄애:"+seq);
				fileDTO.setReplyNum(seq);
				
				fs.insertAttachFile(fileDTO);
			}
			System.out.println("댓글인지 답글인지 판단할 그룹번호 : "+mul.getParameter("groupNum"));
			//답글 작성시
			if(mul.getParameter("groupNum")!=null) {
				redto.setReplyGroup(Integer.parseInt(mul.getParameter("groupNum")));
				rm.saveGroupNumReply(redto);
				cafeMemberMap.replyUp(mul.getParameter("cafeId"),mul.getParameter("userId"));//세션값 넘겨준거임
			}else {//댓글 작성시	
				int saveResult=rm.saveReply(redto);
				if(saveResult==1) {
					cafeMemberMap.replyUp(mul.getParameter("cafeId"),mul.getParameter("userId"));//세션값 넘겨준거임
				}
			}

		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void updateReply(MultipartHttpServletRequest mul) {
		MultipartFile multipartFile = mul.getFile("replyImgName");
		//System.out.println("수정해줄 replyNum :"+mul.getParameter("replyNum"));
		//System.out.println("수정해줄 내용 :"+mul.getParameter("replyContent"));
		rm.updateReply(Integer.parseInt(mul.getParameter("replyNum")),mul.getParameter("replyContent"));
		try {
			//생성할 디렉토리
			String directory = "boardReply";
			String userId =  mul.getParameter("userId");
			
			//System.out.println("userId : " + userId);
			//System.out.println("replyImgName : " + multipartFile.getOriginalFilename());
			
			if(userId != null && userId.length() > 0) {
				directory += "/" + userId;
			}

			if(multipartFile.getSize()!=0) {
				FileDTO fileDTO = null;
				FileResult fileResult = fileUtils.uploadFile(multipartFile, directory);
				fileDTO = fileResult.getFileDTO();
				//존재한다면 교체
				if(fs.getReplyNum(Integer.parseInt(mul.getParameter("replyNum")))!=null) {
					fs.updateReply(fileDTO,Integer.parseInt(mul.getParameter("replyNum")));
				}else {
					//존재하지않으면 replyNum부여
					fileDTO.setReplyNum(Integer.parseInt(mul.getParameter("replyNum")));
					fs.insertAttachFile(fileDTO);
				}
				
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void deleteReply(int replyNum) {
		//System.out.println("삭제할 댓글 : "+replyNum);
		rm.deleteReply(replyNum);
		//System.out.println("사진 삭제할 댓글번호 : "+replyNum);
		fs.deleteReplyNum(replyNum);
	}



}
