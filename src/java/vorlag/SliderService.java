package vorlag;

import com.tinkerforge.*;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Slider Service publishes the current value of Motorized Linear Poti Bricklet
 *
 */
public class SliderService {

    public static final String PROTOCOL = "tcp";
    public static final String BROKER = "147.87.116.218";
    public static final String PORT = "1883";
    public static final URI SERVER_URI;
    public static final String CONNECTION_ID;
    public static final String POSITION_TOPIC;
    public static final String SLIDER_SERVICE_LASTWILL_TOPIC;

    static {
        SERVER_URI = URI.create(PROTOCOL + "://" + BROKER + ":" + PORT);
        CONNECTION_ID = "ch.quantasy.mqtt.sliderservice.sandro";
        POSITION_TOPIC = "/ExampleDimmer/Position";
        SLIDER_SERVICE_LASTWILL_TOPIC = CONNECTION_ID + "/testament";

    }

    private MqttClient mqttClient;

    private static final String HOST = "localhost";
    private static final int BRICK_PORT = 4223;


    private static final String mlpUID = "DCn";
    private BrickletMotorizedLinearPoti mlp;
    private IPConnection ipcon = new IPConnection(); // Create IP connection


    public SliderService() throws MqttException{
        mqttClient = new MqttClient(SERVER_URI.toString(), CONNECTION_ID);

        this.mlp = new BrickletMotorizedLinearPoti(mlpUID, ipcon); // Create device object

        connectToBrickDeamon(HOST, BRICK_PORT);


    }

    private void connectToBrickDeamon(String host, int port){
        try {
            ipcon.connect(host, port);
            System.out.println("connected to Brickdeamon");
        } catch (AlreadyConnectedException | NetworkException ex){
            ex.printStackTrace();
        }
    }


    public void connectToBroker() throws MqttException {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setWill(SLIDER_SERVICE_LASTWILL_TOPIC, "offline".getBytes(), 1, true);
        mqttClient.connect(connectOptions);
        mqttClient.publish(SLIDER_SERVICE_LASTWILL_TOPIC, "online".getBytes(), 1, true);
    }

    public void disconnectFromBroker() throws MqttException {
        mqttClient.disconnect();
    }

    public void startPublishPosition(){
        // Add position listener
        mlp.addPositionListener(new BrickletMotorizedLinearPoti.PositionListener() {
            public void position(int position) {
                System.out.println("Position: " + position); // Range: 0 to 100
                // send topic with position
                // new SliderServiceManager(POSITION_TOPIC );
                try {
                    String positionAsBytes = Integer.toString(position);
                    MqttMessage message = new MqttMessage(positionAsBytes.getBytes());
                    message.setRetained(true);
                    mqttClient.publish(POSITION_TOPIC, message);
                } catch (MqttException ex){
                    Logger.getLogger(SliderService.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

        // Set period for position callback to 0.05s (50ms) without a threshold
        try {
            mlp.setPositionCallbackConfiguration(50, false, 'x', 0, 0);
        } catch (TimeoutException | NotConnectedException e){
            e.printStackTrace();
        }

    }


}