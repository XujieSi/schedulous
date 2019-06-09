import Constraints.{ConsFairWorkload, ConsNoConcurrentSlots, _}
import Core._
import Readers._

object SchedulousDemoApp extends App {
  // paths
  val volPath = "dropbox/pldi-sv/avail.csv"
  val evtPath = "dropbox/pldi-sv/events.csv"

  // constraint config
  val MAXSLOTS  = 5
  val MINSLOTS  = 1
  val MAXDAYS   = 3
  val MINUTEEPS = 30

  // load data
  val (availablePeople: Set[Person],reservedPeople: Set[Person]) = VolunteerCSVReader(volPath).people(false)
  println("available: " + availablePeople.size)
  println("reserved: " + reservedPeople.size)

  val (unfilledSlots,filledSlots) = AssignmentCSVReader(evtPath).assignments(availablePeople.union(reservedPeople))

  println("unfilledSlots.days: " + unfilledSlots.days)
  println("filledSlots.days: " + filledSlots.days)

  println("assignments are ready")
  // constraint config
  val conf = (peopleMap: People#PeopleMap, slotMap: Timeslots#SlotMap, oldSchedule: Option[Schedule]) => {
    val c1 = ConsFillSlots(peopleMap, slotMap, oldSchedule)
    val c2 = ConsMaxSlots(MAXSLOTS, peopleMap, slotMap)
    val c3 = ConsMinSlots(MINSLOTS, peopleMap, slotMap)
    val c4 = ConsMaxDays(MAXDAYS, unfilledSlots.days, peopleMap, slotMap)
    val c5 = ConsWorkload(peopleMap, slotMap)
    val c6 = ConsFairWorkload(MINUTEEPS, c5.name, peopleMap, slotMap)
    val c7 = ConsNoConcurrentSlots(peopleMap, slotMap)

    List(
      c1,
      //c2,
      //c3,
      //c4,

      c5,
      c6,
      c7
    )
  }

  // find schedule
  val schedule = Schedule.find(conf, availablePeople, unfilledSlots.days)

  //println("schedule:" + schedule)
  // print schedule
  schedule match {
    case Some(s) =>

      // add filled slots back in
      val merged = s.merge(filledSlots)

      // sanity checks
      // 1. every assignment is assigned to an actually-available person
      assert(merged.assignments.forall { a =>
        val actuallyAvailable = a.person.availableFor(a.slot)
        if (!actuallyAvailable) {
          filledSlots.assignments.contains(a)
        } else {
          true
        }
      })

      // 2. every person has an assignment
      assert(merged.people.forall { p =>
        merged.assignments.exists { a => a.person == p }
      })

      println("\nSCHEDULE:\n")
      //println(merged)
      merged.agg.foreach {
        case (key,svs) =>
          val names = svs.foldLeft("") {
            case (res, person) =>
              res + "\t" + person.fname + " " + person.lname
          }
          printf("%s%s\n", key, names)
      }

      println("\nWORKLOADS:\n")
      merged.people.foreach { p =>
        //println(p + ", APPROVED: " + merged.workloadFor(p, Approved))
        //println(p + ", PROPOSED: " + merged.workloadFor(p, Proposed))
        println(p + "\t" + merged.workloadFor(p, Proposed))
      }
    case None => println("Cannot find schedule that meets constraints.")
  }

  println("job is done")
}
