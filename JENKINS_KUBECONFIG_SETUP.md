# Jenkins Kubeconfig Setup Guide

## Problem
Jenkins cannot connect to your Kubernetes cluster because the kubeconfig uses `kubernetes.docker.internal:6443` which is not accessible from inside the Jenkins container.

## Solution
Use the modified kubeconfig file: `jenkins-kubeconfig.yaml`

This file uses `host.docker.internal:6443` instead, which Docker automatically resolves to the host machine.

## Steps to Update Jenkins Credential

### 1. Open the kubeconfig file
Open `jenkins-kubeconfig.yaml` in this directory

### 2. Copy the entire content
Select all and copy (Ctrl+A, Ctrl+C)

### 3. Update Jenkins Credential
1. Go to Jenkins: http://localhost:8080 (or your Jenkins URL)
2. Click **Manage Jenkins** → **Credentials**
3. Click on **(global)** domain
4. Find the **kubeconfig** credential and click on it
5. Click **Update** on the left sidebar
6. In the **Content** field:
   - Select **Enter directly**
   - Paste the entire content from `jenkins-kubeconfig.yaml`
7. Click **Save**

### 4. Test the Pipeline
Run your `user-service-CD` pipeline again. It should now connect successfully!

## What Changed?
- **Before**: `server: https://kubernetes.docker.internal:6443`
- **After**: `server: https://host.docker.internal:6443`

`host.docker.internal` is a special DNS name that Docker provides to containers to reach the host machine.

## Verification
After updating, the pipeline should:
1. ✅ Connect to Kubernetes cluster
2. ✅ Apply the deployment
3. ✅ Check rollout status
4. ✅ Verify pods are running

## Troubleshooting
If it still fails:
- Make sure Docker Desktop Kubernetes is running (check Docker Desktop settings)
- Verify Jenkins container can reach the host: `docker exec <jenkins-container> ping host.docker.internal`
- Check if port 6443 is accessible from Jenkins container
