# Sansible Examples

Sample playbooks illustrating [Sansible](http://github.com/citycontext/sansible),
a DSL to generate and run AnsiblePlaybooks using the Scala programming language.

Playbooks are implemented as separate sbt projects, with shared concerns (e.g. installing
Oracle jre-8) factored out into a `common` dependency. This build currently contains the following
sub-projects:

### [Gitbucket](https://github.com/citycontext/sansible-examples/tree/master/gitbucket)

A minimalistic playbook showing how to provision a working instance of [Gitbucket](https://github.com/gitbucket/gitbucket): a fully featured, open source Github clone written in Scala.

### [Elasticsearch Cluster](https://github.com/citycontext/sansible-examples/tree/master/elasticsearch)

A slightly more complex playbook which sets up an Elastisearch cluster and configures it
to run securely within a Tinc VPN. The project also illustrates how to generate an inventory
dynamically using the Digital Ocean API. Configuration follows the steps detailed in
[Digital Ocean wiki](https://www.digitalocean.com/community/tutorials/how-to-set-up-a-production-elasticsearch-cluster-on-ubuntu-14-04).
Please refer to the sub-project README for set up instructions..
