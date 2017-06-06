package com.diich.controller;

import com.diich.core.base.BaseController;
import com.diich.core.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/12 0012.
 */

@Controller
public class SearchController extends BaseController {

    @Autowired
    private SearchService searchSerice;

    @RequestMapping("s")
    @ResponseBody
    public Map searchText(String keyword,String type,Integer pageNum,Integer pageSize,String area_code,String gb_category_code, HttpServletResponse response) throws Exception{

        Map<String,Object> map = new HashMap<String,Object>();

        if(StringUtils.isEmpty(keyword)){
            map.put("keyword",null);
        }else{
            map.put("keyword",keyword);
        }
        if(StringUtils.isEmpty(type)){
            map.put("type",null);
        }else{
            map.put("type",type);
        }

        if(StringUtils.isEmpty(area_code)){
            map.put("area_code",null);
        }else{
            map.put("area_code",area_code);
        }
        if(StringUtils.isEmpty(gb_category_code)){
            map.put("gb_category_code",null);
        }else{
            map.put("gb_category_code",gb_category_code);
        }

        if(StringUtils.isEmpty(pageNum)){
            map.put("pageNum",1);
        }else{
            map.put("pageNum",pageNum);
        }
        if(StringUtils.isEmpty(pageSize)){
            map.put("pageSize",6);
        }else{
            map.put("pageSize",pageSize);
        }

        response.setHeader("Access-Control-Allow-Origin", "*");
        map.put("pageBegin",((Integer)map.get("pageNum")-1)*(Integer)map.get("pageSize"));

        return  searchSerice.searchText(map);

    }

    /**
     * 搜索下拉框展示页
     * @param keyword
     * @return
     */
    @RequestMapping("sk")
    @ResponseBody
    public List<String> searchKeyWord(String keyword,int size){
        List<String> ls  = searchSerice.searchText(keyword,size);
        return ls;
    }

    @RequestMapping("search")
    public String searchExplainText(String keyword, Model model, HttpServletResponse response){

        Map<String,Object> map = new HashMap<String,Object>();

        if(StringUtils.isEmpty(keyword)){
            map.put("keyword",null);
        }else{
            map.put("keyword",keyword);
        }
        map.put("type",null);
        map.put("area_code",null);
        map.put("gb_category_code",null);
        map.put("pageNum",1);
        map.put("pageSize",6);
        map.put("pageBegin",((Integer)map.get("pageNum")-1)*(Integer)map.get("pageSize"));

        model.addAttribute("map",searchSerice.searchText(map));

        // mav.addObject("map", map);
        //mav.addObject("CREATE_HTML", false);
        response.setHeader("Access-Control-Allow-Origin", "*");
        return "search1";
    }



}
