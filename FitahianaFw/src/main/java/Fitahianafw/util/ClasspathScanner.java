package Fitahianafw.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClasspathScanner {
    /**
     *
     * @param packageName     nom complet du package (ex: "Fitahianafw")
     * @param annotationClass classe de l'annotation à rechercher
     */
    public static List<Class<?>> getClassesAnnotatedWith(
            Class<? extends Annotation> annotationClass,
            String packageName) throws IOException, ClassNotFoundException {

        List<Class<?>> result = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String packagePath = (packageName == null || packageName.isEmpty())
                ? ""
                : packageName.replace('.', '/'); //Remplace le package en chemin

        Enumeration<URL> resources = classLoader.getResources(packagePath); //Charge toutes les ressources de ce package
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();
            if ("file".equals(protocol)) {

                File dir = new File(URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8));
                scanDirectory(dir, packageName, annotationClass, result); //Si c'est un fichier ou dossier

            } else if ("jar".equals(protocol)) {

                String jarPath = resource.getFile().substring(5, resource.getFile().indexOf('!')); //Retourne le chemin absolu du jar
                try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
                    scanJar(jar, packageName, annotationClass, result); //Si c'est une archive
                }
            }
        }
        return result;
    }

    private static void scanDirectory(File dir, String packageName,
            Class<? extends Annotation> annotationClass,
            List<Class<?>> result) throws ClassNotFoundException {
        if (!dir.exists())
            return;
        File[] files = dir.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                String sousPackage = packageName == null ? file.getName() : packageName + "." + file.getName();
                scanDirectory(file, sousPackage, annotationClass, result); //Récursif si le file est un dossier
            } else if (file.getName().endsWith(".class")) {
                String className = packageName == null
                        ? file.getName().substring(0, file.getName().length() - 6)
                        : packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(annotationClass)) {
                    result.add(clazz);
                }
            }
        }
    }

    private static void scanJar(JarFile jar, String packageName,
            Class<? extends Annotation> annotationClass,
            List<Class<?>> result) throws ClassNotFoundException {
        Enumeration<JarEntry> entries = jar.entries(); //Récupérer les éléments du jar

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entryName.endsWith(".class")) {
                String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                if (packageName == null || packageName.isEmpty() || className.startsWith(packageName + ".")) {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(annotationClass)) {
                        result.add(clazz);
                    }
                }
            }
        }
    }
}