import graphcore.EdgeDescription
import graphcore.EdgeLabel
import graphcore.GraphDescription
import graphcore.VertexDescription
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class TinkerRevisionGraphValidationUnitTests {

    private lateinit var validGraph: TinkerRevisionGraph


    @BeforeEach
            /**
             * a --> b --> e --> f
             *       \---> c --/
             */
    fun setUp() {
        validGraph = TinkerRevisionGraph.build(
            GraphDescription("g1", listOf(
                VertexDescription("g1", "a", "A", "./a"),
                VertexDescription("g1", "b", "B", "./b"),
                VertexDescription("g1", "c", "C", "./c"),
                VertexDescription("g1", "d", "D", "./d"),
                VertexDescription("g1", "e", "E", "./e"),
                VertexDescription("g1", "f", "F", "./f"),
                VertexDescription("g1", "g", "G", "./g"),

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
                VertexDescription("g1", "a", "A", "./a"),
                VertexDescription("g1", "b", "B", "./b"),
                VertexDescription("g1", "c", "C", "./c"),
                VertexDescription("g1", "d", "D", "./d"),
                VertexDescription("g1", "e", "E", "./e"),
                VertexDescription("g1", "f", "F", "./f"),
                VertexDescription("g1", "g", "G", "./g"),

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
                VertexDescription("g1", "a", "A", "./a"),
                VertexDescription("g1", "b", "B", "./b"),
                VertexDescription("g1", "c", "C", "./c"),
                VertexDescription("g1", "d", "D", "./d"),
                VertexDescription("g1", "e", "E", "./e"),
                VertexDescription("g1", "f", "F", "./f"),
                VertexDescription("g1", "g", "G", "./g"),

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

}
