plugins {
    `java-platform`
    `maven-publish`
}

val libraryGroup: String = project.findProperty("LIBRARY_GROUP") as? String ?: "io.github.12345debdut"
val libraryVersion: String =
    project.findProperty("LIBRARY_VERSION") as? String
        ?: project.findProperty("VERSION") as? String ?: "0.1.0"

group = libraryGroup
version = libraryVersion

dependencies {
    constraints {
        api(project(":anchor-di-api"))
        api(project(":anchor-di-core"))
        api(project(":anchor-di-ksp"))
        api(project(":anchor-di-android"))
        api(project(":anchor-di-presentation"))
        api(project(":anchor-di-compose"))
    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
            artifactId = "anchor-di-bom"
            pom {
                name.set("Anchor DI BOM")
                description.set("Bill of Materials for Anchor DI — aligns all module versions.")
                url.set("https://github.com/12345debdut/anchor-di")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("12345debdut")
                        name.set("Debdut Saha")
                        url.set("https://github.com/12345debdut")
                    }
                }
                scm {
                    url.set("https://github.com/12345debdut/anchor-di")
                    connection.set("scm:git:git://github.com/12345debdut/anchor-di.git")
                    developerConnection.set("scm:git:ssh://git@github.com/12345debdut/anchor-di.git")
                }
            }
        }
    }
}
