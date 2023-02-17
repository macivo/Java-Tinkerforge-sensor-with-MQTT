package Broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerforge.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Door Service publishes the distance to MQTT Broker
 *
 */
public class Door_Service {

    public static final String PROTOCOL = "tcp";
    public static final String BROKER = "147.87.116.218";
    public static final String PORT = "1883";
    public static final URI SERVER_URI;
    public static final String CONNECTION_ID;
    public static final String POSITION_TOPIC;
    public static final String POSITION_TOPIC2;
    public static final String SLIDER_SERVICE_LASTWILL_TOPIC;

    static {
        SERVER_URI = URI.create(PROTOCOL + "://" + BROKER + ":" + PORT);
        CONNECTION_ID = "ch.quantasy.mqtt.Door_Service_dominique";
        POSITION_TOPIC = "/tiny-door/sensor/hHU";
        POSITION_TOPIC2 = "/tiny-door/sensor/hHC";
        SLIDER_SERVICE_LASTWILL_TOPIC = CONNECTION_ID + "/testament";
    }

    private MqttClient mqttClient;
    private static final String HOST = "localhost";
    private static final int BRICK_PORT = 4223;
    private static final String bDIUID1 = "hHU";
    private static final String bDIUID2 = "hHC";
    private BrickletDistanceIR bDI1;
    private BrickletDistanceIR bDI2;
    private IPConnection ipcon1 = new IPConnection(); // Create IP connection
    private IPConnection ipcon2 = new IPConnection(); // Create IP connection

    public Door_Service() throws MqttException{
        mqttClient = new MqttClient(SERVER_URI.toString(), CONNECTION_ID, new MemoryPersistence());
        this.bDI1 = new BrickletDistanceIR(bDIUID1,ipcon1); // Create device object
        this.bDI2 = new BrickletDistanceIR(bDIUID2,ipcon1); // Create device object
        connectToBrickDeamon(HOST, BRICK_PORT);
    }

    private void connectToBrickDeamon(String host, int port){
        try {
            ipcon1.connect(host, port);
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

    public void startPublishPosition() throws MqttException {
        bDI1.addDistanceListener(new BrickletDistanceIR.DistanceListener() {
            @Override
            public void distance(int i) {
                getData(i, POSITION_TOPIC, 1);
            }
        });
        bDI2.addDistanceListener(new BrickletDistanceIR.DistanceListener() {
            @Override
            public void distance(int i) {
                getData(i, POSITION_TOPIC2, 2);
            }
        });
        try {
            bDI1.setDistanceCallbackPeriod(100);
        } catch (TimeoutException | NotConnectedException e) {
            e.printStackTrace();
        }
        try {
            bDI2.setDistanceCallbackPeriod(100);
        } catch (TimeoutException | NotConnectedException e){
            e.printStackTrace();
        }
    }

    private void getData(int value, String topic, int num) {
        Date date = new Date();
        long time = date.getTime();
        Timestamp timestamp = new Timestamp(time);
        ObjectMapper mapper = new ObjectMapper();
        SensorData sensorData = new SensorData(timestamp, value, num);
        try {
            byte[] sensorDataAsBytes = mapper.writeValueAsBytes(sensorData);
            MqttMessage message = new MqttMessage(sensorDataAsBytes);
            message.setRetained(true);
            mqttClient.publish(topic, message);
        } catch (JsonProcessingException | MqttException e) {
            e.printStackTrace();
        }
    }


}