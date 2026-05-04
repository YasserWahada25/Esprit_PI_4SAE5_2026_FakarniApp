# 📊 GUIDE PHASE 2 - MONITORING & OBSERVABILITÉ

## 🎯 OBJECTIF
Ajouter le monitoring avancé après que la Phase 1 fonctionne parfaitement.

---

## ⚠️ PRÉREQUIS

**Avant de commencer la Phase 2, assurez-vous que :**
- ✅ Phase 1 complète et fonctionnelle
- ✅ Tous les services UP dans Docker
- ✅ Tests fonctionnels passent
- ✅ Architecture stable depuis au moins 24h

---

## 📋 STACK MONITORING

```
Prometheus → Collecte des métriques
Grafana → Visualisation
Loki → Logs centralisés
Tempo → Tracing distribué
Alertmanager → Alertes
```

---

## 1️⃣ PROMETHEUS + GRAFANA

### Ajouter au docker-compose.yml

```yaml
  # ==========================================
  # MONITORING - PROMETHEUS
  # ==========================================
  prometheus:
    image: prom/prometheus:latest
    container_name: fakarni_prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - fakarni_prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
    networks:
      - fakarni-net
    profiles:
      - monitoring

  # ==========================================
  # MONITORING - GRAFANA
  # ==========================================
  grafana:
    image: grafana/grafana:latest
    container_name: fakarni_grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_INSTALL_PLUGINS=grafana-piechart-panel
    volumes:
      - fakarni_grafana_data:/var/lib/grafana
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning
    depends_on:
      - prometheus
    networks:
      - fakarni-net
    profiles:
      - monitoring

volumes:
  fakarni_prometheus_data:
  fakarni_grafana_data:
```

### Créer la configuration Prometheus

Créer `monitoring/prometheus/prometheus.yml` :

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  # Eureka
  - job_name: 'eureka'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['eureka-server:8761']

  # Gateway
  - job_name: 'gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['api-gateway:8090']

  # User Service
  - job_name: 'user-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['user-service:8081']

  # Chat Service
  - job_name: 'chat-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['chat-service:8070']

  # Tracking Service
  - job_name: 'tracking-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['tracking-service:9011']

  # Geofencing Service
  - job_name: 'geofencing-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['geofencing-service:9012']

  # Activité Service
  - job_name: 'activite-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['activite-educative-service:8084']

  # Detection Service
  - job_name: 'detection-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['detection-maladie-service:8058']

  # Dossier Service
  - job_name: 'dossier-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['dossier-medical-service:8059']

  # Event Service
  - job_name: 'event-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['event-service:8087']

  # Group Service
  - job_name: 'group-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['group-service:8097']

  # Meeting Service
  - job_name: 'meeting-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['meeting-insights-service:8096']

  # Post Service
  - job_name: 'post-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['post-service:8069']

  # Suivi Service
  - job_name: 'suivi-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['suivi-engagement-service:8088']

  # MongoDB Exporter
  - job_name: 'mongodb'
    static_configs:
      - targets: ['mongodb-exporter:9216']

  # MySQL Exporter
  - job_name: 'mysql'
    static_configs:
      - targets: ['mysql-exporter:9104']
```

### Ajouter Micrometer dans les microservices

Dans chaque `pom.xml` :

```xml
<dependencies>
    <!-- Micrometer Prometheus -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
    
    <!-- Actuator -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

Dans `application-docker.properties` :

```properties
# Actuator endpoints
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
```

### Démarrer avec monitoring

```bash
docker compose --profile monitoring up -d
```

### Accéder aux dashboards

**Prometheus :** http://localhost:9090
**Grafana :** http://localhost:3000 (admin/admin)

---

## 2️⃣ DASHBOARDS GRAFANA

### Importer des dashboards prêts

Dans Grafana :
1. **+ → Import**
2. Importer ces dashboards :
   - **Spring Boot 2.1 Statistics** : ID `11378`
   - **JVM (Micrometer)** : ID `4701`
   - **MySQL Overview** : ID `7362`
   - **MongoDB Overview** : ID `2583`

### Dashboard personnalisé Fakarni

Créer `monitoring/grafana/provisioning/dashboards/fakarni.json` :

