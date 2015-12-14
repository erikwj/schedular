# schedular

## Description
Small play application to schedule the generation of reporst. A more general purpose could be to schedule remote webcalls and handle the response. 

A report can be scheduled or scheduled to be scheduled if the startdate is out of scope of the schedule chosen, e.g. a weekly schedule that starts 3 weeks from now. 

The cron jobs are backup with a Redis client that stores the request and can replay them when needed. 

