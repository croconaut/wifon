package com.croconaut.ratemebuddy.utils.pojo.profiles.states;


import com.croconaut.ratemebuddy.data.pojo.Comment;
import com.croconaut.ratemebuddy.data.pojo.VoteUp;

import java.io.Serializable;
import java.util.ArrayList;

public class ActualState implements Serializable {

    private static final long serialVersionUID = 10L;

    private boolean actual = true;
    private VoteUp notActualVote;
    private ArrayList<Comment> notActualComments;

    private ActualState(Builder builder){
        this.actual = builder.actual;
        this.notActualVote = builder.notActualVote;
        this.notActualComments = builder.notActualComments;
    }

    @Override
    public String toString() {
        return "ActualState{" +
                "actual=" + actual +
                ", notActualVote=" + notActualVote +
                ", notActualComments=" + notActualComments +
                '}';
    }

    public boolean isActual() {
        return actual;
    }

    public void clearAndSetActual(){
        this.notActualVote = null;
        this.notActualComments.clear();
        this.actual = (notActualVote == null) && notActualComments.isEmpty();
    }

    private boolean containsComment(Comment comment){
        return notActualComments.contains(comment);
    }

    public void removeCommentIfContains(Comment comment){
        if(containsComment(comment)){
            this.notActualComments.remove(comment);
        }
        this.actual = (notActualVote == null) && notActualComments.isEmpty();
    }

    public void removeVoteIfEquals(VoteUp voteUp){
        if(this.notActualVote != null &&
                this.notActualVote.equals(voteUp)){
            this.notActualVote = null;
        }
        this.actual = this.notActualVote == null && notActualComments.isEmpty();
    }

    public ArrayList<Comment> getNotActualComments() {
        return notActualComments;
    }

    public VoteUp getNotActualVote() {
        return notActualVote;
    }

    public static class Builder{
        private boolean actual;
        private VoteUp notActualVote = null;
        private ArrayList<Comment> notActualComments = new ArrayList<>();

        public Builder(){
            this.actual = true;
        }

        public Builder(ActualState actualState){
            this.actual = actualState.actual;
            this.notActualVote = actualState.notActualVote;
            this.notActualComments = actualState.notActualComments;
        }

        public Builder setVote(VoteUp voteUp){
            this.notActualVote = voteUp;
            return this;
        }

        public Builder clearVote(){
            this.notActualVote = null;
            return this;
        }

        public Builder addComment(Comment comment){
            this.notActualComments.add(comment);
            return this;
        }

        public Builder clearComments(){
            this.notActualComments.clear();
            return this;
        }


        public ActualState build(){
            this.actual = (notActualVote == null) && notActualComments.isEmpty();
            return new ActualState(this);
        }
    }
}