```json
{
  "dashboard": {
    "title": "Fakarni - Overview",
    "panels": [
      {
        "title": "Services UP",
        "targets": [
          {
            "expr": "up{job=~\".*-service\"}"
          }
        ]
      },
      {
        "title": "Requests per second",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[1m])"
          }
        ]
      },
      {
        "title": "Response time (p95)",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))"
          }
        ]
      },
      {
        "title": "Error rate",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{status=~\"5..\"}[1m])"
          }
        ]
      }
    ]
  }
}
```

---

## 3️⃣ LOKI - LOGS CENTRALISÉS

### Ajouter Loki au docker-compose.yml

```yaml
  # ==========================================
  # LOGGING - LOKI
  # ==========================================
  loki:
    image: grafana/loki:latest
    container_name: fakarni_loki
    ports:
      - "3100:3100"
    volumes:
      - ./monitoring/loki/loki-config.yml:/etc/loki/local-config.yaml
      - fakarni_loki_data:/loki
    command: -config.file=/etc/loki/local-config.yaml
    networks:
      - fakarni-net
    profiles:
      - monitoring

  # ==========================================
  # LOGGING - PROMTAIL
  # ==========================================
  promtail:
    image: grafana/promtail:latest
    container_name: fakarni_promtail
    volumes:
      - ./monitoring/promtail/promtail-config.yml:/etc/promtail/config.yml
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
      - /var/run/docker.sock:/var/run/docker.sock
    command: -config.file=/etc/promtail/config.yml
    depends_on:
      - loki
    networks:
      - fakarni-net
    profiles:
      - monitoring

volumes:
  fakarni_loki_data:
```

### Configuration Loki

Créer `monitoring/loki/loki-config.yml` :

```yaml
auth_enabled: false

server:
  http_listen_port: 3100

ingester:
  lifecycler:
    address: 127.0.0.1
    ring:
      kvstore:
        store: inmemory
      replication_factor: 1
  chunk_idle_period: 5m
  chunk_retain_period: 30s

schema_config:
  configs:
    - from: 2020-05-15
      store: boltdb
      object_store: filesystem
      schema: v11
      index:
        prefix: index_
        period: 168h

storage_config:
  boltdb:
    directory: /loki/index
  filesystem:
    directory: /loki/chunks

limits_config:
  enforce_metric_name: false
  reject_old_samples: true
  reject_old_samples_max_age: 168h
```

### Configuration Promtail

Créer `monitoring/promtail/promtail-config.yml` :

```yaml
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: docker
    docker_sd_configs:
      - host: unix:///var/run/docker.sock
        refresh_interval: 5s
    relabel_configs:
      - source_labels: ['__meta_docker_container_name']
        regex: '/(.*)'
        target_label: 'container'
      - source_labels: ['__meta_docker_container_log_stream']
        target_label: 'stream'
```

### Ajouter Loki dans Grafana

1. **Configuration → Data Sources → Add data source**
2. Choisir **Loki**
3. URL : `http://loki:3100`
4. Save & Test

---

## 4️⃣ ALERTMANAGER

### Ajouter au docker-compose.yml

```yaml
  # ==========================================
  # ALERTING - ALERTMANAGER
  # ==========================================
  alertmanager:
    image: prom/alertmanager:latest
    container_name: fakarni_alertmanager
    ports:
      - "9093:9093"
    volumes:
      - ./monitoring/alertmanager/alertmanager.yml:/etc/alertmanager/alertmanager.yml
    command:
      - '--config.file=/etc/alertmanager/alertmanager.yml'
    networks:
      - fakarni-net
    profiles:
      - monitoring
```

### Configuration Alertmanager

Créer `monitoring/alertmanager/alertmanager.yml` :

```yaml
global:
  resolve_timeout: 5m

route:
  group_by: ['alertname', 'cluster', 'service']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  receiver: 'email'

receivers:
  - name: 'email'
    email_configs:
      - to: 'team@fakarni.com'
        from: 'alertmanager@fakarni.com'
        smarthost: 'smtp.gmail.com:587'
        auth_username: 'votre-email@gmail.com'
        auth_password: 'votre-mot-de-passe-app'
        headers:
          Subject: '🚨 [Fakarni] {{ .GroupLabels.alertname }}'

  - name: 'slack'
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK'
        channel: '#alerts'
        title: '🚨 {{ .GroupLabels.alertname }}'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
```

### Règles d'alerte Prometheus

