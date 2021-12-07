import java.util.*
import javax.swing.*

class VoteNode {
    var nextVote : VoteNode? = null
    var candidateNumber : Int = 0
}

class BallotNode {
    var nextBallot: BallotNode? = null
    var ballotHead = VoteNode()
}

class RankedVoting {
    private val head = BallotNode()
    private var candidateCount = 0
    private var voteTotals = mutableMapOf<Int, Int>()
    private var voterCount = 0
    private var tie = true

    fun runElection() {
        vote()
        computeVotes()
        displayResults()
    }

    private fun vote() {
        val scan = Scanner(System.`in`)
        println("Enter the number of candidates: ")
        candidateCount = scan.nextLine().toInt()
        println("Voting begins now. When finished, type \"stop\".")
        var input = "0"
        var currentBallot: BallotNode = head
        while (input.lowercase() != "stop") {
            println("Rank your choices and enter them separated by spaces. e.g. \"2 1 3 4\"")
            input = scan.nextLine()
            var currentVote = currentBallot.ballotHead
            if (input.lowercase() == "stop") break
            println("input: $input")
            for (i in input.split(" ").map { it.toInt() }) {
                currentVote.candidateNumber = i
                currentVote.nextVote = VoteNode()
                currentVote = currentVote.nextVote!!
            }
            currentBallot.nextBallot = BallotNode()
            currentBallot = currentBallot.nextBallot!!
            voterCount++
        }
        scan.close()
    }

    private fun computeVotes() {

        var minVotesCandidate: Int
        var minVotes: Int

        var currentBallot = head
        while (currentBallot.nextBallot != null) {
            voteTotals[currentBallot.ballotHead.candidateNumber] = (voteTotals[currentBallot.ballotHead.candidateNumber] ?: 0) + 1
            currentBallot = currentBallot.nextBallot!!
        }

        while (!voteTotals.values.any { it > voterCount / 2 }) {

            minVotes = voteTotals.values.minOrNull() ?: 0

            tie = true
            for (i in voteTotals) {
                if (i.value != minVotes) {
                    tie = false
                    break
                }
            }
            if (tie) break

            for (i in voteTotals) {
                if (i.value == minVotes) {
                    minVotesCandidate = i.key
                    currentBallot = head
                    while (currentBallot.nextBallot != null) { // Reassign votes of eliminated candidate
                        if (currentBallot.ballotHead.candidateNumber == minVotesCandidate) {
                            if (currentBallot.ballotHead.nextVote != null) {
                                do {
                                    currentBallot.ballotHead = currentBallot.ballotHead.nextVote!!
                                } while (currentBallot.ballotHead.nextVote != null && (voteTotals[currentBallot.ballotHead.candidateNumber] == null || voteTotals[currentBallot.ballotHead.candidateNumber]!! <= minVotes))
                            }
                        }
                        currentBallot = currentBallot.nextBallot!!
                    }
                }
            }

            voteTotals = mutableMapOf()

            currentBallot = head
            while (currentBallot.nextBallot != null) {
                voteTotals[currentBallot.ballotHead.candidateNumber] = (voteTotals[currentBallot.ballotHead.candidateNumber] ?: 0) + 1
                currentBallot = currentBallot.nextBallot!!
            }
        }
    }


    private fun displayResults() {
        if (tie) println("There was a tie.")
        println("Winning candidates(s):")
        val maxVotes = voteTotals.values.maxOrNull()
        for (i in voteTotals) {
            if (i.value == maxVotes) {
                println(i.key)
                if (!tie) break
            }
        }
    }

}

fun main() {
        RankedVoting().runElection()
}