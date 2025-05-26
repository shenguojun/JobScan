plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

dependencies {
    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
    implementation(project(":utils"))
    
    // 网页爬虫和解析
    implementation("org.jsoup:jsoup:1.17.2")
    
    // HTTP客户端
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // 数据库
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.45.0")
    
    // JSON处理
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")
    
    // 任务调度
    implementation("org.quartz-scheduler:quartz:2.3.2")
    
    // 日志
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    
    // 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // 配置文件处理
    implementation("com.typesafe:config:1.4.3")
    
    // 测试依赖
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "org.shengj.app.AppKt"
}
