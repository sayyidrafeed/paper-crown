plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":shared"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito", module = "mockito-core")
    }
    testImplementation("org.mockito:mockito-core:5.17.0") {
        exclude(group = "net.bytebuddy", module = "byte-buddy")
        exclude(group = "net.bytebuddy", module = "byte-buddy-agent")
    }
    testImplementation("net.bytebuddy:byte-buddy:1.17.2")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.17.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.17.0")
    testImplementation("org.junit.jupiter:junit-jupiter")
}
