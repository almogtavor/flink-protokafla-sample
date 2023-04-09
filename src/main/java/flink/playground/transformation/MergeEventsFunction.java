package flink.playground.transformation;

import flink.playground.model.Event;
import flink.playground.model.Transaction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.guava30.com.google.common.collect.Lists;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MergeEventsFunction extends ProcessWindowFunction<Event, Transaction, String, TimeWindow> {
    private transient MapState<String, Transaction> transactionState;

    @Override
    public void open(Configuration parameters) {
        // Configure state TTL
        StateTtlConfig ttlConfig = StateTtlConfig
                .newBuilder(Time.days(50))
                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                .build();
        MapStateDescriptor<String, Transaction> descriptor =
                new MapStateDescriptor<>("transaction", String.class, Transaction.class);
        descriptor.enableTimeToLive(ttlConfig);
        transactionState = getRuntimeContext().getMapState(descriptor);
    }

    @Override
    public void process(String key, Context context, Iterable<Event> events, Collector<Transaction> out) throws IOException {
        Transaction transaction;
        try {
            transaction = transactionState.get(key);

            if (transaction == null) {
                // Create a new transaction with the event data
                transaction = Transaction.builder()
                        .transactionId(key)
                        .events(Lists.newArrayList(events))
                        .build();
            } else {
                // Merge the event data into the existing transaction
                ArrayList<Event> events1 = Lists.newArrayList(events);
                List<Event> events2 = transaction.getEvents();
                events1.addAll(events2);
                transaction.setEvents(events1);
            }

            transactionState.put(key, transaction);
            out.collect(transaction);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        out.collect(Transaction.builder()
//                .transactionId(key)
//                .events(Lists.newArrayList(events))
//                .build());
    }
}
