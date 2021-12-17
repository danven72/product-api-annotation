# product-api-annotation

REST API example with Java11 - Reactive SPRING and Mongo DB

Pluralsight course **"Spring Webflux: Getting started"** by **Esteban Herrera**

###Test - Url Examples (curl - Windows)
**GET All products**<br/>
curl -v http://localhost:8080/products

**GET One Product**<br/>
curl -v http://localhost:8080/products/{id}

**ADD One Product**<br/>
curl -v -H "Content-type: application/json" -d "{\"name\":\"Italian Coffee\", \"price\": 5.45}" http://localhost:8080/products/

**UPDATE One Product**<br/>
curl -v -H "Content-type: application/json" -d "{\"name\":\"Black Tea\", \"price\": 2.45}" -X PUT http://localhost:8080/products/{id}

**DELETE One Product**<br/>
curl -v -H "Content-type: application/json" -d "{\"name\":\"Black Tea\", \"price\": 2.45}" -X DELETE http://localhost:8080/products/{id}

**DELETE ALL Products**<br/>
curl -v -X DELETE http://localhost:8080/products
