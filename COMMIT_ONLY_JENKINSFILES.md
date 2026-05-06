# 🚀 Commit ONLY Jenkinsfiles

## ✅ What to Commit

**ONLY the Jenkinsfiles:**
- backend/*/Jenkinsfile (19 files)
- backend/*/Jenkinsfile.cd (19 files)

**Total: 38 files**

---

## 🚀 COMMIT COMMANDS

```bash
# 1. Add ONLY Jenkinsfiles (not the docs)
git add backend/*/Jenkinsfile
git add backend/*/Jenkinsfile.cd

# 2. Check what will be committed (should be ONLY Jenkinsfiles)
git status

# 3. Commit
git commit -m "feat: add CI/CD Jenkinsfiles for all 19 microservices

- Add Jenkinsfile (CI) to each service
- Add Jenkinsfile.cd (CD) to each service
- Configure Maven, JaCoCo, SonarQube, Docker
- Setup automated deployment pipeline

Services: User, Tracking, Geofencing, Chat, Event, Post, 
Notification, Paiement, Pharmacie, Rendez-Vous, 
Dossier-Medical, Detection-Maladie, Activite-Educative,
Eureka, Gateway, Session, Suivi-Engagement, 
Meeting-Insights, Group

Total: 38 Jenkinsfiles (19 CI + 19 CD)"

# 4. Push
git push origin main
```

---

## ✅ Verify Before Push

```bash
# Should show ONLY Jenkinsfiles
git diff --cached --name-only

# Should list 38 files like:
# backend/User-Service/Jenkinsfile
# backend/User-Service/Jenkinsfile.cd
# backend/Tracking-Service/Jenkinsfile
# backend/Tracking-Service/Jenkinsfile.cd
# ... etc
```

---

## 📝 Files NOT Committed (You can delete later)

These will stay local only:
- All *.md documentation files
- pipelines/ folder (scripts)
- Any .sh scripts

---

## 🎯 After Push

1. Verify on GitHub
2. Create Jenkins jobs
3. Delete local documentation files if you want

