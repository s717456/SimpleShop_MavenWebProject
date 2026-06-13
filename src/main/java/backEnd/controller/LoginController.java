package backEnd.controller;

import backEnd.dao.MemberDao;
import backEnd.dao.impl.MemberDaoImpl;
import backEnd.entity.Member;
import backEnd.util.JsonUtil;
import backEnd.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet(urlPatterns = {"/login", "/logout"})
public class LoginController extends HttpServlet {

    private final MemberDao memberDao = new MemberDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/logout".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;//
        }
        request.getRequestDispatcher("/login.html").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Optional<Member> optionalMember = memberDao.findByUsername(username);
        if (optionalMember.isEmpty() || !PasswordUtil.verify(password, optionalMember.get().getPasswordHash())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"帳號或密碼錯誤\"}");
            return;
        }

        Member member = optionalMember.get();
        if (!"ACTIVE".equalsIgnoreCase(member.getStatus())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"success\":false,\"message\":\"會員狀態不是 ACTIVE\"}");
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("loginMemberId", member.getId());
        session.setAttribute("loginUsername", member.getUsername());
        session.setAttribute("loginRole", member.getRole());

        response.getWriter().write("{\"success\":true,\"message\":\"登入成功\",\"username\":\"" + JsonUtil.escape(member.getUsername()) + "\",\"role\":\"" + JsonUtil.escape(member.getRole()) + "\"}");
    }
}
