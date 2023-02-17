package Broker;

import java.sql.Timestamp;

/**
 * Object: Container of data from distance sensor.
 *
 */
public class SensorData {

    private Timestamp timestamp;
    private Integer distance;
    private Integer id;

    public SensorData(Timestamp timestamp, Integer distance, Integer id){
        this.timestamp = timestamp;
        this.distance = distance;
        this.id = id;
    }
    // Default constructor
    public SensorData(){}

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public Integer getDistance() {
        return distance;
    }

    public Integer getId() {
        return id;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
