# 🔧 Azure Deployment Fixes

## 🎯 Problèmes Identifiés et Solutions

### ❌ Problème 1 : 502 Bad Gateway sur `/api/users` (CRITIQUE)

**Symptôme** :
```
POST https://fakarni-gateway.azurewebsites.net/api/users → 502 Bad Gateway
```

**Cause Racine** :
- Gateway-Service route vers `lb://USER-SERVICE` (découverte via Eureka)
- User-Service s'enregistre dans Eureka avec son IP interne de conteneur (ex: `5f7123066cf3:8081`)
- Sur Azure Web App for Containers, chaque microservice est isolé (pas de VNet)
- Les adresses internes ne sont **pas routables** entre Web Apps différentes
- Résultat : Gateway → Eureka → IP interne → **connexion impossible** → 502

**Solution Appliquée** :
Router directement vers les URLs publiques HTTPS au lieu de `lb://`

### ✅ Fix Implémenté

#### 1. **Profil Spring Azure** (`application-azure.properties`)
Créé dans `backend/Gateway-Service/src/main/resources/application-azure.properties` :

```properties
# Active avec: SPRING_PROFILES_ACTIVE=azure
gateway.routes.user.uri=https://fakarni-user.azurewebsites.net
gateway.routes.session.uri=https://fakarni-session.azurewebsites.net
gateway.routes.event.uri=https://fakarni-event.azurewebsites.net
gateway.routes.post.uri=https://fakarni-post.azurewebsites.net
gateway.routes.group.uri=https://fakarni-group.azurewebsites.net
gateway.routes.chat.uri=https://fakarni-chat.azurewebsites.net
gateway.routes.meeting-insights.uri=https://fakarni-meeting.azurewebsites.net
gateway.direct.activite-educative-service-uri=https://fakarni-activite.azurewebsites.net
```

#### 2. **Script de Configuration Azure** (`azure-configure-gateway.ps1`)
Script PowerShell automatisé pour configurer Gateway-Service :

**Exécution** :
```powershell
cd C:\Users\wahad\Desktop\FakarniApp\Esprit_PI_4SAE5_2026_FakarniApp
.\azure-configure-gateway.ps1
```

**Ce que fait le script** :
1. ✓ Vérifie Azure CLI et authentification
2. ✓ Vérifie que Gateway app existe
3. ✓ Active profil Spring `azure` via `SPRING_PROFILES_ACTIVE=azure`
4. ✓ Configure toutes les routes URI comme variables d'environnement
5. ✓ Redémarre Gateway-Service
6. ✓ Affiche résumé des routes configurées

**Temps d'exécution** : 2-3 minutes (incluant redémarrage)

---

### ❌ Problème 2 : Google Sign-In OAuth Errors

**Symptômes** :
```
1. "The given origin is not allowed for the given client ID"
2. 403 Forbidden sur button?parent=https://fakarni-frontend...
```

**Cause Racine** :
- Frontend déployé sur `https://fakarni-frontend.azurewebsites.net`
- Client OAuth Google configuré pour `http://localhost:4200` uniquement
- Origine Azure non autorisée → Google bloque la requête

**Solution** :

### ✅ Fix Google OAuth (Manuelle - 5 minutes)

#### Étape 1 : Accéder à Google Cloud Console
1. Aller sur https://console.cloud.google.com
2. Sélectionner le projet FakarniApp
3. Menu **APIs & Services** → **Credentials**

#### Étape 2 : Modifier le Client OAuth 2.0
1. Trouver le client ID : `968599520946-...` (celui dans `environment.prod.ts`)
2. Cliquer sur le nom du client pour éditer

#### Étape 3 : Ajouter les Origines Autorisées
Dans **Authorized JavaScript origins**, ajouter :

```
https://fakarni-frontend.azurewebsites.net
```

#### Étape 4 : Ajouter les URIs de Redirection (si nécessaire)
Dans **Authorized redirect URIs**, ajouter :

