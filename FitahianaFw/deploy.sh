#!/bin/bash
set -euo pipefail

TOMCAT_PATH="${TOMCAT_PATH:-/opt/tomcat}"

if [[ ! -d "$TOMCAT_PATH" ]]; then
    echo "Répertoire Tomcat introuvable: $TOMCAT_PATH"
    echo "Définis TOMCAT_PATH vers l'installation Tomcat de ce PC, par exemple:"
    echo "  TOMCAT_PATH=\"$HOME/apache-tomcat\" ./deploy.sh"
    exit 1
fi

echo "Construction du projet..."
mvn clean package

JAR_FILE=$(find target -name "*.jar" \
    ! -name "*sources*" \
    ! -name "*javadoc*" \
    ! -name "original-*" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "Aucun JAR trouvé."
    exit 1
fi

if [[ ! -d "$TOMCAT_PATH/lib" ]]; then
    echo "Répertoire Tomcat lib introuvable: $TOMCAT_PATH/lib"
    exit 1
fi

echo "Copie du JAR vers Tomcat..."
if [[ -w "$TOMCAT_PATH/lib" ]]; then
    cp "$JAR_FILE" "$TOMCAT_PATH/lib/"
elif command -v sudo >/dev/null 2>&1; then
    sudo cp "$JAR_FILE" "$TOMCAT_PATH/lib/"
else
    echo "Impossible d'écrire dans $TOMCAT_PATH/lib et 'sudo' n'est pas disponible."
    exit 1
fi

echo "Déploiement terminé : $JAR_FILE"