package com.diich.web.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.diich.core.base.BaseController;
import com.diich.core.model.ICHMaster;
import com.diich.core.support.HttpCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2017/5/9.
 */
@Controller
@RequestMapping("ichMaster")
public class ICHMasterController extends BaseController<ICHMaster>{

    @RequestMapping("selectICHMaster")
    @ResponseBody
    public ResponseEntity<ModelMap> selectICHMaster(HttpServletRequest request) {
        String ichMasterId = request.getParameter("ichMasterId");
        ModelMap map = new ModelMap();

        if(ichMasterId == null || "".equals(ichMasterId)) {
            return setModelMap(map, HttpCode.BAD_REQUEST);
        }

        ICHMaster ichMaster = null;

        return setSuccessModelMap(map,ichMaster);
    }

    @RequestMapping("selectICHMasterList")
    @ResponseBody
    public ResponseEntity<ModelMap> selectICHMasterList(HttpServletRequest request) {
        String ichItemId = request.getParameter("ichItemId");
        ModelMap map = new ModelMap();

        if(ichItemId == null || "".equals(ichItemId)) {
            return setModelMap(map, HttpCode.BAD_REQUEST);
        }

        Page<ICHMaster> page = null;

        return setSuccessModelMap(map, page);
    }
}
