package vorlag;

public class main{

    public static void main(String args[]) throws Exception {

        /*
        SliderService slider = new SliderService();
        System.out.println("created SliderService");
        slider.connectToBroker();
        System.out.println("SliderService connected to Broker");
        slider.startPublishPosition();
        System.out.println("started publishing...");

        */

        vorlag.SliderAgent agent = new SliderAgent();
        System.out.println("created DoorAgent");
        agent.connectToBroker();
        System.out.println("DoorAgent connected to Broker");
        agent.startReceivingPosition();
        System.out.println("start receiving Position...");

        System.out.println("...press a key to disrupt the network (simulated by System.exit(0)).");
        System.in.read();
        // critical: If you disconnect gracefully, the testament will not be opened!
        // slider.disconnectFromBroker();
        agent.disconnectFromBroker();
        // agent.disconnectFromBroker();
        System.out.println("Done.");
        System.exit(0);


    }
}

