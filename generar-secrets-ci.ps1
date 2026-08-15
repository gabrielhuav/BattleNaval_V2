# ============================================
# Generador de secrets para el CI/CD de GitHub Actions
# ============================================
# Convierte los archivos binarios de firma a base64 y los deja en
# _sensitive_backup\secrets-github\ para copiarlos a
# GitHub > Settings > Secrets and variables > Actions.
#
#   PowerShell -ExecutionPolicy Bypass -File .\generar-secrets-ci.ps1
#
# La carpeta de salida esta ignorada por git. Borrala cuando termines.
# ============================================

param(
    [string]$Keystore = ".\APPPlayStore.jks",
    [string]$GoogleServices = ".\app\google-services.json"
)

$salida = ".\_sensitive_backup\secrets-github"

Write-Host ""
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host "  Generando secrets para GitHub Actions" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""

if (-not (Test-Path $salida)) {
    New-Item -ItemType Directory -Path $salida -Force | Out-Null
}

function ConvertTo-Base64File {
    param([string]$Origen, [string]$NombreSecret)

    if (-not (Test-Path $Origen)) {
        Write-Host "FALTA: $Origen" -ForegroundColor Red
        Write-Host "  Restauralo primero con: .\verificar-repo.ps1 -Restaurar" -ForegroundColor Gray
        Write-Host ""
        return
    }

    $bytes = [System.IO.File]::ReadAllBytes((Resolve-Path $Origen))
    $b64 = [System.Convert]::ToBase64String($bytes)
    $destino = Join-Path $salida "$NombreSecret.txt"
    [System.IO.File]::WriteAllText((Join-Path (Get-Location) $destino), $b64)

    Write-Host "OK  $NombreSecret" -ForegroundColor Green
    Write-Host "    origen : $Origen" -ForegroundColor Gray
    Write-Host "    salida : $destino" -ForegroundColor Gray
    Write-Host ""
}

ConvertTo-Base64File -Origen $Keystore -NombreSecret "KEYSTORE_BASE64"
ConvertTo-Base64File -Origen $GoogleServices -NombreSecret "GOOGLE_SERVICES_JSON"

# Recordatorio de los secrets que se copian tal cual, sin base64.
Write-Host "---------------------------------------" -ForegroundColor Cyan
Write-Host "  Secrets restantes (texto plano)" -ForegroundColor Cyan
Write-Host "---------------------------------------" -ForegroundColor Cyan
Write-Host ""
Write-Host "  KEYSTORE_PASSWORD          -> storePassword de key.properties" -ForegroundColor Gray
Write-Host "  KEY_ALIAS                  -> keyAlias de key.properties" -ForegroundColor Gray
Write-Host "  KEY_PASSWORD               -> keyPassword de key.properties" -ForegroundColor Gray
Write-Host "  PLAY_SERVICE_ACCOUNT_JSON  -> contenido completo del JSON de la" -ForegroundColor Gray
Write-Host "                                cuenta de servicio de Google Cloud" -ForegroundColor Gray
Write-Host ""
Write-Host "Cuando termines de pegarlos en GitHub, borra la carpeta:" -ForegroundColor Yellow
Write-Host "  Remove-Item $salida -Recurse -Force" -ForegroundColor Yellow
Write-Host ""
