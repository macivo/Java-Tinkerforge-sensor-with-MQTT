package Broker;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.NetworkException;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

/**
 * This example demonstrates how a subscriber can listen to multiple topics on a single connection.<br/>
 * If a subscriber wants to listen to multiple topics, it can register itself explicitly.
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class Main {

    public static void main(String[] args) throws MqttException, IOException, TimeoutException, NetworkException, NotConnectedException, AlreadyConnectedException {

        Door_Service service = new Door_Service();
        service.connectToBroker();
        service.startPublishPosition();
        DoorAgent agent = new DoorAgent();
        System.out.println("created DoorAgent");
        agent.connectToBroker();
        System.out.println("DoorAgent connected to Broker");
        agent.startReceivingPosition();
        System.out.println("start receiving Position...");
        System.out.println("...press a key to disrupt the network (simulated by System.exit(0)).");
        System.in.read();
        service.disconnectFromBroker();
        agent.disconnectFromBroker();

        System.out.println("Done.");
        System.exit(0);
    }
}

