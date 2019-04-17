package com.heaven7.java.message.protocol;

import com.heaven7.java.base.util.Predicates;
import com.heaven7.java.base.util.SparseArrayDelegate;
import com.heaven7.java.base.util.SparseFactory;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author heaven7
 */
public final class MessageConfigManager {

    private static final Comparator<MessageConfig.Pair<Class<?>, Float>> sCompatComparator =
            new Comparator<MessageConfig.Pair<Class<?>, Float>>() {
        @Override
        public int compare(MessageConfig.Pair<Class<?>, Float> o1, MessageConfig.Pair<Class<?>, Float> o2) {
            return Float.compare(o1.value, o2.value);
        }
    };
    //-------------------------
    private static final SparseArrayDelegate<MessageSecureWrapper> sSecureWrappers;
    private static final WeakHashMap<Class<?>, String> sRepresentMap;
    private static volatile MessageConfig sConfig;
    private static TypeAdapterContext sContext;

    static {
        sSecureWrappers = SparseFactory.newSparseArray(5);
        sRepresentMap = new WeakHashMap<>();
    }

    /**
     * indicate the initialize state
     * @return true if is initialized
     */
    public static boolean isInitialized(){
        return sConfig != null;
    }
    /**
     * init message config manager with target config.
     * you should only call this once.
     * @param config the message config
     */
    public static void initialize(MessageConfig config){
        if(sConfig != null){
            sRepresentMap.clear();
            sSecureWrappers.clear();
        }
        sConfig = config;
        for (Map.Entry<String, List<MessageConfig.Pair<Class<?>, Float>>> en : config.compatMap.entrySet()){
            Collections.sort(en.getValue(), sCompatComparator); //AESC
            final String className = en.getKey();
            for (MessageConfig.Pair<Class<?>, Float> pair : en.getValue()){
                sRepresentMap.put(pair.key, className);
            }
        }
    }

    /**
     * get the compat class. this is useful for compat message entity classes.
     * @param className the expect classname
     * @param version the version code
     * @return the actually class as expect
     * @throws ClassNotFoundException if {@linkplain Class#forName(String)} occurs
     */
    public static Class<?> getCompatClass(String className, float version) throws ClassNotFoundException {
        List<MessageConfig.Pair<Class<?>, Float>> pairs = sConfig.compatMap.get(className);
        if(Predicates.isEmpty(pairs)){
            return Class.forName(className);
        }
        for (MessageConfig.Pair<Class<?>, Float> pair : pairs){
            if(pair.value >= version){
                return pair.key;
            }
        }
        throw new ConfigException("wrong version code("+ version +") or class compat had not configuration.");
    }

    /**
     * get the encode type by random .
     * @return the encode type
     */
    public static int randomEncodeType(){
        try{
            int index = new Random().nextInt(sConfig.secures.size());
            return sConfig.secures.keyAt(index);
        }catch (IllegalArgumentException e){
            throw new ConfigException("you must config the message secure.");
        }
    }

    public static TypeAdapterContext getTypeAdapterContext(){
        if(sContext == null){
            sContext = new WrappedTypeAdapterContext(sConfig.context);
        }
        return sContext;
    }

    /**
     * get message secure for target type
     * @param type the secure type for encode and decode
     * @return the message secure
     */
    public static MessageSecureWrapper getMessageSecure(int type){
        MessageSecureWrapper wrapper = sSecureWrappers.get(type);
        if(wrapper != null){
            return wrapper;
        }
        MessageSecure secure = sConfig.secures.get(type);
        if(secure != null){
            wrapper = new MessageSecureWrapper(secure);
            sSecureWrappers.put(type, wrapper);
            return wrapper;
        }
        throw new ConfigException("you must config the message secure for type = " + type);
    }
    /**
     * sign the message
     * @param data the data to signature
     * @return the signed value
     */
    public static String signatureMessage(byte[] data){
        return sConfig.signature.signature(data, sConfig.signKey);
    }

    /**
     * get the segmentation policy
     * @return the policy
     */
    public static SegmentationPolicy getSegmentationPolicy(){
        return sConfig.segmentationPolicy;
    }

    /**
     * get the version of current used
     * @return the version
     */
    public static float getVersion() {
        return sConfig.version;
    }

    /**
     * get the represent compat class name for target class .
     * @param rawClass the raw class
     * @return the represent class name.
     */
    public static String getRepresentClassName(Class<?> rawClass) {
        String classname = sRepresentMap.get(rawClass);
        if(classname == null){
            return rawClass.getName();
        }
        return classname;
    }

   private static class WrappedTypeAdapterContext implements TypeAdapterContext {

        private final TypeAdapterContext base;

        WrappedTypeAdapterContext(TypeAdapterContext base) {
            this.base = base;
        }

       @Override
       public Object newInstance(Class<?> clazz) {
           return base.newInstance(clazz);
       }

       @Override
        public Map createMap(String name) {
            Map map = base.createMap(name);
            if(map != null){
                return map;
            }
            return new HashMap();
        }
        @Override
        public Map getMap(Object obj) {
            return base.getMap(obj);
        }
        @Override
        public Collection createCollection(String name) {
            Collection collection = base.createCollection(name);
            if(collection != null){
                return collection;
            }
            return new ArrayList();
        }

        @Override
        public boolean isMap(Class<?> rawType) {
            return base.isMap(rawType);
        }
        @Override
        public void registerTypeAdapter(Type type, TypeAdapter adapter) {
            base.registerTypeAdapter(type, adapter);
        }
        @Override
        public TypeAdapter getTypeAdapter(Type type) {
            return base.getTypeAdapter(type);
        }
    }


    public static class ConfigException extends RuntimeException{
        public ConfigException() {
        }
        public ConfigException(String message) {
            super(message);
        }
        public ConfigException(String message, Throwable cause) {
            super(message, cause);
        }
        public ConfigException(Throwable cause) {
            super(cause);
        }
    }
}
