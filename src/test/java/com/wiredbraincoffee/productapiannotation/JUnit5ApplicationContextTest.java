package com.wiredbraincoffee.productapiannotation;

import com.wiredbraincoffee.productapiannotation.controller.ProductController;
import com.wiredbraincoffee.productapiannotation.model.Product;
import com.wiredbraincoffee.productapiannotation.model.ProductEvent;
import com.wiredbraincoffee.productapiannotation.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class JUnit5ApplicationContextTest {

    private WebTestClient webTestClient;
    private List<Product> expectedList;

    @Autowired
    private ProductRepository repository;

    //We will bind all applicationContext to the testClient instance
    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void beforeEach() {
        this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext)//here we pass applicationContext
                .configureClient()
                .baseUrl("/products")
                .build();

        this.expectedList = repository.findAll() //this return a Flux
                .collectList() // collect the result in a List when method complete
                .block(); // block the execution until all result are returned
        //System.out.println("***** expectedList size: "+expectedList.size());
    }

    @Test
    void testGetAllProducts() {
        this.webTestClient.get()
                .uri("/")
                .exchange() //this perform the request
                .expectStatus()
                .isOk()
                .expectBodyList(Product.class)
                .isEqualTo(expectedList);
    }

    @Test
    void testProductInvalidNotFound() {
        this.webTestClient.get()
                .uri("/aaaa")
                .exchange() //this perform the request
                .expectStatus()
                .isNotFound();

    }

    @Test
    void testProductIdFound() {
        Product expectedProduct = expectedList.get(0);
        this.webTestClient.get()
                .uri("/{id}", expectedProduct.getId())
                .exchange() //this perform the request
                .expectStatus()
                .isOk()
                .expectBody(Product.class)
                .isEqualTo(expectedProduct);
    }

    @Test
    void testProductEvents() {
        ProductEvent expectedEvent = new ProductEvent(0L, "Product Event");

        FluxExchangeResult<ProductEvent> result =
                webTestClient.get()
                        .uri("/events")
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(ProductEvent.class); //this consume the response body

        //We have to Use StepVerifier for assertiond because webTestClient don't have methods to
        //test the result of a stream
        StepVerifier.create(result.getResponseBody())
                .expectNext(expectedEvent) //expect that the first event is the expected one
                .expectNextCount(2)// pass next two events
                .consumeNextWith(
                        event -> assertEquals(Long.valueOf(3), event.getEventId())) // Verify that the next event has id == 3
                .thenCancel()// cancel the stream
                .verify();// verify the result
    }

}