Créer `monitoring/prometheus/alerts.yml` :

```yaml
groups:
  - name: fakarni_alerts
    interval: 30s
    rules:
      # Service DOWN
      - alert: ServiceDown
        expr: up == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Service {{ $labels.job }} is down"
          description: "{{ $labels.job }} has been down for more than 1 minute"

      # High error rate
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High error rate on {{ $labels.job }}"
          description: "Error rate is {{ $value }} on {{ $labels.job }}"

      # High response time
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time on {{ $labels.job }}"
          description: "95th percentile response time is {{ $value }}s"

      # High CPU
      - alert: HighCPU
        expr: process_cpu_usage > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High CPU usage on {{ $labels.job }}"
          description: "CPU usage is {{ $value }}"

      # High memory
      - alert: HighMemory
        expr: jvm_memory_used_bytes / jvm_memory_max_bytes > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage on {{ $labels.job }}"
          description: "Memory usage is {{ $value }}"

      # Database connection pool exhausted
      - alert: DatabasePoolExhausted
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool almost exhausted on {{ $labels.job }}"
          description: "{{ $value }} of connections are in use"
```

Modifier `prometheus.yml` :

```yaml
rule_files:
  - 'alerts.yml'

alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']
```

---

## 5️⃣ NGINX REVERSE PROXY

### Ajouter Nginx au docker-compose.yml

```yaml
  # ==========================================
  # REVERSE PROXY - NGINX
  # ==========================================
  nginx:
    image: nginx:alpine
    container_name: fakarni_nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/ssl:/etc/nginx/ssl
    depends_on:
      - frontend
      - api-gateway
    networks:
      - fakarni-net
    profiles:
      - production
```

### Configuration Nginx

Créer `nginx/nginx.conf` :

```nginx
events {
    worker_connections 1024;
}

http {
    upstream frontend {
        server frontend:4000;
    }

    upstream api {
        server api-gateway:8090;
    }

    upstream eureka {
        server eureka-server:8761;
    }

    upstream grafana {
        server grafana:3000;
    }

    # Redirect HTTP to HTTPS
    server {
        listen 80;
        server_name fakarni.local;
        return 301 https://$server_name$request_uri;
    }

    # HTTPS
    server {
        listen 443 ssl http2;
        server_name fakarni.local;

        ssl_certificate /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;

        # Frontend
        location / {
            proxy_pass http://frontend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # API
        location /api/ {
            proxy_pass http://api/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Eureka
        location /eureka/ {
            proxy_pass http://eureka/;
            proxy_set_header Host $host;
        }

        # Grafana
        location /grafana/ {
            proxy_pass http://grafana/;
            proxy_set_header Host $host;
        }
    }
}
```

### Générer certificats SSL auto-signés

```bash
mkdir -p nginx/ssl
cd nginx/ssl

openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout key.pem -out cert.pem \
  -subj "/C=TN/ST=Tunis/L=Tunis/O=Fakarni/CN=fakarni.local"
```

---

## 6️⃣ DÉMARRAGE COMPLET

### Avec monitoring

```bash
docker compose --profile monitoring up -d
```

### Avec production (Nginx)

```bash
docker compose --profile production up -d
```

### Tout ensemble

```bash
docker compose --profile monitoring --profile production up -d
```

---

## 📊 URLS FINALES

| Service | URL |
|---------|-----|
| Frontend | https://fakarni.local |
| API | https://fakarni.local/api |
| Eureka | https://fakarni.local/eureka |
| Prometheus | http://localhost:9090 |
| Grafana | https://fakarni.local/grafana |
| Alertmanager | http://localhost:9093 |
| SonarQube | http://localhost:9000 |

---

## ✅ CHECKLIST PHASE 2

- [ ] Prometheus collecte les métriques
- [ ] Grafana affiche les dashboards
- [ ] Loki centralise les logs
- [ ] Alertmanager envoie les alertes
- [ ] Nginx reverse proxy fonctionne
- [ ] HTTPS configuré
- [ ] Dashboards personnalisés créés
- [ ] Alertes configurées et testées

---

## 🎯 PROCHAINES ÉTAPES (PHASE 3)

- Kubernetes (K8s)
- Helm Charts
- ArgoCD (GitOps)
- Service Mesh (Istio)
- Vault (Secrets management)
