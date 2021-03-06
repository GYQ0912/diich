package com.diich.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.baomidou.mybatisplus.toolkit.StringUtils;
import com.diich.core.base.BaseModel;
import com.diich.core.base.BaseService;
import com.diich.core.exception.ApplicationException;
import com.diich.core.model.*;
import com.diich.core.service.*;
import com.diich.core.util.BuildHTMLEngine;
import com.diich.core.util.FileType;
import com.diich.core.util.PropertiesUtil;
import com.diich.mapper.*;
import org.apache.commons.collections.map.HashedMap;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by Administrator on 2017/5/9.
 */
@Service("ichProjectService")
@Transactional
public class IchProjectServiceImpl extends BaseService<IchProject> implements IchProjectService {

    @Autowired
    private IchProjectMapper ichProjectMapper;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AttributeMapper attributeMapper;
    @Autowired
    private ContentFragmentMapper contentFragmentMapper;
    @Autowired
    private ContentFragmentResourceMapper contentFragmentResourceMapper;
    @Autowired
    private IchCategoryService ichCategoryService;
    @Autowired
    private IchMasterMapper ichMasterMapper;
    @Autowired
    private WorksMapper worksMapper;
    @Autowired
    private IchMasterService ichMasterService;
    @Autowired
    private WorksService worksService;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private  ResourceMapper resourceMapper;
    @Autowired
    private VersionService versionService;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 根据id获取项目信息
     * @param id
     * @return
     * @throws Exception
     */
    public IchProject getIchProject(String id) throws Exception {


        IchProject ichProject = null;

        try {
            ichProject = ichProjectMapper.selectByPrimaryKey(Long.parseLong(id));


            if(ichProject != null) {
                 // User user = userMapper.selectByPrimaryKey(ichProject.getLastEditorId());
                //获取传承人列表
                List<IchMaster> ichMasterList = ichMasterService.getIchMasterByIchProjectId(Long.parseLong(id));

                ichProject.setIchMasterList(ichMasterList);
                //作品列表
                List<Works> worksList =worksService.getWorksByIchProjectId(Long.parseLong(id));
                ichProject.setWorksList(worksList);
            }

            //获取项目的field
            List<ContentFragment> contentFragmentList = getContentFragmentListByProjectId(ichProject);
            ichProject.setContentFragmentList(contentFragmentList);
            getIchproject(ichProject);//返回前端需要的特定数据
            //根据id和targetType查询中间表看是否有对应的版本
            Version version = null;
            if("chi".equals(ichProject.getLang())){
                version = versionService.getVersionByLangIdAndTargetType(Long.valueOf(id), null, 0);
            }
            if("eng".equals(ichProject.getLang())){
                version = versionService.getVersionByLangIdAndTargetType(null, Long.valueOf(id),0);
            }
            ichProject.setVersion(version);
        } catch (Exception e) {
            throw new ApplicationException(ApplicationException.INNER_ERROR);
        }

        return  ichProject;
    }

    /**
     * 根据条件查询项目列表信息
     * @param params
     * @return
     * @throws Exception
     */
    public Page<IchProject> getIchProjectPage(Map<String, Object>  params) throws Exception {
        Integer current = 1;
        Integer pageSize = 10;

        if(params != null && params.containsKey("current") && params.containsKey("pageSize")) {
            current = (Integer) params.get("current");
            pageSize = (Integer) params.get("pageSize");
        }

        Page<IchProject> page = new Page<IchProject>(current, pageSize);
        page.setCondition(params);

        List<IchProject> ichProjectList = getIchProjectList(page);

        page.setRecords(ichProjectList);

        return page;
    }

