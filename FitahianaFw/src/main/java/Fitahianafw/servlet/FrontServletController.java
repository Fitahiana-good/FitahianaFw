package Fitahianafw.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Fitahianafw.err.UrlNotSupportedException;
import Fitahianafw.mapping.UrlHTTPMethod;
import Fitahianafw.mapping.UrlKey;
import Fitahianafw.mapping.UrlProcessor;
import Fitahianafw.servlet.listener.FrontServletContextListener;

public class FrontServletController extends HttpServlet {
    private UrlProcessor urlProcessor;

    @Override
    public void init() throws ServletException {
        urlProcessor = (UrlProcessor) getServletContext().getAttribute(FrontServletContextListener.URL_PROCESSOR_ATTR);
    }

    private void executeRequest(HttpServletRequest request) throws UrlNotSupportedException, ReflectiveOperationException {
        String uri = getRequestedUrl(request);
        UrlHTTPMethod method = UrlHTTPMethod.buildUrlHTTPMethod(request.getMethod());
        urlProcessor.executeRequest(new UrlKey(uri, method));
    }

    private String getRequestedUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        return uri.substring(context.length());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        try {
            executeRequest(request);
            out.println("<html><body><h1>OK</h1></body></html>");
        } catch (Exception e) {
            out.println("<html><body><p>" + e.getMessage() + "</p></body></html>");
        }
        out.close();
    }
}
