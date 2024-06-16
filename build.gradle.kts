plugins {
    id("java")
}

group = "org.mcu"
version = "2.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")


}
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
tasks.build {
    doLast {
        copy {
            from("build/libs") {
                include("*.jar")
            }
            into("G:/mc_java_server/plugins")
        }
    }

}