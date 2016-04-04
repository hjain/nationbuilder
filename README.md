# nationbuilder - tech challange - capture messaging events 

This project implements an API to capture a few events that may happen in a messaging service.
 
## Key Points About the Projext

* Data Store - everything is done in memory. Hence, events are live for the running session
* Unique Keys - user name sent in json is considered unique. So is the date per API call.

## Getting Started

The project can either be downloaded from https://github.com/hjain/nationbuilder or cloned using git

### Prerequisities

* jdk 1.7
* Maven - Install with brew

```
To install brew
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"

To install maven
brew install mavem
```

* IntelliJ Community Edition

Install IntelliJ Community Edition from https://www.jetbrains.com/idea/download/

### Running the project

```
cd nationbuilder/initial
mvn spring-boot:run
```

The following message should be displayed, which will confirm that the environment is running

```
INFO 7828 --- [           main] nationbuilder.Application                : Started Application in 10.694 seconds (JVM running for 27.301)
```

http://localhost:8080/greeting should result in

```
{
"id": 1,
"content": "Hello, World!"
}
```

### Setting up IntelliJ to Run Tests 

* Open IntelliJ
* Select File > Open Project
* Select pom.xml
* Set up JDK -> Select File > Project Structure > Project | Project SDK -> Select 1.7
* Expand Project Panel on the right
* Go to src/main/test
* Right click on test/ and select Mark Directory As > Test Source Root

## Running the tests

* Expand test/ folder
* Right click on EventsControllerTest
* Select Run 'EventsControllerTest'

### Example API calls 

* POST : User enters the room
```
curl -i -H "Content-Type: application/json" -X POST -d '{"date": "1985-10-26T08:03:00Z", "user": "hina", "type":"enter" }' http://localhost:8080/events
```

Expected Output 
```
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Mon, 04 Apr 2016 04:10:15 GMT

{"status":"ok"}
```

* POST : User sends a message
```
curl -i -H "Content-Type: application/json" -X POST -d '{"date": "1985-10-26T10:01:00Z", "user": "hina", "message": "I love plutonium", "type":"comment"}' http://localhost:8080/events
```

Expected Output
```
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Mon, 04 Apr 2016 04:13:14 GMT

{"status":"ok"}
```

* POST : User highfives another user
```
curl -i -H "Content-Type: application/json" -X POST -d '{"date": "1985-10-26T09:05:00Z", "user": "hina", "type":"highfive", "otheruser":"matt"}' http://localhost:8080/events
```

Expected Output
```
http://localhost:8080/events
HTTP/1.1 400 Bad Request
Server: Apache-Coyote/1.1
Error-Message: otheruser doesn't exist
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Mon, 04 Apr 2016 04:15:29 GMT
Connection: close

{"status":"error"}
```

* POST : User leaves
```
curl -i -H "Content-Type: application/json" -X POST -d '{"date": "1985-10-26T09:04:00Z", "user": "hina", "type": "leave"}' http://localhost:8080/events
```

Expected Output
```
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Mon, 04 Apr 2016 04:16:36 GMT

{"status":"ok"}
```

* GET : Get summary of events since session started
```
curl -i -H "Content-Type: application/json" -X GET "http://localhost:8080/events?from=1984-10-26T08:00:00Z&to=1989-10-26T10:01:00Z"
```

Expected Output
```
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Mon, 04 Apr 2016 04:18:33 GMT

[
{
"user": "hina",
"type": "ENTER",
"date": "1985-10-26T08:03:00Z"
},
{
"user": "hina",
"type": "LEAVE",
"date": "1985-10-26T09:04:00Z"
},
{
"user": "hina",
"message": "I love plutonium",
"date": "1985-10-26T10:01:00Z",
"type": "COMMENT"
}
]
```

* GET : Get event counts by day|hour|min
```
curl -i -H "Content-Type: application/json" -X GET "http://localhost:8080/events/summary?from=1984-10-26T08:00:00Z&to=1989-10-26T10:01:00Z&by=day"
```

Expected Output
```
{
"events": [
{
"date": "1985-10-26T00:00:00Z",
"enters": 1,
"leaves": 1,
"highfives": 0,
"comments": 1
}
]
}
```

## Built With

* Spring-Boot 
* Maven 
* Java 7
* Spring MVC

## Authors

* **Hina Jain** - (https://github.com/hjain) 
