package ovh.gabrielhuav.battlenaval_v2

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class SinglePlayerActivity : AppCompatActivity() {
    private var running = false
    private lateinit var enemyBoard: Board
    private lateinit var playerBoard: Board
    private var enemyTurn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_player)

        val boardContainer = findViewById<FrameLayout>(R.id.boardContainer)
        val newGameButton = findViewById<Button>(R.id.btnNewGame)
        val backToMenuButton = findViewById<Button>(R.id.btnBackToMenu)

        newGameButton.setOnClickListener {
            startNewGame(boardContainer)
        }

        backToMenuButton.setOnClickListener {
            finish() // Cierra esta actividad y regresa al menú principal
        }

        startNewGame(boardContainer) // Inicia el juego automáticamente
    }

    private fun startNewGame(container: FrameLayout) {
        running = true
        enemyTurn = false

        container.removeAllViews()

        // Crear tablero del enemigo
        enemyBoard = Board(this, true) { cell ->
            if (!running || cell.wasShot) return@Board
            val hit = cell.shoot()

            if (enemyBoard.ships == 0) {
                Toast.makeText(this, "¡GANASTE!", Toast.LENGTH_LONG).show()
                running = false
                return@Board
            }

            // Si el jugador hace un disparo, el enemigo responde
            enemyMove()
        }

        // Crear tablero del jugador
        playerBoard = Board(this, false) { }

        // Colocar barcos automáticamente
        placePlayerShips()
        placeEnemyShips()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(playerBoard, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            ))
            addView(enemyBoard, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            ))
        }

        container.addView(layout)
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
}
