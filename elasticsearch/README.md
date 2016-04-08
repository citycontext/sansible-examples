# Elasticsearch Cluster

Sansible playbook for provisioning up a secure Elasticsearch cluster, configured to 
run securely through a Tinc VPN.

## Setup instructions

In order to run this example you will need a Digital Ocean API token. To configure
the app with your token, create an `application.conf` file like the following, and place it in
the project resource folder (e.g `elasticsearch/src/main/resources/application.conf`)

```
digitalocean.token = "..."
```

The playbook will use the token to fetch your list of Digital Ocean droplets.
The droplets to be used for the cluster are whitelisted through a naming convention: they should be prefixed with `es`
followed by an integer from 1 to 255. For instance, to provision a cluster of three nodes,
you will have to create three droplets and name them `es1`, `es2`, and `es3`.

**Note:** The playbook assumes that you have installed Ansible 2.0, you are using the Ubuntu 14.04 image for your droplets,
you have enabled the private networking functionality, and you have setup them up with a valid SSH key.

## Running the playbook

    sbt elasticsearch/run
