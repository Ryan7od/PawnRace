package pawnrace

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter

// You should not add any more member values or member functions to this class
// (or change its name!). The autorunner will load it in via reflection and it
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
        output.println("AH")

        // Regardless of your colour, you should now receive the gaps verified by the autorunner
        // (or by the human if you are using your own main function below), these are provided
        // in the same form as above ("wb"), for example: "AH"
        val gaps = input.readLine()

        // Now you may construct your initial board
        val board = Board(
            File(gaps.lowercase()[0].code - 'a'.code),
            File(gaps.lowercase()[1].code - 'a'.code),
        )
        var game = Game(board, player)

        val hash = HashMap<Game, Pair<Int, Int>>()

        // If you are the white player, you are now allowed to move
        // you may send your move, once you have decided what it will be, with output.println(move)
        // for example: output.println("axb4")
        if (player == Piece.W) {
            val move = game.parseMove(gaps[1] + "4")
            game = if (move != null) {
                game.applyMove(move)
            } else {
                game.applyMove(game.randomMove())
            }
            output.println(move)
            println(game)
        }

        // After point, you may create a loop which waits to receive the other players move
        // (via input.readLine()), updates the state, checks for game over and, if not, decides
        // on a new move and again send that with output.println(move). You should check if the
        // game is over after every move.
    /* TODO: Create the "game loop", which:
          * gets the opponents move
          * updates board
          * checks game over, if not then
          * choose a move
          * send this move
          * update the state
          * check game over
          * rinse, and repeat.
    */
        while (!game.over()) {
            game = game.applyMove(game.parseMove(input.readLine()) ?: game.randomMove())
            println(game)
            if (game.over()) {
                break
            }
            val move = itDeepN(game, 10, 3500, player, hash)
            output.println(move)
            game = game.applyMove(move ?: game.randomMove())
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
