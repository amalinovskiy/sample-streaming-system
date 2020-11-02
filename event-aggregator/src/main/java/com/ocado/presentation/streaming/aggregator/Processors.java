/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ocado.presentation.streaming.aggregator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

@Configuration
public class Processors {

    private static final Logger logger = Logger.getLogger(Processors.class.getCanonicalName());

    @Bean
    public Function<KStream<String, DomainEvent>, KStream<String, EventCount>> countByEventType() {
        var mapper = new ObjectMapper();
        var domainEventSerde = new JsonSerde<>(DomainEvent.class, mapper);

        return input -> input
                .groupBy(
                        (s, domainEvent) -> domainEvent.getEventType(),
                        Grouped.with(null, domainEventSerde))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(300L)))
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as("countOutput")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()))
                .toStream()
                .map(((key, value) -> new KeyValue<>(null, new EventCount(key.key(), value))));
    }

    @Bean
    public Consumer<KStream<String, DomainEvent>> countEvents(MeterRegistry meterRegistry) {
        return (input) -> input.foreach(
                (String key, DomainEvent event) -> {
                    logger.info("Got an event!");
                    meterRegistry.counter("processed_event_count").increment();
                }
        );
    }
}
