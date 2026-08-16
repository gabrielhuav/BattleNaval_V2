package ovh.gabrielhuav.battlenaval_v2.battlenavalbinario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Las conversiones son el contenido didáctico del juego: si dan mal resultado,
 * la app enseña algo incorrecto. Estos tests fijan las reglas de los capítulos
 * III y IV de la tesis.
 */
class BinaryConversionHelperTest {

    @Test
    fun `las filas A a G se codifican como su ASCII en 8 bits`() {
        assertEquals("01000001", BinaryConversionHelper.charToBinary('A')) // 65
        assertEquals("01000100", BinaryConversionHelper.charToBinary('D')) // 68
        assertEquals("01000111", BinaryConversionHelper.charToBinary('G')) // 71
    }

    @Test
    fun `charToBinary siempre rellena a 8 bits`() {
        // El caso que rompería el formato: un código bajo, de pocos bits.
        assertEquals("00001010", BinaryConversionHelper.charToBinary('\n')) // 10
        assertEquals(8, BinaryConversionHelper.charToBinary(' ').length)
    }

    @Test
    fun `las columnas 1 a 7 se codifican en 3 bits`() {
        assertEquals("001", BinaryConversionHelper.numberToBinary(1))
        assertEquals("101", BinaryConversionHelper.numberToBinary(5))
        assertEquals("111", BinaryConversionHelper.numberToBinary(7))
    }

    @Test
    fun `numberToBinary rellena con ceros a la izquierda`() {
        assertEquals("000", BinaryConversionHelper.numberToBinary(0))
        assertEquals(3, BinaryConversionHelper.numberToBinary(2).length)
    }

    @Test
    fun `binaryToChar y binaryToNumber invierten la codificacion`() {
        assertEquals('A', BinaryConversionHelper.binaryToChar("01000001"))
        assertEquals('G', BinaryConversionHelper.binaryToChar("01000111"))
        assertEquals(5, BinaryConversionHelper.binaryToNumber("101"))
        assertEquals(7, BinaryConversionHelper.binaryToNumber("111"))
    }

    @Test
    fun `ida y vuelta para todas las coordenadas del tablero`() {
        for (fila in 'A'..'G') {
            val binario = BinaryConversionHelper.charToBinary(fila)
            assertEquals(fila, BinaryConversionHelper.binaryToChar(binario))
        }
        for (columna in 1..7) {
            val binario = BinaryConversionHelper.numberToBinary(columna)
            assertEquals(columna, BinaryConversionHelper.binaryToNumber(binario))
        }
    }

    @Test
    fun `checkConversion acepta la respuesta correcta`() {
        // Disparo a la columna 5, fila A: el ejemplo de la secuencia didáctica.
        assertTrue(
            BinaryConversionHelper.checkConversion(
                binaryY = "01000001", // 'A'
                binaryX = "101",      // 5
                answerX = 'A',
                answerY = 5
            )
        )
    }

    @Test
    fun `checkConversion rechaza la letra equivocada`() {
        assertFalse(
            BinaryConversionHelper.checkConversion("01000001", "101", 'B', 5)
        )
    }

    @Test
    fun `checkConversion rechaza el numero equivocado`() {
        assertFalse(
            BinaryConversionHelper.checkConversion("01000001", "101", 'A', 4)
        )
    }

    @Test
    fun `coordsToString usa la fila como letra y la columna como numero`() {
        assertEquals("A1", BinaryConversionHelper.coordsToString(x = 0, y = 0))
        assertEquals("A3", BinaryConversionHelper.coordsToString(x = 2, y = 0))
        assertEquals("G7", BinaryConversionHelper.coordsToString(x = 6, y = 6))
    }

    @Test
    fun `generateRandomCoordinates cae siempre dentro del tablero`() {
        repeat(200) {
            val (x, y) = BinaryConversionHelper.generateRandomCoordinates()
            assertTrue("x fuera de rango: $x", x in 0 until BOARD_SIZE)
            assertTrue("y fuera de rango: $y", y in 0 until BOARD_SIZE)
        }
    }

    @Test
    fun `generateExample produce binarios decodificables`() {
        repeat(50) {
            val (binarioLetra, binarioNumero, _) = BinaryConversionHelper.generateExample()
            val letra = BinaryConversionHelper.binaryToChar(binarioLetra)
            val numero = BinaryConversionHelper.binaryToNumber(binarioNumero)
            assertTrue("letra fuera de A-G: $letra", letra in 'A'..'G')
            assertTrue("numero fuera de 1-7: $numero", numero in 1..7)
        }
    }
}
