//package com.shanghai.core.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.*;
//import javax.servlet.annotation.WebFilter;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
///**
// * CorsConfig Description
// *
// * @author yehan
// */
//@WebFilter(filterName = "CorsConfig",urlPatterns = "/*")
//@Component
//@Order(3)
//@Slf4j
//public class CorsConfig implements Filter{
//
//    @Override
//    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
//
//        HttpServletResponse response = (HttpServletResponse) res;
//
//        response.setHeader("Access-Control-Allow-Origin", "*");
//
//        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, HEAD");
//
//        response.setHeader("Access-Control-Max-Age", "3600");
//
//        response.setHeader("Access-Control-Allow-Headers", "access-control-allow-origin, authority, content-type, version-info, X-Requested-With");
//
//        chain.doFilter(req, res);
//
//    }
//    @Override
//    public void init(FilterConfig filterConfig) {}
//    @Override
//    public void destroy() {}
//
//
//}
