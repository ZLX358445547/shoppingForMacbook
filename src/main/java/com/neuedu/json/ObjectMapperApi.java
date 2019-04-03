package com.neuedu.json;

import org.codehaus.jackson.map.ObjectMapper;
import org.apache.commons.lang.StringUtils;

import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ObjectMapperApi {
    @Autowired
    ObjectMapper objectMapper;

    /*
     * json对象转换成字符串
     * */

    public <T> String obj2str(T t) {
        if (t == null) {
            return null;
        }


        try {
            //三目表达式：   如果t是string类型  就将t强转为string  然后返回；否则执行writeValueAsString
            return t instanceof String ? (String) t : objectMapper.writeValueAsString(t);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     *
     * */
    public <T> String obj2strPretty(T t) {
        if (t == null) {
            return null;
        }


        try {
            //三目表达式：   如果t是string类型  就将t强转为string  然后返回；writerWithDefaultPrettyPrinter
            return t instanceof String ? (String) t : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(t);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /*
     * 将字符串转换成Java对象   String --》Object
     * */
    public <T> T str2Obj(String str, Class<T> clazz) {

        if (StringUtils.isEmpty(str) || clazz == null) {
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /*
     * 将json数组转成java集合
     * 重载即可
     * */
    public <T> T str2Obj(String str, TypeReference<T> typeReference) {

        if (StringUtils.isNotEmpty(str) || typeReference == null) {

            return null;
        }

        try {
            return typeReference.getType().equals(String.class) ? (T) str : objectMapper.readValue(str, typeReference);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
