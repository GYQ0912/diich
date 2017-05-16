package com.diich.web.controller;

import com.diich.core.base.BaseController;
import com.diich.core.model.IchProject;
import com.diich.core.service.ICHItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/9.
 */
@Controller
@RequestMapping("ichProject")
public class ICHItemController extends BaseController<IchProject> {

    @Autowired
    private ICHItemService ichItemService;

    @RequestMapping("getIchProject")
    @ResponseBody
    public Map<String, Object> getICHItem(HttpServletRequest request) {
        String ichItemId = request.getParameter("params");

        Map<String, Object> result = ichItemService.getICHItem(ichItemId);

        return result;
    }

    @RequestMapping("getIchProjectList")
    @ResponseBody
    public Map<String, Object> getICHItemList(HttpServletRequest request) {
        String params = request.getParameter("params");

        Map<String, Object> result = ichItemService.getICHItemList(params);

        return result;
    }

    @RequestMapping("saveIchProject")
    @ResponseBody
    public Map<String, Object> saveICHItem(HttpServletRequest request) {
        String ichItem = request.getParameter("params");

        Map<String, Object> result = ichItemService.saveICHItem(ichItem);

        return result;
    }
}
