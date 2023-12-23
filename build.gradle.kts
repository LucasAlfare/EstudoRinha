plugins {
  kotlin("jvm") version "1.9.21"
  kotlin("plugin.serialization") version "1.9.21"

  /*
  "application" plugin was introduced in order to correctly build the Kotlin project.
   */
  application
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
  implementation("ch.qos.logback:logback-classic:1.4.12")

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

application {
  mainClass.set("com.lucasalfare.estudorinha.MainKt")
}

/*
This specifies a custom task when creating a ".jar" for this project.
The main thing is to define manifest and the fixing classpath stuff.
 */
tasks.withType<Jar> {
  manifest {
    attributes["Main-Class"] = "com.lucasalfare.estudorinha.MainKt"
  }

  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  configurations["compileClasspath"].forEach { file: File ->
    from(zipTree(file.absoluteFile))
  }
}