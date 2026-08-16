# Plan de migración a Jetpack Compose

Estado: **Fases 0 y 1 completas. Fase 2 iniciada (1 de 26 pantallas).**

## Estado actual

| Fase | Estado |
| :--- | :--- |
| 0 — Cimientos | ✅ Completa salvo R8 |
| 1 — Preparar Compose | ✅ Completa |
| 2 — Contenido educativo | 🔄 7 de 18 pantallas |
| 3 — Menús | ⬜ |
| 4 — Minijuegos | ⬜ |
| 5 — Tablero | ⬜ |
| 6 — Bluetooth | ⬜ |

Hecho hasta ahora:

- `BinaryConversionHelper` ya no usa `android.util.Log` y es código puro.
- `Ship` ya no depende de `android.graphics.Color`.
- **[`BoardModel`](../app/src/main/kotlin/ovh/gabrielhuav/battlenaval_v2/battlenavalbinario/BoardModel.kt)**:
  las reglas del tablero —colocación, disparos, conteo de flota— extraídas a
  Kotlin puro. `Board` y `Cell` conservan su API pública pero delegan aquí, así
  que las Activities no cambiaron ni una línea.
- **28 tests JVM** en `app/src/test`, sustituyendo los stubs de ejemplo.
- Compose activado: plugin del compilador, BOM y
  [`BitBattlesTheme`](../app/src/main/kotlin/ovh/gabrielhuav/battlenaval_v2/ui/theme/Theme.kt)
  con las paletas UNAM/IPN leídas del mismo `ThemeManager` que usan las vistas.
- [`ComposeFragment`](../app/src/main/kotlin/ovh/gabrielhuav/battlenaval_v2/ui/ComposeFragment.kt)
  como mecanismo de interop, y `IntroductionFragment` migrado como primera
  pantalla.

> **Los tests ya evitaron un fallo real.** La primera versión de
> `BoardModel.occupiedCells()` usaba `buildList`, donde `size` resuelve al de la
> lista en construcción y no al del tablero: el recorrido quedaba vacío y
> `getShipCoordinates()` habría devuelto siempre una flota vacía, rompiendo el
> intercambio de barcos por Bluetooth sin ningún síntoma visible. Es exactamente
> el escenario que motivaba hacer la Fase 0 antes que nada.

## Diagnóstico del código actual

Medido sobre `app/src/main` en la versión 1.0.0.11, **antes** de empezar:

| Métrica | Valor |
| :--- | ---: |
| Archivos Kotlin | 47 (7 679 líneas) |
| Layouts XML | 40 (4 856 líneas) — 9 de actividad, 26 de fragmento |
| Llamadas a `findViewById` | 251 |
| ViewModels / LiveData / StateFlow | **0** |
| Uso de ViewBinding | **0** |
| Tests reales | **0** (los 2 archivos existentes son los stubs de Android Studio) |

Reparto por paquete:

| Paquete | Archivos | Líneas | Naturaleza |
| :--- | ---: | ---: | :--- |
| `sistemabinario` | 16 | 2 649 | Contenido educativo + 3 minijuegos |
| `battlenavalbinario` | 13 | 2 422 | Juego, Bluetooth, dibujo del tablero |
| `unidadesalmacenamiento` | 6 | 1 210 | Contenido educativo |
| `codigoascii` | 9 | 1 050 | Contenido educativo + tabla interactiva |
| raíz | 3 | 348 | `MainActivity`, `ThemeManager`, `EdgeToEdge` |

## Recomendación

**Migración incremental, no reescritura desde cero.** Y con una condición previa
que no es negociable: **antes de tocar Compose hay que extraer el estado a
ViewModels y escribir tests.**

El motivo es el renglón de los ceros. Hoy todo el estado del juego vive dentro
de las Activities y se accede por `findViewById`. No hay ninguna red de
seguridad: cero tests reales. Migrar a Compose en ese punto de partida no es
migrar la UI, es reescribir a ciegas la lógica y la UI a la vez, sin nada que
avise si una conversión binaria empieza a dar mal resultado o si el protocolo
Bluetooth se desincroniza. Ese es el escenario en el que las migraciones se
atascan a medias y el proyecto queda peor que antes.

Reescribir desde cero es peor todavía por una razón específica de este
proyecto: el contenido pedagógico está **validado en campo** con estudiantes del
CCH Naucalpan y sustenta los capítulos IV y V de la tesis. Los textos, las
tablas ASCII, las explicaciones paso a paso y las reglas del juego no son
detalles de implementación reemplazables: son el resultado de la intervención
docente. Reescribir de cero invita a que se pierdan matices que costaron un
semestre de aula.

### Antes de empezar: ¿conviene migrar?

