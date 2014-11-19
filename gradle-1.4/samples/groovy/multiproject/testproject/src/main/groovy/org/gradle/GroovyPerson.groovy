package org.gradle

/**
 * @author Hans Dockter
 */
class GroovyPerson {
    String readProperty() throws IOException {
        Properties properties = new Properties()
        properties.load(getClass().getClassLoader().getResourceAsStream("org/gradle/main.properties"))
        properties.properties.main
    }
}
