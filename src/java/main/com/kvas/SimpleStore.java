package com.kvas;

import com.kvas.queue.OpQueue;
import com.kvas.queue.DataOp;
import static com.kvas.StoreRegistry.getStore;

/**
 * Simple Store is using StoreRouter to execute simple BLOB update,
 * it simulates behaviuor of standard key value store, but uses KVAS design
 * to do that.
 *
 * Created by psyrtsov.
 */
public class SimpleStore<K,V> {
    private final String storeName;
    private final OpQueue opQueue;

    public SimpleStore(String storeName, OpQueue opQueue) {
        this.storeName = storeName;
        this.opQueue = opQueue;
    }

    public void put(Object ctx, K key, V value) {
        opQueue.submit(new UpdateDataOp<K,V>(storeName,ctx,key,value));
    }

    public V get(Object ctx, K key) {
        StoreRouter<K, V> storeRouter = getStore(storeName);
        return storeRouter.get(ctx, key);
    }

    public void delete(Object ctx, K key) {
        opQueue.submit(new DeleteDataOp<K>(storeName,ctx,key));
    }

    public static class UpdateDataOp<K,V> extends DataOp {
        private final String storeName;
        private final Object ctx;
        private final K key;
        private final V value;

        public UpdateDataOp(String storeName, Object ctx, K key, V value) {
            this.storeName = storeName;
            this.ctx = ctx;
            this.key = key;
            this.value = value;
        }

        public boolean apply() {
            StoreRouter<K, V> storeRouter = getStore(storeName);
            storeRouter.put(ctx, key, value);
            return true;
        }

        public String getStoreName() {
            return storeName;
        }

        public Object getCtx() {
            return ctx;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

    public static class DeleteDataOp<K> extends DataOp {
        private final String storeName;
        private final Object ctx;
        private final K key;

        public DeleteDataOp(String storeName, Object ctx, K key) {
            this.storeName = storeName;
            this.ctx = ctx;
            this.key = key;
        }

        public boolean apply() {
            StoreRouter<K, Object> storeRouter = getStore(storeName);
            storeRouter.delete(ctx, key);
            return true;
        }

        public String getStoreName() {
            return storeName;
        }

        public Object getCtx() {
            return ctx;
        }

        public K getKey() {
            return key;
        }
    }
}
