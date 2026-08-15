# Migración a Android 16 (API 36)

Google Play exigió que la app se orientara a API 36 antes del **31 de agosto de
2026**; de lo contrario dejaban de aceptarse actualizaciones. Esta es la
migración que se hizo y lo que queda pendiente.

## Cambios aplicados

### 1. Nivel de API

`app/build.gradle.kts`

```kotlin
compileSdk = 36
targetSdk  = 36
versionCode = 11
versionName = "1.0.0.11"
```

AGP 9.1.1 y Gradle 9.3.1 ya soportaban API 36, así que no hubo que tocar el
toolchain.

### 2. Predictive back (obligatorio al segmentar API 36)

El sistema **ya no invoca `Activity.onBackPressed()`** ni despacha
`KeyEvent.KEYCODE_BACK`. `MainActivity` sobrescribía `onBackPressed()` para
cerrar el fragmento de opciones de aprendizaje y volver al menú; con API 36 ese
código habría dejado de ejecutarse y el botón atrás habría cerrado la app.

Ahora se registra un `OnBackPressedCallback` que solo está habilitado mientras
el fragmento ocupa la pantalla:

```kotlin
private val backToMainMenuCallback = object : OnBackPressedCallback(false) {
    override fun handleOnBackPressed() {
        fragmentContainer.visibility = View.GONE
        mainContentLayout.visibility = View.VISIBLE
        supportFragmentManager.popBackStack()
        isEnabled = false
    }
}
```

Se habilita al abrir el fragmento y se apaga solo al consumir el gesto, de modo
que en el menú principal el sistema aplique su animación predictiva de salida.

Las llamadas directas a `onBackPressed()` desde los botones de la barra
superior de `EducationalActivity`, `GamesActivity`, `AsciiEducationalActivity`
y `StorageUnitsActivity` se cambiaron por
`onBackPressedDispatcher.onBackPressed()`.

> Existe una salida temporal, `android:enableOnBackInvokedCallback="false"` por
> actividad, que **no** se usó: migrar es lo correcto y la salida desaparecerá.

### 3. Edge-to-edge obligatorio

Al segmentar API 36 el modo edge-to-edge deja de ser opcional y
`R.attr.windowOptOutEdgeToEdgeEnforcement` se ignora. La app no usaba esa
exclusión y los ocho layouts de actividad ya llevaban
`android:fitsSystemWindows="true"` en la raíz, así que el contenido sigue sin
quedar debajo de las barras.

Play Console señalaba además dos cosas, resueltas así:

**«Usa APIs o parámetros obsoletos».** Era `android:statusBarColor`, presente en
seis estilos entre `values/themes.xml` y `values-night/themes.xml`. Android 16
lo ignora por completo. Se eliminaron los seis.

**«Puede que no se muestre para todos los usuarios».** Al quitar esos atributos,
en Android 14 y anteriores las barras habrían quedado con el color por defecto
en lugar de ser transparentes. Se añadió `enableEdgeToEdge()` explícito en las 8
actividades mediante los helpers de [`EdgeToEdge.kt`](../app/src/main/kotlin/ovh/gabrielhuav/battlenaval_v2/EdgeToEdge.kt):

- `enableEdgeToEdgeDark()` en las pantallas de fondo oscuro fijo (tablero
  `#0A192F`, lista de dispositivos) y en las que llevan wallpaper institucional
  (`MainActivity`, `GamesActivity`). Fuerza iconos claros, que si no quedarían
  oscuros sobre fondo oscuro en modo claro.
- `enableEdgeToEdgeDayNight()` en las tres actividades educativas, que siguen el
  tema DayNight y pueden dejar que el sistema decida el color de los iconos.

### 4. Orientación y tamaño en pantallas grandes

En pantallas con ancho mínimo ≥ 600 dp (tablets y plegables), Android 16 ignora
`android:screenOrientation`, `android:resizableActivity`, `minAspectRatio`,
`maxAspectRatio` y `setRequestedOrientation()`. Play Console lo reportaba como
acción recomendada.

Se quitó `android:screenOrientation="portrait"` de las 8 actividades. En su
lugar, cada una amplía `android:configChanges` a
`orientation|screenSize|smallestScreenSize|screenLayout|keyboardHidden|density`,
de modo que **no se recrea** al rotar o redimensionar. Eso importa sobre todo en
`BluetoothActivity`: recrear la actividad cortaría el socket RFCOMM y con él la
partida en curso.

Los layouts aguantan horizontal sin variantes específicas porque ya eran
desplazables:

| Layout | Cómo se adapta |
| :--- | :--- |
| `activity_single_player.xml` | `ScrollView` + `HorizontalScrollView` sobre los tableros |
| `activity_bluetooth.xml` | `ScrollView` con `layout_weight="1"` sobre los tableros |
| `activity_device_list.xml` | `ListView` |
| `activity_educational/ascii_educational/storage_units.xml` | `ViewPager2` con fragmentos desplazables |
| `activity_games.xml` | `fragment_container` entre topes + `BottomNavigationView` |
| `activity_main_unified.xml` | **Se envolvió el menú en un `ScrollView`** con `fillViewport="true"`; era el único que no cabía en horizontal |

Como `mainContentLayout` pasó de ser `LinearLayout` a `ScrollView`, en
`MainActivity` se declara como `View`, que es todo lo que necesita ese campo
(solo se alterna su visibilidad).

## Pendiente para API 37

Nada bloqueante, pero el tablero es mejorable: `Board.cellSize` se calcula una
vez a partir de un `scaleFactor` fijo y no crece para aprovechar una pantalla de
tablet, así que en pantallas grandes el tablero se queda pequeño y centrado en
lugar de escalar. `Board.adjustCellSize()` ya existe; faltaría llamarlo desde
`onConfigurationChanged` con el ancho disponible.

## Deuda técnica detectada (no bloqueante)

Advertencias de compilación que conviene limpiar en algún momento:

- `startActivityForResult` en `BluetoothActivity.kt:96` y
  `DeviceListActivity.kt:88` → migrar a la Activity Result API.
- `BluetoothAdapter.getDefaultAdapter()` en `BluetoothActivity.kt:526`,
  `BluetoothGameManager.kt:33` y `DeviceListActivity.kt:44` → usar
  `BluetoothManager.getAdapter()`.
- `getParcelableExtra(String)` en `DeviceListActivity.kt:169` → usar la
  sobrecarga con `Class<T>`.
- `Resources.getColor(int)` en `BinaryConversionFragment.kt:136` y
  `DecimalToBinaryExplanationFragment.kt:153` → `ContextCompat.getColor`.

## Referencia

[Cambios de comportamiento de Android 16 para apps que se orientan a API 36](https://developer.android.com/about/versions/16/behavior-changes-16)
