# GitHub Actions CI/CD Workflows

This directory contains GitHub Actions workflows for automated testing and building of the InvoiceExtract application.

## 📋 Available Workflows

### 1. Backend Build and Test (`backend-test.yml`)
**Recommended for most use cases**

This workflow focuses on the backend service with a lightweight Docker environment.

**Triggers:**
- Push to `main` or `develop` branches (when backend code changes)
- Pull requests to `main` or `develop` branches
- Manual trigger via GitHub UI

**What it does:**
- ✅ Sets up Java 17 and Maven
- ✅ Starts MySQL databases (invoices on port 3306, mappings on port 3307)
- ✅ Starts Kafka broker
- ✅ Runs Liquibase migrations
- ✅ Executes all backend tests with H2 in-memory database
- ✅ Generates code coverage reports (JaCoCo)
- ✅ **Builds and uploads the JAR artifact** (`invoicextract-backend-0.0.1-SNAPSHOT.jar`)
- ✅ Publishes test results in PR comments

**Artifacts Generated:**
- `invoicextract-backend-{commit-sha}`: The compiled JAR file (retained for 30 days)
- `test-reports-{commit-sha}`: Test results and coverage reports (retained for 14 days)

**Duration:** ~5-8 minutes

---

### 2. Test and Build (`test-and-build.yml`)
**Fast unit testing with services**

This workflow runs backend tests with all required services using GitHub Services.

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches
- Manual trigger

**What it does:**
- ✅ Uses GitHub Services for MySQL, PostgreSQL (Keycloak DB)
- ✅ Starts Keycloak and Kafka in Docker containers
- ✅ Runs Liquibase migrations
- ✅ Executes backend tests
- ✅ Builds frontend application
- ✅ Uploads JAR and frontend build artifacts

**Artifacts Generated:**
- `invoicextract-backend-jar`: Backend JAR file
- `frontend-build`: Compiled frontend assets
- `test-results`: Test and coverage reports

**Duration:** ~6-10 minutes

---

### 3. Integration Tests with Docker Compose (`integration-test-docker.yml`)
**Full stack integration testing**

This workflow uses your complete `docker-compose.yml` to run full integration tests.

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches
- Manual trigger

**What it does:**
- ✅ Spins up the complete application stack using docker-compose
- ✅ Starts all services: MySQL, Keycloak, Kafka, Adminer, Backend, Mapping Service, Frontend
- ✅ Verifies all endpoints are accessible
- ✅ Extracts test results and JAR from containers
- ✅ Tests with your specified URLs:
  - Frontend: `http://localhost:3001`
  - Database Admin (Adminer): `http://localhost:8081`

**Artifacts Generated:**
- `invoicextract-backend-jar-integration`: Backend JAR from Docker container
- `integration-test-results`: Integration test results

**Duration:** ~10-15 minutes (slower due to full stack initialization)

---

### 4. E2E Tests with Playwright (`e2e-tests.yml`)
**End-to-end testing with Playwright**

This workflow runs Playwright E2E tests against the complete running application using your specified localhost URLs.

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches
- Manual trigger

**What it does:**
- ✅ Starts complete application stack using docker-compose
- ✅ Installs Playwright and Chromium browser
- ✅ Creates Playwright config automatically (if not exists)
- ✅ Runs TypeScript Playwright tests
- ✅ Tests against your specified URLs:
  - Frontend: `http://localhost:3001`
  - Database Admin: `http://localhost:8081`
  - Backend API: `http://localhost:8080/invoicextract`
  - Keycloak: `http://localhost:8085`
- ✅ Captures screenshots and videos on failure
- ✅ Generates HTML test reports

**Artifacts Generated:**
- `playwright-report-{commit-sha}`: HTML report with screenshots and videos
- `e2e-test-results-{commit-sha}`: Test results in JSON/JUnit format

**Duration:** ~15-20 minutes (includes full stack startup + browser tests)

**Note:** If no Playwright tests exist, the workflow creates a sample test that verifies all services are accessible.

---

## 🚀 Usage

### Running Workflows Manually

1. Go to your GitHub repository
2. Click on **Actions** tab
3. Select the workflow you want to run
4. Click **Run workflow** button
5. Select the branch and click **Run workflow**

### Downloading Artifacts

After a workflow completes:

1. Go to the workflow run page
2. Scroll to the **Artifacts** section at the bottom
3. Click on the artifact name to download

**Main artifact:** `invoicextract-backend-{commit-sha}` contains the JAR file you can deploy.

---

## 🔧 Configuration

### Environment Variables

The workflows use these URLs (matching your requirements):
- **Frontend URL:** `http://localhost:3001`
- **Database Admin URL:** `http://localhost:8081` (Adminer)
- **Backend API:** `http://localhost:8080/invoicextract`
- **Keycloak:** `http://localhost:8085`
- **Mapping Service:** `http://localhost:8082`

### Required Secrets

No secrets are required for basic functionality. If you add code coverage or security scanning, you may need:
- `GITHUB_TOKEN` (automatically provided by GitHub Actions)

---

## 📊 Test Reports

Test results are automatically published:
- **Unit test results** appear as comments on Pull Requests
- **Coverage reports** show code coverage percentages
- **Test summaries** are visible in the workflow run summary

---

## 🐛 Troubleshooting

### Workflow fails with "Service not ready"
- Increase the wait time in the workflow (currently 30-60 seconds)
- Check if the service health checks are passing

### Tests fail locally but pass in CI
- Ensure your local environment matches the CI environment (Java 17, MySQL 8, Kafka 3.5.1)
- Check if you're using the correct database URLs

### Artifact not generated
- Check the build logs for compilation errors
- Verify Maven build completes successfully
- Ensure the JAR file is created in `target/` directory

---

## 📈 Next Steps

### Recommended Enhancements

1. **Add deployment workflow:**
   - Automatically deploy to staging/production after successful tests
   - Use the JAR artifact from the test workflow

2. **Add Docker image building:**
   - Build and push Docker images to a registry (Docker Hub, GitHub Container Registry)
   - Tag images with version numbers

3. **Add security scanning:**
   - Scan dependencies for vulnerabilities (Snyk, Dependabot)
   - Scan Docker images for security issues

4. **Add SonarQube analysis:**
   - Integrate with your SonarQube instance (currently configured at `http://localhost:9000`)
   - Publish code quality reports

5. **Add E2E tests:**
   - Add Playwright or Cypress tests for frontend
   - Run end-to-end tests against the full stack

---

## 💡 Workflow Recommendation

**For regular development:**
Use **`backend-test.yml`** - it's the fastest and most reliable for catching bugs early.

**For comprehensive testing before release:**
Use **`integration-test-docker.yml`** - it tests the complete stack as it will run in production.

**For end-to-end user flow testing:**
Use **`e2e-tests.yml`** - it runs Playwright tests against the full application in a browser.

**For building artifacts:**
All workflows generate the JAR artifact, but **`backend-test.yml`** is the most efficient.

---

## 📝 Notes

- All workflows use **Ubuntu latest** as the runner OS
- **Java 17** and **Maven 3.9.6** are used for backend builds
- **Node.js 18** is used for frontend builds
- Artifacts are retained for **30 days** (JAR files) and **14 days** (test reports)
- The workflows automatically clean up Docker containers after execution

---

## 🔗 Related Files

- Main docker-compose: `/docker-compose.yml`
- Backend configuration: `/invoicextract-backend/pom.xml`
- Liquibase changelogs: `/liquibase/` and `/liquibase-mappings/`
- Frontend configuration: `/frontend/package.json`
