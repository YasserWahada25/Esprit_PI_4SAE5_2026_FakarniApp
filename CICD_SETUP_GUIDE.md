# 🚀 Guide Complet CI/CD - Projet Fakarni

## 📋 Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Prérequis](#prérequis)
3. [Configuration Jenkins](#configuration-jenkins)
4. [Credentials à créer](#credentials-à-créer)
5. [Pipelines CI](#pipelines-ci)
6. [Pipelines CD](#pipelines-cd)
7. [Déploiement](#déploiement)

---

## 🎯 Vue d'ensemble

### Architecture CI/CD

```
GitHub → Jenkins CI → SonarQube → Docker Hub → Jenkins CD → Production
```

### Services à déployer

- **14 Microservices Spring Boot** (Java 21, Maven)
- **1 Service Python** (detection-alzheimer, Flask)
- **1 Frontend Angular** (TypeScript, Node.js)

### Séparation CI/CD

- **Pipeline CI**: Build → Test → SonarQube → Docker Build → Docker Push
- **Pipeline CD**: Pull Image → Deploy → Health Check → Notification

---

## ✅ Prérequis

### Outils configurés dans Jenkins

- ✅ Maven 3.9
- ✅ JDK 21
- ✅ NodeJS 20
- ✅ SonarQube Scanner
- ✅ Docker

### Credentials de base

- ✅ `github-credentials` - GitHub Access Token
- ✅ `dockerhub-credentials` - Docker Hub Login
- ✅ `sonarqube-token` - SonarQube Token

---

## 🔐 Credentials à créer

### Accès Jenkins Credentials

1. Jenkins → **Administrer Jenkins** → **Credentials**
2. Cliquez sur **(global)**
3. **Add Credentials** pour chaque entrée ci-dessous

### Liste complète (15 nouveaux)

| ID | Type | Valeur | Description |
|---|---|---|---|
| `mail-username-user` | Secret text | `mohamadrayen.jbili@esprit.tn` | User Service Mail |
| `mail-password-user` | Secret text | `ueivocwsiczztvem` | User Service Mail Password |
| `mail-username-geofencing` | Secret text | `bahri.rania098@gmail.com` | Geofencing Mail |
| `mail-password-geofencing` | Secret text | `cfbyuwbpasuwnxvj` | Geofencing Mail Password |
| `mailtrap-username` | Secret text | `votre-mailtrap-username` | Mailtrap Username |
| `mailtrap-password` | Secret text | `votre-mailtrap-password` | Mailtrap Password |
| `google-client-id` | Secret text | `968599520946-llp69cv61a73f9457lpedn7m4tflrr2t.apps.googleusercontent.com` | Google OAuth2 |
| `facebook-app-id` | Secret text | `1270980888473415` | Facebook App ID |
| `facebook-app-secret` | Secret text | `your-facebook-app-secret-here` | Facebook App Secret |
| `twilio-account-sid` | Secret text | `AC67b937e03fdc6d358fe90c94866ca636` | Twilio Account SID |
| `twilio-auth-token` | Secret text | `5129a79de1a2e8c4e4a917dbc4f25f0a` | Twilio Auth Token |
| `twilio-from-number` | Secret text | `+16812708324` | Twilio Phone Number |
| `mysql-root-password` | Secret text | `root` | MySQL Root Password |
| `mongo-root-username` | Secret text | `admin` | MongoDB Username |
| `mongo-root-password` | Secret text | `admin` | MongoDB Password |
| `jwt-secret` | Secret text | `ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=` | JWT Secret Key |

---

## 🔄 Pipeline CI - Microservices Spring Boot

### Créer le job Jenkins

1. Jenkins → **Nouveau item**
2. Nom: `user-service-CI`
3. Type: **Pipeline**
4. **Build Triggers**: ☑️ Scrutation de l'outil de gestion de version
   - Schedule: `H/5 * * * *`
5. **Pipeline** → Definition: **Pipeline script**
6. Coller le script ci-dessous

### Script Pipeline CI

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-21'
    }
    
    environment {
        SERVICE_NAME = 'user-service'
        SERVICE_PATH = 'backend/User-Service'
        DOCKER_IMAGE = "didou2505/${SERVICE_NAME}"
        SONAR_PROJECT_KEY = "mediconnect-${SERVICE_NAME}"
        GIT_REPO = 'https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', 
                    credentialsId: 'github-credentials', 
                    url: "${GIT_REPO}"
            }
        }
        
        stage('Build') {
            steps {
                dir("${SERVICE_PATH}") {
                    sh 'mvn clean compile -DskipTests'
                }
            }
        }
        
        stage('Test') {
            steps {
                dir("${SERVICE_PATH}") {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit "${SERVICE_PATH}/target/surefire-reports/*.xml"
                    jacoco(
                        execPattern: "${SERVICE_PATH}/target/jacoco.exec",
                        classPattern: "${SERVICE_PATH}/target/classes",
                        sourcePattern: "${SERVICE_PATH}/src/main/java"
                    )
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                dir("${SERVICE_PATH}") {
                    withSonarQubeEnv('SonarQube') {
                        withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                            sh """
                                mvn sonar:sonar \
                                    -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                    -Dsonar.projectName=${SERVICE_NAME} \
                                    -Dsonar.host.url=http://sonarqube:9000 \
                                    -Dsonar.token=${SONAR_TOKEN}
                            """
                        }
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
                }
            }
        }
        
        stage('Package') {
            steps {
                dir("${SERVICE_PATH}") {
                    sh 'mvn package -DskipTests'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                dir("${SERVICE_PATH}") {
                    script {
                        dockerImage = docker.build("${DOCKER_IMAGE}:${BUILD_NUMBER}")
                        docker.build("${DOCKER_IMAGE}:latest")
                    }
                }
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                        dockerImage.push("${BUILD_NUMBER}")
                        dockerImage.push("latest")
                    }
                }
            }
        }
        
        stage('Trigger CD') {
            steps {
                build job: 'user-service-CD', wait: false
            }
        }
    }
    
    post {
        success {
            echo "✅ CI Pipeline completed - Image pushed: ${DOCKER_IMAGE}:${BUILD_NUMBER}"
        }
        failure {
            echo "❌ CI Pipeline failed"
        }
        always {
            sh 'docker image prune -f || true'
        }
    }
}
```

---

## 🚀 Pipeline CD - Déploiement

### Créer le job Jenkins

1. Jenkins → **Nouveau item**
2. Nom: `user-service-CD`
3. Type: **Pipeline**
4. **Build Triggers**: Aucun (déclenché par CI)
5. **Pipeline** → Definition: **Pipeline script**
6. Coller le script ci-dessous

### Script Pipeline CD

```groovy
pipeline {
    agent any
    
    environment {
        SERVICE_NAME = 'user-service'
        DOCKER_IMAGE = "didou2505/${SERVICE_NAME}"
    }
    
    stages {
        stage('Pull Latest Image') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                        docker.image("${DOCKER_IMAGE}:latest").pull()
                    }
                }
            }
        }
        
        stage('Deploy') {
            steps {
                script {
                    sh """
                        cd ${WORKSPACE}
                        docker-compose up -d ${SERVICE_NAME}
                    """
                }
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    sleep(time: 30, unit: 'SECONDS')
                    sh """
                        docker ps | grep ${SERVICE_NAME} || exit 1
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo "✅ ${SERVICE_NAME} deployed successfully!"
        }
        failure {
            echo "❌ Deployment failed for ${SERVICE_NAME}"
        }
    }
}
```

---

## 🐍 Pipeline CI - Service Python

### Script pour detection-alzheimer

```groovy
pipeline {
    agent any
    
    environment {
        SERVICE_NAME = 'detection-alzheimer'
        SERVICE_PATH = 'detection-alzheimer'
        DOCKER_IMAGE = "didou2505/${SERVICE_NAME}"
        GIT_REPO = 'https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', 
                    credentialsId: 'github-credentials', 
                    url: "${GIT_REPO}"
            }
        }
        
        stage('Setup Python') {
            steps {
                dir("${SERVICE_PATH}") {
                    sh '''
                        python3 -m venv venv
                        . venv/bin/activate
                        pip install -r requirements.txt
                    '''
                }
            }
        }
        
        stage('Test') {
            steps {
                dir("${SERVICE_PATH}") {
                    sh '''
                        . venv/bin/activate
                        pip install pytest pytest-cov
                        pytest --cov=. --cov-report=xml || true
                    '''
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                dir("${SERVICE_PATH}") {
                    script {
                        dockerImage = docker.build("${DOCKER_IMAGE}:${BUILD_NUMBER}")
                        docker.build("${DOCKER_IMAGE}:latest")
                    }
                }
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                        dockerImage.push("${BUILD_NUMBER}")
                        dockerImage.push("latest")
                    }
                }
            }
        }
        
        stage('Trigger CD') {
            steps {
                build job: 'detection-alzheimer-CD', wait: false
            }
        }
    }
}
```

---

## 🎨 Pipeline CI - Frontend Angular

### Script pour frontend

```groovy
pipeline {
    agent any
    
    tools {
        nodejs 'NodeJS-20'
    }
    
    environment {
        SERVICE_NAME = 'frontend'
        SERVICE_PATH = 'frontend'
        DOCKER_IMAGE = "didou2505/${SERVICE_NAME}"
        GIT_REPO = 'https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', 
                    credentialsId: 'github-credentials', 
                    url: "${GIT_REPO}"
            }
        }
        
        stage('Install Dependencies') {
            steps {
                dir("${SERVICE_PATH}") {
                    sh 'npm ci'
                }
            }
        }
        
        stage('Build') {
            steps {
                dir("${SERVICE_PATH}") {
                    sh 'npm run build -- --configuration production'
                }
            }
        }
        
        stage('Test') {
            steps {
                dir("${SERVICE_PATH}") {
                    sh 'npm run test -- --watch=false --code-coverage --browsers=ChromeHeadless || true'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                dir("${SERVICE_PATH}") {
                    script {
                        dockerImage = docker.build("${DOCKER_IMAGE}:${BUILD_NUMBER}")
                        docker.build("${DOCKER_IMAGE}:latest")
                    }
                }
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                        dockerImage.push("${BUILD_NUMBER}")
                        dockerImage.push("latest")
                    }
                }
            }
        }
        
        stage('Trigger CD') {
            steps {
                build job: 'frontend-CD', wait: false
            }
        }
    }
}
```

---

## 📊 Liste des Pipelines à créer

### Microservices Spring Boot (28 pipelines = 14 CI + 14 CD)

| Service | Pipeline CI | Pipeline CD |
|---------|-------------|-------------|
| User-Service | `user-service-CI` | `user-service-CD` |
| Eureka-Service | `eureka-service-CI` | `eureka-service-CD` |
| Gateway-Service | `gateway-service-CI` | `gateway-service-CD` |
| Tracking-Service | `tracking-service-CI` | `tracking-service-CD` |
| Event-Service | `event-service-CI` | `event-service-CD` |
| activite-educative-service | `activite-educative-CI` | `activite-educative-CD` |
| Chat_Service | `chat-service-CI` | `chat-service-CD` |
| Detection_Maladie-Service | `detection-maladie-CI` | `detection-maladie-CD` |
| Dossier_Medical-service | `dossier-medical-CI` | `dossier-medical-CD` |
| Notification-Service | `notification-service-CI` | `notification-service-CD` |
| Paiement-Service | `paiement-service-CI` | `paiement-service-CD` |
| Pharmacie-Service | `pharmacie-service-CI` | `pharmacie-service-CD` |
| Rendez-Vous-Service | `rendez-vous-service-CI` | `rendez-vous-service-CD` |
| Video-Service | `video-service-CI` | `video-service-CD` |

### Services spéciaux (4 pipelines = 2 CI + 2 CD)

| Service | Pipeline CI | Pipeline CD |
|---------|-------------|-------------|
| detection-alzheimer | `detection-alzheimer-CI` | `detection-alzheimer-CD` |
| frontend | `frontend-CI` | `frontend-CD` |

**Total: 32 pipelines (16 CI + 16 CD)**

---

## 🔄 Flux CI/CD Complet

```
1. Developer: git push
   ↓
2. Jenkins CI: Détection automatique (Poll SCM)
   ↓
3. Build → Test → SonarQube → Quality Gate
   ↓
4. Docker Build → Push to Docker Hub
   ↓
5. Trigger CD Pipeline
   ↓
6. Jenkins CD: Pull Image → Deploy
   ↓
7. Health Check → Service Running ✅
```

---

## 📝 Checklist de déploiement

### Avant de commencer

- [ ] Jenkins configuré avec tous les outils
- [ ] SonarQube connecté à Jenkins
- [ ] Docker Hub accessible
- [ ] GitHub repository accessible

### Configuration

- [ ] 18 credentials ajoutés dans Jenkins
- [ ] Première pipeline CI créée (user-service-CI)
- [ ] Première pipeline CD créée (user-service-CD)
- [ ] Test du flux complet sur user-service

### Déploiement complet

- [ ] 14 pipelines CI pour microservices Spring Boot
- [ ] 14 pipelines CD pour microservices Spring Boot
- [ ] 1 pipeline CI pour detection-alzheimer
- [ ] 1 pipeline CD pour detection-alzheimer
- [ ] 1 pipeline CI pour frontend
- [ ] 1 pipeline CD pour frontend

---

## 🎯 Prochaines étapes

1. **Ajouter les 15 credentials** dans Jenkins
2. **Créer user-service-CI** (première pipeline CI)
3. **Créer user-service-CD** (première pipeline CD)
4. **Tester le flux complet** avec un commit
5. **Répliquer pour les 15 autres services**

---

## 📞 Support

Pour toute question sur la configuration CI/CD, référez-vous à ce guide.

**Version**: 1.0  
**Dernière mise à jour**: 2026-05-05
