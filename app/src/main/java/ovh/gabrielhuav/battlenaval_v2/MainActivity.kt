package ovh.gabrielhuav.battlenaval_v2

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private var running = false
    private lateinit var enemyBoard: Board
    private lateinit var playerBoard: Board
    private var enemyTurn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val newGameButton = findViewById<Button>(R.id.btnNewGame)
        val boardContainer = findViewById<FrameLayout>(R.id.boardContainer)

        newGameButton.setOnClickListener {
            startNewGame(boardContainer)
        }
    }

    private fun startNewGame(container: FrameLayout) {
        container.removeAllViews() // Limpia el contenedor antes de crear nuevos tableros

        // Crear tablero del enemigo
        enemyBoard = Board(this, true) { cell ->
            if (!running) return@Board
            if (cell.wasShot) return@Board

            enemyTurn = !cell.shoot()

            if (enemyBoard.ships == 0) {
                Toast.makeText(this@MainActivity, "¡GANASTE!", Toast.LENGTH_LONG).show()
                running = false
            }

            if (enemyTurn) enemyMove()
        }

        // Crear tablero del jugador
        playerBoard = Board(this, false) { cell ->
            if (running) return@Board
        }

        // Colocar barcos automáticamente en el tablero del jugador
        placePlayerShips()

        // Agregar los tableros al contenedor
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

        container.addView(layout) // Dibuja el diseño en la UI
    }

    private fun placePlayerShips() {
        val shipSizes = listOf(4, 3, 2) // Tamaños de los barcos
        for (size in shipSizes) {
            while (true) {
                val x = Random.nextInt(0, 7)
                val y = Random.nextInt(0, 7)
                val vertical = Random.nextBoolean()

                if (playerBoard.placeShip(Ship(size, vertical), x, y)) {
                    break
                }
            }
        }
    }

    private fun enemyMove() {
        while (enemyTurn) {
            val x = Random.nextInt(0, 7)
            val y = Random.nextInt(0, 7)

            val cell = playerBoard.getCell(x, y)
            if (cell.wasShot) continue

            enemyTurn = cell.shoot()

            if (playerBoard.ships == 0) {
                Toast.makeText(this, "¡PERDISTE!", Toast.LENGTH_LONG).show()
                running = false
            }
        }
    }
}
