package com.diich.web.controller;

import com.diich.core.base.BaseController;
import com.diich.core.exception.ApplicationException;
import com.diich.core.model.Dictionary;
import com.diich.core.service.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/22.
 */
@Controller
@RequestMapping("dictionary")
public class DictionaryController extends BaseController<Dictionary> {

    @Autowired
    private DictionaryService dictionaryService;

    @RequestMapping("getDictionariesByType")
    @ResponseBody
    public Map<String, Object> getDictionariesByType(HttpServletRequest request,HttpServletResponse response) {
        String language = request.getParameter("language");
        Integer type = null;
        List<Dictionary> dictionaryList = null;

        if(language == null) {
            language = "chi";
        }

        try {
            type = Integer.parseInt(request.getParameter("type"));
        } catch (Exception e) {
            ApplicationException ae = new ApplicationException(ApplicationException.PARAM_ERROR);
            return ae.toMap();
        }

        try {
            dictionaryList = dictionaryService.getDictionaryListByType(type, language);
        } catch (Exception e) {
            ApplicationException ae = (ApplicationException) e;
            return ae.toMap();
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        return putDataToMap(dictionaryList);
    }

    @RequestMapping("getTextByTypeAndCode")
    @ResponseBody
    public Map<String, Object> getDictionaryByCode(HttpServletRequest request,HttpServletResponse response) {
        String code = request.getParameter("code");
        String language = request.getParameter("language");
        Integer type = null;
        String name = null;

        if(language == null) {
            language = "chi";
        }

        try {
            type = Integer.parseInt(request.getParameter("type"));
        } catch (Exception e) {
            ApplicationException ae = new ApplicationException(ApplicationException.PARAM_ERROR);
            return ae.toMap();
        }

        if(code == null || "".equals(code)) {
            ApplicationException ae = new ApplicationException(ApplicationException.PARAM_ERROR);
            return ae.toMap();
        }

        try {
            name = dictionaryService.getTextByTypeAndCode(type, code, language);
        } catch (Exception e) {
            ApplicationException ae = (ApplicationException) e;
            return ae.toMap();
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        return putDataToMap(name);
    }

    @RequestMapping("getAllDictionary")
    @ResponseBody
    public Map<String, Object> getDictionaryByCode(HttpServletResponse response) {
        List<Dictionary> dictionaryList = null;

        try {
            dictionaryList = dictionaryService.getAllDictionary();
        } catch (Exception e) {
            ApplicationException ae = (ApplicationException) e;
            return ae.toMap();
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        return putDataToMap(dictionaryList);
    }
}