    /**
     * 获取项目列表信息
     * @param page
     * @return
     * @throws Exception
     */
    public List<IchProject> getIchProjectList(Page<IchProject> page) throws Exception{

        List<IchProject> ichProjectList = null;
        try {
            Map<String, Object> condition = page.getCondition();
            if(condition == null){
                condition = new HashMap<>();
            }
            ichProjectList = ichProjectMapper.selectIchProjectList(page,condition);

            for (IchProject ichProject:ichProjectList) {

                //获取传承人列表
                List<IchMaster> ichMasterList = ichMasterService.getIchMasterByIchProjectId(ichProject.getId());

                ichProject.setIchMasterList(ichMasterList);
                //代表作品列表
                List<Works> worksList =worksService.getWorksByIchProjectId(ichProject.getId());

                ichProject.setWorksList(worksList);
                //根据id和targetType查询中间表看是否有对应的版本
                Version version = null;
                if("chi".equals(ichProject.getLang())){
                    version = versionService.getVersionByLangIdAndTargetType(ichProject.getId(), null, 0);
                }
                if("eng".equals(ichProject.getLang())){
                    version = versionService.getVersionByLangIdAndTargetType(null, ichProject.getId(),0);
                }
                ichProject.setVersion(version);

                //获取项目的field
                List<ContentFragment> contentFragmentList = getContentFragmentListByProjectId(ichProject);
                ichProject.setContentFragmentList(contentFragmentList);
                getIchproject(ichProject);//返回前端需要的特定数据
            }
            return ichProjectList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(ApplicationException.INNER_ERROR);

        }
    }


    /**
     * 保存或更新项目信息
     * @param ichProject
     * @throws Exception
     */
    @Transactional
    public IchProject saveIchProject(IchProject ichProject) throws Exception {
        TransactionStatus transactionStatus = getTransactionStatus();
        if(ichProject.getStatus() != null && ichProject.getStatus() == 2){
            checkIchProject(ichProject,2);//校验项目 2代表保存 3代表提交
        }
        if(ichProject.getStatus() != null && ichProject.getStatus() == 3){
            checkIchProject(ichProject,3);//校验项目 2代表保存 3代表提交
        }
        try {
            saveProject(ichProject);//保存项目
            commit(transactionStatus);
        } catch (Exception e) {
            rollback(transactionStatus);
            throw new ApplicationException(ApplicationException.INNER_ERROR);
        }
        return ichProject;
    }

    private void checkIchProject(IchProject ichProject,Integer status) throws Exception{
        //根据项目名称查询项目是否存在
        checkProjectByName(ichProject);
        //查询自定义字段是否在本项目存在
        checkAttribute(ichProject,status);
    }


    private IchProject saveProject(IchProject ichProject) throws Exception{
        if(StringUtils.isEmpty(ichProject.getLang())){
            ichProject.setLang("chi");
        }
        if(ichProject.getId() == null) {
            long proID = IdWorker.getId();
            ichProject.setId(proID);
            ichProject.setUri(proID +".html");
            ichProjectMapper.insertSelective(ichProject);
            List<ContentFragment> ls = ichProject.getContentFragmentList();
            if(ls !=null && ls.size()>0){
                for(int i=0;i<ls.size();i++){
                    ContentFragment c = ls.get(i);
                    saveContentFragment(c,proID);
                }
            }
        } else {
            ichProject.setUri(ichProject.getId() +".html");
            //判断当前用户是否为同一用户 如果不是同一用户将项目另存
            ichProjectMapper.updateByPrimaryKeySelective(ichProject);
            List<ContentFragment> contentFragmentList = ichProject.getContentFragmentList();
            if (contentFragmentList !=null && contentFragmentList.size()>0){
                for (ContentFragment contentFragment: contentFragmentList) {
                    if(contentFragment.getId()==null){
                        //新增内容片断
                        saveContentFragment(contentFragment,ichProject.getId());
                    }else{//更新内容片断
                        contentFragmentMapper.updateByPrimaryKeySelective(contentFragment);
                        if(contentFragment.getAttribute().getTargetType() == 10){//更新自定义属性的名称
                            attributeMapper.updateByPrimaryKeySelective(contentFragment.getAttribute());
                        }
                        List<Resource> resourceList = contentFragment.getResourceList();
                        if(resourceList != null && resourceList.size()>0){
                            saveResource(resourceList,contentFragment.getId());
                        }
                    }
                }
            }
        }
        List<Works> worksList = ichProject.getWorksList();
        if(worksList !=null && worksList.size()>0){
            for (Works works: worksList) {
                works.setIchProjectId(ichProject.getId());
                worksService.saveWorks(works);
            }
        }
        return ichProject;
    }

