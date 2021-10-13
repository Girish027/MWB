curl -k -v -XPOST -H "Content-Type: application/json" -d '{"username": "brandon.flowers", "lastName": "Flowers", "email": "brandon.flowers@247-inc.com", "firstName": "Brandon", "state": "ENABLED"}' https://localhost:8443/nltools/v1/users
curl -k -v -XPOST -H "Content-Type: application/json" -d '{"name": "TestClient", "description": "Test Client" }' https://localhost:8443/nltools/v1/clients

