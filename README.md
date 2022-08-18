# symeo-monolithic-backend





## Github-adapter

To generate private key from pem file, one has to convert the pem file to a der file compliant with the JVM ecosystem
using the following command:

`openssl pkcs8 -topk8 -inform PEM -outform DER -in symeo-staging-io.private-key.pem -out symeo-staging-io.private-key.der -nocrypt`