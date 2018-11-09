package com.example.babak.baresh;

public class TaskModel {
    private Long mTaskId;
    private Long mDownloadId;
    private Long mStart;
    private Long mEnd;
    public TaskModel(){

    }
    public Long getDownloadId() {
        return mDownloadId;
    }

    public void setDownloadId(Long mDownloadId) {
        this.mDownloadId = mDownloadId;
    }

    public Long getStart() {
        return mStart;
    }

    public void setStart(Long mStart) {
        this.mStart = mStart;
    }

    public Long getEnd() {
        return mEnd;
    }

    public void setEnd(Long mEnd) {
        this.mEnd = mEnd;
    }

    public Long getTaskId() {
        return mTaskId;
    }

    public void setTaskId(Long mTaskId) {
        this.mTaskId = mTaskId;
    }
}