    /**
     * 如果修改人不同就另存版本
     * @param ichProject
     * @return
     * @throws Exception
     */
    private IchProject updateProject(IchProject ichProject) throws Exception{
        IchProject selectProject = ichProjectMapper.selectByPrimaryKey(ichProject.getId());
        if(selectProject.getLastEditorId() != ichProject.getLastEditorId()){
            long id = IdWorker.getId();
            //TODO
        }
        return ichProject;
    }

    /**
     *  根据传承人或者作品信息查询项目 status 不做限制
     * @param id
     * @return
     */
    public IchProject getIchProjectById(Long id) throws Exception {
        //所属项目
        IchProject ichProject = ichProjectMapper.selectByIchProjectById(id);
        if (ichProject != null) {
            //内容片断列表
            List<ContentFragment> contentFragmentList = getContentFragmentListByProjectId(ichProject);
            ichProject.setContentFragmentList(contentFragmentList);
        }
        return ichProject;
    }

    /**
     * 预览
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public String preview(Long id) throws Exception {
        IchProject ichProject = getIchProjectById(id);
        getIchproject(ichProject);//返回前端需要的特定数据
        String fileName = PropertiesUtil.getString("freemarker.projectfilepath")+"/"+ichProject.getId().toString();
        String uri = buildHTML("pro.ftl", ichProject, fileName);
        return uri;
    }

    /**
     * 生成静态页面
     * @param templateName
     * @param ichProject
     * @param fileName
     * @return
     * @throws Exception
     */
    @Override
    public String buildHTML(String templateName, IchProject ichProject, String fileName) throws Exception {
        String uri = BuildHTMLEngine.buildHTML(templateName, ichProject, fileName);
        return uri;
    }

    @Override
    public List<Map> getIchProjectByName(Map<String,Object> map) throws Exception {

       List<Map> ls = ichProjectMapper.selectIchProjectByName(map);

       List<Map> result = new ArrayList<Map>();

       for(int i=0;i<ls.size();i++){
           Map<String,Object> resultMap = new HashMap<String,Object>();

           Long id= (Long)ls.get(i).get("id");
           resultMap.put("id",id);
           resultMap.put("name",ls.get(i).get("name"));
           String lang = ls.get(i).get("lang").toString();


            //获取项目分类
           Long categoryID = (Long)ls.get(i).get("ichCategoryId");
           if(categoryID != null){
               IchCategory category = ichCategoryService.getCategoryById(categoryID);
               if(category != null){
                   resultMap.put("category",category.getName());
               }
           }

           List<ContentFragment> cs = contentFragmentMapper.selectByProjectId(id);

           for(int j=0;j<cs.size();j++){
               //获取项目名
               ContentFragment c= cs.get(j);
               Attribute a = attributeMapper.selectByPrimaryKey(c.getAttributeId());
              /*  if(c.getAttributeId()==4){
                    resultMap.put("name",c.getContent());
                }*/
                //获取项目题图
                if(c.getAttributeId()==1){

                        Resource r = resourceMapper.selectByContentFramentID(c.getId());
                        resultMap.put("img",r.getUri());

                }
                //获取区域地址
               if(a!=null && a.getDataType()==101){
                   String content = c.getContent();
                   if(content!= null){
                       String dis =  dictionaryService.getTextByTypeAndCode(a.getDataType(),c.getContent(),lang);
                       resultMap.put("dis",dis);
                   }
               }
           }
           result.add(resultMap);
       }

        return result;
    }

