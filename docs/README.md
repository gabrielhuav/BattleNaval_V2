# Documentación de BitBattles (BattleNaval_V2)

Esta carpeta concentra el contexto que necesita cualquier persona —o cualquier
asistente de IA— para trabajar en el repositorio sin tener que reconstruirlo
leyendo todo el código.

| Documento | Para qué sirve |
| :--- | :--- |
| [CONTEXTO-PEDAGOGICO.md](CONTEXTO-PEDAGOGICO.md) | Marco de la tesis MADEMS: problema educativo, teoría y secuencia didáctica. Explica *por qué* existe cada módulo. |
| [ARQUITECTURA.md](ARQUITECTURA.md) | Mapa de paquetes y clases, y su correspondencia con los capítulos de la tesis. |
| [ANDROID-16.md](ANDROID-16.md) | Migración a targetSdk 36 y qué queda pendiente para API 37. |
| [MIGRACION-COMPOSE.md](MIGRACION-COMPOSE.md) | Propuesta de migración a Jetpack Compose: diagnóstico, fases y criterios. Aún no iniciada. |
| [RELEASE-Y-FIRMA.md](RELEASE-Y-FIRMA.md) | Archivos de firma, dónde viven, y cómo publicar una versión a mano. |
| [CI-CD.md](CI-CD.md) | Workflows de GitHub Actions y los secrets que necesitan. |

## Punto de entrada rápido

- **App:** BitBattles en Google Play — `ovh.gabrielhuav.battlenaval_v2`
- **Lenguaje:** Kotlin, vistas XML (no Compose), AGP 9.1.1, Gradle 9.3.1, JDK 17
- **minSdk 24 / targetSdk 36**
- **Módulo único:** `app/`, fuentes en `app/src/main/kotlin/`

## Advertencia sobre la ruta local

El checkout principal vive en una ruta con acento (`…/Imágenes/…`). El Android
Gradle Plugin **rechaza rutas con caracteres no ASCII** y falla en la
configuración. Para compilar en local hay que trabajar desde una copia en una
ruta ASCII, o dejar que el CI de GitHub Actions haga la compilación. Ver
[RELEASE-Y-FIRMA.md](RELEASE-Y-FIRMA.md).
