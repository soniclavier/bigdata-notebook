---
layout: post
comments: true
title: QueryableStates in ApacheFlink - How it works
date: 2017-03-19
PAGE_IDENTIFIER: flink_queryable_state*
permalink: /flink_queryable_state1.html
image: /img/flink_queryable_state/queryable_flow.png
tags: ApacheFlink BigData Hadoop Scala Streaming
description: QueryableStates allows users to do real-time queries on the internal state of the stream without having to store the result on to any external storage. In this blog post we will see how this is done in ApacheFlink.
---
<div class="col three">
    <img class="col three" src="/img/flink_queryable_state/header.png">
</div>
QueryableStates allows users to do real-time queries on the internal state of the stream without having to store the result on to any external storage. This opens up many interesting possibilities since we no longer need to wait for the system to write to the external storage (which has always been one of the main bottlenecks in these kinds of systems). It might be even possible to not have any kind of database and make the user facing applications directly query the stream, which will make the application faster and cheaper. This might not be applicable to all the use cases, but if your pipeline has to maintain an internal state (may be to do some aggregations), it would be a good idea to make the state available to query. 

We will first look at the overall steps that take places inside Flink when we make a state queryable, and when we do the query. In the next blog, we will see how to create a Pipeline with queryable state and how to create a client to query its state.

### **Making the State Queryable**
Let us assume that we have created a pipeline with a queryable state and submitted the Job via JobClient. The following diagram shows what happens inside Flink.
<div class="col three">
    <img class="col three expandable" src="/img/flink_queryable_state/queryable_flow.png">
</div>
I hope the figure is pretty much self-explanatory but to sum up, once a Job is submitted, JobManager builds ExecutionGraph from the JobGraph and then deploys the tasks to TaskManager. While creating instances of the Tasks, Operators are created, if an Operator is found to be queryable then reference to the "state" of the operator is saved in KvStateRegistry with a state name. The state name is a unique name that is set during the creation of the Job. Then the JobManager actor is notified about the state registration and JobManager stores the location info in a KvStateLocationRegistry, which is later used during the time of querying.

### **Querying the state**
<div class="col three">
    <img class="col three expandable" src="/img/flink_queryable_state/queryable_flow2.png">
</div>
The above figure shows the steps during execution of a query by a client (who is not part of the submitted Job). The client sends a KvStateLookup message to JobManager actor, this request should contain the JobId and "state name" which was used while building the Job. JobManager checks if the JobId is valid, gets the JobGraph for the JobId and the KvStateLocationRegistry is retrieved from the JobGraph. JobManager then returns the state location information corresponding to the "state name" back to KvStateClient. This response contains a KvStateServer address of where the state is stored. The client then opens a connection with the KvStateServer and fetches the state from the registry using the KvStateID. Once the state is retrieved an Asynchronous query is submitted to fetch the value from the state for a given key. The result obtained is serialized and sent back to the client. Meanwhile, the state is continuously updated by the Job during processing and therefore the client always gets to see the latest state value while querying.

So this is what Apache Flink does under the hood to make its state queryable. In the next part of the blog, we will implement a Streaming Job which exposes its state via QueryableState API and will also create a QueryClient to query this state. Thanks for reading!
<br/><a href="http://vishnuviswanath.com/" class="end-of-page">Home</a>