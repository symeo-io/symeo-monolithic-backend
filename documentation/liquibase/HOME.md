# PostgreSQL, Liquibase Best Practices & Liquibase Tips & Tricks

**Database schema can not be managed by both JPA / Hibernate and Liquibase**. We must therefore make a choice. So we head towards Liquibase.
who becomes the master of the modifications made to the database: structure + data.

**So we must not let JPA / Hibernate change the database state** (it's structure):
~~~
    spring.liquibase.enabled=true
    spring.jpa.hibernate.ddl-auto=none
~~~

Liquibase is automatically executed when the application starts, and executes the `changeset` contained in its changelogs file:` master-changelog.yaml`.

## Commands to remember
- gcloud sql connect : connect to gcloud sql: attention, the installation of the postgres-client is mandatory: sudo apt-get install postgres-client
- \dt : commande psql pour describe tables
- \d partner_infos : commande psql pour describe table partner_infos

## Best practices
- Rename changesets and tag the file at the end by incrementing version
- /!\ Explicitly name all foreign key constraints, indexes, uniqueness constraints ...
- (optional) Test the rollback of a new changelog


# Make changes to the database structure
As a developer, if you want to make any changes to the database structure, you should follow this process:

- Make the changes you have to make on the classes annotated `@ Entity`;

- Run the command: $ `mvn clean install` on the parent

- Create a file in the changelog folder of liquibase with the following pattern:[version]_[quick_changelog_description].yaml.The following format should be followed: for the version a 4-digit number. For the description, lowercase words separated by an underscore.

- Write the changelog with all the changesets it should contain. **/!\ WARNING** : do not forget to "tag" the end of your file in order to facilitate the management of rollbacks for our database:
~~~
    - changeSet:
      id: tag-v00000001
      author: Alexis Vachard
      changes:
        - tagDatabase:
            tag: v00000001
~~~

- Include the changelog file in the liquibase master changelog:

~~~
	- include:
        file: db/liquibase/changelog/[version]_[quick_changelog_description].yaml
~~~

- Start the application to apply changesets on database's structure.

# Liquibase tips & tricks

## Useful commands for Liquibase

 - $ `mvn liquibase:rollback -Dliquibase.rollbackTag=vxxxxxxxx` : allows a rollback on the database by returning to a state given by a liquibase tag. For example : $ `mvn liquibase:rollback -Dliquibase.rollbackTag=v0001`.

 - $ `mvn liquibase:rollback -Dliquibase.rollbackCount=n` : allows you to rollback for the n desired changesets. For example : $ `mvn liquibase:rollback -Dliquibase.rollbackCount=1`.

 - $ `mvn liquibase:update -Dliquibase.toTag=vxxxxxxxx` : pallows you to revert to a more recent version of the database based on the tags present in our changelogs. For example : $ `mvn liquibase:update -Dliquibase.toTag=v00000003`

 - $ `mvn liquibase:tag -Dliquibase.tag='vxxxxxxxx'` : Used to tag the state of the database at a T time.
 
 - $ `mvn liquibase:update` : Allows you to execute changesets that have not yet been played without launching the application. Create the two liquibase tables if they do not exist.

 - $ `mvn liquibase:updateTestingRollback` : this command executes all the changes not applied to the database and will then rollback these changes in order to return to the current state of the database. This command is very useful for testing our rollback strategy without permanently changing the database.


## How to tag a database version and why


In order to allow a better management of the versions of our database, it will be advisable to tag our changelogs via a changeset.
This method makes it possible to go back to a previous version or to start over with a more recent version in a much simpler way.

To tag a changelog, we should add a changeset at the end of our changelogs file as follows:

```
- changeSet:
  id: tag-v00000000
  author: Alexis Vachard
  changes:
    - tagDatabase:
        tag: v00000000
```

## How to add a unique constraint

In order to have a unique constraint on a column, you should follow this changeset format:
```
- changeSet:
  id: add_unique_constraint_on_column_society_name_on_table_society
  author: Alexis Vachard
  changes:
    - addUniqueConstraint:
        columnNames: id
        constraintName: unique_society_id
        tableName: society
```