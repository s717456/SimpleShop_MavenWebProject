package backEnd.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.substring(contextPath.length());
        String method = request.getMethod();

        if (isPublicPath(path, method)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpSession session = request.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("loginMemberId") != null;
        if (loggedIn) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (path.startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"請先登入\"}");
            return;
        }

        response.sendRedirect(contextPath + "/login.html");
    }

    private boolean isPublicPath(String path, String method) {
        if (path.equals("/login") || path.equals("/logout") || path.equals("/login.html") || path.equals("/register.html")) {
            return true;
        }
        if (path.startsWith("/assets/")) {
            return true;
        }
        if (path.equals("/api/members") && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        if (path.equals("/favicon.ico")) {
            return true;
        }
        return false;
    }
}
