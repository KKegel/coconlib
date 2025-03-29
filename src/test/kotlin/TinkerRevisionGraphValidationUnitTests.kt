import graphcore.EdgeDescription
import graphcore.EdgeLabel
import graphcore.GraphDescription
import graphcore.RevisionDescription
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class TinkerRevisionGraphValidationUnitTests {

    private lateinit var validGraph: TinkerRevisionGraph


    @BeforeEach
            /**
             * a --> b --> e --> f --> g
             *       \---> c --/
             */
    fun setUp() {
        validGraph = TinkerRevisionGraph.build(
            GraphDescription("g1", listOf(
                RevisionDescription("g1", "a", "A", "./a"),
                RevisionDescription("g1", "b", "B", "./b"),
                RevisionDescription("g1", "c", "C", "./c"),
                RevisionDescription("g1", "e", "E", "./e"),
                RevisionDescription("g1", "f", "F", "./f"),
                RevisionDescription("g1", "g", "G", "./g"),

            ), listOf(
                EdgeDescription("a", "b", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "c", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "e", EdgeLabel.SUCCESSOR),
                EdgeDescription("c", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("e", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("f", "g", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "f", EdgeLabel.MERGE),
            )))
    }

    @Test
    fun testCyclicGraph() {
        val revisionGraphCyclic5 = TinkerRevisionGraph.build(
            GraphDescription("g1", listOf(
                RevisionDescription("g1", "a", "A", "./a"),
                RevisionDescription("g1", "b", "B", "./b"),
                RevisionDescription("g1", "c", "C", "./c"),
                RevisionDescription("g1", "d", "D", "./d"),
                RevisionDescription("g1", "e", "E", "./e"),
                RevisionDescription("g1", "f", "F", "./f"),
                RevisionDescription("g1", "g", "G", "./g"),

                ), listOf(
                EdgeDescription("a", "b", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "c", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "e", EdgeLabel.SUCCESSOR),
                EdgeDescription("c", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("e", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("f", "g", EdgeLabel.SUCCESSOR),
                EdgeDescription("g", "a", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "f", EdgeLabel.MERGE),
            )))
        assertTrue(revisionGraphCyclic5.hasCycles(5))
    }

    @Test
    fun testCyclicGraph2() {
        val revisionGraphCyclic5 = TinkerRevisionGraph.build(
            GraphDescription("g1", listOf(
                RevisionDescription("g1", "a", "A", "./a"),
                RevisionDescription("g1", "b", "B", "./b"),
                RevisionDescription("g1", "c", "C", "./c"),
                RevisionDescription("g1", "d", "D", "./d"),
                RevisionDescription("g1", "e", "E", "./e"),
                RevisionDescription("g1", "f", "F", "./f"),
                RevisionDescription("g1", "g", "G", "./g"),

                ), listOf(
                EdgeDescription("a", "b", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "c", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "e", EdgeLabel.SUCCESSOR),
                EdgeDescription("c", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("e", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("f", "g", EdgeLabel.SUCCESSOR),
                EdgeDescription("g", "a", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "f", EdgeLabel.MERGE),
            )))
        assertFalse(revisionGraphCyclic5.hasCycles(3))
    }

    @Test
    fun testCyclesValidGraph() {
        assertFalse(validGraph.hasCycles(10))
    }

    @Test
    fun testIsConnectedValidGraph() {
        val miniValidGraph = TinkerRevisionGraph.build(
            GraphDescription("g2", listOf(
                RevisionDescription("g2", "a", "A", "./a"),
                RevisionDescription("g2", "b", "B", "./b"),
                RevisionDescription("g2", "c", "C", "./c"),
                RevisionDescription("g2", "e", "E", "./e")
                ), listOf(
                EdgeDescription("a", "b", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "c", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "e", EdgeLabel.SUCCESSOR)
            )))
        assertTrue(miniValidGraph.hasOnlyOneRoot())
    }

    @Test
    fun testIsConnectedInvalidGraph() {
        validGraph.removeEdge(EdgeDescription("b", "c", EdgeLabel.SUCCESSOR))
        assertFalse(validGraph.hasOnlyOneRoot())
    }

    @Test
    fun testHasOnlyOneRoot() {
        assertTrue(validGraph.hasOnlyOneRoot())
    }

    @Test
    fun testHasOnlyOneRootInvalid() {
        val invalidGraph = TinkerRevisionGraph.build(
            GraphDescription("g1", listOf(
                RevisionDescription("g1", "a", "A", "./a"),
                RevisionDescription("g1", "b", "B", "./b"),
                RevisionDescription("g1", "c", "C", "./c"),
                RevisionDescription("g1", "e", "E", "./e"),
                RevisionDescription("g1", "f", "F", "./f")

                ), listOf(
                EdgeDescription("a", "b", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "c", EdgeLabel.SUCCESSOR),
                EdgeDescription("c", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("e", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "f", EdgeLabel.MERGE),
            )))

        assertFalse(invalidGraph.hasOnlyOneRoot())
    }

    @Test
    fun testMaxTwoIncomingSuccessors() {
        assertTrue(validGraph.maxTwoIncomingSuccessors())
    }

    @Test
    fun testMaxTwoIncomingSuccessorsInvalid() {
        validGraph.addEdge(EdgeDescription("a", "c", EdgeLabel.SUCCESSOR))
        validGraph.addEdge(EdgeDescription("f", "c", EdgeLabel.SUCCESSOR))
        assertFalse(validGraph.maxTwoIncomingSuccessors())
    }

    @Test
    fun testMaxOneIncomingMerge() {
        assertTrue(validGraph.maxOneIncomingMerge())
    }

    @Test
    fun testMaxOneIncomingMergeInvalid() {
        validGraph.addEdge(EdgeDescription("a", "f", EdgeLabel.MERGE))
        assertFalse(validGraph.maxOneIncomingMerge())
    }

    @Test
    fun testTwoIncomingSuccessorsRequiresMerge() {
        assertTrue(validGraph.twoIncomingSuccessorsRequiresMerge())
    }

    @Test
    fun testTwoIncomingSuccessorsRequiresMergeInvalid() {
        validGraph.removeEdge(EdgeDescription("b", "f", EdgeLabel.MERGE))
        assertFalse(validGraph.twoIncomingSuccessorsRequiresMerge())
    }


}
