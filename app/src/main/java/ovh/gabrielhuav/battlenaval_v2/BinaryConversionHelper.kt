package ovh.gabrielhuav.battlenaval_v2

import android.util.Log
import kotlin.random.Random

/**
 * Clase para manejar conversiones binarias en el juego
 */
class BinaryConversionHelper {

    companion object {
        private val TAG = "BinaryConversionHelper"

        // Convertir un carácter a su representación binaria de 8 bits
        fun charToBinary(char: Char): String {
            val asciiValue = char.code
            val result = String.format("%8s", Integer.toBinaryString(asciiValue)).replace(' ', '0')
            Log.d(TAG, "Convirtiendo caracter: $char a binario: $result")
            return result
        }

        // Convertir un número a su representación binaria de 3 bits
        fun numberToBinary(number: Int): String {
            val result = String.format("%3s", Integer.toBinaryString(number)).replace(' ', '0')
            Log.d(TAG, "Convirtiendo número: $number a binario: $result")
            return result
        }

        // Convertir una cadena binaria a ASCII
        fun binaryToChar(binary: String): Char {
            val result = binary.toInt(2).toChar()
            Log.d(TAG, "Convirtiendo binario: $binary a caracter: $result")
            return result
        }

        // Convertir una cadena binaria a número decimal
        fun binaryToNumber(binary: String): Int {
            val result = binary.toInt(2)
            Log.d(TAG, "Convirtiendo binario: $binary a número: $result")
            return result
        }

        // Verificar si la conversión es correcta
        // IMPORTANTE: En esta versión, invertimos los parámetros ya que las coordenadas están intercambiadas
        fun checkConversion(binaryY: String, binaryX: String, answerX: Char, answerY: Int): Boolean {
            val correctY = binaryToChar(binaryY)     // Convertimos el binario Y a carácter
            val correctX = binaryToNumber(binaryX)   // Convertimos el binario X a número

            val isCorrect = correctY == answerX && correctX == answerY
            Log.d(TAG, "Verificando conversión (invertida): $binaryY,$binaryX => ${answerX},${answerY}, Resultado: $isCorrect")
            Log.d(TAG, "Valores correctos: $correctY,$correctX")

            return isCorrect
        }

        // Generar un ejemplo para explicar la conversión
        fun generateExample(): Triple<String, String, String> {
            val randomChar = ('A'..'G').random()
            val randomNum = (1..7).random()

            Log.d(TAG, "Generando ejemplo con: $randomChar, $randomNum")

            val binaryChar = charToBinary(randomChar)
            val binaryNum = numberToBinary(randomNum)

            val explanation = "Ejemplo: $binaryChar es '${randomChar}' en ASCII (valor ${randomChar.code}), " +
                    "y $binaryNum es $randomNum en decimal."

            return Triple(binaryChar, binaryNum, explanation)
        }

        // Generar coordenadas aleatorias para depuración
        fun generateRandomCoordinates(): Pair<Int, Int> {
            val x = Random.nextInt(0, 7)
            val y = Random.nextInt(0, 7)
            Log.d(TAG, "Generando coordenadas aleatorias: ($x, $y) => ${('A' + x)}${y+1}")
            return Pair(x, y)
        }

        // Convertir coordenadas de tipo (x,y) a una representación de coordenadas del tablero (ej. "A3")
        fun coordsToString(x: Int, y: Int): String {
            // En la versión invertida, y se convierte en letra y x en número
            val letter = ('A' + y)
            val number = x + 1
            return "$letter$number"
        }
    }
}