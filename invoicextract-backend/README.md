# Invoice Extract Backend

Skeleton generated from component diagram.

## Análisis con SonarQube (local)

### 1) Levantar SonarQube (Docker)

Si ya tienes docker-compose en la raíz del repo con `sonarqube` y `sonar-db`:

```bash
docker compose up -d sonarqube sonar-db
```

Accede a http://localhost:9000 (admin/admin la primera vez, cambia la contraseña).

### 2) Crear un token

- En tu usuario (arriba a la derecha) → My Account → Security → Generate Tokens.
- Copia el token (se muestra solo una vez).

### 3) Ejecutar tests y cobertura (Maven)

Desde esta carpeta (`invoicextract-backend`):

```bash
mvn clean test jacoco:report
```

El reporte JaCoCo se genera en `target/site/jacoco/jacoco.xml`.

### 4) Ejecutar análisis con Maven Sonar Scanner

Este proyecto ya define propiedades Sonar en el `pom.xml` (key, fuentes, ruta de jacoco). Ejecuta:

```bash
mvn sonar:sonar -Dsonar.token=<TU_TOKEN>
```

El resultado aparecerá en SonarQube (Projects → Invoice Extract Backend).

### 5) Proyecto limpio y baseDir

Si deseas forzar un proyecto nuevo o aislar el módulo, puedes usar el CLI completo:

```bash
mvn sonar:sonar \
  -Dsonar.token=<TU_TOKEN> \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.projectKey=invoicextract-backend-clean \
  -Dsonar.projectName="Invoice Extract Backend" \
  -Dsonar.projectBaseDir=. \
  -Dsonar.sources=src/main/java \
  -Dsonar.tests=src/test/java \
  -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
```

### 6) Notas y troubleshooting

- Tokens: `-Dsonar.token` es preferido sobre `-Dsonar.login`.
- Blame/SCM: commitea los cambios antes de analizar para evitar el warning de "Missing blame information".
- Limpieza: si ves clases antiguas en cobertura, ejecuta `mvn clean` y verifica `jacoco.xml` antes del `sonar:sonar`.
- Exclusiones de cobertura: el `pom.xml` contiene `sonar.coverage.exclusions` para excluir DTOs, entities y configs del porcentaje.