# Test Post-Service Authentication
Write-Host "=== Test Post-Service Authentication ===" -ForegroundColor Cyan
Write-Host ""

# 1. Login to get token
Write-Host "1. Logging in to get token..." -ForegroundColor Yellow
$loginBody = @{
    email = "test@example.com"
    password = "Password123!"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8090/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody
    
    $token = $loginResponse.accessToken
    
    if ([string]::IsNullOrEmpty($token)) {
        Write-Host "❌ Failed to get token. Please check if user exists and credentials are correct." -ForegroundColor Red
        exit 1
    }
    
    Write-Host "✅ Token obtained: $($token.Substring(0, [Math]::Min(50, $token.Length)))..." -ForegroundColor Green
    Write-Host ""
    
    # 2. Test Post-Service directly (bypass Gateway)
    Write-Host "2. Testing Post-Service directly (port 8069)..." -ForegroundColor Yellow
    try {
        $headers = @{
            "Authorization" = "Bearer $token"
        }
        $directResponse = Invoke-WebRequest -Uri "http://localhost:8069/api/posts" `
            -Method Get `
            -Headers $headers
        
        Write-Host "HTTP Status: $($directResponse.StatusCode)" -ForegroundColor Green
        Write-Host "Response: $($directResponse.Content)"
    } catch {
        Write-Host "HTTP Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host ""
    
    # 3. Test via Gateway
    Write-Host "3. Testing via Gateway (port 8090)..." -ForegroundColor Yellow
    try {
        $gatewayResponse = Invoke-WebRequest -Uri "http://localhost:8090/api/posts" `
            -Method Get `
            -Headers $headers
        
        Write-Host "HTTP Status: $($gatewayResponse.StatusCode)" -ForegroundColor Green
        Write-Host "Response: $($gatewayResponse.Content)"
    } catch {
        Write-Host "HTTP Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host ""
    
    # 4. Decode JWT to check content
    Write-Host "4. JWT Token Content:" -ForegroundColor Yellow
    $parts = $token.Split('.')
    if ($parts.Length -ge 2) {
        $payload = $parts[1]
        # Add padding if needed
        while ($payload.Length % 4 -ne 0) {
            $payload += "="
        }
        try {
            $bytes = [Convert]::FromBase64String($payload)
            $json = [System.Text.Encoding]::UTF8.GetString($bytes)
            $decoded = $json | ConvertFrom-Json
            Write-Host ($decoded | ConvertTo-Json -Depth 10)
        } catch {
            Write-Host "Could not decode token payload" -ForegroundColor Red
        }
    }
    
} catch {
    Write-Host "❌ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Make sure User-Service is running and the user exists." -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Cyan