    private List<ContentFragment> getContentFragmentListByProjectId(IchProject ichProject) throws Exception {
        ContentFragment c = new ContentFragment();
        c.setTargetId(ichProject.getId());
        c.setTargetType(0);//标示项目
        List<ContentFragment> ls =  contentFragmentMapper.selectByTargetIdAndType(c);
        for(int i=0;i<ls.size();i++) {
            Attribute attribute = attributeMapper.selectByPrimaryKey(ls.get(i).getAttributeId());
            ls.get(i).setAttribute(attribute);
            Long contentFragmentId = ls.get(i).getId();
            List<ContentFragmentResource> contentFragmentResourceList = contentFragmentResourceMapper.selectByContentFragmentId(contentFragmentId);
            List<Resource> resourceList = new ArrayList<>();
                for (ContentFragmentResource contentFragmentResource : contentFragmentResourceList) {
                    Resource resource = resourceMapper.selectByPrimaryKey(contentFragmentResource.getResourceId());
                    if (resource != null) {
                        resource.setResOrder(contentFragmentResource.getResOrder());
                        resourceList.add(resource);
                    }
                }
                ls.get(i).setResourceList(resourceList);
            }
        return ls;
    }

    /**
     * 获取前端所需要的资源数据
     * @param ichProject
     * @return
     */
    private IchProject getIchproject(IchProject ichProject) throws Exception{

        //list用于向前端传输按模块划分的图片资源   用于显示在详情页特定模块的资源
        List<Map<String,Object>> list = new ArrayList<>();
        //allMap 所有去除重复图片和视频后的资源容器  用于显示在详情页得查看所有图片
        Map<String,Object> allMap = new HashedMap();
        Map<String,Object> headMap = new HashedMap();//放公共数据
        Set<Resource> imgdist = new HashSet<>();//去重后的所有图片集合
        Set<Resource> videosdist = new HashSet<>();//去重后的所有视频集合
        List<ContentFragment> ContentFragmentList = ichProject.getContentFragmentList();
        for (ContentFragment contentFragment:ContentFragmentList) {
            Map<String, Object> map = new HashMap<>();//存放每个模块的图片和视频
            List<Resource> img = new ArrayList<>();//图片资源文件的集合
            List<Resource> video = new ArrayList<>();//视频资源文件的集合
            Long contentFragmentId = contentFragment.getId();
            List<Resource> resourceList = contentFragment.getResourceList();
            if(resourceList !=null && resourceList.size()>0){
                for (Resource resource:resourceList) {
                    if (resource.getType() == 0) {
                        img.add(resource);
                        if(contentFragment.getAttributeId()!=112){//头图不放到所有图片中
                            imgdist.addAll(img);
                        }else{
                            headMap.put("headImage",img);
                        }
                    }
                    if (resource.getType() == 1) {
                        video.add(resource);
                        videosdist.addAll(video);
                    }
                }
            }
            map.put("contentFragmentId", contentFragmentId);
            map.put("imgs", img);
            map.put("videos", video);
            if("chi".equals(ichProject.getLang())){
                if(contentFragment.getAttributeId()==4){
                    headMap.put("projectName",contentFragment.getContent());
                }
            }
            if("eng".equals(ichProject.getLang())){
                if(contentFragment.getAttributeId()==5){
                    headMap.put("projectName",contentFragment.getContent());
                }
            }

            list.add(map);
        }
        allMap.put("imgs",imgdist);
        allMap.put("videos",videosdist);
        headMap.put("lang",ichProject.getLang());
        ichProject.setJson(JSONObject.toJSON(list).toString());
        ichProject.setJsonAll(JSONObject.toJSON(allMap).toString());
        ichProject.setJsonHead(JSONObject.toJSON(headMap).toString());
        return ichProject;
    }
    /**
     * 增加contentFragment
     * @param c
     */
    private void saveContentFragment(ContentFragment c,Long proID) throws Exception{
        Long attributeId = c.getAttributeId();
        Attribute attribute = c.getAttribute();
        if(attributeId == 0 || attributeId == null){
            attributeId = IdWorker.getId();
            attribute.setId(attributeId);
            attribute.setStatus(0);
            attribute.setTargetType(10);
            attribute.setIchCategoryId(proID);
            attribute.setIsOpen(1);
            attribute.setPriority(99);
            attributeMapper.insertSelective(attribute);
        }
        c.setAttributeId(attributeId);
        c.setId(IdWorker.getId());
        c.setTargetId(proID);
        c.setTargetType(0);
        c.setStatus(0);
        contentFragmentMapper.insertSelective(c);
        List<Resource> resourceList = c.getResourceList();
        if(resourceList != null && resourceList.size()>0){
            saveResource(resourceList,c.getId());
        }
    }

