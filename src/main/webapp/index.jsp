<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.nio.file.Files" %>
<%@ page import="java.nio.file.Paths" %>
<%
    try {
        String htmlPath = application.getRealPath("/view/index.html");
        String content = new String(Files.readAllBytes(Paths.get(htmlPath)), "UTF-8");
        out.print(content);
    } catch (Exception e) {
        out.println("Error loading page: " + e.getMessage());
    }
%>
