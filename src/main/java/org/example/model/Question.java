package org.example.model;

import java.sql.Timestamp;

public class Question {
    private String questionID;
    private String questionTitle;
    private String questionContent;
    private String userID;
    private String questionState;
    private Timestamp creationTime;
    private String userName; // For displaying the user's name
    private Integer bestAnswerID; // 最满意答案的评论ID

    // Getters and Setters
    public String getQuestionID() {
        return questionID;
    }

    public void setQuestionID(String questionID) {
        this.questionID = questionID;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getQuestionState() {
        return questionState;
    }

    public void setQuestionState(String questionState) {
        this.questionState = questionState;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getBestAnswerID() {
        return bestAnswerID;
    }

    public void setBestAnswerID(Integer bestAnswerID) {
        this.bestAnswerID = bestAnswerID;
    }
}