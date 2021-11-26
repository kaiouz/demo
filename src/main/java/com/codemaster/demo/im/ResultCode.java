package com.codemaster.demo.im;

public class ResultCode<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ResultCode<T> ok(T data) {
        return new ResultCode(0, null, data);
    }

    public static ResultCode<Void> ok() {
        return new ResultCode<>(0, null, null);
    }

    public ResultCode(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
