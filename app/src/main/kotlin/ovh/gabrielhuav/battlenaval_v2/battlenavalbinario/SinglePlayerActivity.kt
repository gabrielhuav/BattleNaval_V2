package ovh.gabrielhuav.battlenaval_v2.battlenavalbinario

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ovh.gabrielhuav.battlenaval_v2.R
import ovh.gabrielhuav.battlenaval_v2.ThemeManager
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
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_player)
        supportActionBar?.hide()

        playerBoardFrame = findViewById(R.id.playerBoardFrame)
        enemyBoardFrame = findViewById(R.id.enemyBoardFrame)

        val newGameButton = findViewById<Button>(R.id.btnNewGame)
        val backToMenuButton = findViewById<Button>(R.id.btnBackToMenu)

        // Inicializar el diálogo de conversión binaria
        binaryConversionDialog = BinaryConversionDialog(this)

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

        // Ocultar overlay de fin de juego al reiniciar
        findViewById<View>(R.id.endGameOverlay).visibility = View.GONE

        // Limpia los tableros existentes
        playerBoardFrame.removeAllViews()
        enemyBoardFrame.removeAllViews()

        // Crea el tablero del jugador
        playerBoard = Board(this, false) { }
        playerBoardFrame.addView(playerBoard)

        // Crea el tablero del enemigo
        enemyBoard = Board(this, true) { cell ->
            if (!running || cell.wasShot) return@Board

            binaryConversionDialog = BinaryConversionDialog(this)
            showBinaryConversionChallenge(cell.x, cell.y)
        }
        enemyBoardFrame.addView(enemyBoard)

        placePlayerShips()
        placeEnemyShips()
        revealRandomEnemyShipCell()
    }

    // Método centralizado para manejar el fin de la partida visualmente
    private fun showEndGameScreen(isVictory: Boolean) {
        running = false

        val overlay = findViewById<LinearLayout>(R.id.endGameOverlay)
        val title = findViewById<TextView>(R.id.tvEndGameTitle)
        val subtitle = findViewById<TextView>(R.id.tvEndGameSubtitle)

        if (isVictory) {
            title.text = "¡VICTORIA!"
            title.setTextColor(Color.parseColor("#00E5FF")) // Cian neón
            title.setShadowLayer(25f, 0f, 0f, Color.parseColor("#00E5FF"))
            subtitle.text = "Flota enemiga completamente neutralizada"
        } else {
            title.text = "¡MISION FALLIDA!"
            title.setTextColor(Color.parseColor("#FF1744")) // Rojo alerta
            title.setShadowLayer(25f, 0f, 0f, Color.parseColor("#FF1744"))
            subtitle.text = "Tu flota ha sido hundida en combate"
        }

        overlay.visibility = View.VISIBLE

        // Al tocar el mensaje, permitir ver el estado final del tablero
        overlay.setOnClickListener {
            overlay.visibility = View.GONE
        }
    }

    private fun showBinaryConversionChallenge(clickedX: Int, clickedY: Int) {
        val reuseLastCoordinate = lastRevealedCell != null
        val cellToReveal = lastRevealedCell ?: findEnemyShipCell()

        binaryConversionDialog.showConversionChallenge(
            cellToReveal?.first,
            cellToReveal?.second,
            onCorrectConversion = { shootX, shootY, isCorrectConversion ->
                val cell = enemyBoard.getCell(shootX, shootY)

                if (!cell.wasShot) {
                    if (isCorrectConversion) {
                        val hit = cell.shoot()
                        if (enemyBoard.ships == 0) {
                            showEndGameScreen(isVictory = true)
                            return@showConversionChallenge
                        }
                    } else {
                        handleIncorrectShot(cell)
                    }
                    enemyMove()
                } else {
                    Toast.makeText(this, "Ya has disparado en esa coordenada", Toast.LENGTH_SHORT).show()
                }

                if (isCorrectConversion) {
                    lastRevealedCell = null
                    revealRandomEnemyShipCell()
                }
            },
            reuseLastCoordinate = reuseLastCoordinate
        )
    }

    private fun handleIncorrectShot(cell: Cell) {
        if (cell.ship != null) {
            val emptyCell = findSafeEmptyCell()
            if (emptyCell != null) {
                val emptyBoardCell = enemyBoard.getCell(emptyCell.first, emptyCell.second)
                emptyBoardCell.wasShot = true
                emptyBoardCell.invalidate()
                Toast.makeText(this, "¡Error de cálculo! Disparo desviado", Toast.LENGTH_SHORT).show()
            }
        } else {
            cell.wasShot = true
            cell.invalidate()
            Toast.makeText(this, "Coordenada incorrecta", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findSafeEmptyCell(): Pair<Int, Int>? {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until 7) {
            for (x in 0 until 7) {
                val cell = enemyBoard.getCell(x, y)
                if (cell.ship == null && !cell.wasShot) emptyCells.add(Pair(x, y))
            }
        }
        return if (emptyCells.isNotEmpty()) emptyCells.random() else null
    }

    private fun findEnemyShipCell(): Pair<Int, Int>? {
        val shipCells = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until 7) {
            for (x in 0 until 7) {
                val cell = enemyBoard.getCell(x, y)
                if (cell.ship != null && !cell.wasShot) shipCells.add(Pair(x, y))
            }
        }
        return if (shipCells.isNotEmpty()) shipCells.random() else null
    }

    private fun revealRandomEnemyShipCell() {
        val cellToReveal = findEnemyShipCell()
        if (cellToReveal != null) {
            lastRevealedCell = cellToReveal
            Toast.makeText(this, "¡Nueva señal de radar detectada!", Toast.LENGTH_SHORT).show()
        } else if (running) {
            showEndGameScreen(isVictory = true)
        }
    }

    private fun placePlayerShips() {
        val shipData = listOf(Pair(4, Color.GREEN), Pair(3, Color.WHITE), Pair(2, Color.RED))
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
        if (!running) return
        while (true) {
            val x = Random.nextInt(0, 7)
            val y = Random.nextInt(0, 7)
            val cell = playerBoard.getCell(x, y)
            if (cell.wasShot) continue

            val hit = cell.shoot()
            if (hit) Toast.makeText(this, "¡Alerta! Un barco ha sido impactado", Toast.LENGTH_SHORT).show()

            if (playerBoard.ships == 0) {
                showEndGameScreen(isVictory = false)
            }
            break
        }
    }
}