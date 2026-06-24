---
name: jdk-26-build-setup
description: User wants latest Java (26); how to build SeismicMonitor with JDK 26 despite system JAVA_HOME being 21
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 31e368a0-86be-4931-8c86-ae4a405e754f
---

User prefers the most recent Java version (Java 26) as good practice — keep `pom.xml` `java.version=26`, do not downgrade to 21.

**Why:** Explicit preference stated during exam setup; Spring Boot 4.1.0 was confirmed to compile cleanly under JDK 26.

**How to apply:** System `JAVA_HOME` points to `C:\Program Files\Java\jdk-21` and `java` on PATH is 21, so plain `mvnw` builds target 21 and would mismatch. For terminal Maven builds, export `JAVA_HOME="C:/Users/magnu/.jdks/openjdk-26.0.1"` first. IntelliJ project SDK is `openjdk-26` (in `.idea/misc.xml`); language level was bumped from JDK_21 to JDK_26. Other installed JDKs: jdk-17, jdk-21, and several in `C:/Users/magnu/.jdks/`.
