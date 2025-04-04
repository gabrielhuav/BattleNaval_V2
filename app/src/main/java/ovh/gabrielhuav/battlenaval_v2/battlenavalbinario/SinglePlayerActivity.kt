package ovh.gabrielhuav.battlenaval_v2.battlenavalbinario

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ovh.gabrielhuav.battlenaval_v2.R
import kotlin.random.Random

class SinglePlayerActivity : AppCompatActivity() {
    private var running = false
    private lateinit var enemyBoard: Board
    private lateinit var playerBoard: Board
    private lateinit var playerBoardFrame: FrameLayout
    private lateinit var enemyBoardFrame: FrameLayout

    // Para el desafío de conversión binaria
    private lateinit var binaryConversionDialog: BinaryConversionDialog
    private var lastRevealedCell: Pair<Int, Int>? = null

    // Límites de zoom
    private val minZoom = 0.5f
    private val maxZoom = 2.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_player)

        playerBoardFrame = findViewById(R.id.playerBoardFrame)
        enemyBoardFrame = findViewById(R.id.enemyBoardFrame)

        val newGameButton = findViewById<Button>(R.id.btnNewGame)
        val backToMenuButton = findViewById<Button>(R.id.btnBackToMenu)
        val zoomInButton = findViewById<Button>(R.id.btnZoomIn)
        val zoomOutButton = findViewById<Button>(R.id.btnZoomOut)

        // Inicializar el diálogo de conversión binaria
        binaryConversionDialog = BinaryConversionDialog(this)

        // Configuración de botones de zoom
        zoomInButton.setOnClickListener {
            adjustZoom(0.1f)
        }

        zoomOutButton.setOnClickListener {
            adjustZoom(-0.1f)
        }

        newGameButton.setOnClickListener {
            startNewGame()
        }

        backToMenuButton.setOnClickListener {
            finish()
        }

        startNewGame()
    }

    private fun startNewGame() {
        running = true
        lastRevealedCell = null

        // Limpia los tableros existentes
        playerBoardFrame.removeAllViews()
        enemyBoardFrame.removeAllViews()

        // Crea el tablero del jugador
        playerBoard = Board(this, false) { }
        playerBoardFrame.addView(playerBoard)

        // Crea el tablero del enemigo
        enemyBoard = Board(this, true) { cell ->
            if (!running || cell.wasShot) return@Board

            // Crear nuevo diálogo cada vez para evitar problemas de estado
            binaryConversionDialog = BinaryConversionDialog(this)

            // Mostrar el desafío de conversión binaria antes de disparar
            showBinaryConversionChallenge(cell.x, cell.y)
        }
        enemyBoardFrame.addView(enemyBoard)

        // Coloca los barcos
        placePlayerShips()
        placeEnemyShips()

        // Revelar una celda inicial
        revealRandomEnemyShipCell()
    }

    private fun showBinaryConversionChallenge(clickedX: Int, clickedY: Int) {
        // Determinar si debemos reutilizar la última coordenada
        val reuseLastCoordinate = lastRevealedCell != null
        val cellToReveal = lastRevealedCell ?: findEnemyShipCell()

        binaryConversionDialog.showConversionChallenge(
            cellToReveal?.first,
            cellToReveal?.second,
            onCorrectConversion = { shootX, shootY, isCorrectConversion ->
                // Si la conversión fue correcta, disparar en la coordenada revelada
                // Si fue incorrecta, disparar en la coordenada que ingresó el usuario

                val cell = enemyBoard.getCell(shootX, shootY)

                // Solo permitir acertar a barcos si la conversión fue correcta
                if (!cell.wasShot) {
                    if (isCorrectConversion) {
                        // Conversión correcta: disparo normal
                        val hit = cell.shoot()
                        Log.d("SinglePlayerActivity", "Disparo correcto en ${BinaryConversionHelper.coordsToString(shootX, shootY)} - Hit: $hit")

                        if (enemyBoard.ships == 0) {
                            Toast.makeText(this, "¡GANASTE!", Toast.LENGTH_LONG).show()
                            running = false
                            return@showConversionChallenge
                        }
                    } else {
                        // Conversión incorrecta: asegurarse de no acertar a un barco por casualidad
                        if (cell.ship != null) {
                            // Si hay un barco, no permitir acertar - buscar otra celda vacía
                            val emptyCell = findSafeEmptyCell()
                            if (emptyCell != null) {
                                val emptyCellX = emptyCell.first
                                val emptyCellY = emptyCell.second
                                val emptyBoardCell = enemyBoard.getCell(emptyCellX, emptyCellY)
                                emptyBoardCell.wasShot = true
                                emptyBoardCell.invalidate()
                                Log.d("SinglePlayerActivity", "Conversión incorrecta, redirigiendo disparo a celda vacía: ${BinaryConversionHelper.coordsToString(emptyCellX, emptyCellY)}")

                                Toast.makeText(this, "Disparo incorrecto en ${BinaryConversionHelper.coordsToString(emptyCellX, emptyCellY)}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // No hay barco, disparar normalmente
                            cell.wasShot = true
                            cell.invalidate()
                            Log.d("SinglePlayerActivity", "Disparo incorrecto en ${BinaryConversionHelper.coordsToString(shootX, shootY)}")

                            Toast.makeText(this, "Disparo incorrecto en ${BinaryConversionHelper.coordsToString(shootX, shootY)}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Turno del enemigo (siempre, independiente de si acertó o no)
                    enemyMove()
                } else {
                    Toast.makeText(this, "Ya has disparado en esa coordenada", Toast.LENGTH_SHORT).show()
                }

                // Reiniciar la última celda revelada si la conversión fue exitosa
                if (isCorrectConversion) {
                    lastRevealedCell = null
                    // Revelar una nueva celda para el próximo turno
                    revealRandomEnemyShipCell()
                } else if (!reuseLastCoordinate) {
                    // Mantener la misma coordenada para el próximo intento si fue incorrecto
                    // No cambia lastRevealedCell
                } else {
                    // Si estaba reusando la coordenada y aún así falló, seguir con la misma
                }
            },
            reuseLastCoordinate = reuseLastCoordinate
        )
    }

    // Encontrar una celda vacía para disparos fallidos, para evitar acertar por casualidad
    private fun findSafeEmptyCell(): Pair<Int, Int>? {
        val emptyCells = mutableListOf<Pair<Int, Int>>()

        for (y in 0 until 7) {
            for (x in 0 until 7) {
                val cell = enemyBoard.getCell(x, y)
                if (cell.ship == null && !cell.wasShot) {
                    emptyCells.add(Pair(x, y))
                }
            }
        }

        return if (emptyCells.isNotEmpty()) {
            emptyCells.random()
        } else {
            null
        }
    }

    private fun findEnemyShipCell(): Pair<Int, Int>? {
        // Recolectar SOLO celdas con barcos no disparados
        val shipCells = mutableListOf<Pair<Int, Int>>()

        for (y in 0 until 7) {
            for (x in 0 until 7) {
                val cell = enemyBoard.getCell(x, y)
                if (cell.ship != null && !cell.wasShot) {
                    shipCells.add(Pair(x, y))
                }
            }
        }

        // Si hay celdas disponibles, seleccionar una al azar
        return if (shipCells.isNotEmpty()) {
            Log.d("SinglePlayerActivity", "Encontradas ${shipCells.size} celdas con barcos sin disparar")
            val selected = shipCells.random()
            Log.d("SinglePlayerActivity", "Revelando celda con barco en: (${selected.first},${selected.second}) = ${BinaryConversionHelper.coordsToString(selected.first, selected.second)}")
            selected
        } else {
            Log.d("SinglePlayerActivity", "No se encontraron celdas con barcos sin disparar")
            null
        }
    }

    private fun revealRandomEnemyShipCell() {
        // Obtener una celda de barco enemigo - SIEMPRE debe ser una celda con barco
        val cellToReveal = findEnemyShipCell()

        if (cellToReveal != null) {
            lastRevealedCell = cellToReveal

            // Log para depuración
            val coordString = BinaryConversionHelper.coordsToString(cellToReveal.first, cellToReveal.second)
            Log.d("SinglePlayerActivity", "Revelando coordenada de barco: $coordString")

            // Notificar al usuario
            Toast.makeText(this, "¡Nueva coordenada binaria revelada!", Toast.LENGTH_SHORT).show()
        } else {
            // Si no hay barcos sin disparar, notificar victoria
            Toast.makeText(this, "¡Has encontrado todos los barcos enemigos!", Toast.LENGTH_LONG).show()
            running = false
        }
    }

    private fun placePlayerShips() {
        val shipData = listOf(
            Pair(4, Color.GREEN),
            Pair(3, Color.RED),
            Pair(2, Color.GRAY)
        )

        for ((size, color) in shipData) {
            while (true) {
                val x = Random.nextInt(0, 7)
                val y = Random.nextInt(0, 7)
                val vertical = Random.nextBoolean()

                if (playerBoard.placeShip(Ship(size, vertical, color), x, y)) break
            }
        }
    }

    private fun placeEnemyShips() {
        val shipSizes = listOf(4, 3, 2)
        for (size in shipSizes) {
            while (true) {
                val x = Random.nextInt(0, 7)
                val y = Random.nextInt(0, 7)
                val vertical = Random.nextBoolean()

                if (enemyBoard.placeShip(Ship(size, vertical, Color.TRANSPARENT), x, y)) break
            }
        }
    }

    private fun enemyMove() {
        while (true) {
            val x = Random.nextInt(0, 7)
            val y = Random.nextInt(0, 7)

            val cell = playerBoard.getCell(x, y)
            if (cell.wasShot) continue

            val hit = cell.shoot()
            if (hit) {
                Toast.makeText(this, "El enemigo te ha golpeado un barco", Toast.LENGTH_SHORT).show()
            }

            if (playerBoard.ships == 0) {
                Toast.makeText(this, "¡PERDISTE!", Toast.LENGTH_LONG).show()
                running = false
            }
            break
        }
    }

    private fun adjustZoom(delta: Float) {
        val newZoom = playerBoard.scaleFactor + delta
        if (newZoom in minZoom..maxZoom) {
            playerBoard.adjustCellSize(newZoom)
            enemyBoard.adjustCellSize(newZoom)
        }
    }
}