package com.jdbc.springweb;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.jdbc.dao.BoardDAO3;
import com.jdbc.dto.BoardDTO;
import com.jdbc.util.MyUtil;

@Controller
public class BoardController {

	@Autowired
	@Qualifier("boardDAO3")
	BoardDAO3 dao;
	
	@Autowired
	MyUtil myUtil;
	
	@RequestMapping(value="/", method=RequestMethod.GET)
	public String mode() {
		return "index";
	}
	
	/*
	@RequestMapping(value="/created.action", method= {ReqeustMethod.GET, RequestMethod.POST})
	public String created(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		return "bbs/created";
	}
	*/
	
	/* 위에 한것 과 똑같은 것 */	
	@RequestMapping(value="/created.action")
	public ModelAndView created() {
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName("bbs/created");
		
		return mav;
	}
	
	
	@RequestMapping(value="/created_ok.action", method= {RequestMethod.GET, RequestMethod.POST})
	public String created(BoardDTO dto, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		int maxNum = dao.getMaxNum();
		dto.setNum(maxNum + 1);
		dto.setIpAddr(request.getRemoteAddr());
		
		dao.insertData(dto);

		return "redirect:/list.action";
		
	}
	
	@RequestMapping(value="/list.action", method= {RequestMethod.GET, RequestMethod.POST})
	public String list(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		String cp = request.getContextPath();
		
		String pageNum = request.getParameter("pageNum");
		int currentPage = 1;
		
		if(pageNum != null)
			currentPage = Integer.parseInt(pageNum);
		
		String searchKey = request.getParameter("searchKey");
		String searchValue = request.getParameter("searchValue");
		
		if(searchKey == null){
			
			searchKey = "subject";
			searchValue = "";
			
		}else{
			
			if(request.getMethod().equalsIgnoreCase("GET"))
				searchValue =
					URLDecoder.decode(searchValue, "UTF-8");
			
		}
		
		//전체데이터갯수
		int dataCount = dao.getDataCount(searchKey, searchValue);
		
		//전체페이지수
		int numPerPage = 10;
		int totalPage = myUtil.getPageCount(numPerPage, dataCount);
		
		if(currentPage > totalPage)
			currentPage = totalPage;
		
		int start = (currentPage-1)*numPerPage+1;
		int end = currentPage*numPerPage;
		
		List<BoardDTO> lists =
			dao.getList(start, end, searchKey, searchValue);
		
		//페이징 처리
		String param = "";
		if(!searchValue.equals("")){
			param = "searchKey=" + searchKey;
			param+= "&searchValue=" 
				+ URLEncoder.encode(searchValue, "UTF-8");
		}
		
		String listUrl = cp + "/list.action";
		if(!param.equals("")){
			listUrl = listUrl + "?" + param;				
		}
		
		String pageIndexList =
			myUtil.pageIndexList(currentPage, totalPage, listUrl);
		
		//글보기 주소 정리
		String articleUrl = 
			cp + "/article.action?pageNum=" + currentPage;
			
		if(!param.equals(""))
			articleUrl = articleUrl + "&" + param;
		
		//포워딩 될 페이지에 데이터를 넘긴다
		request.setAttribute("lists", lists);
		request.setAttribute("pageIndexList",pageIndexList);
		request.setAttribute("dataCount",dataCount);
		request.setAttribute("articleUrl",articleUrl);
		
		return "bbs/list";

	}
	
	
	@RequestMapping(value="/article.action", method= {RequestMethod.GET, RequestMethod.POST})
	public String article(HttpServletRequest request, HttpServletResponse response) throws Exception{

		int num = Integer.parseInt(request.getParameter("num"));
		String pageNum = request.getParameter("pageNum");
		
		String searchKey = request.getParameter("searchKey");
		String searchValue = request.getParameter("searchValue");
		
		if(searchKey != null)
			searchValue = URLDecoder.decode(searchValue, "UTF-8");
		
		//조회수 증가
		dao.updateHitCount(num);
		
		BoardDTO dto = dao.getReadData(num);

		int lineSu = dto.getContent().split("\n").length;
		
		dto.setContent(dto.getContent().replaceAll("\n", "<br/>"));
		
		String param = "pageNum=" + pageNum;
		if(searchKey!=null){
			param += "&searchKey=" + searchKey;
			param += "&searchValue=" 
				+ URLEncoder.encode(searchValue, "UTF-8");
		}
		
		request.setAttribute("dto", dto);
		request.setAttribute("params",param);
		request.setAttribute("lineSu",lineSu);
		request.setAttribute("pageNum",pageNum);			
		
		
		return "bbs/article";
		
		/*
		ModelAndView mav = new ModelAndView();
		
		mav.setViewName("bbs/article");
		
		mav.addObject("dto", dto);
		mav.addObject("params", param);
		mav.addObject("lineSu", lineSu);
		mav.addObject("pageNum", pageNum);
		
		return mav;
		*/
	}
	
	@RequestMapping(value="/updated.action", method= {RequestMethod.GET, RequestMethod.POST})
	public String updated(HttpServletRequest request, HttpServletResponse response) throws Exception{

		int num = Integer.parseInt(request.getParameter("num"));
		String pageNum = request.getParameter("pageNum");
		
		BoardDTO dto = dao.getReadData(num);
		
		if(dto == null){
			return "redirect:/list.action";
		}
		
		request.setAttribute("dto", dto);
		request.setAttribute("pageNum", pageNum);
		
		return "bbs/updated";
	
	}
	
	@RequestMapping(value="/updated_ok.action", method= {RequestMethod.GET, RequestMethod.POST})
	public String updated_ok(BoardDTO dto, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		String pageNum = request.getParameter("pageNum");
		
		dao.updateData(dto);
		
		return "redirect:/list.action?pageNum=" + pageNum;
		
	}
	
	@RequestMapping(value="/deleted.action", method= {RequestMethod.GET, RequestMethod.POST})
	public String deleted(HttpServletRequest request, HttpServletResponse response) throws Exception{
	
		String pageNum = request.getParameter("pageNum");
		int num =Integer.parseInt(request.getParameter("num"));
		
		dao.deleteData(num);
		
		return "redirect:/list.action?pageNum=" + pageNum;
		
	}
	
}
