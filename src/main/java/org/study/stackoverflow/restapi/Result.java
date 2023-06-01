package org.study.stackoverflow.restapi;


import lombok.Data;

@Data
public class Result<T> {

    private Integer code;

    private T data;

    public Result(Integer code,T data){
        this.code = code;
        this.data = data;
    }

    public  static Result ok(Object data){
        return new  Result(200, data);
    }

    public  static Result ok(){
        return new  Result(200, null);
    }

}
