package com.kvas;

import java.util.ServiceLoader;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by psyrtsov.
 */
public class StoreRegistry {
    private static class Holder {
        public static final StoreRegistry instance = new StoreRegistry();
    }

    public static StoreRegistry storeRegistry() {
        return Holder.instance;
    }

    private Map<String,StoreRouter> map = new HashMap<String, StoreRouter>();
    
    public StoreRegistry() {
        init();
    }

    public void init() {
        final ServiceLoader<StoreRegistrationService> storeRegistrationServiceLoader = ServiceLoader.load(StoreRegistrationService.class);
        for (StoreRegistrationService storeRegistrationService : storeRegistrationServiceLoader) {
            storeRegistrationService.register(this);
        }
    }

    public StoreRouter register(String name,StoreRouter store) {
        return map.put(name, store);
    }

    public static<K,V> StoreRouter<K,V> getStore(String name) {
        //noinspection unchecked
        return storeRegistry().map.get(name);
    }
}
