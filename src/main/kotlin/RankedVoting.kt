import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.*
import javax.swing.*

class VoteNode {
    var nextVote: VoteNode? = null
    var candidateNumber: Int = 0
}

class BallotNode {
    var nextBallot: BallotNode? = null
    var ballotHead = VoteNode()
}

class JThingWithCandidateThatCanBeMovedUpAndDownOnTheBallot(_name: String) : JPanel() {
    val name = JLabel(_name)
    val upButton = JButton("up")
    val downButton = JButton("down")
    val panel = JPanel()

    init {
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(name)
        panel.add(upButton)
        panel.add(downButton)
        this.add(panel)
    }
}

class RankedVoting : ActionListener {
    // Voting stuff
    private val head = BallotNode()
    private var candidateCount = 0
    private var voteTotals = mutableMapOf<Int, Int>()
    private var voterCount = 0
    private var tie = false

    var currentBallot = head

    // Swing stuff
    val frame = JFrame("Ranked Voting")
    val candidateCountEntryLabel = JLabel("Please enter the number of candidates:")
    val candidateCountEntryField = JTextField(4)
    val candidateCountSubmissionButton = JButton("Proceed to Voting")
    val candidateRankLabel = JLabel("Please rank your choices below:")
    val candidateListPanel = JPanel()
    val nextVoterButton = JButton("Submit Vote")
    val finishVotingButton = JButton("End Voting")
    val resultsTitleLabel = JLabel("Winner:")
    val resultsLabel = JLabel()

    init {
        frame.layout = FlowLayout()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        setUpWindow(1)

        frame.setSize(280, 310)
        frame.isVisible = true
    }

    /**
     * Invoked when an action occurs.
     * In this case, when a button is pressed
     * @param e the event to be processed
     */
    override fun actionPerformed(e: ActionEvent?) {
        when (e?.actionCommand) {
            "Proceed to Voting" -> {
                candidateCount = candidateCountEntryField.text.toInt()
                setUpWindow(2)
            }
            "Submit Vote" -> {
                recordVote()
                setUpWindow(2)
            }
            "End Voting" -> {
                computeVotes()
                setUpWindow(3)
            }
            "up", "down" -> {
                val components: MutableList<Component> = candidateListPanel.components.toMutableList()
                val component = (e.source as JButton).parent.parent
                val componentIndex = components.indexOf(component)
                if (e.actionCommand == "up" && componentIndex > 0) {
                    components.remove(component)
                    components.add(componentIndex - 1, component)
                } else if (e.actionCommand == "down" && componentIndex < components.size - 1) {
                    components.remove(component)
                    components.add(componentIndex + 1, component)
                }
                candidateListPanel.removeAll()
                components.forEach {
                    candidateListPanel.add(it)
                }
                frame.contentPane.revalidate()
            }
        }
    }

    fun setUpWindow(num: Int) {
        when (num) {
            1 -> {
                // Add contents of first screen
                frame.add(candidateCountEntryLabel)
                frame.add(candidateCountEntryField)
                candidateCountSubmissionButton.addActionListener(this)
                frame.add(candidateCountSubmissionButton)
            }
            2 -> {
                // Add contents of second screen
                removeWindowItems()

                candidateListPanel.removeAll()
                candidateListPanel.repaint()
                frame.add(candidateRankLabel)
                candidateListPanel.layout = BoxLayout(candidateListPanel, BoxLayout.Y_AXIS)
                for (i in 1..candidateCount) {
                    val currentCandidateSelector = JThingWithCandidateThatCanBeMovedUpAndDownOnTheBallot(i.toString())
                    currentCandidateSelector.upButton.addActionListener(this)
                    currentCandidateSelector.downButton.addActionListener(this)
                    candidateListPanel.add(currentCandidateSelector)
                }
                frame.add(candidateListPanel)
                if (finishVotingButton.actionListeners.isEmpty()) finishVotingButton.addActionListener(this)
                frame.add(finishVotingButton)
                if (nextVoterButton.actionListeners.isEmpty()) nextVoterButton.addActionListener(this)
                frame.add(nextVoterButton)
            }
            3 -> {
                removeWindowItems()

                frame.add(resultsTitleLabel)
                val maxVotes = voteTotals.values.maxOrNull()
                val winners : MutableList<String> = mutableListOf()
                for (i in voteTotals) {
                    if (i.value == maxVotes) {
                        winners.add(i.key.toString())
                        if (!tie) break
                    }
                }
                println(winners.joinToString(","))
                if (winners.size > 1) resultsTitleLabel.text = "Winners:"
                resultsLabel.text = winners.joinToString(", ")
                frame.add(resultsLabel)
            }
        }
        frame.contentPane.revalidate()
    }

    fun removeWindowItems() {
        frame.contentPane.removeAll()
        frame.repaint()
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

    fun recordVote() {
        var currentVote = currentBallot.ballotHead
        for (component in candidateListPanel.components) {
            if (component is JThingWithCandidateThatCanBeMovedUpAndDownOnTheBallot) {
                currentVote.candidateNumber = component.name.text.toInt()
                currentVote.nextVote = VoteNode()
                currentVote = currentVote.nextVote!!
            }
        }
        currentBallot.nextBallot = BallotNode()
        currentBallot = currentBallot.nextBallot!!
        voterCount++
    }

    private fun computeVotes() {

        var minVotesCandidate: Int
        var minVotes: Int

        var currentBallot = head
        while (currentBallot.nextBallot != null) {
            voteTotals[currentBallot.ballotHead.candidateNumber] =
                (voteTotals[currentBallot.ballotHead.candidateNumber] ?: 0) + 1
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
                voteTotals[currentBallot.ballotHead.candidateNumber] =
                    (voteTotals[currentBallot.ballotHead.candidateNumber] ?: 0) + 1
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
    RankedVoting()
}