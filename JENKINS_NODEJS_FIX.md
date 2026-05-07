# 🔧 Fix: Node.js libatomic.so.1 Error in Jenkins

## ❌ Problème

```
node: error while loading shared libraries: libatomic.so.1: cannot open shared object file: No such file or directory
```

Cette erreur se produit car le conteneur Jenkins n'a pas la bibliothèque `libatomic` nécessaire pour Node.js.

---

## ✅ Solutions

### **Solution 1: Installer libatomic dans le Conteneur Jenkins (Recommandé)**

#### **Méthode A: Via Docker Exec (Temporaire)**

```bash
# 1. Trouver le conteneur Jenkins
docker ps | grep jenkins

# 2. Se connecter au conteneur
docker exec -it <jenkins-container-id> bash

# 3. Installer libatomic
apt-get update
apt-get install -y libatomic1

# 4. Vérifier l'installation
ldconfig -p | grep libatomic

# 5. Sortir du conteneur
exit
```

#### **Méthode B: Créer un Dockerfile Personnalisé (Permanent)**

Créer un fichier `jenkins/Dockerfile`:

```dockerfile
FROM jenkins/jenkins:lts

USER root

# Installer les dépendances système nécessaires
RUN apt-get update && \
    apt-get install -y \
    libatomic1 \
    curl \
    git \
    && rm -rf /var/lib/apt/lists/*

# Installer Node.js (si pas déjà fait via plugin)
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

# Vérifier les installations
RUN node --version && npm --version

USER jenkins
```

Puis rebuilder l'image Jenkins:

```bash
# 1. Arrêter Jenkins
docker-compose down

# 2. Builder la nouvelle image
docker build -t jenkins-custom:latest jenkins/

# 3. Mettre à jour docker-compose.yml
# Remplacer:
#   image: jenkins/jenkins:lts
# Par:
#   image: jenkins-custom:latest

# 4. Redémarrer Jenkins
docker-compose up -d
```

---

### **Solution 2: Utiliser un Agent Docker pour Node.js**

Modifier le Jenkinsfile du frontend pour utiliser un conteneur Docker avec Node.js:

```groovy
pipeline {
    agent {
        docker {
            image 'node:20-alpine'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    stages {
        stage('📦 Install Dependencies') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                }
            }
        }
        
        stage('🏗️ Build') {
            steps {
                dir('frontend') {
                    sh 'npm run build -- --configuration=production'
                }
            }
        }
        
        // ... autres stages
    }
}
```

---

### **Solution 3: Utiliser NodeJS Plugin avec Installation Complète**

Dans Jenkins:

1. **Aller dans**: Manage Jenkins → Tools → NodeJS installations
2. **Cliquer sur**: Add NodeJS
3. **Configurer**:
   - Name: `NodeJS-20`
   - Install automatically: ✅
   - Version: `NodeJS 20.x`
   - Global npm packages to install: `npm@latest`

4. **Sauvegarder**

Puis dans le Jenkinsfile, s'assurer que le tool est bien configuré:

```groovy
tools {
    nodejs 'NodeJS-20'
}
```

---

## 🚀 Solution Rapide (Pour Tester Maintenant)

### **Étape 1: Installer libatomic dans Jenkins**

```bash
# Exécuter cette commande sur ton serveur Jenkins
docker exec -u root <jenkins-container-id> apt-get update
docker exec -u root <jenkins-container-id> apt-get install -y libatomic1
```

### **Étape 2: Relancer le Build**

```bash
# Dans Jenkins UI, cliquer sur "Build Now" pour frontend-CI
```

---

## 🔍 Vérification

Pour vérifier que libatomic est installé:

```bash
# Dans le conteneur Jenkins
docker exec <jenkins-container-id> ldconfig -p | grep libatomic

# Résultat attendu:
# libatomic.so.1 (libc6,x86-64) => /lib/x86_64-linux-gnu/libatomic.so.1
```

---

## 📝 Commandes Utiles

### **Trouver le Container ID de Jenkins**

```bash
docker ps --filter "name=jenkins"
```

### **Voir les Logs Jenkins**

```bash
docker logs -f <jenkins-container-id>
```

### **Redémarrer Jenkins**

```bash
docker restart <jenkins-container-id>
```

---

## ✅ Après la Correction

Une fois libatomic installé, le frontend devrait builder correctement:

```
✅ npm ci
✅ npm run test
✅ npm run build
✅ docker build
✅ docker push
```

---

## 🎯 Recommandation Finale

**Pour une solution permanente**, utilise la **Méthode B** (Dockerfile personnalisé) pour que la dépendance soit toujours présente même après un redémarrage de Jenkins.
