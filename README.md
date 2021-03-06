# Meshy
the general message protocal of 'CS'. use byte stream to read and write message with 'Encryption/Decrypt'.

### total message Protocol
|Magic|total-length| Version  | Signature | EncodeType |           Encoded-Data | 
| :----------- | ----:| :--------- | ----:| :--------- | --------------: |
### detail message protocol(after decode)
| Message-Type  | String-Message | Any-Object |    
| :----------- | ----: | :--------- |

# Features
- 1, support the all base types of java , include their wrapper types.
also support String, collection,Map and self types...etc.
  - byte, short,int,long, float,double,char,boolean and wrappers.
  - String, collection, Map, types.
  - ParameterizedType, GenericArrayType, WildcardType, TypeVariable, Class.
  - Self object type. 
- 2, support double message secure: Encryption/Decrypt, Signature.
  - Encryption/Decrypt . 
  ```java
    byte[] data = {1,2,3};
        try {
            KeyPair keyPair = RSACoder.initKeys();
            RSAMessageSecure secure = new RSAMessageSecure(keyPair.getPublic(), keyPair.getPrivate());
            byte[] bytes = secure.encode(data);
            byte[] result = secure.decode(bytes);
            assertEquals(data, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
  ```
  - Signature. (built in md5-slat and sha256-salt, you expend it)
  ```java
  public class HMAC_SHA1SignatureTest {

    private static final String KEY = "~!@#$%^&*()";
    private final HMAC_SHA1Signature mSign = new HMAC_SHA1Signature();

    @Test
    public void test1(){
        String data = "Hello Google";
        String signature = mSign.signature(data.getBytes(StandardCharsets.UTF_8), KEY);
        System.out.println(signature);
        Assert.assertEquals(28, signature.length());
    }
  }
  ```
 - 3, Support Data object compat for multi versions. that means you can write high version object to lower remote.
or write lower version object to higher.
    - from com.heaven7.java.message.protocol.OkMessageTest.
  ```java
   @Test //local is lower. remote is higher.
    public void testCompatLowToHigh() throws Exception{
        initContext(1.0F);

        String msg = "testCompatLowToHigh";
        Person2 person = new Person2();
        person.setAge(18);
        person.setName("Google");
        person.setAuchor("heaven7");
        Message<Person2> mess = Message.create(Message.COMMON, msg, person);

        //sender is higher.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //here we want to send message to client. but client's  version is higher. so we need assign version
        float applyVersion = TestContext.getHigherVersion();
        BufferedSink bufferedSink = Okio.buffer(Okio.sink(baos));
        int evaluateSize = mContext.getWriter().evaluateMessageSize(mess, TestContext.TYPE_RSA, applyVersion);

        int writeSize = mContext.getWriter().writeMessage(bufferedSink, mess, TestContext.TYPE_RSA, applyVersion);
        bufferedSink.close();
        Assert.assertTrue(evaluateSize == writeSize);
        Assert.assertTrue(writeSize == baos.size());

        //receiver is lower
        BufferedSource source = Okio.buffer(Okio.source(new ByteArrayInputStream(baos.toByteArray())));
        Message<Person> mess2 = mContext.getReader().readMessage(source);
        Assert.assertTrue(mess.getType() == mess2.getType());
        Assert.assertTrue(mess.getMsg().equals(mess2.getMsg()));
        Assert.assertTrue(mess2.getEntity().getClass() == Person.class);
        Assert.assertEquals(mess2.getEntity().getName(), mess.getEntity().getName());
        Assert.assertEquals(mess2.getEntity().getAge(), mess.getEntity().getAge());
    }
  ```
## Quick Start
- 1, Gradle config
```java
    compile 'com.heaven7.java.meshy:Meshy:<Release-version>'
```
- 2, create a 'Meshy' object then you can enjoy it.
```java
//this method shows create meshy by MeshyBuilder.
public static Meshy initialize(String rsaKey, boolean priKey){
        Key key = priKey ? RSAUtils.getPrivateKey(rsaKey) : RSAUtils.getPublicKey(rsaKey);
        return new MeshyBuilder()
                .setVersion(1.0f)
                .setSignatureKey("Hello Google")
                .registerMessageSecure(CSConstant.TYPE_RSA_SINGLE, new SingleRSAMessageSecure(key.getEncoded(), priKey))
                .build();
    }
```
- 3, the sample to use this framework. you can see [CS-Communication](https://github.com/LightSun/CS-Communication)

## Thanks
- [Google/Gson](https://github.com/google/gson). idea from this.
- [square/okio](https://github.com/square/okio). read and write data depend on it.

## License

    Copyright 2019  
                    heaven7(donshine723@gmail.com)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
