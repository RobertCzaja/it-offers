# Installation

1. create SSL `jks` key in main catalog 
   * `keytool -keystore keystore.jks -genkey -alias tomcat -keyalg RSA`
   * put password that you've used in `.env` `SSL_PASSWORD`
2. generate JWT Token secret in `.env` `JWT_SECRET`
3. run `docker compose up` to start DB