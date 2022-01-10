package com.wiredbraincoffee.productapiannotation;

import com.wiredbraincoffee.productapiannotation.controller.ProductController;
import com.wiredbraincoffee.productapiannotation.model.Product;
import com.wiredbraincoffee.productapiannotation.model.ProductEvent;
import com.wiredbraincoffee.productapiannotation.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

//Only JUnit5 extension for spring: it doesn't start springboot application server but only Spring context
@ExtendWith(SpringExtension.class)
public class JUnit5ControllerMockTest {

    private WebTestClient webTestClient;
    private List<Product> expectedList;

    @MockBean
    private ProductRepository repository; //Mock the repository

    @BeforeEach
    void beforeEach() {
        this.webTestClient = WebTestClient.bindToController(new ProductController(repository))
                .configureClient()
                .baseUrl("/products")
                .build();

        this.expectedList = Arrays.asList(new Product("1","Big Latte", 1.99));
    }

    @Test
    void testGetAllProducts() {
        Mockito.when(repository.findAll()).thenReturn(Flux.fromIterable(this.expectedList));
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
        Mockito.when(repository.findById("aaaa")).thenReturn(Mono.empty());
        this.webTestClient.get()
                .uri("/aaaa")
                .exchange() //this perform the request
                .expectStatus()
                .isNotFound();

    }

    @Test
    void testProductIdFound() {
        Mockito.when(repository.findById("1")).thenReturn(Mono.just(expectedList.get(0)));
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
