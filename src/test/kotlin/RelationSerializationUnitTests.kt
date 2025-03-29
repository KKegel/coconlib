import org.junit.jupiter.api.Test
import system.Relation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach

class RelationSerializationUnitTests {

    @Test
    fun testParse() {
        val serialized = "L;g1;g2;a;b;payload"
        val relation = Relation.parse(serialized)
        assertEquals("g1", relation.fromGraph)
        assertEquals("g2", relation.toGraph)
        assertEquals("a", relation.fromRevision)
        assertEquals("b", relation.toRevision)
        assertEquals("payload", relation.payload)
    }

}