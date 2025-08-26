# ADESA RTI – Azure Event Grid CloudEvents Webhook

This Spring Boot service acts as an **Azure Event Grid** subscriber.  
It exposes a **CloudEvents 1.0** webhook endpoint (`/events`) that:

- Handles Event Grid’s **validation handshake** (HTTP `OPTIONS`).
- Accepts **CloudEvents batch notifications** (`POST`).
- Asynchronously imports events into a database table `lads.EventImports`.

The project supports **SQL Server** (production) and **H2** (demo/testing).

---

## 1. Build & Run

### Build
```bash
mvn clean package

mvn spring-boot:run -Dspring-boot.run.profiles=h2

H2 console: http://localhost:8080/h2
JDBC URL: jdbc:h2:mem:adesa, user: sa, password: (empty)

//Validation Handshake

curl -i -X OPTIONS http://localhost:8080/events \
  -H "WebHook-Request-Origin: https://eventgrid.azure.net" \
  -H "WebHook-Request-Rate: 120"

//Event Delivery Payload
curl -i -X POST http://localhost:8080/events \
  -H "Content-Type: application/cloudevents-batch+json" \
  -d '[{
    "specversion":"1.0",
    "id":"evt-1",
    "source":"/contoso/items",
    "type":"Contoso.ItemCreated",
    "subject":"items/42",
    "time":"2025-08-22T12:00:00Z",
    "datacontenttype":"application/json",
    "data": { "id":"42", "vin":"1ABCD10001AB12345" }
  }]'
```
### Smoke Testing
```bash
//minimal load testing
cd src/main/resources/scripts/
WEBHOOK_SECRET=dev-secret BASE_URL=http://localhost:8080 k6 run cloudevents_load_test.js
```


