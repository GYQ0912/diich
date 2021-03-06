package com.diich.service.impl;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.diich.core.base.BaseService;
import com.diich.core.exception.ApplicationException;
import com.diich.core.model.*;
import com.diich.core.service.DictionaryService;
import com.diich.core.service.IchMasterService;
import com.diich.core.service.IchProjectService;
import com.diich.core.service.WorksService;
import com.diich.core.util.BuildHTMLEngine;
import com.diich.core.util.FileType;
import com.diich.core.util.PropertiesUtil;
import com.diich.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/19.
 */
@Service("worksService")
public class WorksServiceImpl extends BaseService<Works> implements WorksService{

    @Autowired
    private WorksMapper worksMapper;

    @Autowired
    private IchProjectMapper ichProjectMapper;
    @Autowired
    private IchMasterMapper ichMasterMapper;
    @Autowired
    private ContentFragmentMapper contentFragmentMapper;
    @Autowired
    private AttributeMapper attributeMapper;
    @Autowired
    private ContentFragmentResourceMapper contentFragmentResourceMapper;
    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    private IchProjectService ichProjectService;
    @Autowired
    private IchMasterService ichMasterService;
    @Autowired
    private DictionaryService dictionaryService;
    /**
     * 根据id查询作品信息
     * @param id
     * @return
     * @throws Exception
     */
    public Works getWorks(String id) throws Exception{

        Works works = null;
        try{
           works = worksMapper.selectByPrimaryKey(Long.parseLong(id));
            if(works !=null){
                //获取所属项目信息
                IchProject ichProject = ichProjectService.getIchProjectById(works.getIchProjectId());
                works.setIchProject(ichProject);
                //获取传承人信息
                IchMaster ichMaster = ichMasterService.getIchMasterByWorks(works);
                works.setIchMaster(ichMaster);
            }
            //获取内容片断
            List<ContentFragment> contentFragmentList = getContentFragmentListByWorksId(works);
            works.setContentFragmentList(contentFragmentList);
        }catch(Exception e){
            throw new ApplicationException(ApplicationException.INNER_ERROR);
        }
        return works;
    }

    /**
     * 根据条件查询作品的列表信息
     * @param params
     * @return
     * @throws Exception
     */
    @Override
    public Page<Works> getWorksPage(Map<String, Object> params) throws Exception {

        Integer current = 1;
        Integer pageSize = 10;

        if(params != null && params.containsKey("current") && params.containsKey("pageSize")) {
            current = (Integer) params.get("current");
            pageSize = (Integer) params.get("pageSize");
        }

        Page<Works> page = new Page<Works>(current, pageSize);
        page.setCondition(params);
        List<Works> worksList = getWorksList(page);

        page.setRecords(worksList);
        return page;
    }

    @Override
    public List<Works> getWorksList(Page<Works> page) throws Exception {
        List<Works> worksList = null;
        try{
            Map<String, Object> condition = page.getCondition();
            if(condition == null){
                condition = new HashMap<>();
            }
           worksList = worksMapper.selectWorksList(page, page.getCondition());
           for (Works works:worksList) {
               //获取所属项目信息
               IchProject ichProject = ichProjectService.getIchProjectById(works.getIchProjectId());
               works.setIchProject(ichProject);
               //获取传承人信息
               IchMaster ichMaster = ichMasterService.getIchMasterByWorks(works);
               works.setIchMaster(ichMaster);
               //获取内容片断
               List<ContentFragment> contentFragmentList = getContentFragmentListByWorksId(works);
               works.setContentFragmentList(contentFragmentList);
           }
       }catch (Exception e){
            throw new ApplicationException(ApplicationException.INNER_ERROR);
       }

        return worksList;
    }

    /**
     * 添加或更新作品
     * @param works
     * @throws Exception
     */
    @Override
    public void saveWorks(Works works) throws Exception {
        TransactionStatus transactionStatus = getTransactionStatus();
        try{
            if(works.getId() == null){//新增
                Long worksId = IdWorker.getId();
                works .setId(worksId);
                works.setStatus(0);
                works.setIsRepresent(1);
                works.setUri(worksId + ".html");
                worksMapper.insertSelective(works);
                List<ContentFragment> contentFragmentList = works.getContentFragmentList();
                if(contentFragmentList != null && contentFragmentList.size()>0){
                    for (ContentFragment contentFragment:contentFragmentList) {
                        saveContentFragment(contentFragment,worksId);
                    }
                }
            }else{
                //更新
                works.setUri(works.getId() + ".html");
                worksMapper.updateByPrimaryKeySelective(works);
                List<ContentFragment> contentFragmentList = works.getContentFragmentList();
                if(contentFragmentList != null && contentFragmentList.size()>0){
                    for (ContentFragment contentFragment : contentFragmentList) {
                        if(contentFragment.getId() == null){
                            //添加
                            saveContentFragment(contentFragment,works.getId());
                        }else{
                            //更新
                            contentFragmentMapper.updateByPrimaryKeySelective(contentFragment);
                            List<Resource> resourceList = contentFragment.getResourceList();
                            if(resourceList != null && resourceList.size()>0){
                                IchProjectServiceImpl ips = new IchProjectServiceImpl();
                                ips.saveResource(resourceList,contentFragment.getId());
                            }
                        }
                    }
                }
            }
//            //获取项目信息
//            if(works.getIchProjectId() != null){
//                IchProject ichProject = ichProjectService.getIchProjectById(works.getIchProjectId());
//                works.setIchProject(ichProject);
//            }
//            //获取传承人信息
//            if(works.getIchMasterId() != null){
//                IchMaster ichMaster = ichMasterService.getIchMasterByWorks(works);
//                works.setIchMaster(ichMaster);
//            }
            commit(transactionStatus);
        }catch (Exception e){
            rollback(transactionStatus);
            throw new ApplicationException(ApplicationException.INNER_ERROR);
        }
    }

