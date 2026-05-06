#!/bin/bash
# Execute these commands to commit ONLY Jenkinsfiles

echo "🚀 Adding Jenkinsfiles..."

# Add ONLY Jenkinsfiles
git add backend/*/Jenkinsfile
git add backend/*/Jenkinsfile.cd

echo ""
echo "✅ Files staged for commit:"
git diff --cached --name-only

echo ""
echo "📊 Total files to commit:"
git diff --cached --name-only | wc -l

echo ""
echo "⚠️  VERIFY: Should be 38 files (19 Jenkinsfile + 19 Jenkinsfile.cd)"
echo ""
read -p "Press Enter to continue with commit, or Ctrl+C to cancel..."

# Commit
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

echo ""
echo "✅ Committed!"
echo ""
read -p "Press Enter to push to GitHub, or Ctrl+C to cancel..."

# Push
git push origin main

echo ""
echo "🎉 Done! Jenkinsfiles pushed to GitHub"
echo ""
echo "📋 Next steps:"
echo "1. Verify on GitHub"
echo "2. Create Jenkins jobs"
echo "3. Delete local documentation files if needed"
