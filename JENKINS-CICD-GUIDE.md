# 🚀 GUIDE CI/CD JENKINS - FAKARNI

## 📋 OBJECTIF
Mettre en place des pipelines Jenkins pour automatiser le build, test et déploiement.

---

## 🏗️ ARCHITECTURE CI/CD

```
GitHub/GitLab → Jenkins → Build → Test → SonarQube → Docker Build → Deploy
```

---

## 1️⃣ INSTALLATION JENKINS AVEC DOCKER

### Ajouter Jenkins au docker-compose.yml

Créer un fichier `docker-compose.jenkins.yml` :

```yaml
version: '3.8'

services:
  jenkins:
    image: jenkins/jenkins:lts-jdk21
    container_name: fakarni_jenkins
    privileged: true
    user: root
    ports:
      - "8080:8080"
      - "50000:50000"
    volumes:
      - fakarni_jenkins_home:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock
    environment:
      - JAVA_OPTS=-Djenkins.install.runSetupWizard=false
    networks:
      - fakarni-net

networks:
  fakarni-net:
    external: true

volumes:
  fakarni_jenkins_home:
```

### Démarrer Jenkins

```bash
# Démarrer Jenkins
docker compose -f docker-compose.jenkins.yml up -d

# Récupérer le mot de passe initial
docker exec fakarni_jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### Accéder à Jenkins
```
http://localhost:8080
```

### Configuration initiale

1. Coller le mot de passe initial
2. Installer les plugins suggérés
3. Créer un compte admin
4. Installer les plugins supplémentaires :
   - Docker Pipeline
   - Maven Integration
   - NodeJS Plugin
   - SonarQube Scanner
   - Git Plugin
   - GitHub Integration

---

## 2️⃣ CONFIGURATION JENKINS

### Installer Maven dans Jenkins

**Manage Jenkins → Tools → Maven installations**
- Name : `Maven-3.9`
- Install automatically : ✅
- Version : `3.9.6`

### Installer NodeJS dans Jenkins

**Manage Jenkins → Tools → NodeJS installations**
- Name : `NodeJS-20`
- Install automatically : ✅
- Version : `20.x`

### Configurer Docker

**Manage Jenkins → Tools → Docker installations**
- Name : `Docker`
- Install automatically : ✅

### Configurer SonarQube

**Manage Jenkins → System → SonarQube servers**
- Name : `SonarQube`
- Server URL : `http://sonarqube:9000`
- Server authentication token : (générer depuis SonarQube)

---

## 3️⃣ PIPELINE POUR MICROSERVICES SPRING BOOT

### Jenkinsfile pour User-Service

