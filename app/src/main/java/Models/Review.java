package Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


import javax.annotation.processing.Generated;

@Generated("jsonschema2pojo")
public class Review {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("rating")
    @Expose
    private int rating;

    @SerializedName("comment")
    @Expose
    private String comment;

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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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