package com.kvas;

/**
 * Created by psyrtsov.
 */
public class TestStoreRegistrationService implements StoreRegistrationService{
    public static final String TEST_STORE = "testStore";
    public static StoreRouter testStore;

    public void register(StoreRegistry storeRegistry) {
        storeRegistry.register(TEST_STORE, testStore);

    }
}
