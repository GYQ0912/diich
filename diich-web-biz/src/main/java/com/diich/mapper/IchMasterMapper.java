package com.diich.mapper;

import com.baomidou.mybatisplus.plugins.Page;
import com.diich.core.base.BaseMapper;
import com.diich.core.model.IchMaster;
import com.diich.core.model.IchProject;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface IchMasterMapper extends BaseMapper<IchMaster> {
    int deleteByPrimaryKey(Long id);

    Integer insert(IchMaster record);

    int insertSelective(IchMaster record);

    IchMaster selectByPrimaryKey(Long id);

    IchMaster selectByMasterById(Long id);

    int updateByPrimaryKeySelective(IchMaster record);

    int updateByPrimaryKey(IchMaster record);

    //根据条件查询列表信息
    List<IchMaster> selectIchMasterList(Page<IchMaster> page, @Param("params") Map<String, Object> params);

    //根据项目id查询传承人列表
    List<IchMaster> selectByIchProjectId(Long ichProjectId);

}