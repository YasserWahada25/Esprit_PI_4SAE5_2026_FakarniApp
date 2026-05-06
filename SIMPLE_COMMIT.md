# ⚡ Simple Commit - Copy & Paste

## 🚀 Execute These Commands

```bash
# 1. Add ONLY Jenkinsfiles
git add backend/*/Jenkinsfile backend/*/Jenkinsfile.cd

# 2. Verify (should show 38 files)
git diff --cached --name-only | wc -l

# 3. Commit
git commit -m "feat: add CI/CD Jenkinsfiles for all 19 microservices"

# 4. Push
git push origin main
```

---

## ✅ That's it!

After push:
1. Verify on GitHub
2. Create Jenkins jobs (follow JENKINSFILE_SETUP_GUIDE.md)
3. Delete local .md files if you want

