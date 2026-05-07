// Script Groovy pour configurer tous les jobs Jenkins
// À exécuter dans Jenkins Script Console: Manage Jenkins → Script Console

import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import hudson.plugins.git.GitSCM
import hudson.plugins.git.BranchSpec

def jenkins = Jenkins.instance

// Configuration commune
def gitUrl = 'https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git'
def credentialsId = 'github-credentials'
def branch = '*/main'

// Liste des services backend
def backendServices = [
    'User-Service',
    'Gateway-Service',
    'Eureka-Service',
    'Chat_Service',
    'Post-Service',
    'Event-Service',
    'Geofencing-Service',
    'Tracking-Service',
    'session-service',
    'group',
    'Dossier_Medical-service',
    'Detection_Maladie-Service',
    'activite-educative-service',
    'suivi-engagement-service',
    'meeting-insights-service'
]

// Fonction pour créer/mettre à jour un job
def createOrUpdateJob(String jobName, String scriptPath) {
    def job = jenkins.getItem(jobName)
    
    if (job == null) {
        job = jenkins.createProject(WorkflowJob, jobName)
        println "✅ Created job: ${jobName}"
    } else {
        println "🔄 Updating job: ${jobName}"
    }
    
    // Configurer le SCM
    def scm = new GitSCM(gitUrl)
    scm.branches = [new BranchSpec(branch)]
    scm.userRemoteConfigs[0].credentialsId = credentialsId
    
    // Configurer le pipeline
    def definition = new CpsScmFlowDefinition(scm, scriptPath)
    definition.lightweight = true
    job.definition = definition
    
    job.save()
    println "   Script Path: ${scriptPath}"
}

println "🚀 Starting Jenkins Jobs Configuration..."
println "=" * 60

// 1. Créer les jobs orchestrateurs
println "\n📋 Creating Orchestrator Jobs..."
createOrUpdateJob('fakarni-orchestrator', 'Jenkinsfile.orchestrator')
createOrUpdateJob('fakarni-ci-all', 'Jenkinsfile.ci-all')

// 2. Créer les jobs backend
println "\n🔧 Creating Backend Service Jobs..."
backendServices.each { service ->
    def serviceName = service.toLowerCase().replaceAll('_', '-').replaceAll(' ', '-')
    
    // Job CI
    createOrUpdateJob(
        "${serviceName}-CI",
        "backend/${service}/Jenkinsfile"
    )
    
    // Job CD
    createOrUpdateJob(
        "${serviceName}-CD",
        "backend/${service}/Jenkinsfile.cd"
    )
}

// 3. Créer les jobs frontend
println "\n🎨 Creating Frontend Jobs..."
createOrUpdateJob('frontend-CI', 'frontend/Jenkinsfile')
createOrUpdateJob('frontend-CD', 'frontend/Jenkinsfile.cd')

// 4. Créer les jobs Python
println "\n🐍 Creating Python Service Jobs..."
createOrUpdateJob('detection-alzheimer-CI', 'detection-alzheimer/detection-alzheimer/Jenkinsfile')
createOrUpdateJob('detection-alzheimer-CD', 'detection-alzheimer/detection-alzheimer/Jenkinsfile.cd')

println "\n" + "=" * 60
println "✅ Jenkins Jobs Configuration Complete!"
println "=" * 60
println "\n📝 Summary:"
println "   - Orchestrator jobs: 2"
println "   - Backend services: ${backendServices.size() * 2} (CI + CD)"
println "   - Frontend jobs: 2"
println "   - Python jobs: 2"
println "   - Total jobs: ${2 + (backendServices.size() * 2) + 2 + 2}"
println "\n🎯 Next steps:"
println "   1. Verify jobs in Jenkins UI"
println "   2. Run a test build"
println "   3. Check logs for any issues"
