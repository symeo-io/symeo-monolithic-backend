#!/bin/bash

set -e

function prepare_github_private_key(){
    echo $GITHUB_PRIVATE_KEY_PEM_BASE_64 | base64 -d >> /tmp/catlean-io.private-key.pem
    openssl pkcs8 -topk8 -inform PEM -outform DER -in /tmp/catlean-io.private-key.pem -out $1/catlean-io.private-key.der -nocrypt
}