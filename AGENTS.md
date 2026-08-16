# Guía para asistentes de IA

BitBattles (`BattleNaval_V2`) es una app Android educativa desarrollada como
parte de una tesis de la Maestría en Docencia para la Educación Media Superior
(MADEMS, UNAM). Enseña sistema binario, código ASCII y unidades de
almacenamiento a estudiantes de bachillerato del CCH Naucalpan mediante una
batalla naval con coordenadas binarias.

**Antes de proponer cambios, lee [`docs/`](docs/README.md).** Ahí está el
contexto que no se deduce del código.

| Si vas a… | Lee |
| :--- | :--- |
| Cambiar reglas de juego o contenido didáctico | [docs/CONTEXTO-PEDAGOGICO.md](docs/CONTEXTO-PEDAGOGICO.md) |
| Tocar código de cualquier módulo | [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md) |
| Subir el nivel de API o resolver avisos de Play | [docs/ANDROID-16.md](docs/ANDROID-16.md) |
| Compilar, firmar o publicar | [docs/RELEASE-Y-FIRMA.md](docs/RELEASE-Y-FIRMA.md) |
| Tocar los workflows | [docs/CI-CD.md](docs/CI-CD.md) |
| Proponer o hacer cambios de arquitectura de UI | [docs/MIGRACION-COMPOSE.md](docs/MIGRACION-COMPOSE.md) |

## Hechos que ahorran tiempo

- **Kotlin, migración a Compose en curso.** Conviven vistas XML y Compose. Antes
  de tocar UI lee [docs/MIGRACION-COMPOSE.md](docs/MIGRACION-COMPOSE.md), que
  lleva el estado por fases y la receta para migrar una pantalla.
  Módulo único `app/`, fuentes en `app/src/main/kotlin/`.
- **Para migrar un fragmento:** hereda de `ui/ComposeFragment.kt`, usa los
  componentes de `ui/components/Lesson.kt` y envuelve con `BitBattlesTheme`.
- **Las reglas del juego viven en `BoardModel`**, en Kotlin puro y con tests.
  `Board` y `Cell` solo dibujan y delegan ahí. No metas lógica de juego en las
  vistas ni `android.*` en `BoardModel`, `Ship` o `BinaryConversionHelper`:
  perderían la testabilidad en JVM.
- **Hay 28 tests JVM** en `app/src/test`. Córrelos con
  `./gradlew :app:testDebugUnitTest` antes de dar por buena cualquier cosa que
  toque la lógica del juego.
- **Todavía no hay ViewModels.** El estado de partida sigue en las Activities;
  por eso el manifest usa `configChanges` en vez de dejar que se recreen.
- **minSdk 24, targetSdk 36, compileSdk 36.** AGP 9.1.1, Gradle 9.3.1, JDK 17.
- **La ruta local del checkout tiene un acento** (`…/Imágenes/…`) y AGP se
  niega a compilar en rutas no ASCII. Para compilar hay que copiar el proyecto
  a una ruta ASCII o usar el CI. No es un fallo del proyecto.
- **`key.properties`, `*.jks` y `google-services.json` no están en git.** Si
  faltan, `bundleRelease` genera un AAB sin firmar en lugar de fallar; el resto
  de la compilación necesita `app/google-services.json` porque el plugin de
  Google Services lo exige.
- **La navegación atrás usa `OnBackPressedDispatcher`.** Desde API 36 el
  sistema ya no llama `onBackPressed()`; no reintroduzcas ese override.
- **No hay bloqueo de orientación.** Se quitó `android:screenOrientation` de
  todas las actividades por los requisitos de pantalla grande de Play. Los
  layouts deben ser desplazables y funcionar en horizontal.
- **Borde a borde:** los layouts raíz llevan `android:fitsSystemWindows="true"`
  y cada actividad llama a `enableEdgeToEdgeDark()` o
  `enableEdgeToEdgeDayNight()`. No reintroduzcas `android:statusBarColor`.
- **La app funciona offline.** El multijugador es Bluetooth RFCOMM punto a
  punto, pensado para aulas sin internet. No introduzcas dependencias de red.
- **Sin publicidad, sin recolección de datos personales sensibles.** Es una
  herramienta didáctica universitaria sin fines de lucro.

## Al preparar una versión

Sube `versionCode` **y** `versionName` en `app/build.gradle.kts` y actualiza
`distribution/whatsnew/`. Play rechaza un `versionCode` repetido.
