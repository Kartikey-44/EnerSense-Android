$env:DB_USERNAME="kartikey"
$env:DB_PASSWORD="Sarita@1502"
Set-Location "C:\Projects\Enersense"
java -Dserver.port=8080 -jar target/enersense-backend-0.0.1-SNAPSHOT.jar
