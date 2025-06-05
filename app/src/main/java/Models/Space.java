package Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.processing.Generated;

@Generated("jsonschema2pojo")
public class Space {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("spaceName")
    @Expose
    private String spaceName;

    @SerializedName("capacity")
    @Expose
    private int capacity;

    @SerializedName("availability")
    @Expose
    private boolean availability;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }
}