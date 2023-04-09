package flink.playground.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
  private String id;
  private String transactionId;
  private String hopId;
  private String coolId;
  private String text;
  private Date createdDate;
  private Set<Integer> numbers;
}
