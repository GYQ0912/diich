package com.diich.service.impl;

import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.diich.core.base.BaseService;
import com.diich.core.exception.ApplicationException;
import com.diich.core.model.Attribute;
import com.diich.core.model.ContentFragment;
import com.diich.core.model.ContentFragmentResource;
import com.diich.core.model.Resource;
import com.diich.core.service.ContentFragmentService;
import com.diich.mapper.AttributeMapper;
import com.diich.mapper.ContentFragmentMapper;
import com.diich.mapper.ContentFragmentResourceMapper;
import com.diich.mapper.ResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/18.
 */
@Service("contentFragmentService")
@Transactional
public class ContentFragmentServiceImpl extends BaseService<ContentFragment> implements ContentFragmentService  {

    @Autowired
    private ContentFragmentMapper contentFragmentMapper;
    @Autowired
    private AttributeMapper attributeMapper;
    @Autowired
    private ContentFragmentResourceMapper contentFragmentResourceMapper;
    @Autowired
    private ResourceMapper resourceMapper;

    @Override
    public ContentFragment getContentFragment(String id) throws Exception {
        ContentFragment contentFragment = null;
        try{
            contentFragment = contentFragmentMapper.selectByPrimaryKey(Long.parseLong(id));
            if(contentFragment ==null){
                return contentFragment;
            }
            //根据contentFragment获取资源文件  获取attribute
            Attribute attribute = attributeMapper.selectByPrimaryKey(contentFragment.getAttributeId());
            contentFragment.setAttribute(attribute);
            List<ContentFragmentResource> contentFragmentResourceList = contentFragmentResourceMapper.selectByContentFragmentId(contentFragment.getId());
            List<Resource> resourceList = new ArrayList<>();
            for (ContentFragmentResource contentFragmentResource:contentFragmentResourceList) {
                Resource resource = resourceMapper.selectByPrimaryKey(contentFragmentResource.getContentFragmentId());
                if(resource == null){
                    continue;
                }
                resourceList.add(resource);
            }
            contentFragment.setResourceList(resourceList);
        }catch (Exception e){
            throw new ApplicationException(ApplicationException.INNER_ERROR);
        }


        return contentFragment;
    }

    @Override
    public ContentFragment saveContentFragment(ContentFragment contentFragment) throws Exception {
        TransactionStatus transactionStatus = getTransactionStatus();
        checkSaveField(contentFragment);//校验字段
        try{
            if(contentFragment.getId() == null){
                Long attributeId = contentFragment.getAttributeId();
                if(attributeId == 0 || attributeId == null){
                    attributeId = saveAttribute(contentFragment);
                }
                contentFragment.setId(IdWorker.getId());
                contentFragment.setAttributeId(attributeId);
                contentFragment.setStatus(0);
                contentFragmentMapper.insertSelective(contentFragment);
            }else{
                contentFragmentMapper.updateByPrimaryKeySelective(contentFragment);
                Attribute attribute = contentFragment.getAttribute();
                if(attribute != null && (attribute.getTargetType() ==10 || attribute.getTargetType() ==11 || attribute.getTargetType() ==12)){//更新自定义属性的名称
                    attributeMapper.updateByPrimaryKeySelective(contentFragment.getAttribute());
                }
            }
            //保存资源文件
            if(contentFragment.getResourceList() != null && contentFragment.getResourceList().size() > 0){
                IchProjectServiceImpl ips = new IchProjectServiceImpl();
                ips.saveResource(contentFragment.getResourceList(),contentFragment.getId());
            }
            commit(transactionStatus);
        }catch (Exception e){
            rollback(transactionStatus);
            throw new ApplicationException(ApplicationException.INNER_ERROR);
        }
        return contentFragment;
    }

    @Override
    public void deleteContentFragment(Long id) throws Exception {
        TransactionStatus transactionStatus = getTransactionStatus();
        try{
            ContentFragment contentFragment = contentFragmentMapper.selectByPrimaryKey(id);

            contentFragmentResourceMapper.deleteByContentFragmentId(id);
            List<Resource> resourceList = contentFragment.getResourceList();
            if(resourceList != null && resourceList.size()>0){
                for (Resource resource : resourceList) {
                    resourceMapper.deleteByPrimaryKey(resource.getId());
                }
            }
            contentFragmentMapper.deleteByPrimaryKey(id);
            Attribute attribute = attributeMapper.selectByPrimaryKey(contentFragment.getAttributeId());
            if(attribute != null && (attribute.getTargetType() ==10 || attribute.getTargetType() ==11 || attribute.getTargetType() ==12)){
                attributeMapper.deleteByPrimaryKey(contentFragment.getAttributeId());
            }
            commit(transactionStatus);
        }catch (Exception e){
            rollback(transactionStatus);
            throw new ApplicationException(ApplicationException.INNER_ERROR);
        }

    }

    private void checkSaveField(ContentFragment contentFragment) throws Exception {
        Long attributeId = contentFragment.getAttributeId();
        if (attributeId != null) {
            Attribute attribute = null;
            try {
                attribute = attributeMapper.selectByPrimaryKey(attributeId);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ApplicationException(ApplicationException.INNER_ERROR);
            }

            if (attribute.getMinLength() != null) {
                String content = contentFragment.getContent();
                if (content == null || (content.length() < attribute.getMinLength())) {
                    throw new ApplicationException(ApplicationException.PARAM_ERROR, attribute.getCnName().toString()+" 字段不符合要求");
                }
            }
            if (attribute.getMaxLength() != null) {
                String content = contentFragment.getContent();
                if (content != null && content.length() > attribute.getMaxLength()) {
                    throw new ApplicationException(ApplicationException.PARAM_ERROR, attribute.getCnName().toString()+" 字段不符合要求");
                }
            }

        }
    }

    /**
     * 保存自定义字段
     * @param contentFragment
     * @return
     * @throws Exception
     */
    private Long saveAttribute(ContentFragment contentFragment) throws Exception{
        String targetType = 1+""+contentFragment.getTargetType();
        Attribute attribute = contentFragment.getAttribute();
        Long attributeId = IdWorker.getId();
        attribute.setId(attributeId);
        attribute.setStatus(0);
        attribute.setTargetType(Integer.parseInt(targetType));
        attribute.setIchCategoryId(contentFragment.getTargetId());
        attribute.setIsOpen(1);
        attribute.setPriority(99);
        attributeMapper.insertSelective(attribute);
        return attributeId;
    }

}
