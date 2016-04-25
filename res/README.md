# Running PSQL

    cd res/vagrant
    vagrant up

PostegreSQL server is running on localhost on port 5432 (default).

  * Username: `vlad`
  * Password: `vlad`
  * Database: `store`

In order to stop the server:

    cd res/vagrant
    vagrant destroy


In order to connect to the database from your local machine with 'psql' with password above:

    psql -U vlad -h localhost store
