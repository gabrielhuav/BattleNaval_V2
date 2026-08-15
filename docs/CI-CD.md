# CI/CD con GitHub Actions

Dos workflows en `.github/workflows/`:

| Workflow | Cuándo corre | Qué hace |
| :--- | :--- | :--- |
| `android-ci.yml` | push y PR a `main` | Compila debug y corre lint. No firma nada. |
| `release-play-store.yml` | tag `v*` o ejecución manual | Compila el AAB firmado, lo guarda como artefacto y lo sube a Google Play. |

Compilar en el runner resuelve de paso el problema de la ruta con acento del
checkout local: Ubuntu trabaja en `/home/runner/work/...`, que es ASCII.

## Secrets necesarios

`Settings → Secrets and variables → Actions → New repository secret`

| Secret | Contenido |
| :--- | :--- |
| `KEYSTORE_BASE64` | `APPPlayStore.jks` codificado en base64 |
| `KEYSTORE_PASSWORD` | `storePassword` de `key.properties` |
| `KEY_ALIAS` | `keyAlias` de `key.properties` (`bitbattles`) |
| `KEY_PASSWORD` | `keyPassword` de `key.properties` |
| `GOOGLE_SERVICES_JSON` | `app/google-services.json` codificado en base64 |
| `PLAY_SERVICE_ACCOUNT_JSON` | JSON completo de la cuenta de servicio de Google Cloud |

Los dos valores en base64 se generan con el script incluido:

```powershell
PowerShell -ExecutionPolicy Bypass -File .\generar-secrets-ci.ps1
```

Deja los `.txt` en `_sensitive_backup\secrets-github\` (carpeta ignorada por
git). Copia el contenido a GitHub y **borra la carpeta después**.

## Cuenta de servicio para la Play Developer API

Es el permiso que deja al workflow subir versiones sin intervención manual.

1. **Google Play Console → Configuración → Acceso a API.** Vincular un proyecto
   de Google Cloud si aún no lo está.
2. En **Google Cloud Console → IAM y administración → Cuentas de servicio**,
   crear una cuenta (por ejemplo `github-actions-bitbattles`).
3. En esa cuenta, **Claves → Agregar clave → Crear clave nueva → JSON**. Se
   descarga el archivo; ese es el valor de `PLAY_SERVICE_ACCOUNT_JSON`.
4. De vuelta en Play Console → **Usuarios y permisos**, invitar a la cuenta de
   servicio y darle permiso sobre la app BitBattles:
   - *Ver información de la app*
   - *Administrar versiones de producción, exclusión y prueba*
   - *Crear y editar versiones en borrador*
5. Guardar el JSON fuera del repositorio. `.gitignore` ya cubre
   `*service-account*.json`, `play-store-api.json` y `api-*.json`.

## Entorno protegido (opcional pero recomendable)

`release-play-store.yml` declara `environment: play-store`. Si creas ese
entorno en `Settings → Environments` y le añades *required reviewers*, cada
publicación quedará esperando tu aprobación explícita antes de tocar Play.
Si no lo creas, el workflow corre igual pero sin esa pausa.

## Publicar una versión

```bash
# 1. Subir versionCode y versionName en app/build.gradle.kts, commit y push.
# 2. Etiquetar:
git tag v1.0.0.11
git push origin v1.0.0.11
```

El tag dispara el workflow, que sube al track `internal` con estado `draft`.

Para elegir track y estado: pestaña **Actions → Release a Google Play → Run
workflow**, y seleccionar `internal`/`alpha`/`beta`/`production` y
`draft`/`completed`.

El `versionCode` debe ser mayor al de la última versión publicada; si se
repite, Play rechaza la subida.

## Notas de las versiones

Los textos de «Novedades» viven en `distribution/whatsnew/`, un archivo por
idioma con el nombre `whatsnew-<locale>`:

```
distribution/whatsnew/whatsnew-es-MX
distribution/whatsnew/whatsnew-en-US
```

Actualízalos junto con el `versionCode`; el workflow los envía tal cual.
