package filter;


import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BaseFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        String contextpath = req.getContextPath();
        String uri = req.getRequestURI();
        uri = StringUtils.remove(uri,contextpath);
        System.out.println(uri);
        if (uri.startsWith("/user_")){
            String servlet = StringUtils.substringBetween(uri,"_","_");
            servlet+="Servlet";
            String method = StringUtils.substringAfterLast(uri,"_");
            req.getSession().setAttribute("invMethod",method);
            req.getRequestDispatcher("/"+servlet).forward(req,res);
        }
        filterChain.doFilter(req,res);
    }

    @Override
    public void destroy() {

    }
}
