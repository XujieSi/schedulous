Schedulous is intended to make scheduling easier.  You provide a set of available workers and a set of timeslots to Schedulous (both in CSV format), along with a set of constraints you want met, and Schedulous finds a schedule for you.

# prerequisites

You will need the following software installed:

1. [Scala](https://www.scala-lang.org/) 
2. [SBT](https://www.scala-sbt.org/) 
3. [Z3](https://github.com/Z3Prover/z3/releases) (no need to rebuild as long as the command `z3` is available)

# how to use

After you have installed the above prerequisites, you will need to create two CSVs.

1. You will need a `events.csv` file containing all of the timeslots you need filled, one per line.  It must have the at least the following header fields in any order: `date,start,end,duration,event,role,approval,person`.  Extra header fields will be ignored.
2. You will need a `workers.csv` file containing all of the workers and their availability, one worker per line.  It must have at least the following header fields in any order: `First Name,Last Name` as well as a set of fields indicating availability, e.g., `Available Tue 6/14`.
3. For now, you will need to create/modify the `VolunteerCSVReader` to indicate how to interpret the `Available [date]` fields.  See [here](https://github.com/XujieSi/schedulous/blob/pldi18/src/main/scala/Readers/VolunteerCSVReader.scala).
4. Create a Scala program that reads in your event and worker data files, sets Schedulous configuration options, and then produces a schedule.  Feel free to modify the example [here](https://github.com/XujieSi/schedulous/blob/pldi18/src/main/scala/SchedulousDemoApp.scala).
5. Run your program (e.g. `sbt run`).


# features

The set of currently available constraints are:

1. Ensure a "fair" workload.  All workers work the same number of hours +/- `e` of minutes (user-definable).
2. Ensure that all timeslots are filled.
3. Limit the total number of days that a worker works.
4. Ensure that workers have at most `n` assignments (user-definable).
5. Ensure that workers have at least `n` assignments (user-definable).
6. Ensure that workers only work 1 assignment at a time (i.e., no concurrent assignments).