    /**
     * 根据项目名称查询项目是否存在
     * @param ichProject
     * @throws Exception
     */
    private void checkProjectByName(IchProject ichProject) throws Exception{
        List<ContentFragment> contentFragmentList = ichProject.getContentFragmentList();
        List<ContentFragment> contentFragments = null;
        for (ContentFragment contentFragment:contentFragmentList) {
            if(contentFragment.getAttributeId() !=4){
                continue;
            }else{
                contentFragments = contentFragmentMapper.selectByAttIdAndContent(contentFragment);
                break;
            }
        }
        if(ichProject.getId() == null){
            if(contentFragments.size()>0){
                throw new ApplicationException(ApplicationException.PARAM_ERROR);
            }
        }else{
            if(contentFragments.size()>0){
                Long targetId = contentFragments.get(0).getTargetId();
                if(ichProject.getId()!=targetId){
                    throw new ApplicationException(ApplicationException.PARAM_ERROR);
                }
            }
        }
    }

    /**
     *  检查属性是否符合条件
     * @param ichProject
     */
    private void checkAttribute(IchProject ichProject,Integer status) throws Exception{
        List<ContentFragment> contentFragmentList = ichProject.getContentFragmentList();
        List<String> list = new ArrayList<>();
        for (ContentFragment contentFragment:contentFragmentList) {
            if (contentFragment.getAttributeId() !=0) {
                continue;
            }
            checkDefineField(contentFragment,list);//校验自定义字段
            list.add(contentFragment.getAttribute().getCnName());
        }
        if(status == 2){//保存
            for (ContentFragment contentFragment:contentFragmentList) {
                if(contentFragment.getAttributeId() !=0){
                    //判断字段是否符合条件
                    checkSaveField(contentFragment);
                }
            }
        }
        if(status == 3){//提交
            List<Attribute> attributeList = null;
            try{
                //根据targetType获取属性列表
                Attribute attribute = new Attribute();
                attribute.setTargetType(0);
                attributeList = attributeMapper.selectAttrListByCatIdAndTarType(attribute);
            }catch (Exception e){
                e.printStackTrace();
                throw new ApplicationException(ApplicationException.INNER_ERROR);
            }

            for (Attribute attr :attributeList) {
                checkSubmitField(attr,contentFragmentList);
            }
        }
    }
    /**
     * 检查自定义字段是否重名
     * @param contentFragment
     * @throws Exception
     */
    private void checkDefineField(ContentFragment contentFragment,List<String> list) throws Exception {

        Map map = new HashMap();
        Attribute attribute = contentFragment.getAttribute();
        String cnName = attribute.getCnName();
        //判断是否重名
        if(list.contains(cnName)){
            throw new ApplicationException(ApplicationException.PARAM_ERROR);
        }
        //根据属性名称和targetType查询attribute表中是否存在该属性

        map.put("cnName",cnName);
        map.put("targetType",0);
        List<Attribute> attributeList = null;
        try{
            attributeList = attributeMapper.selectAttrByNameAndTargetType(map);
        }catch (Exception e){
            throw new ApplicationException(ApplicationException.INNER_ERROR);
        }

        if(attributeList.size()>0){
            throw new ApplicationException(ApplicationException.PARAM_ERROR,"自定义属性名称已存在");
        }
    }

