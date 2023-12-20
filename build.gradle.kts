plugins {
  kotlin("jvm") version "1.9.21"
  kotlin("plugin.serialization") version "1.9.21"
}

group = "com.lucasalfare"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  val ktor_version = "2.3.7"

  // dependências do Ktor (core e motor de fundo)
  implementation("io.ktor:ktor-server-core:$ktor_version")
  implementation("io.ktor:ktor-server-netty:$ktor_version")

  // dependências para habilitar serialização
  implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

  // isso aqui serve apenas para gerar os logs da engine do servidor...
  implementation("ch.qos.logback:logback-classic:1.4.8")

  implementation("org.jetbrains.exposed:exposed-core:0.45.0")
  implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
  implementation("org.postgresql:postgresql:42.6.0")

  testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(17)
}