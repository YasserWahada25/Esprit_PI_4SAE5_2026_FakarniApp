# 🚀 Guide de Déploiement Jenkins - Fakarni App

## 📋 Table des Matières
- [Architecture des Pipelines](#architecture-des-pipelines)
- [Quand Utiliser Chaque Pipeline](#quand-utiliser-chaque-pipeline)
- [Configuration Jenkins](#configuration-jenkins)
- [Scénarios d'Utilisation](#scénarios-dutilisation)

---

## 🏗️ Architecture des Pipelines

### **Approche Hybride (Recommandée)**

Nous utilisons une approche hybride qui combine:
1. **Pipelines Individuels** (par service)
2. **Pipelines Orchestrés** (pour déploiements groupés)

```
📁 Racine du Projet
├── Jenkinsfile.orchestrator    # 🎭 Orchestre tous les déploiements
├── Jenkinsfile.ci-all          # 🔨 Build tous les services
├── Jenkinsfile.cd-all          # 🚀 Deploy tous les services
│
└── 📁 backend/
    ├── User-Service/
    │   ├── Jenkinsfile         # CI individuel
    │   └── Jenkinsfile.cd      # CD individuel
    ├── Gateway-Service/
    │   ├── Jenkinsfile
    │   └── Jenkinsfile.cd
    └── ...
```

---

## 🎯 Quand Utiliser Chaque Pipeline

### **1. Pipeline Orchestrateur** (`Jenkinsfile.orchestrator`)

**Utiliser quand:**
- ✅ Premier déploiement complet
- ✅ Mise à jour majeure (plusieurs services)
- ✅ Déploiement dans un nouvel environnement
- ✅ Besoin de contrôler l'ordre de déploiement

**Avantages:**
- Déploie dans le bon ordre (Eureka → Gateway → Services)
- Gère les dépendances entre services
- Permet de choisir ce qu'on déploie (FULL, BACKEND_ONLY, etc.)
- Inclut des temps d'attente pour la stabilisation

**Comment lancer:**
```bash
# Dans Jenkins UI:
1. Aller sur le job "fakarni-orchestrator"
2. Cliquer "Build with Parameters"
3. Choisir:
   - DEPLOYMENT_MODE: FULL / BACKEND_ONLY / FRONTEND_ONLY
   - SKIP_TESTS: true/false
   - DEPLOY_TO_PROD: true/false
4. Cliquer "Build"
```

---

### **2. Pipeline CI-All** (`Jenkinsfile.ci-all`)

**Utiliser quand:**
- ✅ Build rapide de tous les services
- ✅ Vérification que tout compile
- ✅ Tests d'intégration globaux
- ✅ Création d'images Docker pour tous les services

**Avantages:**
- Build en parallèle (plus rapide)
- Une seule commande pour tout builder
- Utile pour les releases

**Comment lancer:**
```bash
# Dans Jenkins UI:
1. Aller sur le job "fakarni-ci-all"
2. Cliquer "Build Now"
```

---

### **3. Pipelines Individuels** (par service)

**Utiliser quand:**
- ✅ Développement actif sur UN service
- ✅ Hotfix sur un service spécifique
- ✅ Tests d'un seul service
- ✅ Déploiement incrémental

**Avantages:**
- Rapide (seulement un service)
- Pas d'impact sur les autres services
- Idéal pour le développement quotidien

**Comment lancer:**
```bash
# Dans Jenkins UI:
1. Aller sur le job du service (ex: "user-service-CI")
2. Cliquer "Build Now"

# Ou automatiquement via Git push:
git add .
git commit -m "fix: correction bug user service"
git push origin main
# → Jenkins détecte et lance automatiquement
```

---

## ⚙️ Configuration Jenkins

### **Créer les Jobs Jenkins**

#### **1. Job Orchestrateur**
```groovy
Name: fakarni-orchestrator
Type: Pipeline
Pipeline script from SCM:
  - SCM: Git
  - Repository URL: https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git
  - Script Path: Jenkinsfile.orchestrator
```

#### **2. Job CI-All**
```groovy
Name: fakarni-ci-all
Type: Pipeline
Pipeline script from SCM:
  - Script Path: Jenkinsfile.ci-all
```

#### **3. Jobs Individuels** (déjà configurés)
```groovy
Name: user-service-CI
Type: Pipeline
Pipeline script from SCM:
  - Script Path: backend/User-Service/Jenkinsfile

Name: user-service-CD
Type: Pipeline
Pipeline script from SCM:
  - Script Path: backend/User-Service/Jenkinsfile.cd
```

---

## 📖 Scénarios d'Utilisation

### **Scénario 1: Premier Déploiement Complet**

```bash
Objectif: Déployer toute l'application pour la première fois

Étapes:
1. Utiliser Jenkinsfile.orchestrator
2. Choisir DEPLOYMENT_MODE = FULL
3. SKIP_TESTS = false (pour vérifier que tout fonctionne)
4. Attendre ~15-20 minutes

Ordre d'exécution:
1. Eureka Service (service discovery)
2. Attente 60s
3. Gateway Service (API gateway)
4. Attente 30s
5. Tous les autres services en parallèle
6. Attente 120s
7. Frontend
8. Vérification

Résultat: Application complète déployée et fonctionnelle
```

---

### **Scénario 2: Développement Quotidien**

```bash
Objectif: Travailler sur le User Service

Étapes:
1. Modifier le code du User Service
2. git add backend/User-Service/
3. git commit -m "feat: nouvelle fonctionnalité"
4. git push origin main

Jenkins:
- Détecte automatiquement le push
- Lance user-service-CI
- Build, test, docker push
- Lance user-service-CD
- Deploy sur Kubernetes

Temps: ~5-7 minutes
Impact: Seulement User Service redéployé
```

---

### **Scénario 3: Hotfix en Production**

```bash
Objectif: Corriger un bug critique sur Gateway Service

Étapes:
1. Créer une branche hotfix
   git checkout -b hotfix/gateway-auth-bug

2. Corriger le bug

3. Tester localement

4. Push et merge
   git push origin hotfix/gateway-auth-bug
   # Créer PR et merger

5. Dans Jenkins:
   - Aller sur gateway-service-CI
   - Build Now
   - Vérifier les tests
   - CD se lance automatiquement

6. Vérifier le déploiement:
   kubectl get pods | grep gateway
   kubectl logs deployment/gateway-service

Temps: ~5 minutes
Impact: Seulement Gateway redéployé
```

---

### **Scénario 4: Release Majeure**

```bash
Objectif: Déployer une nouvelle version avec plusieurs services modifiés

Étapes:
1. Créer une branche release
   git checkout -b release/v2.0.0

2. Merger toutes les features

3. Utiliser Jenkinsfile.ci-all:
   - Build tous les services
   - Vérifier que tout compile
   - Tous les tests passent

4. Si OK, utiliser Jenkinsfile.orchestrator:
   - DEPLOYMENT_MODE = FULL
   - SKIP_TESTS = false
   - DEPLOY_TO_PROD = true (nécessite approbation)

5. Approuver le déploiement production

6. Tag la release:
   git tag v2.0.0
   git push origin v2.0.0

Temps: ~20-30 minutes
Impact: Tous les services redéployés
```

---

### **Scénario 5: Déploiement Backend Seulement**

```bash
Objectif: Mettre à jour les services backend sans toucher au frontend

Étapes:
1. Utiliser Jenkinsfile.orchestrator
2. DEPLOYMENT_MODE = BACKEND_ONLY
3. SKIP_TESTS = false

Services déployés:
- Eureka
- Gateway
- User, Chat, Post, Event
- Geofencing, Tracking, Session, Group
- Dossier Medical, Detection Maladie, Detection Alzheimer
- Activite Educative, Suivi Engagement, Meeting Insights

Services NON déployés:
- Frontend (reste inchangé)

Temps: ~15 minutes
```

---

## 🎯 Recommandations

### **Pour le Développement:**
✅ Utiliser les pipelines individuels
✅ Push fréquents sur des branches feature
✅ CI/CD automatique par service

### **Pour les Releases:**
✅ Utiliser Jenkinsfile.ci-all pour build
✅ Utiliser Jenkinsfile.orchestrator pour deploy
✅ Tester en staging avant production

### **Pour la Production:**
✅ Toujours utiliser Jenkinsfile.orchestrator
✅ Activer DEPLOY_TO_PROD = true
✅ Nécessite approbation manuelle
✅ Faire un backup avant

---

## 📊 Comparaison des Approches

| Critère | Pipeline Individuel | Pipeline Orchestrateur | Pipeline CI-All |
|---------|-------------------|----------------------|----------------|
| **Vitesse** | ⚡⚡⚡ Très rapide (5min) | 🐢 Lent (20min) | ⚡⚡ Rapide (10min) |
| **Contrôle** | 🎯 Précis (1 service) | 🎭 Total (ordre) | 🔨 Build only |
| **Complexité** | ✅ Simple | ⚠️ Complexe | ✅ Simple |
| **Usage** | Dev quotidien | Releases, 1er deploy | Vérification globale |
| **Impact** | Minimal | Total | Aucun (pas de deploy) |

---

## 🚀 Commandes Utiles

### **Vérifier le Déploiement**
```bash
# Voir tous les pods
kubectl get pods

# Voir les services
kubectl get services

# Logs d'un service
kubectl logs deployment/user-service -f

# Vérifier Eureka
kubectl port-forward service/eureka-service 8761:8761
# Ouvrir http://localhost:8761
```

### **Rollback si Problème**
```bash
# Voir l'historique des déploiements
kubectl rollout history deployment/user-service

# Rollback à la version précédente
kubectl rollout undo deployment/user-service

# Rollback à une version spécifique
kubectl rollout undo deployment/user-service --to-revision=2
```

---

## ✅ Conclusion

**Approche Hybride = Meilleur des Deux Mondes**

- 🎯 **Flexibilité**: Choisis ce que tu déploies
- ⚡ **Rapidité**: Pipelines individuels pour le dev
- 🎭 **Contrôle**: Orchestrateur pour les releases
- 🔒 **Sécurité**: Approbation pour la production

**Règle d'Or:**
- Dev quotidien → Pipelines individuels
- Releases/Production → Orchestrateur
- Vérification globale → CI-All

🎉 **Bonne chance avec tes déploiements !**
