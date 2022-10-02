package com.itbank.navercafe.user.board.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itbank.navercafe.user.board.dto.BoardDTO;
import com.itbank.navercafe.user.board.service.BoardService;
import com.itbank.navercafe.user.cafe.dto.CafeDTO;
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
	
	@RequestMapping("/writeForm")
	public String writeForm(CafeDTO cafeDTO, Model model) {
		model.addAttribute("cafeDTO", cafeDTO);
		return "user/board/writeForm";
	}
	//전체목록인데 수영이형이랑 상의.
	@GetMapping("/goBoardList")	
	public String goBoardList(Model model, String cafeId, MenuDTO menuDTO, RedirectAttributes ra) throws Exception{
		int boardMenuNum = menuDTO.getBoardMenuNum();
		int boardMenuType = 1;
		String returnUrl = "user/board/boardList";
		
		System.out.println("보드 리스트 cafeId:"+cafeId);
		
		if(cafeId != null) {
			menuDTO.setCafeId(cafeId);
		}
		
		if(boardMenuNum > 0) {
			menuDTO = menuService.selectBoardMenu(boardMenuNum);
			boardMenuType = menuDTO.getBoardMenuType();
		}
		
		// 게시판 타입에 따라 다른 view 설정
		switch(boardMenuType) {
		case 4 :
			ra.addFlashAttribute("cafeId", cafeId);
			ra.addFlashAttribute("menuDTO", menuDTO);
			returnUrl = "redirect:/user/board/goGradeBoardList";
			break;
		case 5 :
			returnUrl = "user/board/memoBoardList";
			break;
		}
		
		model.addAttribute("boardList",ser.getBoardList(menuDTO));
		model.addAttribute("cafeId",cafeId);
		
		return returnUrl;
	}
	

	@GetMapping("/goBoardInside")
	public String goBoardInside(MenuDTO menuDTO, BoardDTO boardDTO, Model model,HttpSession session,String cafeId,
			@RequestParam(value="num",required=false,defaultValue="0")int num) {
		
		int boardNum = boardDTO.getBoardNum();
		int boardMenuNum = menuDTO.getBoardMenuNum();
		
		System.out.println("넘어온 보드 값 : "+boardNum);
		System.out.println("넘어온 boardMenuNum 값 :"+boardMenuNum);
		
		//System.out.println("boardInside실행");
		System.out.println("cafeId:"+cafeId);
		//댓글 갯수 세오기
		model.addAttribute("replyCount",replySer.getReplyCount(boardNum));
		//System.out.println("댓글 갯수 세오기 컷");
		//게시물 가져오기
		BoardDTO dto= ser.getUserBoard(boardNum,menuDTO,model,num);
		model.addAttribute("userBoard",dto);
		//System.out.println("게시물 가져오기 컷");
		//카페유저 정보 가져오기
		model.addAttribute("cafeUserInfo",boardCafeSer.getCafeUserInfo(cafeId,dto.getUserId()));
		//위에있는거 2개 맵으로 가져와서 합쳐 줄 라고 했는데 clob이 문제가 생기네?
		//System.out.println("카페 유저 정보 가져오기 컷");
		//댓글 리스트 가져오기
		model.addAttribute("replyList",replySer.getReplyList(boardNum));
		//System.out.println(replySer.getReplyList(boardNum));
		//System.out.println("댓글 리스트 가져오기 컷");
		
		
		//세션 아이디 줘서 정보 가져오기
		model.addAttribute("sessionUser",boardCafeSer.getSessionUserInfo(cafeId,(String) session.getAttribute("loginId")));
		//System.out.println("세션 아디 줘서 정보 가져오기 컷");
		//조회수
		ser.hit(boardNum,num);
		//System.out.println("조회수 컷");
		//좋아요 되어있는지 여부
		ser.likeViewChk(boardNum, (String) session.getAttribute("loginId"), model);
		//System.out.println("좋아요 되있는지 여부 이상부");
		//인기글 리스트
		ser.topList(model);
		//System.out.println("인기글 리스트 이상무");
		//파일테이블리스트
		ser.getFileList(model);
		//System.out.println("파일 테이블 리스트 이상무");

		return "user/board/boardInside";
	}

	
//	댓글, 답글 step으로 식별하기
	@PostMapping("saveReply")
	public String saveReply(MultipartHttpServletRequest mul,
		@RequestParam(value="step",required=false,defaultValue="0")int step) {
		replySer.saveReply(mul,step);
		return "redirect:goBoardInside?boardNum="
				+mul.getParameter("boardNum")+"&num="+1;
							//댓글 작성시 조회수 오르는거 방지
	}
	
	@GetMapping("likeChk")
	public String likeChk(int boardNum,String userId,Model model) {
		System.out.println("컨트롤러 : "+boardNum+" - "+userId);
		ser.likeChk(boardNum,userId,model);
		return "redirect:goBoardInside?boardNum="+boardNum+"&num="+1;
	}
	
	@GetMapping("/goGradeBoardList")
	public String goGradeBoardList(String cafeId, MenuDTO menuDTO, Model model) {
		upSer.getGradeList(model);
		return "user/board/gradeBoardList";
	}
	
	//@RequestParam(value="num",required=false,defaultValue="0")int num)
	//boardInside 방식참고해서 num 도 넣어주고
	@GetMapping("/goMemoBoardList")
	public String goMemoBoardList(String cafeId,Model model,HttpSession sesison){
		//메모 게시글들
		model.addAttribute("mapList",memoSer.getMemoList());
		//System.out.println("메모 게시물들 키 :"+memoSer.getMemoList());
		//메모 댓글들
		model.addAttribute("memoReplyList",memoSer.getReplyList());
		//System.out.println("메모 댓글 키 :"+memoSer.getReplyList());
		//세션 아이디 줘서 정보 가져오기
		model.addAttribute("sessionUser",cafeMemberService.getSessionUserInfo(cafeId,(String) sesison.getAttribute("loginId")));
		return "user/board/memoBoardList";
	}
	

	@PostMapping("saveMemoReply")
	public String saveReply(MultipartHttpServletRequest mul) {
		System.out.println("그룹번호:"+mul.getParameter("groupNum"));
		System.out.println("작성하고있는쉐끼:"+mul.getParameter("userId"));
		System.out.println("내용:"+mul.getParameter("memoReplyContent"));
		System.out.println("파일명:"+mul.getFile("replyImageName"));
		memoSer.saveMemoReply(mul);
		return "redirect:goMemoBoardList";
	}
	
	@PostMapping("memoSave")
	public String memoSave(HttpServletRequest res) {
		memoSer.memoSave(res);
		return "redirect:goMemoBoardList";
	}
}
