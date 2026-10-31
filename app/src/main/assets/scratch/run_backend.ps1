$env:DB_USERNAME=${DB_USERNAME}
$env:DB_PASSWORD=${DB_PASSWORD}
Set-Location "C:\Projects\Enersense"
java -Dserver.port=8080 -jar target/enersense-backend-0.0.1-SNAPSHOT.jar
