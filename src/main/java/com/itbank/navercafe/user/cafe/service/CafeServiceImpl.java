package com.itbank.navercafe.user.cafe.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itbank.navercafe.common.pagination.Pagination;
import com.itbank.navercafe.user.cafe.dto.CafeDTO;
import com.itbank.navercafe.user.cafe.mapper.CafeMapper;
import com.itbank.navercafe.user.menu.dto.MenuDTO;
import com.itbank.navercafe.user.menu.service.MenuService;

@Service
public class CafeServiceImpl implements CafeService {
	@Autowired
	private CafeMapper cafeMapper;
	
	@Autowired
	private MenuService menuService;
	
	@Override
	public List<CafeDTO> selectCafeList(CafeDTO cafeDTO, Pagination pagination) throws Exception {
		HashMap<Object, Object> map = new HashMap<>();
		
		try {
			map.put("cafeName", cafeDTO.getCafeName());
			map.put("startRow", pagination.getStartRow());
			map.put("endRow", pagination.getEndRow());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return cafeMapper.selectCafeList(map);
	}

	@Override
	public int selectCafeListTotalCount(CafeDTO cafeDTO) throws Exception {
		return cafeMapper.selectCafeListTotalCount(cafeDTO);
	}
	
	@Override
	public CafeDTO selectCafe(String cafeId) throws Exception {
		CafeDTO cafeDTO = null;
		List<MenuDTO> cafeMenuList = null;
		
		try {
			cafeDTO = cafeMapper.selectCafe(cafeId);
			cafeMenuList = menuService.selectBoardMenuList(cafeId);
			
			if(cafeMenuList != null) {
				cafeDTO.setCafeMenuList(cafeMenuList);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return cafeDTO;
	}

	@Override
	public int checkCafeId(String cafeId) throws Exception {
		return cafeMapper.checkCafeId(cafeId);
	}

	@Override
	public int InsertCafe(CafeDTO cafeDTO) throws Exception {
		return cafeMapper.InsertCafe(cafeDTO);
	}

	@Override
	public int getIconSeq() throws Exception {
		return cafeMapper.getIconSeq();
	}

	@Override
	public int getTitleSeq() throws Exception {
		return cafeMapper.getTitleSeq();
	}
}
