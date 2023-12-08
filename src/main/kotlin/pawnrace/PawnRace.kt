package pawnrace

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.util.concurrent.Executors
import kotlin.random.Random

// You should not add any more member values or member functions to this class
// (or change its name!). The autorunner will load it in via reflection, and it
// will be safer for you to just call your code from within the playGame member
// function, without any unexpected surprises!
class PawnRace {
    // Don't edit the type or the name of this method
    // The colour can take one of two values: 'W' or 'B', this indicates your player colour
    fun playGame(colour: Char, output: PrintWriter, input: BufferedReader) {
        // You should call your code from within here
        // Step 1: If you are the black player, you should send a string containing the gaps
        // It should be of the form "wb" with the white gap first and then the black gap: i.e. "AH"
        val player: Piece = parseColour(colour)
        val opp = player.opposite()
        if (player == Piece.B) {
            val gapsList = listOf("Ad", "Ae", "Af", "Ag", "Hb", "Hc", "Hd", "He")
            output.println(gapsList[Random.nextInt(0, gapsList.size)])
        }

        // Regardless of your colour, you should now receive the gaps verified by the autorunner
        // (or by the human if you are using your own main function below), these are provided
        // in the same form as above ("wb"), for example: "AH"
        val gaps = input.readLine()

        // Now you may construct your initial board
        val board = Board(
            File(gaps.lowercase()[0].code - 'a'.code),
            File(gaps.lowercase()[1].code - 'a'.code),
        )
        var game = Game(board, Piece.W)

        val hash = HashMap<Game, Pair<Int, Int>>()

        // If you are the white player, you are now allowed to move
        // you may send your move, once you have decided what it will be, with output.println(move)
        // for example: output.println("axb4")
        if (player == Piece.W) {
            val executor = Executors.newSingleThreadExecutor()
            var move = itDeepN(game, 4500, player, hash, executor)
//            var move: Move? = null
            if (move == null) {
                move = game.randomMove(player)
            }
            output.println(move)
            executor.shutdown()
            game = game.applyMove(move)
            println(game)
        }

        // After point, you may create a loop which waits to receive the other players move
        // (via input.readLine()), updates the state, checks for game over and, if not, decides
        // on a new move and again send that with output.println(move). You should check if the
        // game is over after every move.
        while (!game.over()) {
            game = game.applyMove(game.parseMove(input.readLine(), opp) ?: game.randomMove(opp))
            println(game)
            if (game.over()) {
                break
            }
            val executor = Executors.newSingleThreadExecutor()
            var move = itDeepN(game,  4500, player, hash, executor)
//            var move: Move? = null
            if (move == null) {
                move = game.randomMove(player)
            }
            output.println(move)
            executor.shutdown()
            game = game.applyMove(move)
            println(game)
        }

        // Once the loop is over, the game has finished and you may wish to print who has won
        // If your advanced AI has used any files, make sure you close them now!
        // TODO: tidy up resources, if any
    }
}

// When running the command, provide an argument either W or B, this indicates your player colour
fun main(args: Array<String>) {
    PawnRace().playGame(args[0][0], PrintWriter(System.out, true), BufferedReader(InputStreamReader(System.`in`)))
}
