import java.util.*

class RankedVoting {
    private val head = BallotNode()
    private var candidateCount = 0
    private var voteTotals = mutableMapOf<Int, Int>()
    private var voterCount = 0

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
            println("input: $input")
            if (input.lowercase() == "stop") break
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

            println("vote totals:")
            voteTotals.forEach{println("${it.key} ${it.value}")}

            minVotes = voteTotals.values.minOrNull() ?: 0
            for (i in voteTotals) {
                if (i.value == minVotes) {
                    minVotesCandidate = i.key
                    currentBallot = head
                    while (currentBallot.nextBallot != null) { // Reassign votes of eliminated candidate
                        if (currentBallot.ballotHead.candidateNumber == minVotesCandidate) {
                            if (currentBallot.ballotHead.nextVote != null) {
                                currentBallot.ballotHead = currentBallot.ballotHead.nextVote!!
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
        println("Election results:")
        for (i in voteTotals.toList().sortedBy { it.second }) {
            println("candidate: ${i.first}, votes: ${i.second}")
        }
    }

    fun printVotes() {
        var currentBallot = head
        while (currentBallot.nextBallot != null) {
            println("ballot: ")
            var currentVote = currentBallot.ballotHead
            while (currentVote.nextVote != null) {
                println("${currentVote.candidateNumber} ")
                currentVote = currentVote.nextVote!!
            }
            currentBallot = currentBallot.nextBallot!!
        }

        println("vote totals:")
        voteTotals.forEach{println("${it.key} ${it.value}")}
    }
}

fun main() {
    val election = RankedVoting()
    election.runElection()
    election.printVotes()
}