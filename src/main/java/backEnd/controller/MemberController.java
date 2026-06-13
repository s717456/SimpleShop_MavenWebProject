package backEnd.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/member", "/member/list", "/member/register"})
public class MemberController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getServletPath().equals("/member/register")) {
            request.getRequestDispatcher("/register.html").forward(request, response);
            return;
        }
        request.getRequestDispatcher("/members.html").forward(request, response);
    }
}
