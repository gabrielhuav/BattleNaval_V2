# Contexto pedagógico del proyecto

## Ficha técnica

- **Tesis:** *Planeación didáctica para la codificación de información en forma
  binaria y ASCII con estrategias lúdicas en el aula* (Reporte de Práctica Docente).
- **Autor:** M. en C. Gabriel Hurtado Avilés.
- **Programa:** Maestría en Docencia para la Educación Media Superior (MADEMS),
  campo de conocimiento Matemáticas.
- **Institución:** UNAM, Facultad de Estudios Superiores Acatlán.
- **Tutor principal:** Dr. Jorge Alonso Bustamante Torres (FES Acatlán).
- **Comité tutor:** Dr. Rafael Fernández Flores (FES Acatlán),
  Dr. J. Jesús Becerra Ramírez (FES Iztacala).
- **Sede de la intervención:** CCH Plantel Naucalpan, UNAM.
- **Asignatura:** Taller de Cómputo (área de Matemáticas), 2º semestre.
- **App:** BitBattles — `ovh.gabrielhuav.battlenaval_v2`.

## Problema educativo

En la Educación Media Superior, los temas fundacionales de computación —sistema
binario, codificación ASCII y unidades de almacenamiento— suelen enseñarse de
forma abstracta y descontextualizada. Eso produce baja asimilación, poca
motivación y una brecha entre la matemática y su aplicación tangible en la
tecnología cotidiana.

## Marco teórico

- **Modelo constructivista del CCH:** aprender a aprender, a hacer, a ser y a
  convivir. El estudiante es protagonista de su proceso cognitivo.
- **Aprendizaje significativo (Ausubel):** se enlazan los conocimientos previos
  sobre números y letras con su representación binaria mediante experiencias
  prácticas.
- **Aprendizaje basado en juegos y gamificación:** las mecánicas de juego
  convierten problemas abstractos en retos interactivos, bajan la ansiedad
  matemática y fomentan la perseverancia ante el error.
- **TPACK (Mishra & Koehler):** integración articulada de contenido disciplinar,
  pedagogía constructivista y tecnología móvil.
- **Cognición distribuida (Hutchins):** la carga cognitiva se reparte entre la
  app, los recursos físicos (tableros, tablas de consulta) y la interacción
  entre pares.
- **Estrategia dual inclusiva:** para atender la brecha digital, los kits
  físicos (tableros impresos, guías plastificadas, tablas ASCII) implementan
  exactamente las mismas reglas que la app. **Cualquier cambio de reglas en el
  código debe seguir siendo reproducible con papel y lápiz.**

## Ubicación curricular

- Taller de Cómputo, 64 horas totales.
- Unidad 2: Hardware y Software (14–16 horas).
- Aprendizaje esperado: *examina la representación y cuantificación de la
  información en los dispositivos digitales*.
- Temas: sistema binario (decimal ↔ binario), código ASCII y unidades de
  almacenamiento (bit, byte, KB, MB, GB, TB, PB…).

## Mecánica central: «Hundir la Flota Digital»

Tablero de 7×7: columnas numéricas 1–7 (eje X) y filas alfabéticas A–G (eje Y).

**Disparo**

1. El atacante elige una celda (por ejemplo columna 5, fila `A`).
2. Convierte la coordenada X a binario de 3 bits → `5` = `101`.
3. Convierte la coordenada Y a su código ASCII decimal y luego a binario de
   8 bits → `A` = 65 = `01000001`.
4. Transmite el disparo **solo** en formato binario: `X=101,Y=01000001`.

**Recepción**

1. El oponente decodifica el vector a decimal y letra dentro de un tiempo
   límite, apoyándose en las tablas de referencia.
2. Si el cálculo es correcto, verifica el impacto en su flota («Tocado» /
   «Agua») y marca el estado con `1` o `0`.
3. Si se equivoca en la conversión inversa, pierde su turno de contraataque.

**Victoria:** hundir toda la flota enemiga aplicando correctamente las
conversiones durante la partida.

## Restricciones de producto derivadas de la pedagogía

- **Sin fines de lucro y sin publicidad invasiva.** Herramienta didáctica
  universitaria; no recopila datos personales sensibles.
- **Independencia de red.** El aula puede no tener internet: el modo
  multijugador va por Bluetooth RFCOMM punto a punto y el resto funciona
  offline.
- **Identidad institucional.** Los temas visuales (UNAM, IPN, FES Acatlán,
  ESCOM) refuerzan el sentido de pertenencia; no son decoración accesoria.
- **Contraste legible.** Los esquemas de color deben mantenerse aptos para
  lectura pedagógica en proyector y en pantallas pequeñas.
