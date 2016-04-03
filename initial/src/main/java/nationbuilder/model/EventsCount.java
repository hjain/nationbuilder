package nationbuilder.model;

public class EventsCount {

    public String date;

    public Integer enters;

    public Integer leaves;

    public Integer highfives;

    public Integer comments;

    public EventsCount(){}

    public EventsCount(String date, int enters, int comments, int highfives, int leaves) {
        this.date = date;
        this.enters = enters;
        this.highfives = highfives;
        this.comments = comments;
        this.leaves = leaves;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getEnters() {
        return enters;
    }

    public void setEnters(Integer enters) {
        this.enters = enters;
    }

    public Integer getLeaves() {
        return leaves;
    }

    public void setLeaves(Integer leaves) {
        this.leaves = leaves;
    }

    public Integer getHighfives() {
        return highfives;
    }

    public void setHighfives(Integer highfives) {
        this.highfives = highfives;
    }

    public Integer getComments() {
        return comments;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }
}