    /**
     * 生成静态页面
     * @param templateName
     * @param works
     * @param fileName
     * @return
     * @throws Exception
     */
    @Override
    public String buildHTML(String templateName, Works works, String fileName) throws Exception {
        String uri = BuildHTMLEngine.buildHTML(templateName, works, fileName);
        return uri;
    }
    /**
     * 根据项目id获取代表作品列表
     * @param ichProjectId
     * @return
     */
    public List<Works> getWorksByIchProjectId(Long ichProjectId) throws Exception {
        Works works = new Works();
        works.setIchProjectId(ichProjectId);
        works.setIsRepresent(1);
        List<Works> worksList = worksMapper.selectWorks(works);
        if(worksList.size()>0){
            worksList = getWorkList(worksList);
        }
        return worksList;
    }


    /**
     * 根据传承人id查询代表作品列表
     * @param ichMasterId
     * @return
     */
    public List<Works> getWorksByIchMasterId(Long ichMasterId) throws Exception {
        Works works = new Works();
        works.setIchMasterId(ichMasterId);
        works.setIsRepresent(1);
        List<Works> getWorkList = worksMapper.selectWorks(works);
        List<Works> worksList = getWorkList(getWorkList);
        return worksList;
    }

    private List<Works> getWorkList(List<Works> worksList) throws Exception {
        for (Works works:worksList) {
            //获取传承人信息
            IchMaster ichMaster = ichMasterService.getIchMasterByWorks(works);
            works.setIchMaster(ichMaster);
            //获取内容片断
            List<ContentFragment> contentFragments = getContentFragmentListByWorksId(works);
            works.setContentFragmentList(contentFragments);
        }
        return worksList;
    }

    private List<ContentFragment> getContentFragmentListByWorksId(Works works) throws Exception {
        //获取内容片断
        ContentFragment con = new ContentFragment();
        con.setTargetId(works.getId());
        con.setTargetType(2);
        List<ContentFragment> contentFragmentList = contentFragmentMapper.selectByTargetIdAndType(con);
        for (ContentFragment contentFragment : contentFragmentList) {
            Long attrId = contentFragment.getAttributeId();
            Attribute attribute = attributeMapper.selectByPrimaryKey(attrId);
            contentFragment.setAttribute(attribute);//添加属性
            List<ContentFragmentResource> contentFragmentResourceList = contentFragmentResourceMapper.selectByContentFragmentId(contentFragment.getId());
            List<Resource> resourceList = new ArrayList<>();
            for (ContentFragmentResource contentFragmentResource: contentFragmentResourceList) {
                Resource resource = resourceMapper.selectByPrimaryKey(contentFragmentResource.getResourceId());
                if(resource !=null){
                    resource.setResOrder(contentFragmentResource.getResOrder());
                    resourceList.add(resource);
                }

            }
            contentFragment.setResourceList(resourceList);
        }
        return contentFragmentList;
    }

    /**
     * 填加contentFragment
     * @param c
     */
    private void saveContentFragment(ContentFragment c,Long id) throws Exception{
        Long attributeId = c.getAttributeId();
        if(attributeId == 0 || attributeId == null){
            Attribute attribute = c.getAttribute();
            attributeId = IdWorker.getId();
            attribute.setId(attributeId);
            attribute.setTargetType(12);
            attribute.setIchCategoryId(id);
            attribute.setStatus(0);
            attribute.setIsOpen(1);
            attribute.setPriority(99);
            attributeMapper.insertSelective(attribute);
        }
        c.setAttributeId(attributeId);
        c.setId(IdWorker.getId());
        c.setTargetId(id);
        c.setTargetType(2);
        c.setStatus(0);
        contentFragmentMapper.insertSelective(c);
        List<Resource> resourceList = c.getResourceList();
        if(resourceList != null && resourceList.size()>0){
            IchProjectServiceImpl ips = new IchProjectServiceImpl();
            ips.saveResource(resourceList,c.getId());
        }
    }

}
