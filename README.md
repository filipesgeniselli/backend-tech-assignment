# Listings API
Implement a REST service for managing listings for online advertising service.

## Terminology

- **Listing** - a vehicle advertisement. Listing can be in one of two possible states:
    - published - available online
    - draft - not available online
- **Car Dealer** - an owner of the advertisement
- **Tier Limit** - a number of published listings a dealer can have online

The API was build with Java 17, Spring-boot and Maven as build tool.

## Requirements
- **Docker** - used to create postgres instance for integration tests and running the final version
- **Maven** - used to build the API
- **Postgres** - optional, only required if docker is unavailable

Service should implement following functionality:

- Create a listing. 
  - All the created listings should have state `draft` by default;
- Update a listing;
  - Only listing in `draft` state can be edited
- Get all listings of a dealer with a given state;
- Publish a listing;
  - Verify the dealer tier limit
    - Return an error to the client or
    - Publish the listing, but unpublish the oldest listing of a dealer to conform to the tier limit. 
- Unpublish a listing.

# Dealer specification

## Model
- id: UUID
    - Auto generated on insert and returned on Location header
- name: String
    - The name of the Dealer
- DealerTierLimit tier
    - Tier limit for the published Listings validation:
        - Free: 3 listings
        - Basic: 10 listings
        - Premium: 20 listings
        - Business: 100 listings
- allowRemovingOldListings:Boolean
    - What to do when dealer reaches the limit
        - True: remove old published listings and publish the new one
        - False: return a 409(Conflict) status code when trying to publish a listing

## Endpoints
### GET /dealer
Get the list of registered dealers, returns a paged response with the requested data
Query parameters:
- **name** - Filter of type contains that will look for dealers with the specified name
- **page** - Page index to get the results(zero-based) - Defaults to `0`
- **pageSize** - Amount of records to be returned per page - Defaults to `20`

Response result:
- **200(OK)** - Paged response with the list of dealers

### GET /dealer/{id}
Get the single dealer with the selected ID
Path parameters:
- **id** The UUID of the Dealer

Response result:
- **200(OK)** - The details of the requested Dealer
- **400(Bad request)** - If the ID is not a valid UUID
- **404(Not found)** - If the api didn't find the Dealer with the requested ID

### POST /dealer
Creates a new dealer and returns the UUID
Body data:
- **name**: String - Required, the name of the dealer
- **tier**: DealerTierLimit - Required, the tier limit of the dealer
  - FREE
  - BASIC
  - PREMIUM
  - BUSINESS
- **allowRemovingOldListings**: Boolean - Required, what to do when dealer reaches the limit of published listings

Response Result
- **201(Created)** - Empty response with the Location header containing the url to access the resource
- **400(Bad request)** - When any required value is not sent

### PUT /dealer/{id}
Updates the dealer information
Path parameters:
- **id** - The UUID of the dealer
Body data:
  Creates a new dealer and returns the UUID
  Body data:
- **name** - Required, the name of the dealer
- **tier** - Required, the tier limit of the dealer
    - FREE
    - BASIC
    - PREMIUM
    - BUSINESS
- **allowRemovingOldListings** - Required, what to do when dealer reaches the limit of published listings

Response Result
- **202(Accepted)** - Empty response, the values are updated
- **400(Bad request)** - When any required value is not sent

# Listings specification

## Model
- id: UUID
  - Auto generated on insert and returned on Location header
- dealerId: UUID
  - Reference of the dealer for this listing
- vehicle: String
  - Description of the vehicle to be listed
- condition: VehicleCondition
  - The current condition for the vehicle
    - NEW
    - USED
- price: BigDecimal
  - The selling price for this listing
- color: String
  - Color name of the vehicle
- transmission: VehicleTransmission
  - The type of transmission of this vehicle
    - MANUAL
    - AUTOMATIC
- mileage: Integer
  - How many miles this vehicle has
- fuelType: VehicleFuelType
  - The type of fuel the vehicle uses
    - GASOLINE
    - HYBRID: Gasoline / Electrical
    - ELECTRICAL
- status: ListingStatus
  - The current status of the listing
    - DRAFT: Initial status, changes permitted
    - PUBLISHED: Final state, no changes permitted
    - REMOVED: Listing removed, inactive
- createdAd: DateTime
- publishedAt: DateTime
- removedAt: dateTime
  - Dates to register the moment a status change occurred

## Endpoints
### GET /{dealerId}/listings/
Get the list of listings with a given state, returns a paged response with the requested data
Path parameters:
- **dealerId** The UUID of the Dealer
Query parameters:
- **status** - Valid available filters to query (DRAFT, PUBLISHED, REMOVED) 
- **page** - Page index to get the results(zero-based) - Defaults to `0`
- **pageSize** - Amount of records to be returned per page - Defaults to `20`

Response result:
- **200(OK)** - Paged response with the list of dealers
- **400(BadRequest)** - If the dealerId is not a valid UUID

### GET /{dealerId}/listings/{listingId}
Get the single listing with the selected ID
Path parameters:
- **dealerId** The UUID of the Dealer
- **id** The UUID of the Listing

