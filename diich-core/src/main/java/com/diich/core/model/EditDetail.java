package com.diich.core.model;

import com.diich.core.base.BaseModel;

/**
 * Created by Administrator on 2016/11/25.
 */
public class EditDetail extends BaseModel{
    private Long id;
    private Long editRecordId;
    private String fieldName;
    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEditRecordId() {
        return editRecordId;
    }

    public void setEditRecordId(Long editRecordId) {
        this.editRecordId = editRecordId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
