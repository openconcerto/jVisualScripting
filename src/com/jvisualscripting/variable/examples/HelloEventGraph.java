package com.jvisualscripting.variable.examples;

import com.jvisualscripting.EventGraph;
import com.jvisualscripting.event.StartEventNode;
import com.jvisualscripting.flowcontrol.Sequence;
import com.jvisualscripting.function.Print;
import com.jvisualscripting.variable.IntegerFormatter;
import com.jvisualscripting.variable.StringLength;
import com.jvisualscripting.variable.StringVariable;

public class HelloEventGraph {

    public static void main(String[] args) {
        final StringVariable str1 = new StringVariable("str1");
        str1.setValue("Hello");
        str1.setLocation(100, 300);
        final StringVariable str2 = new StringVariable("str2");
        str2.setValue("World");
        str2.setLocation(100, 400);
        final StringLength length = new StringLength();
        length.setLocation(280, 400);

        IntegerFormatter intFormatter = new IntegerFormatter();
        intFormatter.setLocation(480, 400);
        final StringVariable str3 = new StringVariable("str3");
        str3.setValue("!");
        str3.setLocation(100, 500);

        final EventGraph g = new EventGraph();
        final Print print1 = new Print();
        print1.setLocation(300, 200);
        g.addLink(str1.getDataOuputPin(), print1.getInputs().get(1));

        final StartEventNode startEventNode = new StartEventNode();
        startEventNode.setLocation(100, 100);

        g.add(startEventNode);

        g.add(str1);
        g.add(str2);
        g.add(str3);
        g.add(print1);
        g.add(length);
        g.add(intFormatter);
        g.addLink(str2.getDataOuputPin(), length.getInputs().get(0));

        Sequence seq = new Sequence(2);
        g.addLink(print1.getFirstOutputExecutionPin(), seq.getFirstInputExecutionPin());
        seq.setLocation(500, 200);
        g.add(seq);

        final Print print2 = new Print();
        print2.setLocation(700, 200);
        g.addLink(seq.getOutputs().get(0), print2.getInputs().get(0));
        g.add(print2);

        final Print print3 = new Print();
        print3.setLocation(700, 400);
        g.addLink(seq.getOutputs().get(1), print3.getInputs().get(0));
        g.add(print3);

        g.addLink(length.getLengthOuputPin(), intFormatter.getIntegerInputPin());
        g.addLink(intFormatter.getDataOuputPin(), print2.getInputs().get(1));
        g.addLink(str3.getDataOuputPin(), print3.getInputs().get(1));

        startEventNode.setNext(print1);

        startEventNode.execute();

    }

}
