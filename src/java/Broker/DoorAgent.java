package Broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerforge.*;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import vorlag.SliderService;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Door agent subscriebs MQTT Topics and calculates the distance
 *
 */
public class DoorAgent {

    public static final String PROTOCOL = "tcp";
    public static final String BROKER = "147.87.116.218";
    public static final String PORT = "1883";
    public static final URI SERVER_URI;
    public static final String CONNECTION_ID;
    public static final String POSITION_TOPIC;
    public static final String POSITION_TOPIC2;
    public static final String SLIDER_AGENT_LASTWILL_TOPIC;

    public static final Integer DISTANCE_BETWEEN_SENSORS = 10;
    public static final Integer DISTANCE_BETWEEN_SENSORS_AND_DOOR = 20;
    public static final Integer DOOR_OPENING_DISTANCE = 10;

    static {
        SERVER_URI = URI.create(PROTOCOL + "://" + BROKER + ":" + PORT);
        CONNECTION_ID = "ch.quantasy.mqtt.doorAgent.door";
        POSITION_TOPIC = "/tiny-door/sensor/hHU";
        POSITION_TOPIC2 = "/tiny-door/sensor/hHC";
        SLIDER_AGENT_LASTWILL_TOPIC = SliderService.SLIDER_SERVICE_LASTWILL_TOPIC;
    }

    private final MqttClient mqttClient;
    private MqttCallback messageHandler;
    private static final String HOST = "localhost";
    private static final int BRICK_PORT = 4223;
    private ArrayList<SensorData> payloads = new ArrayList<>();
    private IPConnection ipcon = new IPConnection(); // Create IP connection


    public DoorAgent() throws MqttException {
        mqttClient = new MqttClient(SERVER_URI.toString(), CONNECTION_ID);
        connectToBrickDeamon(HOST, BRICK_PORT);
    }

    private void connectToBrickDeamon(String host, int port) {
        try {
            ipcon.connect(host, port);
        } catch (AlreadyConnectedException | NetworkException ex) {
            ex.printStackTrace();
        }
    }

    public void connectToBroker() throws MqttException {
        mqttClient.setCallback(new MQTTMessageHandler());
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(false);
        mqttClient.connect(connectOptions);
    }

    public void disconnectFromBroker() throws MqttException {
        mqttClient.disconnect();
    }

    public void startReceivingPosition() throws MqttException {
        mqttClient.subscribe(POSITION_TOPIC);
        mqttClient.subscribe(POSITION_TOPIC2);
    }

    public boolean calculatePosition(ArrayList<SensorData> payloads){
            double distance1 = payloads.get(0).getDistance();
            Timestamp time1 = payloads.get(0).getTimestamp();
            Timestamp time2 = payloads.get(1).getTimestamp();
            double distance2 = payloads.get(1).getDistance();
            double alpha = calculateAlpha(distance1, distance2, DISTANCE_BETWEEN_SENSORS);
            boolean hitormiss = hitOrMiss(distance1, distance2, alpha, DISTANCE_BETWEEN_SENSORS_AND_DOOR, DOOR_OPENING_DISTANCE);
            return hitormiss;
    }

    public boolean hitOrMiss(double d1, double d2, double alpha, int y, int z){
        double z1 = d2 + (y * Math.sin(90 - alpha));
        double z2 = y * Math.sin(alpha - 90);
        if (d2 > d1 && z < z1){
            return false;
        }
        else if(d1 > d2 && d2 < z2){
            return false;
        }
        else if (d1 == d2 && d2 > z){
            return false;
        }
        else {
            return true;
        }
    }

    public double calculateAlpha(double d1, double d2, double X){
        double alpha = 90;
        if (d2 > d1){
            return (90 - Math.toDegrees(Math.asin((d2-d1)/X)));
        }
        else if (d1 > d2){
            return (90 + Math.toDegrees(Math.asin((d1-d2)/X)));
        }
        return alpha;
    }


    private class MQTTMessageHandler implements MqttCallback {
        public void connectionLost(Throwable throwable) {
            System.out.println("Connection Lost...");
        }
        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            SensorData data = mapper.readValue(mqttMessage.getPayload(), SensorData.class);
            //System.out.println(data.getId());
            if (data.getId() == 1 && payloads.size() < 1){
                payloads.add(data);
            } else if (data.getId() == 2 && payloads.size() < 2) {
                payloads.add(data);
                if (payloads.size() == 2){
                    System.out.println(calculatePosition(payloads));
                }
                else{
                    payloads.clear();
                }
            }
            else {
                payloads.clear();
            }
        }
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            System.out.println("Delivery Complete...");
        }
    }
}
