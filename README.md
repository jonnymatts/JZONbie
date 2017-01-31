# JZONbie   ![Travis build status](https://travis-ci.org/jonnymatts/JZONbie.svg?branch=master "JZONbie build status")

JZONbie is a mock HTTP server that serves known JSON responses for primed requests. It is ran as a standalone process, stubbing the behaviour of an application's external dependencies during integration testing. It also offers the ability to verify that requests have been received.

## Usage
JZONbie is ran as a standalone process, most commonly as a docker container. It can be found on Docker Hub [here](https://hub.docker.com/r/jonnymatts/jzonbie/ "JZONbie on Docker Hub"). The docker image can also be built locally by cloning the repository and running the following command.

```bash
./gradlew clean docker
```


To prime JZONbie to return a known response for a given request, the JZONbie JSON API can be used. 

Sending the following request, with the header `zombie:priming`, will prime the zombie to listen for a POST request on any path and abody with the field `var` having the value `val`.
```json
{
  "request": {
    "path": ".*",
    "method": "POST",
    "body": {
      "var": "val"
    }
  },
  "response": {
    "statusCode": 200,
    "body": {
      "message": "Hello!"
    }
  }
}
```
If JZONbie receives a request matching this description, it will serve a 200 (OK) response with a body containing the `message` field with the value `Hello!`.



Other functions of JZONbie are accessed by changing the value of the request's `zombie` header. the table below shows each of the JZONbie functions available.

| Zombie Header Value  | JZONbie Function |
| ------------- | ------------- |
| "priming"  | Primes JZONbie with the given request and response  |
| "list"  | Responds with the list of request/response mappings currently stored in JZONbie  |
| "history"  | Responds with the list of successful calls to JZONbie  |
| "reset"  | Clears the current state  |



In addition to the JSON API, there is also a Java client that can be used to interact with a JZONbie instance. An example usage of this client is shown below.

```java
JzonbieClient client = new JzonbieClient("http://localhost:8080");

final AppRequest zombieRequest = AppRequest.builder("POST", "/blah")
        .withBody(singletonMap("one", 1))
        .build();

final AppResponse zombieResponse = AppResponse.builder(200)
        .withHeader("Content-Type", "application/json")
        .withBody(singletonMap("message", "Well done!"))
        .build();

client.primeZombie(zombieRequest, zombieResponse);
```
