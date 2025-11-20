# GitHub Actions Quick Start Guide

## 🚀 Getting Started in 3 Steps

### Step 1: Push your code to GitHub
```bash
git add .
git commit -m "Add GitHub Actions workflows"
git push origin main
```

### Step 2: Watch the workflows run
1. Go to your repository on GitHub
2. Click the **Actions** tab
3. See workflows running automatically

### Step 3: Download your artifacts
1. Click on a completed workflow run
2. Scroll to **Artifacts** section
3. Download `invoicextract-backend-{sha}`

## 📊 Quick Reference

### Which Workflow Should I Use?

```
┌─────────────────────────────────────────────────────────┐
│                   Decision Tree                         │
└─────────────────────────────────────────────────────────┘

Need fast feedback on backend changes?
    → Use: backend-test.yml ⚡
    
Need to test frontend + backend together?
    → Use: test-and-build.yml ⚡⚡
    
Need to test the complete stack?
    → Use: integration-test-docker.yml ⚡⚡⚡

Need to run E2E tests with Playwright?
    → Use: e2e-tests.yml 🎭
```

## 🎯 Your Specified URLs

These URLs are used during testing:

| Service | URL | Status |
|---------|-----|--------|
| Frontend | `http://localhost:3001` | ✅ Working |
| Database Admin | `http://localhost:8081` | ✅ Working |

## 📦 Where is My JAR File?

The JAR artifact is generated in **three workflows**:

1. **In backend-test.yml**: 
   - Artifact name: `invoicextract-backend-{commit-sha}`
   - Example: `invoicextract-backend-a1b2c3d`

2. **In test-and-build.yml**:
   - Artifact name: `invoicextract-backend-jar`

3. **In integration-test-docker.yml**:
   - Artifact name: `invoicextract-backend-jar-integration`

## 🎭 Where are My Playwright Test Reports?

E2E test reports are generated in **e2e-tests.yml**:

- **Playwright HTML Report**: `playwright-report-{commit-sha}`
  - Interactive HTML report with screenshots and videos
  - View test traces and detailed failure information
  
- **Test Results**: `e2e-test-results-{commit-sha}`
  - JSON and JUnit format results
  - Screenshots on failure
  - Videos on failure

## 📥 How to Download Artifacts

### From GitHub UI:
1. Go to **Actions** tab
2. Click on a workflow run
3. Scroll to **Artifacts** section
4. Click artifact name to download

### From Command Line (GitHub CLI):
```bash
# Install GitHub CLI first
brew install gh

# Login to GitHub
gh auth login

# List artifacts from latest run
gh run list --workflow=backend-test.yml

# Download artifacts
gh run download {run-id}
```

## 🔄 Manual Trigger

To manually run a workflow:

1. Go to **Actions** tab
2. Select workflow from left sidebar
3. Click **Run workflow** button
4. Select branch (default: main)
5. Click **Run workflow**

## 🧪 Test Reports

Test results are automatically:
- ✅ Uploaded as artifacts
- ✅ Posted as PR comments
- ✅ Visible in Checks tab
- ✅ Include coverage reports

## 🛠️ Troubleshooting

### Workflow doesn't trigger?
Check:
- [ ] Workflows are in `.github/workflows/` directory
- [ ] You pushed to `main` or `develop` branch
- [ ] Files have `.yml` extension

### Tests failing?
Check the logs:
1. Click on failed workflow run
2. Click on failed step
3. Read error messages
4. Common issues:
   - Database connection timeout (increase wait time)
   - Port conflicts (shouldn't happen in GitHub runners)
   - Missing dependencies (check pom.xml)

### Artifacts not found?
Check:
- [ ] Build completed successfully
- [ ] Check artifact retention (30 days for JARs)
- [ ] Look in correct workflow run

## 📞 Common Commands

### Check workflow status:
```bash
gh run list --workflow=backend-test.yml --limit 5
```

### View workflow logs:
```bash
gh run view {run-id} --log
```

### Download latest artifact:
```bash
gh run download --name invoicextract-backend-jar
```

### Cancel running workflow:
```bash
gh run cancel {run-id}
```

## 🎓 Learning Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker in GitHub Actions](https://docs.github.com/en/actions/publishing-packages/publishing-docker-images)
- [Maven in GitHub Actions](https://docs.github.com/en/actions/automating-builds-and-tests/building-and-testing-java-with-maven)

## 💡 Pro Tips

1. **Use backend-test.yml for daily work** - It's the fastest
2. **Check PR comments** - Test results appear automatically
3. **Download artifacts before they expire** - 30 days for JARs
4. **Use manual triggers for experiments** - No need to push code
5. **Read the logs** - They contain detailed error messages

## 📈 What's Next?

Once workflows are working:
1. ✅ Add deployment workflow
2. ✅ Build Docker images
3. ✅ Add security scanning
4. ✅ Set up staging environment
5. ✅ Add E2E tests

## 🆘 Need Help?

Check these files:
- `README.md` - Detailed workflow documentation
- `ARCHITECTURE.md` - Visual diagrams and flows
- Workflow files - Inline comments explain each step

## ✅ Checklist for First Run

- [ ] Code is on GitHub
- [ ] Workflows are in `.github/workflows/`
- [ ] pom.xml is in `invoicextract-backend/`
- [ ] docker-compose.yml is at root
- [ ] Pushed to main or develop branch
- [ ] Go to Actions tab and watch it run!

---

**Ready?** Push your code and watch the magic happen! 🚀
