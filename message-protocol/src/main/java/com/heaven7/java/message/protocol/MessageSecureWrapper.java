/*
 * Copyright 2019
 * heaven7(donshine723@gmail.com)

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.heaven7.java.message.protocol;

import com.heaven7.java.message.protocol.secure.UnsafeMessageSecure;
import com.heaven7.java.message.protocol.util.ArrayUtils;
import okio.BufferedSink;
import okio.BufferedSource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * the message secure wrapper used to encode and decode data in multi segments.
 * why need this class ? because some encryption algorithm only support max length data in bytes.
 * eg: RSA-512 only support 53 bytes data. RSA-1024 only support 117 bytes data.
 * @author heaven7
 */
public final class MessageSecureWrapper {

    private final MessageSecure secure;

    public MessageSecureWrapper(MessageSecure secure) {
        this.secure = secure;
    }
    public MessageSecure getMessageSecure() {
        return secure;
    }

    public int encodeWithFlush(BufferedSink sink, byte[] data) throws GeneralSecurityException, IOException {
        int result = encode(sink, data);
        if(sink != null){
            sink.flush();
        }
        return result;
    }
    public int encode(BufferedSink sink, byte[] data) throws GeneralSecurityException, IOException {
        int count = 0;
        int length = MessageConfigManager.getSegmentationPolicy().getSecureSegmentLength();
        //length required
        if(!(secure instanceof UnsafeMessageSecure) && length > 0 && data.length > length){
            List<byte[]> bytes = ArrayUtils.splitArray(data, length);
            for (byte[] splitData : bytes){
                byte[] encodes = secure.encode(splitData);
                count += 4 + encodes.length;
                if(sink != null){
                    sink.writeInt(encodes.length);
                    sink.write(encodes);
                }
            }
        }else {
            byte[] encodes = secure.encode(data);
            count += 4 + encodes.length;
            if(sink != null){
                sink.writeInt(encodes.length);
                sink.write(encodes);
            }
        }
        return count;
    }

    public byte[] decode(BufferedSource source) throws IOException, GeneralSecurityException {
        List<byte[]> list = new ArrayList<>();
        int total = 0;
        while (!source.exhausted()){
            int size = source.readInt();
            byte[] data = secure.decode(source.readByteArray(size));
            list.add(data);
            total += data.length;
        }
        if(list.isEmpty()){
            return null;
        }
        byte[] out = new byte[total];
        int startPos = 0;
        for (byte[] data : list){
            System.arraycopy(data,0, out, startPos, data.length);
            startPos += data.length;
        }
        return out;
    }
}
