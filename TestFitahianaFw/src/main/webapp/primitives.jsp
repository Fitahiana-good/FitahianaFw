<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head><meta charset="UTF-8"><title>Binding des types primitifs</title></head>
<body>
    <h1>Binding des types primitifs</h1>
    <form action="${pageContext.request.contextPath}/test/primitives" method="post">
        <p><label>Nom : <input name="nom" required></label></p>
        <p><label>Âge : <input name="age" type="number" required></label></p>
        <p><label>Actif : <select name="actif"><option value="true">Oui</option><option value="false">Non</option></select></label></p>
        <p><label>Note (facultative) : <input name="note" type="number" step="any"></label></p>
        <button type="submit">Tester</button>
    </form>
    <p>Âge reçu : ${age}</p>
    <p>Actif reçu : ${actif}</p>
    <p>Note reçue : ${note}</p>
</body>
</html>