Vale la pena decirlo claro. La app está terminada y estable, tiene 37
instalaciones y su principal motor de mantenimiento es el aumento anual de
`targetSdk` que exige Play — que cuesta alrededor de un día y **no depende del
framework de UI**. Si BitBattles va a quedar como artefacto de tesis con
actualizaciones de cumplimiento una vez al año, **la migración a Compose no se
paga sola**: haz solo la Fase 0 y detente ahí, porque esa sí rinde en cualquier
escenario.

La migración completa se justifica si se cumple al menos una de estas:

- Se piensa seguir ampliando contenido didáctico (nuevos módulos, nuevos
  minijuegos).
- Se quiere soporte real de tablet, que es lo que Play ya empezó a señalar y lo
  que queda pendiente para API 37.
- Va a entrar más gente a colaborar, para quienes Compose es hoy el camino que
  asume la documentación oficial de Android.

## Lo que no se migra

Aproximadamente un tercio del código es independiente del framework de UI y
**sobrevive intacto**. Conviene tenerlo claro porque reduce el tamaño real del
problema:

- `BluetoothService.kt`, `BluetoothGameManager.kt` — sockets RFCOMM y máquina de
  estados.
- `BinaryConversionHelper.kt`, `Board.kt` (la lógica, no el dibujo), `Ship.kt`,
  `GameState.kt`.
- `ThemeManager.kt` — la persistencia de preferencia de tema; cambia solo cómo
  se consume.
- `res/values/strings.xml` — Compose lee `stringResource()` igual.

## Fases

### Fase 0 — Cimientos (hacer aunque nunca se migre)

No toca Compose. Es la fase que paga en cualquier escenario.

1. **Quitar `android.util.Log` de `BinaryConversionHelper`.** Hoy lo usa, y eso
   hace que la clase no sea testeable en la JVM (`Log` lanza «not mocked» fuera
   de un dispositivo). Es el bloqueo más pequeño y más rentable del proyecto.
2. **Tests de la lógica pura**: `BinaryConversionHelper` (conversiones en ambos
   sentidos, casos límite de 3 y 8 bits), colocación de barcos en `Board`,
   transiciones de `GameState`. Son funciones deterministas; el coste es bajo y
   son exactamente la red que falta.
3. **Extraer el estado del juego a ViewModels.** Empezar por
   `SinglePlayerActivity`, seguir con `BluetoothActivity`. Señal de que está
   bien hecho: se puede **quitar `android:configChanges`** del manifest y la
   partida sobrevive a una rotación. Hoy ese atributo está tapando que el estado
   vive en la Activity.
4. **Activar R8** (`isMinifyEnabled = true`) y verificar. Importa más en cuanto
   entre Compose, que suma peso al binario.

Al terminar la Fase 0 la app sigue siendo XML, pero ya es migrable con red.

### Fase 1 — Preparar el terreno de Compose

- Activar `buildFeatures { compose = true }` y añadir la BOM de Compose más
  reciente.
- **Nota:** `libs.versions.toml` ya declara
  `androidx.compose.material3:material3-android` y `app/build.gradle.kts` ya lo
  incluye, pero `compose` no está activado. Hoy esa dependencia solo suma peso
  sin aportar nada; esta fase la vuelve útil.
- Portar `ThemeManager` a un `MaterialTheme` de Compose que exponga las paletas
  UNAM / IPN / FES / ESCOM, conviviendo con los temas XML.
- Primera pantalla de prueba con `ComposeView` dentro de un layout existente,
  para validar el interop antes de comprometerse.

### Fase 2 — Contenido educativo (el grueso, el menor riesgo)

Los 26 fragmentos de `sistemabinario`, `codigoascii` y
`unidadesalmacenamiento`. Es más de la mitad del XML del proyecto y en su
mayoría es contenido estático: textos, tablas, explicaciones paso a paso.

Es la fase con mejor relación volumen/riesgo: si un texto queda mal, se ve al
instante y no rompe nada más.

**Receta para migrar un fragmento**, ya establecida con `IntroductionFragment`:

1. Hereda de `ComposeFragment` e implementa `Content()`. El `ViewPager2` y su
   adaptador no se tocan: para ellos sigue siendo un `Fragment`.
2. Mueve el texto del layout a `res/values/strings.xml` y léelo con
   `stringResource()`. El contenido didáctico estaba incrustado en los XML;
   al migrarlo se pasa a recursos, que es donde debe estar.
3. Arma la pantalla con `LessonScreen` / `LessonTitle` / `LessonParagraph` de
   `ui/components/Lesson.kt`, para que todas mantengan el mismo espaciado.
   `LessonScreen` ya es desplazable, requisito desde que las actividades no
   bloquean la orientación.
4. Añade un `@Preview` privado.
5. Borra el layout XML.

**Componentes disponibles** en `ui/components/`:

| Componente | Para qué |
| :--- | :--- |
| `LessonScreen` | Contenedor desplazable con el margen estándar |
| `LessonTitle` / `LessonSubtitle` / `LessonParagraph` | Texto |
| `LessonImage` | Ilustraciones (200 dp por defecto) |
| `LessonExpandableCard` | Tarjetas «¿SABÍAS QUE...?» |
| `LessonTable` | Tablas de consulta, con desplazamiento horizontal |

