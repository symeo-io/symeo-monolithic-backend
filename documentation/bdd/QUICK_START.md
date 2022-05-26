#Démarrage rapide

## Prendre en compte la configuration externalisée
 `-Dspring.config.location={PATH_GGL_CONFIG}/application.yml`

## Démarer un PostgreSQL en local
Installer PostgreSQL sur sa machine ou bien passer par docker ou bien par le docker-compose:

### docker-compose:
 - Aller dans le dossier docker.

 - lancer la commande: $ `docker volume create postgres_data_volume_catlean-delivery-processor`

 - lancer la commande : $ `docker-compose -f postgres_catlean-delivery-processor.yml up -d`

La base de données se lance avec le port 5432.

### docker:
`docker run --name postgres -e POSTGRES_PASSWORD=P@ssw0rd -e POSTGRES_USER=postgres -e POSTGRES_DB=catlean-delivery-processor -p 5432:5432 -d postgres`

## Changer le niveau de Logs et le format
Le projet se base sur logback pour gerer le niveau d'erreur ainsi que le format
Par défaut il l'affiche en ligne standard avec un niveau INFO
il est possible de changer le format de logs ainsi que le niveau en utilisant les variables :
`LOG_LEVEL` (DEBUG,INFO ...) et `LOG_FORMAT` (default, json, cloud)
`LOG_LEVEL=debug LOG_FORMAT=json mvn spring-boot:run`

## Le projet avec Docker
Pour packager le programme dans une image docker, il est possible d'éxecuter le script `./package.sh` ou de suivre les commandes qu'il décrit.

## Lancer le projet dans Docker et communiquer avec une base PostgreSQL locale
Afin de pouvoir communiquer avec la base de données hébérgé sur le localhost il faut ajouter lors de la création du container le flag `--network host`
afin de dire a docker que le container se trouve sur le meme réseau que la machine hôte, cela évitera les problemes de communications.

`docker run --network host catlean-delivery-processor`
