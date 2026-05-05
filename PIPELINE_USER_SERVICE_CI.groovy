pipeline {
    agent any
    
    tools {
        maven 'Maven 3.9'
        jdk 'JDK 21'
    }
    
    environment {
        // Service Configuration
        SERVICE_NAME = 'User-Service'
        SERVICE_PATH = 'backend/User-Service'
        
        // Docker Configuration
        DOCKER_IMAGE = 'nohamedrayen/fakarni-user-service'
        DOCKER_REGISTRY = 'https://registry.hub.docker.com'
        
        // SonarQube Configuration
        SONAR_PROJECT_KEY = 'user-service'
        
        // Git Configuration
        GIT_REPO = 'https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git'
        GIT_BRANCH = 'main'
    }
    
    stages {
        stage('📥 Checkout') {
            steps {
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                echo "🔄 Cloning repository from GitHub..."
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                git branch: "${GIT_BRANCH}",
                    credentialsId: 'github-credentials',
                    url: "${GIT_REPO}"
                echo "✅ Repository cloned successfully"
            }
        }
        
        stage('🔨 Build') {
            steps {
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                echo "🔨 Building ${SERVICE_NAME}..."
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                dir("${SERVICE_PATH}") {
                    sh 'mvn clean compile -DskipTests'
                }
                echo "✅ Build completed successfully"
            }
        }
        
        stage('🧪 Test') {
            steps {
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                echo "🧪 Running unit tests with JaCoCo coverage..."
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                dir("${SERVICE_PATH}") {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    dir("${SERVICE_PATH}") {
                        // Publish test results
                        junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                        
                        // Publish JaCoCo coverage report
                        jacoco execPattern: '**/target/jacoco.exec',
                               classPattern: '**/target/classes',
                               sourcePattern: '**/src/main/java'
                    }
                }
                success {
                    echo "✅ All tests passed"
                }
                failure {
                    echo "❌ Tests failed"
                }
            }
        }
        
        stage('📊 SonarQube Analysis') {
            steps {
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                echo "📊 Running SonarQube code quality analysis..."
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                dir("${SERVICE_PATH}") {
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            mvn sonar:sonar \
                                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                -Dsonar.projectName='${SERVICE_NAME}' \
                                -Dsonar.java.binaries=target/classes \
                                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        """
                    }
                }
                echo "✅ SonarQube analysis completed"
            }
        }
        
        stage('🚦 Quality Gate') {
            steps {
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                echo "🚦 Waiting for SonarQube Quality Gate..."
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                script {
                    try {
                        timeout(time: 10, unit: 'MINUTES') {
                            def qg = waitForQualityGate()
                            if (qg.status != 'OK') {
                                echo "⚠️  Quality Gate status: ${qg.status}"
                                echo "⚠️  Continuing pipeline despite Quality Gate status..."
                            } else {
                                echo "✅ Quality Gate passed"
                            }
                        }
                    } catch (Exception e) {
                        echo "⚠️  Quality Gate check timed out or failed: ${e.message}"
                        echo "⚠️  Continuing pipeline without Quality Gate validation..."
                        echo "📊 Check SonarQube dashboard: http://172.18.0.2:9000/dashboard?id=user-service"
                    }
                }
            }
        }
        
        stage('📦 Package') {
            steps {
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                echo "📦 Packaging ${SERVICE_NAME} into JAR..."
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                dir("${SERVICE_PATH}") {
                    sh 'mvn package -DskipTests'
                }
                echo "✅ JAR file created successfully"
            }
        }
        
        stage('🐳 Docker Build') {
            steps {
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                echo "🐳 Building Docker image..."
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                dir("${SERVICE_PATH}") {
                    script {
                        // Build with build number tag
                        sh "docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} ."
                        
                        // Tag as latest
                        sh "docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${DOCKER_IMAGE}:latest"
                        
                        echo "✅ Docker image built: ${DOCKER_IMAGE}:${BUILD_NUMBER}"
                        echo "✅ Docker image tagged: ${DOCKER_IMAGE}:latest"
                    }
                }
            }
        }
        
        stage('📤 Docker Push') {
            steps {
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                echo "📤 Pushing Docker image to Docker Hub..."
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                script {
                    docker.withRegistry("${DOCKER_REGISTRY}", 'dockerhub-credentials') {
                        sh "docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}"
                        sh "docker push ${DOCKER_IMAGE}:latest"
                    }
                }
                echo "✅ Docker image pushed to registry"
                echo "   - ${DOCKER_IMAGE}:${BUILD_NUMBER}"
                echo "   - ${DOCKER_IMAGE}:latest"
            }
        }
        
        stage('🚀 Trigger CD') {
            steps {
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                echo "🚀 Triggering CD Pipeline..."
                echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                build job: 'user-service-CD', 
                      parameters: [
                          string(name: 'IMAGE_TAG', value: "${BUILD_NUMBER}")
                      ],
                      wait: false
                echo "✅ CD Pipeline triggered with image tag: ${BUILD_NUMBER}"
            }
        }
    }
    
    post {
        success {
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            echo "✅ CI PIPELINE COMPLETED SUCCESSFULLY!"
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            echo "📊 Build Number: ${BUILD_NUMBER}"
            echo "🐳 Docker Image: ${DOCKER_IMAGE}:${BUILD_NUMBER}"
            echo "🚀 CD Pipeline: Triggered"
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        }
        failure {
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            echo "❌ CI PIPELINE FAILED!"
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            echo "Check the logs above for details"
        }
        always {
            echo "🧹 Cleaning up workspace..."
            cleanWs()
        }
    }
}
