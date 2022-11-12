package com.itbank.navercafe.user.board.controller;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.itbank.navercafe.common.CommonUtils;
import com.itbank.navercafe.common.file.service.FileService;
import com.itbank.navercafe.user.board.dto.BoardDTO;
import com.itbank.navercafe.user.board.dto.BoardExtendDTO;
import com.itbank.navercafe.user.board.service.BoardService;
import com.itbank.navercafe.user.cafe.dto.CafeDTO;
import com.itbank.navercafe.user.cafemember.dto.CafeMemberDTO;
import com.itbank.navercafe.user.cafemember.service.CafeMemberService;
import com.itbank.navercafe.user.grade.service.GradeService;
import com.itbank.navercafe.user.memo.service.MemoService;
import com.itbank.navercafe.user.menu.dto.MenuDTO;
import com.itbank.navercafe.user.menu.service.MenuService;
import com.itbank.navercafe.user.reply.service.ReplyService;

@Controller
@RequestMapping("/user/board")
public class BoardController {
	@Autowired BoardService ser;
	@Autowired ReplyService replySer;
	@Autowired CafeMemberService boardCafeSer;
	@Autowired MenuService menuService;
	@Autowired GradeService upSer;
	@Autowired MemoService memoSer;
	@Autowired CafeMemberService cafeMemberService;
	@Autowired FileService fs;
	
	@RequestMapping("/writeForm")
	public String writeForm(String cafeId, HttpSession session, CafeDTO cafeDTO, Model model) {
		System.out.println("글쓰기 이동");
		
		String userId = (String) session.getAttribute("loginId");
		System.out.println("아이디 :  "+userId);
		CafeMemberDTO dto = boardCafeSer.getCafeMember(cafeId,userId);
		model.addAttribute("cafeMember",dto);
		model.addAttribute("cafeDTO", cafeDTO);
		return "user/board/writeForm";
	}
	

	@GetMapping("/goBoardList")	
	public String goBoardList(Model model, String cafeId, MenuDTO menuDTO) throws Exception{
		int boardMenuNum = menuDTO.getBoardMenuNum();
		int boardMenuType = 1;
		String boardMenuName = "전체글보기";
		
		String returnUrl = "user/board/boardList";
		
		if(cafeId != null) {
			menuDTO.setCafeId(cafeId);
		}
		
		if(boardMenuNum > 0) {
			menuDTO = menuService.selectBoardMenu(boardMenuNum);
			boardMenuType = menuDTO.getBoardMenuType();
			boardMenuName = menuDTO.getBoardMenuName();
		}
		
		// 게시판 타입에 따라 다른 view 설정
		switch(boardMenuType) {
		case 4 :
			returnUrl = "user/board/gradeBoardList";
			break;
		case 5 :
			returnUrl = "user/board/memoBoardList?cafeId="+cafeId;
			break;
		}
		
		model.addAttribute("boardMenuName",boardMenuName);
		model.addAttribute("boardList",ser.getBoardList(menuDTO));
		model.addAttribute("cafeId",cafeId);
		
		return returnUrl;
	}
	

	@GetMapping("/goBoardInside")
	public String goBoardInside(int boardNum, Model model,HttpSession session,String cafeId,int boardMenuNum,
			@RequestParam(value="num",required=false,defaultValue="0")int num,
			@RequestParam(value="next",required=false,defaultValue="0")int next,
			@RequestParam(value="preview",required=false,defaultValue="0")int preview) {

		//System.out.println("넘어온 보드 값 : "+boardNum);
		//System.out.println("넘어온 boardMenuNum 값 :"+boardMenuNum);
		//System.out.println("boardInside실행");
		//System.out.println("cafeId:"+cafeId);
		
		//댓글 갯수 세오기
		model.addAttribute("replyCount",replySer.getReplyCount(boardNum));

		//게시물 가져오기
		BoardDTO dto= ser.getUserBoard(boardNum,boardMenuNum,model,num,cafeId,next,preview);
		model.addAttribute("userBoard",dto);

		//카페유저 정보 가져오기
		model.addAttribute("cafeUserInfo",boardCafeSer.getCafeUserInfo(cafeId,dto.getUserId()));
		//위에있는거 2개 맵으로 가져와서 합쳐 줄 라고 했는데 clob이 문제가 생기네?

		//댓글 리스트 가져오기
		model.addAttribute("replyList",replySer.getReplyList(boardNum,cafeId));
		
		//세션 아이디 줘서 정보 가져오기
		model.addAttribute("sessionUser",boardCafeSer.getSessionUserInfo(cafeId,(String) session.getAttribute("loginId")));

		//조회수
		ser.hit(boardNum,num);
		//좋아요 되어있는지 여부
		ser.likeViewChk(boardNum, (String) session.getAttribute("loginId"), model);
		//인기글 리스트
		ser.topList(model,cafeId);
		//파일테이블리스트
		fs.getFileList(model);

		return "user/board/boardInside";
	}

	
//	댓글, 답글 step으로 식별하기
	@PostMapping("saveReply")
	public String saveReply(MultipartHttpServletRequest mul,
		@RequestParam(value="step",required=false,defaultValue="0")int step) {
		replySer.saveReply(mul,step);
		return "redirect:goBoardInside?boardNum="
				+mul.getParameter("boardNum")+"&num="+1+
				"&boardMenuNum="+mul.getParameter("boardMenuNum")+"&cafeId="+mul.getParameter("cafeId");
	//댓글 작성시 조회수 오르는거 방지
	}
	