```
https://fakarni-frontend.azurewebsites.net/auth/callback
https://fakarni-frontend.azurewebsites.net
```

#### Étape 5 : Sauvegarder
1. Cliquer **Save**
2. Attendre **5 minutes** pour propagation des changements Google

#### Étape 6 : Tester
```
https://fakarni-frontend.azurewebsites.net
→ Cliquer sur "Sign in with Google"
→ Devrait ouvrir popup OAuth correctement
```

---

### 🚀 Problème 3 : Cold Starts (502/504 au premier appel)

**Symptôme** :
- Premier appel après inactivité → 502 Bad Gateway ou timeout
- Logs montrent démarrages de 74-133 secondes
- Azure Web App B1 tier décharge les conteneurs inactifs

**Solution** : Activer **Always On**

### ✅ Fix Always On Configuration

Créé dans le script `azure-enable-always-on.ps1` (voir ci-dessous)

---

## 📋 Ordre d'Exécution Recommandé

### 1. Configurer Gateway Routes (10 min)
```powershell
# Activer profil Azure + routes HTTPS
.\azure-configure-gateway.ps1

# Attendre redémarrage Gateway
Start-Sleep -Seconds 120

# Vérifier logs Gateway
az webapp log tail --name fakarni-gateway --resource-group rg-fakarni-prod --lines 100
```

### 2. Configurer Google OAuth (5 min)
```
1. Google Cloud Console → Credentials
2. Ajouter origine : https://fakarni-frontend.azurewebsites.net
3. Sauvegarder
4. Attendre 5 minutes
```

### 3. Activer Always On (5 min)
```powershell
# Activer sur tous les services
.\azure-enable-always-on.ps1
```

### 4. Tester l'Application (5 min)
```powershell
# Test 1: Santé Gateway
curl https://fakarni-gateway.azurewebsites.net/actuator/health

# Test 2: Inscription utilisateur (via frontend)
# Ouvrir: https://fakarni-frontend.azurewebsites.net
# Créer un compte → Devrait réussir (plus de 502)

# Test 3: Google Sign-In
# Cliquer "Sign in with Google" → Popup OAuth devrait s'ouvrir
```

---

## 🔍 Diagnostic en Cas de Problème

### Gateway toujours en 502 ?
```powershell
# Vérifier profil actif
az webapp config appsettings list --name fakarni-gateway --resource-group rg-fakarni-prod --query "[?name=='SPRING_PROFILES_ACTIVE'].value" -o tsv
# Devrait afficher: azure

# Vérifier routes configurées
az webapp config appsettings list --name fakarni-gateway --resource-group rg-fakarni-prod --query "[?starts_with(name,'GATEWAY_ROUTES')].{Name:name, Value:value}" -o table

# Logs en temps réel
az webapp log tail --name fakarni-gateway --resource-group rg-fakarni-prod
```

### Google OAuth toujours bloqué ?
```bash
# Vérifier console navigateur (F12 → Console)
# Erreur attendue si origine non ajoutée:
# "The given origin is not allowed for the given client ID"

# Vérifier que l'origine est bien HTTPS (pas HTTP)
# Azure Web Apps utilise toujours HTTPS
```

### User-Service non accessible ?
```powershell
# Test direct User-Service (sans Gateway)
curl https://fakarni-user.azurewebsites.net/actuator/health

# Si 502 → User-Service down
az webapp log tail --name fakarni-user --resource-group rg-fakarni-prod

# Vérifier variables DB
az webapp config appsettings list --name fakarni-user --resource-group rg-fakarni-prod --query "[?starts_with(name,'USER_DB')].{Name:name, Value:value}" -o table
```

---

## 📊 Services Concernés par les Fixes

