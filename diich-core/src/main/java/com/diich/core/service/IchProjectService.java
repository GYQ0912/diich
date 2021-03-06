package com.diich.core.service;

import com.baomidou.mybatisplus.plugins.Page;
import com.diich.core.base.BaseModel;
import com.diich.core.model.IchMaster;
import com.diich.core.model.IchProject;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/10.
 */
public interface IchProjectService {

    IchProject getIchProject(String id)throws Exception;

    IchProject saveIchProject(IchProject ichProject) throws Exception;

    List<IchProject> getIchProjectList(Page<IchProject> page) throws Exception;

    Page<IchProject> getIchProjectPage(Map<String, Object>  params)throws Exception;

    IchProject getIchProjectById(Long id) throws Exception;

    String preview(Long id) throws Exception;

    String buildHTML(String templateName, IchProject ichProject,String fileName) throws Exception;

    //项目创建 搜索使用  最多显示 5 条数据
    List<Map> getIchProjectByName(Map<String,Object> map) throws Exception;
}
