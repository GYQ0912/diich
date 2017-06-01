package com.diich.search.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreeMarkerViewUtil extends FreeMarkerView {
    @Override
    protected void doRender(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // Expose model to JSP tags (as request attributes).
        exposeModelAsRequestAttributes(model, request);
        // Expose all standard FreeMarker hash models.
        SimpleHash fmModel = buildTemplateModel(model, request, response);

        if (logger.isDebugEnabled()) {
            logger.debug("Rendering FreeMarker ģ�� [" + getUrl() + "] in FreeMarkerView '" + getBeanName() + "'");
        }
        // Grab the locale-specific version of the template.
        Locale locale = RequestContextUtils.getLocale(request);

        /*
         * Ĭ�����ɾ�̬�ļ�,�����ڱ�дModelAndViewʱָ��CREATE_HTML = false, �����Ծ�̬�ļ����ɵ����ȿ��Ƹ�ϸһ��
         * ���磺ModelAndView mav = new ModelAndView("search");
         * mav.addObject("CREATE_HTML", false);
         */
        if (Boolean.FALSE.equals(model.get("CREATE_HTML"))) {
            processTemplate(getTemplate(locale), fmModel, response);
        } else {
            createHTML(getTemplate(locale), fmModel, request, response);
        }
    }

    public void createHTML(Template template, SimpleHash model, HttpServletRequest request,
                           HttpServletResponse response) throws IOException, TemplateException, ServletException {
        // վ���Ŀ¼�ľ���·��
        String basePath = request.getSession().getServletContext().getRealPath("/");
        String requestHTML = this.getRequestHTML(request);
        // ��̬ҳ�����·��
        String htmlPath = basePath + requestHTML;
        System.out.println("��̬ҳ�����·��===========>>:"+htmlPath);
        File htmlFile = new File(htmlPath);
        if (!htmlFile.getParentFile().exists()) {
            htmlFile.getParentFile().mkdirs();
        }
        if (!htmlFile.exists()) {
            htmlFile.createNewFile();
        }
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(htmlFile), "UTF-8"));
        // ����ģ��
        template.process(model, out);
        out.flush();
        out.close();
        /* ������ת�������ɵ�htm�ļ� */
        request.getRequestDispatcher(requestHTML).forward(request, response);
    }

    /**
     * ����Ҫ���ɵľ�̬�ļ����·�� ��Ϊ����ڵ��Ե�ʱ��һ����Tomcat��webapps�����½�վ��Ŀ¼�ģ�
     * ����ʵ��Ӧ��ʱֱ�Ӳ���ROOTĿ¼����,����Ҫ��֤·����һ���ԡ�
     *
     * @param request
     *            HttpServletRequest
     * @return /Ŀ¼/*.htm
     */
    private String getRequestHTML(HttpServletRequest request) {
        // webӦ������,������ROOTĿ¼ʱΪ��
        String contextPath = request.getContextPath();
        // webӦ��/Ŀ¼/�ļ�.do
        String requestURI = request.getRequestURI();
        // basePath�����Ѿ�����webӦ�����ƣ�����ֱ�Ӱ���replace���������ظ�
        requestURI = requestURI.replaceFirst(contextPath, "");
        // ��.do��Ϊ.html,�Ժ�����ת������html�ļ�
        requestURI = requestURI.substring(0, requestURI.indexOf(".")) + ".html";
        return requestURI;
    }
}