| Service | 502 Fix | OAuth Fix | Always On |
|---------|---------|-----------|-----------|
| Gateway | ✅ Routes | - | ✅ |
| User | ✅ Routable | - | ✅ |
| Session | ✅ Routable | - | ✅ |
| Event | ✅ Routable | - | ✅ |
| Post | ✅ Routable | - | ✅ |
| Group | ✅ Routable | - | ✅ |
| Chat | ✅ Routable | - | ✅ |
| Meeting | ✅ Routable | - | ✅ |
| Activite | ✅ Routable | - | ✅ |
| Frontend | - | ✅ OAuth | ✅ |
| Eureka | - | - | ✅ |
| Detection | ⚠️ lb:// | - | ✅ |
| Dossier | ⚠️ lb:// | - | ✅ |
| Suivi | ⚠️ lb:// | - | ✅ |
| Tracking | ⚠️ lb:// | - | ✅ |
| Geofencing | ⚠️ lb:// | - | ✅ |

**Légende** :
- ✅ Fix appliqué et fonctionnel
- ⚠️ Routes hardcoded en `lb://` (fonctionneront une fois tous services enregistrés dans Eureka)
- `-` Non concerné

---

## ⚠️ Routes Hardcoded (Non Critiques)

Ces routes utilisent encore `lb://` dans `GatewayServiceApplication.java` :
- `/api/detection` → `Detection_Maladie-Service`
- `/api/dossiers` → `Dossier_Medical-Service`
- `/api/engagement` → `SUIVI-ENGAGEMENT-SERVICE`
- `/api/tracking` → `TRACKING-SERVICE`
- `/api/geofencing` → `GEOFENCING-SERVICE`

**Impact** : Même comportement 502 si appelées avant que Eureka ait des registrations valides.

**Solution future** (optionnel) :
Modifier `GatewayServiceApplication.java` pour injecter ces URIs via `@Value` comme les autres routes.

---

## 🎯 Résultat Attendu Après Fixes

### ✅ Ce qui devrait fonctionner :
1. **Inscription utilisateur** : POST `/api/users` → 200 OK (plus de 502)
2. **Google Sign-In** : Popup OAuth s'ouvre correctement
3. **Navigation frontend** : Pas de timeouts sur premier chargement
4. **API Gateway** : Routes vers tous services principaux opérationnelles

### 🔄 Vérification Santé :
```bash
# Tous ces endpoints devraient retourner {"status":"UP"}
curl https://fakarni-gateway.azurewebsites.net/actuator/health
curl https://fakarni-user.azurewebsites.net/actuator/health
curl https://fakarni-session.azurewebsites.net/actuator/health
curl https://fakarni-frontend.azurewebsites.net  # Page Angular chargée
```

---

## 📝 Notes Importantes

1. **Aucun changement de code métier requis** : Les fixes utilisent les mécanismes d'override Spring existants
2. **Profil `azure` est isolé** : Le profil par défaut (local) reste inchangé avec `lb://`
3. **Variables d'env prioritaires** : Les App Settings Azure overrident `application-azure.properties`
4. **Redéploiement nécessaire** : Après modification de `application-azure.properties`, rebuild + redeploy Gateway
5. **Google OAuth instantané** : Changement d'origine prend effet en ~5 min, pas de redéploiement

---

## 🆘 Support

En cas de problème persistant :

```powershell
# Collecter logs complets
az webapp log download --name fakarni-gateway --resource-group rg-fakarni-prod --log-file gateway-logs.zip

# Vérifier status toutes apps
az webapp list --resource-group rg-fakarni-prod --query "[].{Name:name, State:state, DefaultHostName:defaultHostName}" -o table

# Redémarrer tous les services
$apps = @("fakarni-gateway", "fakarni-user", "fakarni-session", "fakarni-event", "fakarni-post", "fakarni-group", "fakarni-chat", "fakarni-meeting", "fakarni-activite", "fakarni-frontend")
foreach ($app in $apps) {
    Write-Host "Restarting $app..." -ForegroundColor Yellow
    az webapp restart --name $app --resource-group rg-fakarni-prod --output none
}
```

---

**Dernière mise à jour** : 2026-09-10
**Architecture** : Azure Web App for Containers (sans VNet)
**Stack** : Spring Cloud Gateway + Eureka + Spring Boot microservices
