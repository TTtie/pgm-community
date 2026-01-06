plugins {
    id("buildlogic.java-conventions")
}

repositories {
    maven("https://repo.viaversion.com") // ViaVersion
}

dependencies {
    implementation(project(":core"))
    implementation(project(":util"))
    compileOnly("app.ashcon:sportpaper:1.8.8-R0.1-SNAPSHOT")
    compileOnly("com.viaversion:viaversion-api:5.0.0")
}
