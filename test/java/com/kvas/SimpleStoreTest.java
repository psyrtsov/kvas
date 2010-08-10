package com.kvas;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import com.kvas.queue.OpQueue;
import com.kvas.queue.InMemOpQueue;
import com.kvas.queue.DataOp;
import com.kvas.processor.DataOpProcessor;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by psyrtsov.
 */
public class SimpleStoreTest {
    private SimpleStore<String,TestDataBean> subject;
    private KVStore<String,StampedData<TestDataBean>> tempStore;
    private KVStore<String,TestDataBean> persistentStore;
    private ArrayBlockingQueue<DataOp> queue;
    private DataOpProcessor processor;

    @SuppressWarnings({"unchecked"})
    @Before
    public void setUp() {
        tempStore = mock(KVStore.class);
        persistentStore = mock(KVStore.class);
        TestStoreRegistrationService.testStore = new StoreRouter<String,TestDataBean>(tempStore, persistentStore);
        queue = new ArrayBlockingQueue<DataOp>(1);
        OpQueue opQueue = new InMemOpQueue(queue);
        processor = new DataOpProcessor(queue);
        subject = new SimpleStore<String, TestDataBean>(TestStoreRegistrationService.TEST_STORE, opQueue);
    }

    @After
    public void tearDown() {
        subject = null;
        tempStore = null;
        persistentStore = null;
        queue = null;
        processor = null;
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testPut() {
        Object ctx = "ctx";
        String key = "key";
        TestDataBean value = new TestDataBean();
        String scopedKey = TestStoreRegistrationService.testStore.getScopedKey(ctx, key);
        subject.put(ctx, key, value);

        ArgumentCaptor<StampedData> stampedDataArgumentCaptor = ArgumentCaptor.forClass(StampedData.class);
        verify(tempStore).put(eq(scopedKey), stampedDataArgumentCaptor.capture());
        final StampedData<TestDataBean> tempSavedData = stampedDataArgumentCaptor.getValue();
        assertSame(tempSavedData.getValue(), value);
        verifyZeroInteractions(persistentStore);
        processor.processOneItem();
    }

    @Test
    public void testGet() {
        Object ctx = "ctx";
        String key = "key";
        TestDataBean value = new TestDataBean();
        TestDataBean rv = subject.get(ctx, key);
    }

    @Test
    public void testDelete() {
        Object ctx = "ctx";
        String key = "key";
        subject.delete(ctx, key);        
    }
}
