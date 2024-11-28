package ovh.gabrielhuav.battlenaval_v2

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class SinglePlayerActivity : AppCompatActivity() {
    private var running = false
    private lateinit var enemyBoard: Board
    private lateinit var playerBoard: Board
    private lateinit var playerBoardFrame: FrameLayout
    private lateinit var enemyBoardFrame: FrameLayout

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

        // Limpia los tableros existentes
        playerBoardFrame.removeAllViews()
        enemyBoardFrame.removeAllViews()

        // Crea el tablero del jugador
        playerBoard = Board(this, false) { }
        playerBoardFrame.addView(playerBoard)

        // Crea el tablero del enemigo
        enemyBoard = Board(this, true) { cell ->
            if (!running || cell.wasShot) return@Board
            val hit = cell.shoot()

            if (enemyBoard.ships == 0) {
                Toast.makeText(this, "¡GANASTE!", Toast.LENGTH_LONG).show()
                running = false
                return@Board
            }

            enemyMove()
        }
        enemyBoardFrame.addView(enemyBoard)

        // Coloca los barcos
        placePlayerShips()
        placeEnemyShips()
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
