package vorlag;

import com.tinkerforge.*;
import org.eclipse.paho.client.mqttv3.*;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SliderAgent {

    public static final String PROTOCOL = "tcp";
    public static final String BROKER = "147.87.116.218";
    public static final String PORT = "1883";
    public static final URI SERVER_URI;
    public static final String CONNECTION_ID;
    public static final String POSITION_TOPIC;
    public static final String SLIDER_AGENT_LASTWILL_TOPIC;

    static {
        SERVER_URI = URI.create(PROTOCOL + "://" + BROKER + ":" + PORT);
        CONNECTION_ID = "ch.quantasy.mqtt.doorAgent.door";
        POSITION_TOPIC = "/tiny-door/Position";
        SLIDER_AGENT_LASTWILL_TOPIC = SliderService.SLIDER_SERVICE_LASTWILL_TOPIC;
    }

    private final MqttClient mqttClient;
    private MqttCallback messageHandler;

    private static final String HOST = "localhost";
    private static final int BRICK_PORT = 4223;

    private static final String mlpUID = "DLL";
    private BrickletMotorizedLinearPoti mlp;
    IPConnection ipcon = new IPConnection(); // Create IP connection


    public SliderAgent() throws MqttException {
        mqttClient = new MqttClient(SERVER_URI.toString(), CONNECTION_ID);

        this.mlp = new BrickletMotorizedLinearPoti(mlpUID, ipcon); // Create device object

        connectToBrickDeamon(HOST, BRICK_PORT);


    }

    private void connectToBrickDeamon(String host, int port){
        try {
            ipcon.connect(host, port);
        } catch (AlreadyConnectedException | NetworkException ex){
            ex.printStackTrace();
        }
    }


    public void connectToBroker() throws MqttException {
        mqttClient.setCallback(new MQTTMessageHanlder());
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(false);
        mqttClient.connect(connectOptions);
    }

    public void disconnectFromBroker() throws MqttException {
        mqttClient.disconnect();
    }

    public void startReceivingPosition() throws MqttException{
// Add position listener
        mlp.addPositionListener(new BrickletMotorizedLinearPoti.PositionListener() {
            public void position(int position) {
                System.out.println("Position: " + position); // Range: 0 to 100
                try {
                    mqttClient.subscribe(POSITION_TOPIC);
                    mlp.setMotorPosition(position, BrickletMotorizedLinearPoti.DRIVE_MODE_SMOOTH, false);


                } catch (MqttException ex){
                    Logger.getLogger(SliderService.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NotConnectedException | TimeoutException e) {
                    e.printStackTrace();
                }}});
    }


    private class MQTTMessageHanlder implements MqttCallback {

        public void connectionLost(Throwable throwable) {
            System.out.println("Connection Lost...");
        }

        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
            System.out.printf("Topic: (%s) Payload: (%s) Retained: (%b) \n", s, new String(mqttMessage.getPayload()), mqttMessage.isRetained());
        }

        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            System.out.println("Delivery Complete...");
        }
    }
}
