import Constraints.{ConsFairWorkload, ConsNoConcurrentSlots, _}
import Core._
import Readers._

object SchedulousDemoApp extends App {
  // paths
  val volPath = "/Users/dbarowy/OneDrive/UMass/Volunteer/PLDI 2016/assignment_data/volunteers.csv"
  val evtPath = "/Users/dbarowy/OneDrive/UMass/Volunteer/PLDI 2016/assignment_data/events.csv"

  // constraint config
  val MAXSLOTS  = 3
  val MINSLOTS  = 1
  val MAXDAYS   = 2
  val MINUTEEPS = 90

  // load data
  val (availablePeople: Set[Person],reservedPeople: Set[Person]) = VolunteerCSVReader(volPath).people(false)
  val (unfilledSlots,filledSlots) = AssignmentCSVReader(evtPath).assignments(availablePeople.union(reservedPeople))

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
//      c2,
//      c3,
      c4,
      c5,
      c6,
      c7
    )
  }

  // find schedule
  val schedule = Schedule.find(conf, availablePeople, unfilledSlots.days)

  // print schedule
  schedule match {
    case Some(s) =>

      // add filled slots back in
      val merged = s.merge(filledSlots)

      // sanity check
      assert(merged.assignments.forall { a =>
        val actuallyAvailable = a.person.availableFor(a.slot)
        if (!actuallyAvailable) {
          filledSlots.assignments.contains(a)
        } else {
          true
        }
      })

      println("\nSCHEDULE:\n")
      println(merged)
      println("\nWORKLOADS:\n")
      merged.people.foreach { p =>
        println(p + ", APPROVED: " + merged.workloadFor(p, Approved))
        println(p + ", PROPOSED: " + merged.workloadFor(p, Proposed))
      }
    case None => "Cannot find schedule that meets constraints."
  }
}