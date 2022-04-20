# Starlify connector for Kong gateway
Exports the API details to Starlify as systems, services and flows.

## Dependencies
   Java-8 +
   
### spring-boot-starter-web
For exposure of connector etc. on http.

## Configuration
Put the text below in your property file to configure your URL for Kong API gateway and Starlify:

```
		kong:
		  server:
			url: http://localhost:8001 
		starlify:
		  url: https://api.starlify.com
```
 
## Start
Start with copying the project using the link below:
     https://github.com/entiros/starlify-kong-connector.git

Go to cleaned location and run the command below to start the process:
	mvn clean spring-boot:run

## import kong api details to Starlify
Use the endpoint below to start importing API details to Starlify as services, systems and flows 

```
	Method : POST
	URL : http://localhost:8080/submitRequest
	Body : 
			{
				"starlifyKey":"starlify-api-key",
				"apiKey":"kong-api-key",
				"networkId":"starlify-network-id-to-create-services-systems-and-flows"
			}
```
