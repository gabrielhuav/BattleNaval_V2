package ovh.gabrielhuav.battlenaval_v2.battlenavalbinario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reglas del tablero. Estos tests son la red que protege el refactor de la capa
 * de dibujo y, más adelante, la migración del tablero a Compose.
 */
class BoardModelTest {

    @Test
    fun `el tablero arranca vacio y con la flota completa`() {
        val board = BoardModel()
        assertEquals(DEFAULT_FLEET_SIZE, board.remainingShips)
        assertTrue(board.occupiedCells().isEmpty())
        assertNull(board.shipAt(0, 0))
        assertFalse(board.wasShot(0, 0))
    }

    @Test
    fun `un barco horizontal ocupa celdas hacia la derecha`() {
        val board = BoardModel()
        val ship = Ship(type = 3, vertical = false)

        assertTrue(board.placeShip(ship, x = 1, y = 2))

        assertSame(ship, board.shipAt(1, 2))
        assertSame(ship, board.shipAt(2, 2))
        assertSame(ship, board.shipAt(3, 2))
        assertNull(board.shipAt(4, 2))
        assertNull(board.shipAt(1, 3))
    }

    @Test
    fun `un barco vertical ocupa celdas hacia abajo`() {
        val board = BoardModel()
        val ship = Ship(type = 2, vertical = true)

        assertTrue(board.placeShip(ship, x = 4, y = 0))

        assertSame(ship, board.shipAt(4, 0))
        assertSame(ship, board.shipAt(4, 1))
        assertNull(board.shipAt(4, 2))
    }

    @Test
    fun `no se puede colocar un barco que se sale del tablero`() {
        val board = BoardModel()

        assertFalse(board.placeShip(Ship(type = 3, vertical = false), x = 5, y = 0))
        assertFalse(board.placeShip(Ship(type = 4, vertical = true), x = 0, y = 5))
        assertTrue("un rechazo no debe dejar rastro", board.occupiedCells().isEmpty())
    }

    @Test
    fun `no se puede encimar dos barcos`() {
        val board = BoardModel()
        val primero = Ship(type = 3, vertical = false)
        assertTrue(board.placeShip(primero, x = 0, y = 0))

        assertFalse(board.placeShip(Ship(type = 2, vertical = false), x = 2, y = 0))
        assertFalse(board.placeShip(Ship(type = 2, vertical = true), x = 1, y = 0))

        // El primero sigue intacto y no quedó nada del intento fallido.
        assertEquals(3, board.occupiedCells().size)
        assertSame(primero, board.shipAt(2, 0))
    }

    @Test
    fun `coordenadas fuera de rango no rompen ni colocan`() {
        val board = BoardModel()
        assertFalse(board.placeShip(Ship(type = 1, vertical = false), x = -1, y = 0))
        assertFalse(board.placeShip(Ship(type = 1, vertical = false), x = 0, y = BOARD_SIZE))
        assertNull(board.shipAt(-1, 0))
        assertFalse(board.wasShot(99, 99))
        assertTrue(board.occupiedCells().isEmpty())
    }

    @Test
    fun `disparar al agua marca la celda y devuelve false`() {
        val board = BoardModel()

        assertFalse(board.shoot(3, 3))

        assertTrue(board.wasShot(3, 3))
        assertEquals(DEFAULT_FLEET_SIZE, board.remainingShips)
    }

    @Test
    fun `disparar a un barco devuelve true sin hundir la flota todavia`() {
        val board = BoardModel()
        board.placeShip(Ship(type = 2, vertical = false), x = 0, y = 0)

        assertTrue(board.shoot(0, 0))

        assertTrue(board.wasShot(0, 0))
        assertEquals("aún le queda una celda", DEFAULT_FLEET_SIZE, board.remainingShips)
    }

    @Test
    fun `hundir un barco descuenta uno de la flota`() {
        val board = BoardModel()
        board.placeShip(Ship(type = 2, vertical = false), x = 0, y = 0)

        board.shoot(0, 0)
        board.shoot(1, 0)

        assertEquals(DEFAULT_FLEET_SIZE - 1, board.remainingShips)
    }

    @Test
    fun `hundir toda la flota deja el contador en cero`() {
        val board = BoardModel()
        board.placeShip(Ship(type = 2, vertical = false), x = 0, y = 0)
        board.placeShip(Ship(type = 3, vertical = false), x = 0, y = 2)
        board.placeShip(Ship(type = 4, vertical = true), x = 6, y = 0)

        listOf(0 to 0, 1 to 0).forEach { (x, y) -> board.shoot(x, y) }
        listOf(0 to 2, 1 to 2, 2 to 2).forEach { (x, y) -> board.shoot(x, y) }
        listOf(6 to 0, 6 to 1, 6 to 2, 6 to 3).forEach { (x, y) -> board.shoot(x, y) }

        assertEquals(0, board.remainingShips)
    }

    @Test
    fun `clearShips vacia el tablero y pone la flota en cero`() {
        val board = BoardModel()
        board.placeShip(Ship(type = 3, vertical = false), x = 0, y = 0)

        board.clearShips()

        assertTrue(board.occupiedCells().isEmpty())
        assertEquals(0, board.remainingShips)
    }

    @Test
    fun `clearShips conserva las marcas de disparo`() {
        val board = BoardModel()
        board.shoot(2, 2)

        board.clearShips()

        assertTrue("limpiar barcos no borra el historial de disparos", board.wasShot(2, 2))
    }

    @Test
    fun `occupiedCoordinates usa la notacion del juego`() {
        val board = BoardModel()
        board.placeShip(Ship(type = 2, vertical = false), x = 0, y = 0)

        assertEquals(listOf("A" to "1", "A" to "2"), board.occupiedCoordinates())
    }

    @Test
    fun `occupiedCoordinates coincide con la conversion binaria`() {
        val board = BoardModel()
        board.placeShip(Ship(type = 1, vertical = false), x = 4, y = 6)

        val (letra, numero) = board.occupiedCoordinates().single()

        assertEquals("G", letra)
        assertEquals("5", numero)
        assertEquals(letra[0], BinaryConversionHelper.binaryToChar("01000111"))
        assertEquals(numero.toInt(), BinaryConversionHelper.binaryToNumber("101"))
    }

    @Test
    fun `un barco de longitud uno se hunde de un disparo`() {
        val board = BoardModel()
        board.placeShip(Ship(type = 1, vertical = false), x = 3, y = 3)

        assertTrue(board.shoot(3, 3))

        assertEquals(DEFAULT_FLEET_SIZE - 1, board.remainingShips)
    }
}
