package com.example.springai;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponse<T> {
    /**
     * 成功响应的错误码
     */
    public static final int SUCCESS_CODE = 0;

    /**
     * 失败响应的错误码
     */
    public static final int ERROR_CODE = -1;

    /**
     * 成功响应的HTTP状态码
     */
    public static final int SUCCESS_STATUS = 200;

    /**
     * 失败响应的HTTP状态码
     */
    public static final int ERROR_STATUS = 500;

    /**
     * 错误码，0表示成功，非0表示失败
     */
    private Integer errcode;

    /**
     * 响应时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date timestamp;

    /**
     * HTTP状态码
     */
    private int status;

    /**
     * 错误类型描述
     */
    private String error;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 响应数据
     */
    private T data;
}
