# Arquitectura del repositorio

App Android de módulo único (`app/`), en Kotlin con vistas XML, Jetpack,
Material Components y Fragments. Sin Compose y sin código nativo (NDK).

## Paquetes

Raíz: `app/src/main/kotlin/ovh/gabrielhuav/battlenaval_v2/`

```
MainActivity.kt              Menú principal y navegación unificada
ThemeManager.kt              Temas institucionales (UNAM, IPN, FES Acatlán, ESCOM)

sistemabinario/              MÓDULO 1 — Sistema binario
  EducationalActivity.kt         Contenedor ViewPager2 de lecciones
  EducationalPagerAdapter.kt
  IntroductionFragment.kt        Información digital vs. analógica
  NumerationSystemFragment.kt    Sistemas de numeración posicionales
  BinarySystemFragment.kt        El bit y los pesos posicionales
  BinarySwitchesFragment.kt      Visualización con interruptores
  DecimalToBinaryFragment.kt     Conversión interactiva
  DecimalToBinaryExplanationFragment.kt   Divisiones sucesivas
  ConversionFragment.kt          Práctica asistida
  GamesActivity.kt               Centro de actividades lúdicas
  SwitchesGameFragment.kt        Minijuego de switches
  AsciiPracticeGameFragment.kt   Conversión rápida
  StorageUnitsGameFragment.kt    Unidades de almacenamiento
  ResponsiveUtils.kt             Adaptación de layouts

codigoascii/                 MÓDULO 2 — ASCII y Unicode
  AsciiEducationalActivity.kt / AsciiEducationalPagerAdapter.kt
  AsciiIntroductionFragment.kt        Origen del estándar ASCII
  AsciiTableFragment.kt               Tabla expandible y filtrable
  AsciiConverterFragment.kt           Texto ↔ decimal ↔ binario
  AsciiDecimalConversionFragment.kt   Explicación paso a paso
  AsciiBinaryConversionFragment.kt    Cadenas binarias de 8 bits
  AsciiEmojiComparisonFragment.kt     Evolución hacia Unicode
  ChildViewHolder.kt

unidadesalmacenamiento/      MÓDULO 3 — Unidades de medida digital
  StorageUnitsActivity.kt / StorageUnitsPagerAdapter.kt
  StorageUnitsIntroFragment.kt          Del bit al byte
  StorageUnitsTypesFragment.kt          Dispositivos físicos vs. nube
  StorageUnitsMeasurementsFragment.kt   Prefijos SI y binarios
  StorageUnitsRelationshipFragment.kt   Factores de escala

battlenavalbinario/          MÓDULO 4 — Motor de juego BitBattles
  MainActivityBattleNaval.kt     Selector de modalidad
  SinglePlayerActivity.kt        1 jugador contra IA local
  BluetoothActivity.kt           Multijugador local por Bluetooth P2P
  BluetoothService.kt            Sockets RFCOMM
  BluetoothGameManager.kt        Máquina de estados y sincronización
  DeviceListActivity.kt          Búsqueda y emparejamiento
  Board.kt / Cell.kt / Ship.kt   Modelo de la cuadrícula 7×7
  GameState.kt                   Turnos y victoria
  BinaryConversionDialog.kt      Captura y validación de conversiones
  BinaryConversionHelper.kt      Verificación matemática
  ConversionTutorialFragment.kt  Tutorial de reglas
```

## Correspondencia con la tesis

| Capítulos | Objetivo de aprendizaje | Paquete | Clases clave |
| :--- | :--- | :--- | :--- |
| III y IV — Sistema binario | Base 2, pesos posicionales, conversiones | `sistemabinario` | `BinarySystemFragment`, `BinarySwitchesFragment`, `DecimalToBinaryFragment`, `SwitchesGameFragment` |
| III y IV — Código ASCII | Asociar caracteres a códigos de 8 bits | `codigoascii` | `AsciiIntroductionFragment`, `AsciiTableFragment`, `AsciiConverterFragment`, `AsciiEmojiComparisonFragment` |
| III y IV — Unidades de almacenamiento | Cuantificar tamaños y jerarquía de bytes | `unidadesalmacenamiento` | `StorageUnitsIntroFragment`, `StorageUnitsMeasurementsFragment`, `StorageUnitsRelationshipFragment` |
| IV y V — Estrategia lúdica central | Aplicar conversiones bajo reglas de juego | `battlenavalbinario` | `SinglePlayerActivity`, `BluetoothActivity`, `BinaryConversionDialog`, `BinaryConversionHelper` |
| I y III — Identidad institucional | Sentido de pertenencia | `ThemeManager.kt`, `res/drawable/wallpaper_*` | `dialog_theme_selector.xml`, `wallpaper_unam.jpg`, `wallpaper_fes.jpg`, `wallpaper_ipn.jpg`, `wallpaper_escom.jpg` |
| IV y V — Aulas sin internet | Colaboración sin depender de la red | Bluetooth RFCOMM | `BluetoothService.kt`, `BluetoothGameManager.kt`, `DeviceListActivity.kt` |

## Convenciones al modificar el código

- **No declares `android:screenOrientation`.** Se quitó de todas las actividades
  para cumplir con los requisitos de pantalla grande; los layouts deben ser
  desplazables y funcionar en horizontal.
- Las actividades declaran `android:configChanges` amplio para no recrearse al
  rotar. Si añades una pantalla con estado en memoria, copia esa lista.
- Los layouts de actividad llevan `android:fitsSystemWindows="true"` en la raíz
  para que el contenido no quede bajo las barras del sistema en modo
  edge-to-edge. Mantener esa propiedad al añadir pantallas nuevas.
- Llama a `enableEdgeToEdgeDark()` o `enableEdgeToEdgeDayNight()`
  (`EdgeToEdge.kt`) antes de `super.onCreate()` en cada actividad nueva. **No
  uses `android:statusBarColor` ni `setStatusBarColor()`**: están obsoletos y
  Play los marca.
- La navegación hacia atrás usa `OnBackPressedDispatcher`, **no**
  `onBackPressed()` (ver [ANDROID-16.md](ANDROID-16.md)).
- Las reglas del juego deben poder ejecutarse también con los kits físicos de
  papel; ver [CONTEXTO-PEDAGOGICO.md](CONTEXTO-PEDAGOGICO.md).
