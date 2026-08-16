package ovh.gabrielhuav.battlenaval_v2.battlenavalbinario

import kotlin.random.Random

/**
 * Conversiones binarias del juego.
 *
 * Es lógica pura y sin dependencias de Android a propósito: así se puede
 * probar en la JVM con tests locales, que son la red de seguridad de las
 * reglas didácticas del juego. No introduzcas `android.util.Log` aquí; lanza
 * «not mocked» fuera de un dispositivo y deja la clase sin tests.
 */
class BinaryConversionHelper {

    companion object {

        /** Carácter a binario de 8 bits vía su código ASCII. `'A'` → `"01000001"`. */
        fun charToBinary(char: Char): String =
            String.format("%8s", Integer.toBinaryString(char.code)).replace(' ', '0')

        /** Número a binario de 3 bits. `5` → `"101"`. */
        fun numberToBinary(number: Int): String =
            String.format("%3s", Integer.toBinaryString(number)).replace(' ', '0')

        /** Cadena binaria a carácter ASCII. `"01000001"` → `'A'`. */
        fun binaryToChar(binary: String): Char = binary.toInt(2).toChar()

        /** Cadena binaria a decimal. `"101"` → `5`. */
        fun binaryToNumber(binary: String): Int = binary.toInt(2)

        /**
         * Verifica la respuesta del jugador ante un disparo recibido.
         *
         * Ojo con el orden: el eje Y viaja como carácter ASCII (la fila, `A`–`G`)
         * y el eje X como número de 3 bits (la columna, 1–7). Los parámetros de
         * respuesta llegan cruzados respecto a sus nombres —`answerX` es la letra
         * y `answerY` el número— porque así los usan `BinaryConversionDialog` y
         * el modo Bluetooth. No cambies el orden sin ajustar a esos dos.
         */
        fun checkConversion(binaryY: String, binaryX: String, answerX: Char, answerY: Int): Boolean {
            val correctY = binaryToChar(binaryY)
            val correctX = binaryToNumber(binaryX)
            return correctY == answerX && correctX == answerY
        }

        /** Ejemplo aleatorio para las pantallas de explicación. */
        fun generateExample(): Triple<String, String, String> {
            val randomChar = ('A'..'G').random()
            val randomNum = (1..7).random()

            val binaryChar = charToBinary(randomChar)
            val binaryNum = numberToBinary(randomNum)

            val explanation = "Ejemplo: $binaryChar es '${randomChar}' en ASCII (valor ${randomChar.code}), " +
                    "y $binaryNum es $randomNum en decimal."

            return Triple(binaryChar, binaryNum, explanation)
        }

        /** Coordenadas aleatorias dentro del tablero 7×7, en índices base 0. */
        fun generateRandomCoordinates(): Pair<Int, Int> = Pair(Random.nextInt(0, 7), Random.nextInt(0, 7))

        /**
         * Índices del tablero a etiqueta legible. `(2, 0)` → `"A3"`.
         * La fila (`y`) da la letra y la columna (`x`) el número.
         */
        fun coordsToString(x: Int, y: Int): String = "${'A' + y}${x + 1}"
    }
}
