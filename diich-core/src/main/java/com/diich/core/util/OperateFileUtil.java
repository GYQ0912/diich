package com.diich.core.util;

import org.apache.commons.fileupload.FileItem;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by Administrator on 2017/5/26.
 */
public class OperateFileUtil {

    public static List<String> uplaodFile(HttpServletRequest request) throws Exception {
        //创建一个通用的多部分解析器
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        List<String> list = new ArrayList<>();

        //判断 request 是否有文件上传,即多部分请求
        if (multipartResolver.isMultipart(request)) {
            //转换成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;

            MultiValueMap<String, MultipartFile> multiFileMap = multiRequest.getMultiFileMap();

            for(String key : multiFileMap.keySet()) {
                List<MultipartFile> fileList = multiFileMap.get(key);

                for(MultipartFile file : fileList) {
                    String fileName = file.getOriginalFilename();

                    StringBuilder url = new StringBuilder();

                    url.append("image/" + new Date().getTime() + fileName.trim());

                    String fileUrl = "http://diich-resource.oss-cn-beijing.aliyuncs.com/" + url.toString();

                    //将图片上传至阿里云
                    boolean bool = AliOssUtil.uploadFile(file, "diich-resource", url.toString());

                    if(bool) {
                        list.add(fileUrl);
                    }
                }
            }
        }

        return list;
    }

    public static void deleteFile() throws Exception {
        //删除文件
    }
}