Response result:
- **200(OK)** - The details of the requested Listing
- **400(Bad request)** - If the dealerId or the Id is not a valid UUID
- **404(Not found)** - If the api didn't find the Listing with the requested ID

### POST /{dealerId}/listings/
Creates a new listing and returns the UUID
Path parameters:
- **dealerId** - The UUID of the Dealer
Body data:
- **vehicle**: String - Required, the vehicle description
- **condition**: VehicleCondition - Required, the vehicle condition
  - NEW
  - USED
- **price**: BigDecimal - Required, the listed price, greater than 0
- **color**: String - Optional, the color of the vehicle
- **transmission**: VehicleTransmission - Optional, the type of transmission
  - MANUAL
  - AUTOMATIC
- **mileage**: Integer - Optional, current vehicle mileage
- **fuelType**: VehicleFuelType - Optional, type of fuel of the vehicle
  - GASOLINE
  - HYBRID
  - ELECTRIC

Response Result
- **201(Created)** - Empty response with the Location header containing the url to access the resource
- **400(Bad request)** - When any required value is not sent
- **404(Not found)** - When the dealerId is not found on the database

### PUT /{dealerId}/listings/{listingId}
Updates a listing information
Path parameters:
- **dealerId** - The UUID of the Dealer
- **listingId** - The UUID of the Listing
Body data:
- **vehicle**: String - Required, the vehicle description
- **condition**: VehicleCondition - Required, the vehicle condition
    - NEW
    - USED
- **price**: BigDecimal - Required, the listed price, greater than 0
- **color**: String - Optional, the color of the vehicle
- **transmission**: VehicleTransmission - Optional, the type of transmission
    - MANUAL
    - AUTOMATIC
- **mileage**: Integer - Optional, current vehicle mileage
- **fuelType**: VehicleFuelType - Optional, type of fuel of the vehicle
    - GASOLINE
    - HYBRID
    - ELECTRIC

Response Result
- **202(Accepted)** - Empty response, the values are updated
- **400(Bad request)** - When any required value is not sent
- **404(Not found)** - When the dealerId is not found on the database

### PATCH /{dealerId}/listings/{listingId}
Updates the status of a listing
Path parameters:
- **dealerId** - The UUID of the Dealer
- **listingId** - The UUID of the Listing
Body data:
- **status**: ListingStatus - Required, the new status for the listing
  - DRAFT: Moves the listing to the DRAFT status
  - PUBLISHED: Moves the listing to the PUBLISHED status
    - If the dealer reaches the limit of published listings the API will take an action based on the dealer configuration
      - Remove the oldest listing and publish the new; or
      - Return a 409 conflict exception
  - REMOVED: Inactivate a listing, removing from PUBLISHED/DRAFT status
    - This is the final state, any changes to a listing on REMOVED state will cause a 400 Bad Request error

Response Result
- **202(Accepted)** - Empty response, the status is updated
- **400(Bad request)** - When the status change is not permitted
- **404(Not found)** - When the dealerId or listingId is not found on the database
- **409(Conflict)** - When the dealerId does not permit an automatic status change on published listing

# Running and testing

## Testing
Currently, there's 2 options for testing phases:
- Local tests with H2 in-memory database
- Integration tests with a second postgres instance

To run all tests simply run the following commands:

### Option 1 - Local tests
```bash
mvn clean verify
```

### Option 2 - Integration tests
Start a new postgres instance
```bash
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=Admin1234" -p 5555:5432 --name postgresdb-test -e POSTGRES_USER=listings -e POSTGRES_PASSWORD=mysecretpassword -e POSTGRES_DB=listings-test -d postgres
```

Run the tests
```bash
mvn clean verify "-Dspring.profiles.active=test-it"
```

## Run the API
The API requires a postgres instance with an empty database, you can create a new one with the following command:

### Linux
```bash
export DB_PORT=5556
export DB_NAME=listings
export DB_USERNAME=api
export DB_USER_PASSWORD=mysecret
export DB_URL=jdbc:postgresql://localhost:$DB_PORT/$DB_NAME

docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=Admin1234" -p $DB_PORT:5432 --name postgresdb -e POSTGRES_USER=$DB_USERNAME -e POSTGRES_PASSWORD=$DB_USER_PASSWORD -e POSTGRES_DB=$DB_NAME -d postgres
```

### Windows
```bash
$Env:DB_PORT=5556
$Env:DB_NAME="listings"
$Env:DB_USERNAME="api"
$Env:DB_USER_PASSWORD="mysecret"
$Env:DB_URL="jdbc:postgresql://localhost:$Env:DB_PORT/$Env:DB_NAME"

docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=Admin1234" -p ${Env:DB_PORT}:5432 --name postgresdb -e POSTGRES_USER=${Env:DB_USERNAME} -e POSTGRES_PASSWORD=${Env:DB_USER_PASSWORD} -e POSTGRES_DB=${Env:DB_NAME} -d postgres
```

And run the program with the following command:
```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=prod"
```