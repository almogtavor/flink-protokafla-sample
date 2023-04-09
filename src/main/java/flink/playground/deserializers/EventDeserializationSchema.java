package flink.playground.deserializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import flink.playground.model.Event;
import flink.playground.model.Transaction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class EventDeserializationSchema implements KafkaDeserializationSchema<Event> {
  private final ObjectMapper objectMapper;

  public EventDeserializationSchema(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public TypeInformation<Event> getProducedType() {
    return TypeInformation.of(new TypeHint<Event>(){});
  }

  @Override
  public boolean isEndOfStream(Event nextElement) {
    return false;
  }

  @Override
  public Event deserialize(ConsumerRecord<byte[], byte[]> consumerRecord) throws Exception {
    return objectMapper.readValue(consumerRecord.value(), Event.class);
  }
}