	@GetMapping("likeChk")
	public String likeChk(int boardNum,String userId,Model model,HttpServletRequest res) {
		System.out.println("컨트롤러 : "+boardNum+" - "+userId);
		System.out.println("좋아요 클릭시 넘어오는 :"+res.getParameter("boardMenuNum"));
		System.out.println("좋아요 클릭시 넘어오는:"+res.getParameter("cafeId"));
		ser.likeChk(boardNum,userId,model);
		return "redirect:goBoardInside?boardNum="+boardNum+"&num="+1+
				"&boardMenuNum="+res.getParameter("boardMenuNum")+"&cafeId="+res.getParameter("cafeId");
	}
	
	@GetMapping("/goGradeBoardList")
	public String goGradeBoardList(String cafeId, MenuDTO menuDTO, Model model) {
		upSer.getGradeList(model);
		return "user/board/gradeBoardList";
	}
	

	
	@PostMapping(value="writeBoard", produces="application/json; charset=utf-8")
	@ResponseBody
	public int writeBoard(@RequestBody Map<String, Object> map) {
		int result = 0;
		CommonUtils commonUtils = new CommonUtils();
		BoardDTO boardDTO = new BoardDTO();
		
		try {
			boardDTO = (BoardDTO) commonUtils.setDTO(map, boardDTO);
			result = ser.insertBoard(boardDTO);
			System.out.println("작성완료시 : "+ boardDTO.getBoardNum());
			System.out.println("작성완료시 : "+boardDTO.getBoardMenuNum());
			System.out.println("작성완료시 : "+boardDTO.getBoardContent());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	@PostMapping(value="checkGradeBoard", produces="application/json; charset=utf-8")
	@ResponseBody
	public int checkBoard(@RequestBody Map<String, Object> map) {
		int result = 0;
		CommonUtils commonUtils = new CommonUtils();
		BoardExtendDTO boardExtDTO = new BoardExtendDTO();
		
		try {
			boardExtDTO = (BoardExtendDTO) commonUtils.setDTO(map, boardExtDTO);
			result = ser.checkGradeBoard(boardExtDTO);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	@GetMapping("deleteBoard")
	public String deleteBoard(int boardNum) {
		//보드넘으로 카페아이디 가져오기
		MenuDTO dto=ser.getBoardCafeId(boardNum);
		ser.deleteBoard(boardNum);
		return "user/board/boardList?cafeId="+dto.getCafeId()+
				"&boardMenuNum="+1;
	}//boardList로 어캐 넘기누?
	
	@PostMapping("deleteReply")
	public String deleteReply(HttpServletRequest res) {
//		System.out.println("댓글삭제시 넘어오는 :"+Integer.parseInt(res.getParameter("boardNum")));
//		System.out.println("댓글삭제시 넘어오는 :"+Integer.parseInt(res.getParameter("replyNum")));
//		System.out.println("댓글삭제시 넘어오는 :"+res.getParameter("boardMenuNum"));
//		System.out.println("댓글삭제시 넘어오는:"+res.getParameter("cafeId"));
		replySer.deleteReply(Integer.parseInt(res.getParameter("replyNum")));
		return "redirect:goBoardInside?boardNum="+res.getParameter("boardNum")+"&num="+1+
		"&boardMenuNum="+res.getParameter("boardMenuNum")+"&cafeId="+res.getParameter("cafeId");
	}
	
	@PostMapping("update_reply")
	public String updateReply(MultipartHttpServletRequest mul) {
		replySer.updateReply(mul);
		return "redirect:goBoardInside?boardNum="
			+mul.getParameter("boardNum")+"&num="+1+
			"&boardMenuNum="+mul.getParameter("boardMenuNum")+"&cafeId="+mul.getParameter("cafeId");
	}
	
}

