---
name: use-springboottest-not-datajpatest
description: "In this Spring Boot 4.1 project @DataJpaTest fails to load context; use @SpringBootTest + @ActiveProfiles(\"test\") + @Transactional for repo/service tests"
metadata: 
  node_type: memory
  type: project
  originSessionId: 92b91ad7-b1fa-40d3-a253-2da52924df94
---

In this SeismicMonitor project (Spring Boot 4.1.0), the `@DataJpaTest` slice **fails to load the ApplicationContext**: `FileNotFoundException: org/springframework/boot/autoconfigure/data/jpa/JpaRepositoriesAutoConfiguration.class ... does not exist`. Boot 4 relocated autoconfigurations into per-module packages, but the split `spring-boot-starter-data-jpa-test` slice still references the old package path.

**Working pattern for service/repository tests:**
```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional   // rolls back each test
```
Full context boots fine and the `test` profile points the datasource at H2 (via `src/test/resources/application-test.properties`, which overlays the MySQL base `application.properties`).

**Why:** Confirmed working — SensorDataServiceTest (Delopgave 1) passes this way; the @DataJpaTest version did not. Use this for all later delopgave tests too. See [[exam-delivery-mode]].
