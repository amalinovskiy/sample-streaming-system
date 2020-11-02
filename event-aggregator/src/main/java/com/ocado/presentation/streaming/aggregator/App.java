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

import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;

@SpringBootApplication
public class App {

	private final InteractiveQueryService interactiveQueryService;
	private final Clock clock;

	public App(InteractiveQueryService interactiveQueryService) {
		this.interactiveQueryService = interactiveQueryService;
		this.clock = Clock.systemUTC();
	}

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@RestController
	public class AppController {

		@RequestMapping("/count")
		public Long count() {
			var countStore =
					interactiveQueryService.getQueryableStore("countOutput", QueryableStoreTypes.<String, Long>windowStore());
			var resultIterator = countStore.fetchAll(clock.instant().minusSeconds(30), clock.instant());
			if (resultIterator.hasNext()) {
				return countStore.fetchAll(clock.instant().minusSeconds(30), clock.instant()).next().value;
			} else {
				return 0L;
			}
		}
	}

}
