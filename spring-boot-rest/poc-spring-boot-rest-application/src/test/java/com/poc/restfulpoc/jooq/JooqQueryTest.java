/*
 * Copyright 2015-2020 the original author or authors.
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

package com.poc.restfulpoc.jooq;

import java.sql.Timestamp;
import java.util.List;

import com.poc.restfulpoc.AbstractRestFulPOCApplicationTest;
import com.poc.restfulpoc.data.DataBuilder;
import com.poc.restfulpoc.jooq.tables.Address;
import com.poc.restfulpoc.jooq.tables.Customer;
import com.poc.restfulpoc.jooq.tables.Orders;
import com.poc.restfulpoc.jooq.tables.records.AddressRecord;
import com.poc.restfulpoc.jooq.tables.records.CustomerRecord;
import com.poc.restfulpoc.jooq.tables.records.OrdersRecord;
import com.poc.restfulpoc.repository.AddressRepository;
import com.poc.restfulpoc.repository.CustomerRepository;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record2;
import org.jooq.Record8;
import org.jooq.Result;
import org.jooq.SelectSeekStep1;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.poc.restfulpoc.jooq.tables.Address.ADDRESS;
import static com.poc.restfulpoc.jooq.tables.Customer.CUSTOMER;
import static com.poc.restfulpoc.jooq.tables.Orders.ORDERS;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(Lifecycle.PER_CLASS)
@Disabled
class JooqQueryTest extends AbstractRestFulPOCApplicationTest {

	@Autowired
	private DSLContext context;

	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private DataBuilder dataBuilder;

	@BeforeAll
	void setUp() {
		this.addressRepository.deleteAll();
		this.customerRepository.deleteAll();
		this.dataBuilder.run();
	}

	@Test
	void testJoinCustomerAddress() throws Exception {
		// All of these tables were generated by jOOQ's Maven plugin
		Customer c = CUSTOMER.as("c");
		Address a = ADDRESS.as("a");

		Result<Record2<String, String>> results = this.context.select(c.FIRST_NAME, c.LAST_NAME).from(c).join(a)
				.on(a.CUSTOMER_ID.eq(c.ID)).orderBy(c.FIRST_NAME.desc()).fetch();

		assertThat(results.size()).isEqualTo(3);
		assertThat(results.getValue(0, c.FIRST_NAME)).isEqualTo("Steve");
		assertThat(results.getValue(1, c.FIRST_NAME)).isEqualTo("Raja");
		assertThat(results.getValue(2, c.FIRST_NAME)).isEqualTo("Paul");

		assertThat(results.getValue(0, c.LAST_NAME)).isEqualTo("Toale");
		assertThat(results.getValue(1, c.LAST_NAME)).isEqualTo("Kolli");
		assertThat(results.getValue(2, c.LAST_NAME)).isEqualTo("Jones");

	}

	@Test
	void testJoinAll() throws Exception {
		// All of these tables were generated by jOOQ's Maven plugin
		Customer c = CUSTOMER.as("c");
		Address a = ADDRESS.as("a");
		Orders o = ORDERS.as("o");

		SelectSeekStep1<Record8<Long, String, String, Timestamp, Long, String, String, Long>, String> sql = this.context
				.select(c.ID, c.FIRST_NAME, c.LAST_NAME, c.DATE_OF_BIRTH, o.ORDER_ID, o.ORDER_NUMBER, o.ORDER_STATUS,
						a.CUSTOMER_ID)
				.from(c).join(a).on(a.CUSTOMER_ID.eq(c.ID)).join(o).on(o.CUSTOMER_ID.eq(c.ID))
				.where(o.ORDER_STATUS.eq("NEW")).orderBy(c.FIRST_NAME.desc());

		CustomerRecord customer = sql.fetchOneInto(CUSTOMER);
		AddressRecord address = sql.fetchOneInto(ADDRESS);
		OrdersRecord order = sql.fetchOneInto(ORDERS);

		assertThat(customer.getValue(c.LAST_NAME)).isEqualTo("Kolli");
		assertThat(customer.getValue(c.FIRST_NAME)).isEqualTo("Raja");
		assertThat(order.getValue(o.ORDER_ID)).isNotNull();
		assertThat(address.getValue(a.CUSTOMER_ID)).isEqualTo(customer.getValue(c.ID));

	}

	@Test
	void jooqSql() {
		Query query = this.context
				.select(CUSTOMER.ID, CUSTOMER.FIRST_NAME, CUSTOMER.LAST_NAME, CUSTOMER.DATE_OF_BIRTH, ORDERS.ORDER_ID,
						ORDERS.ORDER_NUMBER, ORDERS.ORDER_STATUS, ADDRESS.COUNTY)
				.from(CUSTOMER).join(ADDRESS).on(ADDRESS.CUSTOMER_ID.eq(CUSTOMER.ID)).join(ORDERS)
				.on(ORDERS.CUSTOMER_ID.eq(CUSTOMER.ID)).where(ORDERS.ORDER_STATUS.eq("NEW"));
		Object[] bind = query.getBindValues().toArray(new Object[0]);
		List<String> list = this.jdbc.query(query.getSQL(), bind,
				(rs, rowNum) -> rs.getLong(1) + " : " + rs.getString(2) + " " + rs.getString(3) + "-"
						+ rs.getTimestamp(4) + "-" + rs.getLong(5) + "-" + rs.getString(6) + "-" + rs.getString(7) + "-"
						+ rs.getString(8));
		assertThat(list).size().isEqualTo(1);
	}

	@Test
	void testActiveRecords() throws Exception {
		Result<CustomerRecord> result = this.context.selectFrom(CUSTOMER).orderBy(CUSTOMER.ID).fetch();

		assertThat(result.size()).isEqualTo(3);
	}

}
