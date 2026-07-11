package Fitahianafw.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Fitahianafw.err.UrlNotSupportedException;
import Fitahianafw.mapping.UrlHTTPMethod;
import Fitahianafw.mapping.UrlKey;
import Fitahianafw.mapping.UrlProcessor;
import Fitahianafw.model.ModelView;
import Fitahianafw.servlet.listener.FrontServletContextListener;

public class FrontServletController extends HttpServlet {
    private UrlProcessor urlProcessor;

    @Override
    public void init() throws ServletException {
        urlProcessor = (UrlProcessor) getServletContext().getAttribute(FrontServletContextListener.URL_PROCESSOR_ATTR);
        if (urlProcessor == null) {
            throw new ServletException("UrlProcessor introuvable dans le ServletContext");
        }
    }

    private Object executeRequest(HttpServletRequest request)
            throws UrlNotSupportedException, ReflectiveOperationException {
        String uri = getRequestedUrl(request);
        UrlHTTPMethod method = UrlHTTPMethod.buildUrlHTTPMethod(request.getMethod());
        return urlProcessor.executeRequest(new UrlKey(uri, method));
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

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        try {
            Object result = executeRequest(request);
            if (result instanceof ModelView modelView) {
                forwardToView(request, response, modelView);
            } else if (result == null) {
                writeHtml(response, "Exécution réussie",
                        "La méthode du contrôleur a été exécutée correctement.");
            } else {
                writeHtml(response, "Résultat", escapeHtml(String.valueOf(result)));
            }
        } catch (UrlNotSupportedException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeHtml(response, "404 - Route introuvable", "La route demandée n'est pas supportée.");
        } catch (ReflectiveOperationException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeHtml(response, "500 - Erreur interne", "Impossible d'exécuter la méthode du contrôleur.");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeHtml(response, "500 - Erreur interne", "Une erreur interne est survenue.");
        }
    }

    private void forwardToView(HttpServletRequest request, HttpServletResponse response, ModelView modelView)
            throws ServletException, IOException {
        for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
        request.getRequestDispatcher(buildJspPath(modelView.getViewName())).forward(request, response);
    }

    private String buildJspPath(String viewName) {
        if (viewName == null || viewName.isBlank()) {
            return "/index.jsp";
        }
        String jspPath = viewName.startsWith("/") ? viewName : "/" + viewName;
        return jspPath.endsWith(".jsp") ? jspPath : jspPath + ".jsp";
    }

    private void writeHtml(HttpServletResponse response, String title, String message) throws IOException {
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"fr\"><head><meta charset=\"UTF-8\"><title>"
                + escapeHtml(title) + "</title></head><body>");
        out.println("<h1>" + escapeHtml(title) + "</h1><p>" + message + "</p>");
        out.println("</body></html>");
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
