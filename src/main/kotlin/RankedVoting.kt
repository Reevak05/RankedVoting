import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

// The votes are stored in a specialized linked list consisting of a list of BallotNodes, each of which points to a linked list of VoteNodes making up a ballot

// Each VoteNode stores one segment of a voter's vote
class VoteNode {
    var nextVote: VoteNode? = null
    var candidateNumber: Int = 0
}

// Each BallotNode stores the first VoteNode of its ballot and the next BallotNode
class BallotNode {
    var nextBallot: BallotNode? = null
    var ballotHead = VoteNode()
}

// A component which is used to represent the candidates when voters rank their selections
class JBallotCandidate(_name: String) : JPanel() {
    val name = JLabel(_name)
    val upButton = JButton("up")
    val downButton = JButton("down")
    private val panel = JPanel()

    init {
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(name)
        panel.add(upButton)
        panel.add(downButton)
        this.add(panel)
    }
}

class RankedVoting : ActionListener {
    // Voting-related variables
    private val head = BallotNode()
    private var candidateCount = 0
    private var voteTotals = mutableMapOf<Int, Int>()
    private var voterCount = 0
    private var tie = false
    private var currentBallot = head

    // UI-related variables
    private val frame = JFrame("Ranked Voting")
    private val candidateCountEntryLabel = JLabel("Please enter the number of candidates:")
    private val candidateCountEntryField = JTextField(4)
    private val candidateCountSubmissionButton = JButton("Proceed to Voting")
    private val candidateRankLabel = JLabel("Please rank your choices below:")
    private val candidateListPanel = JPanel()
    private val nextVoterButton = JButton("Submit Vote")
    private val finishVotingButton = JButton("End Voting")
    private val resultsTitleLabel = JLabel("Winner:")
    private val resultsLabel = JLabel()

    // When the program is started, configure the window and show the first screen
    init {
        frame.layout = FlowLayout()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        setUpWindow(1)

        frame.setSize(280, 310)
        frame.isVisible = true
    }

    /**
     * Invoked when an action occurs
     * In this case, when a button is pressed
     * @param e the event to be processed
     */
    override fun actionPerformed(e: ActionEvent?) {
        when (e?.actionCommand) {
            "Proceed to Voting" -> { // When the user wishes to proceed to the voting stage, set the number of candidates and set up screen 2 (for voting)
                candidateCount = candidateCountEntryField.text.toInt()
                setUpWindow(2)
            }
            "Submit Vote" -> { // When a user votes, record their vote and set up the screen for the next voter
                recordVote()
                setUpWindow(2)
            }
            "End Voting" -> { // When voting is complete, compute the election result and display screen 3 (to display the winner)
                computeVotes()
                setUpWindow(3)
            }
            "up", "down" -> { // When the up or down buttons are pressed on the JBallotCandidates, move the JBallot Candidate up or down the list accordingly
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

    /**
     * Sets up the appropriate components for each step
     * Screen 1: The user enters the number of candidates
     * Screen 2: The voters rank their choices
     * Screen 3: The program shows the winner
     * @param num which screen to set up
     */
    private fun setUpWindow(num: Int) {
        when (num) {
            1 -> {
                // Add contents of first screen
                frame.add(candidateCountEntryLabel)
                frame.add(candidateCountEntryField)
                candidateCountSubmissionButton.addActionListener(this)
                frame.add(candidateCountSubmissionButton)
            }
            2 -> {
                // Remove contents of previous screen and add contents of second screen
                removeWindowItems()

                candidateListPanel.removeAll()
                candidateListPanel.repaint()
                frame.add(candidateRankLabel)
                candidateListPanel.layout = BoxLayout(candidateListPanel, BoxLayout.Y_AXIS)
                for (i in 1..candidateCount) { // Add a JBallot Candidate to the screen for each candidate
                    val currentCandidateSelector = JBallotCandidate(i.toString())
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
                // Remove contents of previous screen and add contents of third screen
                removeWindowItems()

                frame.add(resultsTitleLabel)
                val maxVotes = voteTotals.values.maxOrNull()
                val winners : MutableList<String> = mutableListOf()
                for (i in voteTotals) { // Get which candidates have the most votes
                    if (i.value == maxVotes) {
                        winners.add(i.key.toString())
                        if (!tie) break
                    }
                }
                println(winners.joinToString(","))
                if (winners.size > 1) resultsTitleLabel.text = "Winners:" // If there is a tie, adjust the heading accordingly
                resultsLabel.text = winners.joinToString(", ")
                frame.add(resultsLabel)
            }
        }
        frame.contentPane.revalidate()
    }

    /**
     * Removes all components from the window
     * Useful in between different screens or steps
     */
    private fun removeWindowItems() {
        frame.contentPane.removeAll()
        frame.repaint()
    }

    /**
     * Records the voter's choices to the vote storage data structure
     */
    private fun recordVote() {
        var currentVote = currentBallot.ballotHead
        for (component in candidateListPanel.components) {
            if (component is JBallotCandidate) {
                currentVote.candidateNumber = component.name.text.toInt()
                currentVote.nextVote = VoteNode()
                currentVote = currentVote.nextVote!!
            }
        }
        currentBallot.nextBallot = BallotNode()
        currentBallot = currentBallot.nextBallot!!
        voterCount++
    }

    /**
     * Calculates the winning candidate using ranked-choice (instant-runoff) voting procedure
     */
    private fun computeVotes() {

        var minVotesCandidate: Int
        var minVotes: Int

        // Populate voteTotals, which contains each candidate's number of primary votes
        var currentBallot = head
        while (currentBallot.nextBallot != null) {
            voteTotals[currentBallot.ballotHead.candidateNumber] =
                (voteTotals[currentBallot.ballotHead.candidateNumber] ?: 0) + 1
            currentBallot = currentBallot.nextBallot!!
        }

        // The following process continues until a candidate reaches a majority
        while (!voteTotals.values.any { it > voterCount / 2 }) {

            minVotes = voteTotals.values.minOrNull() ?: 0

            // If the only remaining candidates have the same number of primary votes, there is a tie
            tie = true
            for (i in voteTotals) {
                if (i.value != minVotes) {
                    tie = false
                    break
                }
            }
            if (tie) break

            // Reassign all ballot with a candidate who is to be eliminated to the voter's next choice
            for (i in voteTotals) {
                if (i.value == minVotes) {
                    minVotesCandidate = i.key
                    currentBallot = head
                    while (currentBallot.nextBallot != null) {
                        if (currentBallot.ballotHead.candidateNumber == minVotesCandidate) {
                            if (currentBallot.ballotHead.nextVote != null) {
                                do {
                                    currentBallot.ballotHead = currentBallot.ballotHead.nextVote!!
                                } while (currentBallot.ballotHead.nextVote != null
                                    && (voteTotals[currentBallot.ballotHead.candidateNumber] == null || voteTotals[currentBallot.ballotHead.candidateNumber]!! <= minVotes))
                            }
                        }
                        currentBallot = currentBallot.nextBallot!!
                    }
                }
            }

            voteTotals = mutableMapOf()

            // Repopulate the values of voteTotals following candidate elimination and vote reallocation
            currentBallot = head
            while (currentBallot.nextBallot != null) {
                voteTotals[currentBallot.ballotHead.candidateNumber] =
                    (voteTotals[currentBallot.ballotHead.candidateNumber] ?: 0) + 1
                currentBallot = currentBallot.nextBallot!!
            }
        }
    }

}

fun main() {
    RankedVoting()
}