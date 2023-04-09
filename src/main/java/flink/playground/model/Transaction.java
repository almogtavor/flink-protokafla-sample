package flink.playground.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.apache.flink.api.java.tuple.Tuple2;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class Transaction {
  private String transactionId;
  private List<String> eventIds;
  private Tuple2<String, String> hopIds;
  private Tuple2<String, String> coolIds;
  private Tuple2<Date, Date> createdDates;
  private List<Event> events;
}