**Ya migradas (7):** `IntroductionFragment`, `NumerationSystemFragment`,
`BinarySystemFragment`, `ConversionFragment`, `AsciiIntroductionFragment`,
`StorageUnitsIntroFragment`, `StorageUnitsTypesFragment`.

**Pendientes (11):** `BinarySwitchesFragment`, `DecimalToBinaryFragment`,
`DecimalToBinaryExplanationFragment`, `BinaryConversionFragment`,
`AsciiTableFragment`, `AsciiConverterFragment`,
`AsciiDecimalConversionFragment`, `AsciiBinaryConversionFragment`,
`AsciiEmojiComparisonFragment`, `StorageUnitsMeasurementsFragment`,
`StorageUnitsRelationshipFragment`. Son las que tienen estado propio
(conversores en vivo, tablas filtrables), así que a partir de aquí toca
introducir `ViewModel` o `remember` según el caso.

**Dos trampas encontradas:**

- Los nombres de recurso pueden chocar con los que ya usan otras pantallas
  (pasó con `binary_system_title` y `conversion_title`). Comprueba antes de
  añadir, y si hay choque renombra el nuevo, nunca el existente.
- `Icons.Default` solo trae el set básico. `ExpandMore` obligaría a añadir
  `material-icons-extended`, que pesa varios MB; usa `KeyboardArrowDown`.

Casos con algo más de trabajo: `AsciiTableFragment` (lista expandible → `LazyColumn`)
y `AsciiConverterFragment` (conversión en vivo, ya es un buen candidato a
`ViewModel` + `StateFlow`).

### Fase 3 — Menús

`MainActivity` y `GamesActivity`. Poco código, y la navegación por
visibilidades de `MainActivity` (mostrar/ocultar `mainContentLayout` contra
`fragment_container`) se sustituye por Navigation Compose, que es un cambio
claramente a mejor.

### Fase 4 — Minijuegos

`SwitchesGameFragment`, `AsciiPracticeGameFragment`, `StorageUnitsGameFragment`.
Tienen estado propio pero está acotado a la pantalla. Buen ensayo antes de
tocar el tablero.

### Fase 5 — El tablero

`Board.kt` (174 líneas) y `Cell.kt` (209 líneas) son dibujo personalizado sobre
`View`. Van a un `Canvas` de Compose.

Es la fase técnicamente más exigente **y la más rentable**: resuelve gratis el
pendiente ya anotado en [ANDROID-16.md](ANDROID-16.md), porque un `Canvas` de
Compose se dimensiona con el espacio disponible en lugar de con el `cellSize`
fijo que hoy deja el tablero pequeño y centrado en tablet.

### Fase 6 — Bluetooth

`BluetoothActivity` y `DeviceListActivity` al final, a propósito. Es donde los
errores de ciclo de vida y de estado duelen más, y conviene llegar con la
experiencia de las cinco fases anteriores y con los ViewModels de la Fase 0 ya
rodados.

## Riesgos concretos

| Riesgo | Mitigación |
| :--- | :--- |
| Regresión silenciosa en las conversiones binarias | Tests de la Fase 0 antes de tocar nada |
| Pérdida de fidelidad del contenido pedagógico | Migrar texto por texto desde el XML; no reescribir de memoria |
| El socket RFCOMM se corta al recrear la Activity | Fase 0 punto 3: sacar el estado a ViewModel y validar rotando |
| Migración que se queda a medias | Cada fase debe dejar la app publicable; nunca fusionar una fase incompleta |
| Aumento del tamaño del binario | Activar R8 en la Fase 0 y medir el AAB en cada fase |

## Criterio de «fase terminada»

Una fase solo se da por cerrada si la app **se puede publicar en ese punto**:
compila, `lintVitalRelease` pasa, la prueba cerrada se ve bien en un teléfono
real, y una partida completa —un jugador y Bluetooth— se juega de principio a
fin. Si una fase no puede publicarse, está a medias.

## Lo que la migración resuelve de paso

- **Escalado del tablero en tablet** (Fase 5), hoy pendiente para API 37.
- **Insets del sistema**: Compose los maneja de forma nativa con `WindowInsets`,
  sin `fitsSystemWindows` ni helpers propios.
- **Las 251 llamadas a `findViewById`** desaparecen.
- **El truco de `configChanges`**: deja de ser necesario en cuanto el estado
  viva en ViewModels.

## Orden sugerido si hay poco tiempo

Fase 0 completa → Fase 2 (contenido) → parar y evaluar. En ese punto está
migrado más de la mitad del XML, con la parte de menor riesgo, y se tiene una
medida real de cuánto cuesta el resto antes de comprometerse con el tablero y
con Bluetooth.
