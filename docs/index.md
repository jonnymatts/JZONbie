# Usage

## Starting JZONbie
JZONbie can be started either as an embedded server within any JVM application, or as a standalone process.

### Embedded JZONbie
A JZONbie instance can be started within a JVM application by using the Jzonbie class. This JZONbie instance can be configured with options for port, zombie header name (explained later), and custom object mapper to be used for deserialization.

```java
// Default Jzonbie: Random port, zombie header name 'zombie', and default object mapper  
final Jzonbie default = new Jzonbie();

// Jzonbie with port 8080
final Jzonbie customPort = new Jzonbie(options().withPort(8080));
```

### Standalone JZONbie
When running as a standalone server, it is recommended to use the JZONbie docker container. This can be found on Docker Hub [here](https://hub.docker.com/r/jonnymatts/jzonbie/ "JZONbie on Docker Hub"). The docker image can also be built locally by cloning the repository and running the following command.

```bash
./gradlew clean docker
```

The standalone server within the docker container will default to starting on port 8080 with zombie header `zombie`. These can be configured by setting the enviroment variables `JZONBIE_PORT` and `JZONBIE_PORT` respectively within the container.

```bash
# Runs latest JZONbie with server started within container on port 30000 mapped to port 8080 on the host
docker run -p 8080:30000 -e JZONBIE_PORT=30000 jonnymatts/jzonbie:latest
```

## Stubbing
The main usage of JZONbie is the stubbing of external services required by your application within integration tests.

### Embedded Stubbing
To prime JZONbie using the embedded instance, a request and response must first be created. This can be deon easily using the included builders as shown below.

```java
final AppRequest appRequest = AppRequest.builder("POST", "/blah")
    .withBody(singletonMap("one", 1))
    .build();

final AppResponse appResponse = AppResponse.builder(200)
    .contentType("application/json")
    .withBody(singletonMap("message", "Well done!"))
    .build();
```

#### App Requests
For app requests, the required fields are the HTTP method and path of the expected request. This request mapping will map any requests matching against these two fields. This request can then become more specific by specifying headers, query parameters and a request body.

There are multiple places in the request priming where regex values can be used. These include in header keys and values, query param keys and values, and in JSON object string keys and values.

The app request (and response) body is primed with a value of type BodyContent. There are multiple variations of BodyContent including:

```java
// Body content representing a JSON object, taking in a map
final ObjectBodyContent object = objectBody(singletonMap("key", "val"));

// Body content representing a JSON array, taking in a list
final ArrayBodyContent array = arrayBody(asList("val1", "val2"));

// Body content representing a JSON string, taking in a string
final StringBodyContent string = stringBody("randomString");

// Body content representing a literal, taking in a string, number or boolean
final LiteralBodyContent string = literalBody("<xml>val</xml>");
```

Each of these body contents can be inferred from the input type to the `withBody` method on each of the builders, except for string body content which must be explicit.

#### App Responses
For app responses, the only required field is the status code. The requests can also optionally be primed with headers and a response body. Responses can also be primed to respond with a delay, which takes a Duration.

---

Once the app request and response have been created, they can then be used to prime the JZONbie instance. 

```java
// Using the request and response objects defined above
jzonbie.primeZombie(request, response);
```

The above snippet will prime the jzonbie to respond with a 200 response with a JSON object body for a POST request to `/blah` with a JSON object request body. Once this primed request has been matched and the corresponding request has been returned, the priming will be removed from the JZONbie instance.

As this may not be the desired functionality, JZONbie can also be primed to respond with a default response for a given request.

```java
// Using the request and response objects defined above
jzonbie.primeZombieForDefault(request, new StaticDefaultResponse(response));
```

This will prime the zombie to return the default response whenever the primed request is matched. If the JZONbie is primed with a standard response while also primed with a default response, when the primed request is matched the standard priming will always be consumed first.

In addition to the StaticDefaultResponse used above, there is also a DynamicDefaultResponse that can be primed.

```java
// Infinite integer iterator
Iterator<Integer> iterator;

// Using the request and response objects defined above
jzonbie.primeZombieForDefault(request, new DynamicDefaultResponse(() -> response.contentType("application/xml").withBody("<number>" + iterator.next() + "</number>")));
```

This is useful for defining sequences that can be returned for similar primed requests. It's constructor takes a supplier of AppResponse.

### Stubbing Over HTTP
To prime the JZONbie using HTTP with the same priming as used above, a request containing the following body must be sent to the server.

```json
{
  "request" : {
    "path" : "/blah",
    "method" : "GET",
    "body" : {
            "JZONBIE_CONTENT_TYPE": "J_OBJECT",
            "one" : 1
    }
  },
  "response" : {
    "statusCode" : 200,
    "headers" : {
      "Content-Type" : "application/json"
    },
    "body" : {
      "JZONBIE_CONTENT_TYPE": "J_OBJECT",
      "message" : "Well done!"
    }
  }
}
```

The HTTP method used when sending this can be either POST, PATCH or PUT. It is necessary to put the header `zombie:priming` when sending this request so that JZONbie can see that it is a priming request. This is a usage of the previously mentioned zombie header name.

There are two other usages of this header when priming, `priming-default` and `priming-file`. Using `priming-default` with the above request body will prime the JZONbie to respond with the response as the default for the request. Currently, this only supports static default responses. The `priming-file` value can be used to prime the JZONbie instance with multiple mappings defined in a file via a multi-part form request. A common use case for this is to prime JZONbie with the same priming from a previous test scenario. Downloading the current mappings into a file will be shown later.

### Stubbing Using HTTP Client
As trying to stub a JZONbie instance over HTTP can become complicated, a Java client has been provided. The client has the same interface as the embedded JZONbie, and can be used interchangeably.
 
```java
final File primingfile = new File("/path/to/file");

// Interacts with a local JZONbie instance serving on port 8080
final JzonbieClient client = new JzonbieHttpClient("http://localhost:8080");

client.primeZombie(file);
```

## Verification
Another integral function of JZONbie is to allow for verification that a request has been called.

### Verifying Using Embedded JZONbie And HTTP Client

```java
// Verifies that the request defined earlier was received by JZONbie at most 3 times 
final boolean verified = jzonbie.verify(request, atMost(3))
```

As well as the `atMost` verification criteria, there are also `equalTo`, `atLeast` and `between`.

### Verifying Over HTTP
To verify over HTTP, the request and criteria must be sent in a request with the header `zombie:verify`. The above example would be sent with the following request body.

```json
{
  "request" : {
    "path" : "/blah",
    "method" : "GET",
    "body" : {
            "JZONBIE_CONTENT_TYPE": "J_OBJECT",
            "one" : 1
    }
  },
  "criteria" : {
    "atMost": 3
  }
}
```

## Other Commands
The other values for the zombie header are: `current`, `current-file`, `history`, and `reset` 

### Get Current Mapping
There are two methods for getting the current mapping of the JZONbie, either using the zombie header value `current` or `current-file`. Using `current` will return the list of primed mappings for the JZONbie instance, an example of which is shown below.

```json
[ {
  "request" : {
    "path" : "/path",
    "headers" : { },
    "method" : "GET"
  },
  "responses" : {
    "default" : {
      "statusCode" : 200,
      "headers" : {
        "Content-Type" : "application/json"
      },
      "body" : {
        "JZONBIE_CONTENT_TYPE": "J_OBJECT",
        "key" : "val"
      }
    },
    "primed" : [ {
      "statusCode" : 201,
      "headers" : {
        "Content-Type" : "application/json"
      },
      "body" : {
        "JZONBIE_CONTENT_TYPE": "J_OBJECT",
        "key" : "val"
      }
    } ]
  }
} ]
```

Using the following snippet will return the current state of JZONbie using the Java implementations.
```java
final List<ZombieMapping> currentPriming = jzonbie.getCurrentPriming()
```

The `current-file` zombie header value will respond with a file download of the current state in the form above. This is not supported by the Java implementations currently.

### Get History
Getting the history of the JZONbie will return an ordered list of the request received and responses served by the JZONbie in it's current session. This can be done over HTTP by using the `history` zombie header value.

Using the following snippet will return the call history of the current JZONbie session using the Java implementations.
```java
final List<ZombiePriming> history = jzonbie.getHistory()
```

### Resetting The Session
The current session state can be cleared from the JZONbie instance by using the `reset` zombie header value over HTTP, or via the following code snippet using the embedded JZONbie or HTTP client:

```java
jzonbie.reset()
```
