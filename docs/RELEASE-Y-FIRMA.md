# Firma y publicación de BitBattles

## Los archivos que hacen falta

Para generar un AAB firmado y subirlo a Google Play se necesitan **cuatro**
archivos. Ninguno está en git: todos los cubre `.gitignore`.

| Archivo | Ubicación esperada | Qué es | Si se pierde |
| :--- | :--- | :--- | :--- |
| `APPPlayStore.jks` | raíz del repo | Keystore con la clave de subida (alias `bitbattles`) | **Irrecuperable por ti.** Hay que pedir a Google el restablecimiento de la clave de subida. |
| `key.properties` | raíz del repo | Apunta al keystore y guarda las contraseñas | Se puede reescribir si recuerdas las contraseñas |
| `google-services.json` | `app/` | Configuración de Firebase Analytics | Se vuelve a descargar desde la consola de Firebase |
| JSON de cuenta de servicio | fuera del repo | Credencial de la Play Developer API, solo para el CI | Se genera otra en Google Cloud |

Contenido de `key.properties`:

```properties
storeFile=../APPPlayStore.jks
storePassword=…
keyAlias=bitbattles
keyPassword=…
```

`storeFile` se resuelve desde el módulo `app/`, por eso el `../` apunta a la
raíz del repositorio.

> El keystore de subida **no** es la clave con la que Google firma la app para
> los usuarios: Play App Signing guarda esa aparte. Perder el keystore de
> subida es recuperable (Google lo restablece); perder el acceso a la cuenta de
> Play Console no.

## Dónde están hoy en esta PC

El script `verificar-repo.ps1` mueve los archivos sensibles a
`_sensitive_backup/` antes de subir a GitHub, y los devuelve con `-Restaurar`.

```
_sensitive_backup/
  bitbattles.jks                 el keystore (se copia como APPPlayStore.jks)
  key.properties
  google-services.json
  ubicaciones-originales.txt     log de las rutas de las que se movieron
```

**Ojo con `-Restaurar`:** el log guarda rutas absolutas de
`C:\Users\gabri\AndroidStudioProjects\BattleNaval_V2`, que es un checkout
distinto del actual. Restaurar devuelve los archivos a *esa* carpeta, no a la
del repositorio en OneDrive. Para el repo actual conviene copiarlos a mano:

```powershell
Copy-Item _sensitive_backup\key.properties .\key.properties
Copy-Item _sensitive_backup\bitbattles.jks .\APPPlayStore.jks
Copy-Item _sensitive_backup\google-services.json app\google-services.json
```

Además, el nombre del keystore en el backup (`bitbattles.jks`) no coincide con
el que espera `key.properties` (`APPPlayStore.jks`); por eso la copia lo
renombra.

## Kit para firmar desde una PC nueva

Son **dos archivos**. Con estos dos y el repositorio clonado en una ruta ASCII,
`./gradlew :app:bundleRelease` produce un AAB firmado y subible.

1. **`bitbattles.jks`** → colócalo en la raíz del repo como `APPPlayStore.jks`.
2. **`key.properties`** → en la raíz del repo. Contiene alias y contraseñas; sin
   él el keystore no se puede abrir.

Y para que compile (no para firmar): **`google-services.json`** en `app/`, que
siempre se puede volver a descargar desde la consola de Firebase.

Huella del keystore correcto, para verificar que llevas el bueno:

```
MD5  cc6b50e5158fed61e1ff8769c182ed0a  bitbattles.jks
```

> No guardes el keystore únicamente en `Descargas`: esa carpeta se limpia sola y
> es lo primero que se pierde al reinstalar el sistema. Y guarda las contraseñas
> en un gestor, no solo dentro de `key.properties`.

## El requisito de nivel de API se evalúa sobre TODAS las pistas

Play no mira solo producción: cuenta cualquier versión que un usuario todavía
pueda instalar, en interna, cerrada y abierta incluidas.

Con la v11 ya publicada en producción con `targetSdk 36`, el aviso de política
siguió activo señalando **API 34**. El culpable era un build olvidado:

```
1 (1.0) · Prueba interna · targetSdk 34 · 30 abr 2025
```

Se encuentra desde **Estado con respecto a las políticas → Detalles del
problema → «Ver paquetes de aplicaciones»**, que lista los bundles que
incumplen.

La solución es **promover la versión actual a esa pista** en vez de intentar
borrar la vieja: mismo AAB, no consume otro `versionCode`, y deja la pista sin
nada que incumpla. Al preparar cada versión anual, conviene revisar las tres
pistas de prueba y no dejar builds antiguos activos.

## Advertencias de Play al subir el AAB

Dos advertencias aparecen desde la versión 11. Ninguna bloquea la publicación:
son «acciones recomendadas», no incumplimientos de política.