    /**
     * 保存资源文件 项目  传承人  作品
     * @param resList
     * @param cId
     */
    public void saveResource(List<Resource> resList,Long cId) throws Exception{
        for(int i = 0; i<resList.size();i++){
            Resource resource = resList.get(i);
            Long resourceId = resource.getId();
            if(resourceId == null){
                resourceId = IdWorker.getId();
                resource.setId(resourceId);
                resource.setStatus(0);
                resource.setType(resource.getType());
                //判断上传的文件类型 0图片 1 视频 2 音频
                String sType = FileType.fileType(resource.getUri());
                if("图片".equals(sType)){
                    resource.setType(0);
                }
                if("视频".equals(sType)){
                    resource.setType(1);
                }
                if("音乐".equals(sType)){
                    resource.setType(2);
                }
                if("文档".equals(sType)){
                    resource.setType(3);
                }
                //保存resource
                resourceMapper.insertSelective(resource);
                ContentFragmentResource cfr = new ContentFragmentResource();
                cfr.setId(IdWorker.getId());
                cfr.setContentFragmentId(cId);
                cfr.setResourceId(resourceId);
                if(resource.getResOrder() !=null && !"".equals(resource.getResOrder())){
                    cfr.setResOrder(resource.getResOrder());
                }else{
                    cfr.setResOrder(i+1);
                }
                cfr.setStatus(0);
                //保存中间表
                contentFragmentResourceMapper.insertSelective(cfr);
            }else{
                //更新资源文件
                resourceMapper.updateByPrimaryKeySelective(resource);

            }
        }

    }

    /**
     * 提交时对字段校验 项目  传承人  作品
     * @param attribute
     * @param contentFragmentList
     * @throws Exception
     */
    public void checkSubmitField(Attribute attribute, List<ContentFragment> contentFragmentList) throws Exception{

        int count = 0;
        for (ContentFragment contentFragment:contentFragmentList) {
            if(contentFragment.getAttributeId() == 0 || contentFragment.getAttributeId() == null){
                continue;
            }
            if(attribute.getMaxLength() != null){
                if(contentFragment.getContent() !=null && contentFragment.getContent().length() > attribute.getMaxLength()){
                    throw  new ApplicationException(ApplicationException.PARAM_ERROR,attribute.getCnName().toString()+" 字段不符合要求");
                }
            }
            if(attribute.getMinLength() > 0){//检查必填项是否已填
                if(contentFragment.getAttributeId() != attribute.getId()){
                    continue;
                }
                String content = contentFragment.getContent();
                count ++;
                if(content == null || (content.length() < attribute.getMinLength())){
                    throw new ApplicationException(ApplicationException.PARAM_ERROR,attribute.getCnName().toString()+" 字段不符合要求");
                }
            }
            if(count == 0){
                throw new ApplicationException(ApplicationException.PARAM_ERROR, attribute.getCnName().toString()+" 字段不符合要求");
            }

        }
    }

    /**
     * 保存时对字段的校验  项目  传承人  作品
     * @param contentFragment
     * @throws Exception
     */
    public void checkSaveField(ContentFragment contentFragment) throws Exception {
        Long attributeId = contentFragment.getAttributeId();
        if(attributeId != null){
            Attribute attribute = null;
            try{
                attribute = attributeMapper.selectByPrimaryKey(attributeId);
            }catch (Exception e){
                e.printStackTrace();
                throw new ApplicationException(ApplicationException.INNER_ERROR);
            }

            if(attribute.getMinLength() != null){
                String content = contentFragment.getContent();
                if(content == null || (content.length() < attribute.getMinLength())){
                    throw new ApplicationException(ApplicationException.PARAM_ERROR, attribute.getCnName().toString()+" 字段不符合要求");
                }
            }
            if(attribute.getMaxLength() != null){
                String content = contentFragment.getContent();
                if(content != null && content.length() > attribute.getMaxLength()){
                    throw new ApplicationException(ApplicationException.PARAM_ERROR, attribute.getCnName().toString()+" 字段不符合要求");
                }
            }
        }
    }

}
