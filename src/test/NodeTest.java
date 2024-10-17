package test;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jvisualscripting.EventGraph;
import com.jvisualscripting.Pin;
import com.jvisualscripting.function.Print;
import com.jvisualscripting.variable.StringVariable;

class NodeTest {
    EventGraph graph;
    StringVariable str = new StringVariable("Hello");
    Print prt = new Print();

    @BeforeEach
    void setUp() throws Exception {
        this.graph = new EventGraph();
        this.graph.add(this.str);
        this.graph.add(this.prt);
        this.graph.addLink(this.str.getDataOuputPin(), this.prt.getStringInputPin());
    }

    @Test
    void testRemove() {
        this.graph.remove(this.str);
        Assert.assertTrue(this.graph.getLinks().isEmpty());
        for (Pin p : this.str.getOutputs()) {
            Assert.assertFalse(p.isConnected());
        }
        for (Pin p : this.prt.getOutputs()) {
            Assert.assertFalse(p.isConnected());
        }
    }

    @Test
    void testRemoveMultiple() {
        Print prt2 = new Print();
        this.graph.addLink(this.str.getDataOuputPin(), prt2.getStringInputPin());
        this.graph.dump(System.out);
        this.graph.remove(this.str);
        Assert.assertTrue(this.graph.getLinks().isEmpty());
        for (Pin p : this.str.getOutputs()) {
            Assert.assertFalse(p.isConnected());
        }
        for (Pin p : this.prt.getOutputs()) {
            Assert.assertFalse(p.isConnected());
        }
        for (Pin p : prt2.getOutputs()) {
            Assert.assertFalse(p.isConnected());
        }
    }
}
