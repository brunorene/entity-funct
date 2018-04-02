import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    id("org.jetbrains.kotlin.jvm") version "1.2.31"
    id("com.github.johnrengelman.shadow") version "2.0.3"
    id("io.spring.dependency-management") version "1.0.4.RELEASE"
    id("com.bmuschko.docker-remote-api") version "3.2.5"
}

group = "uk.sky.poc"
version = "1.0.0"
val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion

configure<ApplicationPluginConvention> {
    mainClassName = "uk.sky.poc.EntityApp"
}

configure<DependencyManagementExtension> {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:2.0.0.RELEASE")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

repositories {
    mavenCentral()
    jcenter()
}

configurations.all {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compile("org.jetbrains.kotlin:kotlin-reflect")
    compile("io.projectreactor.ipc:reactor-netty")
    compile("org.springframework.boot:spring-boot-starter-actuator")
    compile("org.springframework.boot:spring-boot-starter-webflux")
    compile("org.springframework.boot:spring-boot-starter-log4j2")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    compile("org.springframework.kafka:spring-kafka")
    compile("com.google.code.gson:gson:2.8.2")
    compile("com.hazelcast:hazelcast-spring:3.9.3")
    compile("com.hazelcast:hazelcast-zookeeper:3.6.3")
    compile("org.apache.curator:curator-x-discovery:2.12.0")
    compile("com.lmax:disruptor:3.4.1")
    compile("log4j:log4j:1.2.17")
    compile("com.uchuhimo:konf:0.10")
}

tasks {
    val buildImage by creating(DockerBuildImage::class) {
        dependsOn("shadowJar")
        inputDir = projectDir
        tag = "poc/entity-funct:latest"
    }
}