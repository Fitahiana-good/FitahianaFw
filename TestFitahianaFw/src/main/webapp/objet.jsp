<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="test.model.Personne" %>
<!DOCTYPE html>
<html lang="fr">
<head><meta charset="UTF-8"><title>Binding d'un objet</title></head>
<body>
    <h1>Binding d'un objet Personne</h1>
    <form action="${pageContext.request.contextPath}/test/objet" method="post">
        <p><label>Nom : <input name="nom" required></label></p>
        <p><label>Âge : <input name="age" type="number" required></label></p>
        <p><label>Actif : <select name="actif"><option value="true">Oui</option><option value="false">Non</option></select></label></p>
        <p><label>Note (facultative) : <input name="note" type="number" step="any"></label></p>
        <button type="submit">Tester l'objet</button>
    </form>
    <% Personne personne = (Personne) request.getAttribute("personne");
       if (personne != null) { %>
    <h2>Objet reçu</h2>
    <p>Nom : <%= personne.getNom() == null ? "" : personne.getNom().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;") %></p>
    <p>Âge : ${personne.age}</p>
    <p>Actif : ${personne.actif}</p>
    <p>Note : ${personne.note}</p>
    <% } %>
    <p><a href="${pageContext.request.contextPath}/primitives.jsp">Tester les types primitifs</a></p>
</body>
</html>