Créer `backend/User-Service/Jenkinsfile` :

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-21'
    }
    
    environment {
        DOCKER_IMAGE = "fakarni/user-service"
        DOCKER_TAG = "${BUILD_NUMBER}"
        SONAR_PROJECT_KEY = "fakarni-user-service"
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/votre-repo/fakarni.git'
            }
        }
        
        stage('Build') {
            steps {
                dir('backend/User-Service') {
                    sh 'mvn clean compile'
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                dir('backend/User-Service') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Code Coverage - JaCoCo') {
            steps {
                dir('backend/User-Service') {
                    sh 'mvn jacoco:report'
                }
            }
            post {
                always {
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java'
                    )
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                dir('backend/User-Service') {
                    withSonarQubeEnv('SonarQube') {
                        sh '''
                            mvn sonar:sonar \
                              -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                              -Dsonar.projectName="User Service" \
                              -Dsonar.java.binaries=target/classes
                        '''
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Package') {
            steps {
                dir('backend/User-Service') {
                    sh 'mvn package -DskipTests'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                dir('backend/User-Service') {
                    script {
                        docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                        docker.build("${DOCKER_IMAGE}:latest")
                    }
                }
            }
        }
        
        stage('Docker Push') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push()
                        docker.image("${DOCKER_IMAGE}:latest").push()
                    }
                }
            }
        }
        
        stage('Deploy to Dev') {
            steps {
                sh '''
                    docker compose -f docker-compose.yml up -d user-service
                '''
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo '✅ Pipeline succeeded!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}
```

---

## 4️⃣ PIPELINE POUR FRONTEND ANGULAR

### Jenkinsfile pour Frontend

Créer `frontend/Jenkinsfile` :

```groovy
pipeline {
    agent any
    
    tools {
        nodejs 'NodeJS-20'
    }
    
    environment {
        DOCKER_IMAGE = "fakarni/frontend"
        DOCKER_TAG = "${BUILD_NUMBER}"
        SONAR_PROJECT_KEY = "fakarni-frontend"
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/votre-repo/fakarni.git'
            }
        }
        
        stage('Install Dependencies') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                }
            }
        }
        
        stage('Lint') {
            steps {
                dir('frontend') {
                    sh 'npm run lint || true'
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                dir('frontend') {
                    sh 'npm run test:ci'
                }
            }
            post {
                always {
                    junit 'frontend/test-results/**/*.xml'
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                dir('frontend') {
                    withSonarQubeEnv('SonarQube') {
                        sh '''
                            sonar-scanner \
                              -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                              -Dsonar.projectName="Frontend Angular" \
                              -Dsonar.sources=src \
                              -Dsonar.exclusions=**/*.spec.ts,**/node_modules/** \
                              -Dsonar.typescript.lcov.reportPaths=coverage/lcov.info
                        '''
                    }
                }
            }
        }
        
        stage('Build') {
            steps {
                dir('frontend') {
                    sh 'npm run build -- --configuration=production'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                dir('frontend') {
                    script {
                        docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                        docker.build("${DOCKER_IMAGE}:latest")
                    }
                }
            }
        }
        
        stage('Docker Push') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push()
                        docker.image("${DOCKER_IMAGE}:latest").push()
                    }
                }
            }
        }
        
        stage('Deploy to Dev') {
            steps {
                sh '''
                    docker compose -f docker-compose.yml up -d frontend
                '''
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo '✅ Pipeline succeeded!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}
```

---

## 5️⃣ PIPELINE MULTI-BRANCHES

### Jenkinsfile Global (racine du projet)

Créer `Jenkinsfile` à la racine :

```groovy
pipeline {
    agent any
    
    stages {
        stage('Build All Services') {
            parallel {
                stage('Eureka') {
                    steps {
                        build job: 'fakarni-eureka-service', wait: true
                    }
                }
                stage('Gateway') {
                    steps {
                        build job: 'fakarni-gateway-service', wait: true
                    }
                }
                stage('User Service') {
                    steps {
                        build job: 'fakarni-user-service', wait: true
                    }
                }
                stage('Chat Service') {
                    steps {
                        build job: 'fakarni-chat-service', wait: true
                    }
                }
                stage('Frontend') {
                    steps {
                        build job: 'fakarni-frontend', wait: true
                    }
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                sh '''
                    docker compose up -d
                    sleep 60
                    # Exécuter tests d'intégration
                    docker compose down
                '''
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to production?', ok: 'Deploy'
                sh '''
                    docker compose -f docker-compose.prod.yml up -d
                '''
            }
        }
    }
}
```

---

## 6️⃣ CONFIGURATION JACOCO

### Ajouter JaCoCo au pom.xml

Pour chaque microservice, ajouter dans `pom.xml` :

```xml
<build>
    <plugins>
        <!-- JaCoCo Plugin -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
                <execution>
                    <id>jacoco-check</id>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>PACKAGE</element>
                                <limits>
                                    <limit>
                                        <counter>LINE</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.50</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## 7️⃣ WEBHOOKS GITHUB/GITLAB

### GitHub Webhook

1. Aller dans **Settings → Webhooks** de votre repo
2. Ajouter un webhook :
   - Payload URL : `http://votre-jenkins:8080/github-webhook/`
   - Content type : `application/json`
   - Events : `Push events`, `Pull requests`

### GitLab Webhook

1. Aller dans **Settings → Webhooks**
2. Ajouter un webhook :
   - URL : `http://votre-jenkins:8080/project/fakarni`
   - Trigger : `Push events`, `Merge request events`

---

## 8️⃣ CRÉER LES JOBS JENKINS

### Job pour User-Service

1. **New Item** → `fakarni-user-service` → **Pipeline**
2. **Pipeline** :
   - Definition : `Pipeline script from SCM`
   - SCM : `Git`
   - Repository URL : `https://github.com/votre-repo/fakarni.git`
   - Script Path : `backend/User-Service/Jenkinsfile`
3. **Build Triggers** :
   - ✅ GitHub hook trigger for GITScm polling

### Répéter pour tous les services

- `fakarni-eureka-service`
- `fakarni-gateway-service`
- `fakarni-chat-service`
- `fakarni-tracking-service`
- etc.

---

## 9️⃣ NOTIFICATIONS

### Ajouter Slack/Email

Dans le `Jenkinsfile`, ajouter :

```groovy
post {
    success {
        slackSend(
            color: 'good',
            message: "✅ Build ${env.JOB_NAME} #${env.BUILD_NUMBER} succeeded"
        )
    }
    failure {
        slackSend(
            color: 'danger',
            message: "❌ Build ${env.JOB_NAME} #${env.BUILD_NUMBER} failed"
        )
        emailext(
            subject: "Build Failed: ${env.JOB_NAME}",
            body: "Build ${env.BUILD_NUMBER} failed. Check Jenkins for details.",
            to: "team@fakarni.com"
        )
    }
}
```

---

## 🔟 MONITORING DES PIPELINES

### Blue Ocean

Installer le plugin **Blue Ocean** pour une meilleure visualisation :

```
http://localhost:8080/blue
```

### Dashboard

Créer un dashboard avec :
- Nombre de builds réussis/échoués
- Temps moyen de build
- Couverture de code
- Quality Gates SonarQube

---

## ✅ CHECKLIST CI/CD

- [ ] Jenkins installé et accessible
- [ ] Plugins installés (Docker, Maven, NodeJS, SonarQube)
- [ ] Maven et NodeJS configurés
- [ ] SonarQube connecté
- [ ] Jenkinsfile créé pour chaque service
- [ ] Jobs Jenkins créés
- [ ] Webhooks GitHub/GitLab configurés
- [ ] JaCoCo configuré dans pom.xml
- [ ] Tests unitaires passent
- [ ] Quality Gate SonarQube OK
- [ ] Docker build fonctionne
- [ ] Déploiement automatique fonctionne

---

## 🎯 RÉSULTAT ATTENDU

Après configuration :

1. **Push sur GitHub** → Jenkins détecte automatiquement
2. **Build Maven** → Compile le code
3. **Tests** → Exécute les tests unitaires
4. **JaCoCo** → Génère le rapport de couverture
5. **SonarQube** → Analyse la qualité du code
6. **Quality Gate** → Vérifie les seuils
7. **Docker Build** → Crée l'image Docker
8. **Docker Push** → Pousse sur Docker Hub
9. **Deploy** → Redémarre le container

**Temps total : 5-10 minutes par service**

---

## 📚 RESSOURCES

- [Jenkins Pipeline Documentation](https://www.jenkins.io/doc/book/pipeline/)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
- [SonarQube Scanner](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-jenkins/)
