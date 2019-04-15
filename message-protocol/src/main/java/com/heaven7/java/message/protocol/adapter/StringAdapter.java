package com.heaven7.java.message.protocol.adapter;

import com.heaven7.java.base.util.Predicates;
import com.heaven7.java.message.protocol.TypeAdapter;
import okio.BufferedSink;
import okio.BufferedSource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author heaven7
 */
public class StringAdapter extends TypeAdapter {

    @Override
    public int write(BufferedSink sink, Object obj) throws IOException{
        String msg = (String) obj;
        if(msg == null){
            sink.writeInt(-1);
            return 4;
        }else if(msg.length() == 0){
            sink.writeInt(0);
            return 4;
        }else {
            sink.writeInt(msg.length());
            sink.writeUtf8(msg);
        }
        return 4 + msg.length();
    }

    @Override
    public Object read(BufferedSource sink) throws IOException{
        int len = sink.readInt();
        if(len == -1){
            return null;
        }else if(len == 0){
            return "";
        }else if(len > 0){
            return sink.readUtf8(len);
        }else {
            throw new UnsupportedOperationException("wrong len of string.");
        }
    }

    @Override
    public int evaluateSize(Object obj){
        String msg = (String) obj;
        if(Predicates.isEmpty(msg)){
            return 4;
        }else {
            return 4 + msg.length();
        }
    }
}