### «Usa APIs o parámetros obsoletos para la pantalla de borde a borde»

**Se decidió aceptarla.** No es un descuido: es una contradicción entre dos
comprobaciones de Play.

En la versión 11 se quitaron los seis `android:statusBarColor` de los temas y
no queda ninguna llamada obsoleta en el código del proyecto —verificado con
búsqueda exhaustiva en `res/` y `kotlin/`—. Aun así el aviso persiste, porque
Play analiza el DEX completo y ahí están estas clases:

```
androidx/activity/EdgeToEdgeApi21, 23, 26, 28, 29, 30
```

Son la implementación de `enableEdgeToEdge()`, que internamente usa
`setStatusBarColor`, `setNavigationBarColor` y `setDecorFitsSystemWindows` para
pintar los scrims en Android 14 y anteriores.

El problema: `enableEdgeToEdge()` es justamente lo que resolvió la **otra**
advertencia, la de «puede que no se muestre para todos los usuarios». Es decir,
la API que Google recomienda para una comprobación es la que dispara la otra.
Se probó subir `androidx.activity` a 1.10.1 y las referencias siguen presentes,
así que no se arregla actualizando.

Las opciones son:

- **Conservar `enableEdgeToEdge()`** (lo actual): borde a borde real también en
  Android 14 y anteriores, a cambio de que la advertencia siga.
- **Quitarlo**: la advertencia probablemente se va, pero el borde a borde queda
  solo en Android 15+ vía la imposición de `targetSdk 36`, y volvería el otro
  aviso.

Se eligió lo primero: la experiencia del usuario pesa más que un aviso que no
bloquea nada. Si Google actualiza la comprobación o AndroidX reescribe esas
clases, revisar de nuevo.

**«Este App Bundle contiene código nativo y no subiste símbolos de depuración».**
Ignorable. El único código nativo es `libandroidx.graphics.path.so` (cuatro
ABIs), que entra de forma transitiva por AndroidX. No es código del proyecto y
no hay símbolos que subir.

**«No hay un archivo de desofuscación asociado».** Es real: `isMinifyEnabled`
está en `false`, así que R8 no corre y no se genera `mapping.txt`.

⚠️ **No actives R8 sin añadir reglas antes.** `BluetoothActivity` intercambia la
posición de la flota entre dispositivos serializando `ShipData` con Gson
(`toJson`/`fromJson`, líneas ~202 y ~223), y `proguard-rules.pro` no tiene
ninguna regla propia. R8 renombraría esos campos y el multijugador se rompería
**en silencio**: la app compila, instala y arranca, y solo falla al colocar los
barcos. Sin tests que lo detecten.

Para hacerlo bien: añadir `-keep` para las clases que viajan por Gson, activar
R8, y validar una partida Bluetooth completa en prueba cerrada antes de promover
a producción. Ver también la Fase 0 de
[MIGRACION-COMPOSE.md](MIGRACION-COMPOSE.md).

## Compilar en local

⚠️ El checkout principal está en una ruta con acento
(`…\OneDrive\Imágenes\Documentos\…`). El Android Gradle Plugin **rechaza rutas
con caracteres no ASCII** y falla antes de compilar:

```
Your project path contains non-ASCII characters.
```

Hay que compilar desde una copia en ruta ASCII:

```powershell
$dst = "C:\Users\gabri\builds\BattleNaval_V2"
robocopy "." $dst /E /XD build .gradle .git _sensitive_backup .idea .kotlin /NFL /NDL /NJH /NJS /NP
```

Copia también `key.properties`, `APPPlayStore.jks` y `app\google-services.json`
a esa carpeta y ahí sí:

```powershell
cd $dst
.\gradlew.bat :app:bundleRelease
```

El AAB queda en `app\build\outputs\bundle\release\app-release.aab`.

La alternativa recomendada es no compilar a mano y dejar que lo haga el CI;
ver [CI-CD.md](CI-CD.md).

## Publicar una versión a mano

1. Subir `versionCode` y `versionName` en `app/build.gradle.kts`. Play rechaza
   un `versionCode` repetido.
2. Compilar el AAB según lo anterior.
3. Play Console → **Prueba y lanza** → track (interna / cerrada / abierta /
   producción) → **Crear versión** → subir el `.aab`.
4. Escribir las novedades y enviar a revisión.

## Antes de subir a GitHub

```powershell
PowerShell -ExecutionPolicy Bypass -File .\verificar-repo.ps1
```

Mueve keystores, `key.properties`, `google-services.json` y cualquier JSON de
cuenta de servicio a `_sensitive_backup/`, los quita del índice de git y avisa
si alguno seguía versionado.
