package Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

import javax.annotation.processing.Generated;

@Generated("jsonschema2pojo")
public class Reservation {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("startTime")
    @Expose
    private Date startTime;

    @SerializedName("endTime")
    @Expose
    private Date endTime;

    @SerializedName("resident")
    @Expose
    private User user;

    @SerializedName("space")
    @Expose
    private Space space;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }
}