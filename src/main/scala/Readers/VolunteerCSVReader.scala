package Readers

import java.io.File
import java.time.Month

import Core.{Availability, Person}
import com.github.tototoshi.csv.CSVReader

case class VolunteerCSVReader(filename: String) {
  private val reader = CSVReader.open(new File(filename))
  private val raw: List[Map[String, String]] = reader.allWithHeaders()

  private def availability(row: Map[String,String], tentativeMeansAvailable: Boolean) : Availability = {
    val sun = "Available Sun 6/17"    -> Availability.AllDay(2018, Month.JUNE, 17)
    val mon = "Available Mon 6/18"    -> Availability.AllDay(2018, Month.JUNE, 18)
    val tue = "Available Tue 6/19"    -> Availability.AllDay(2018, Month.JUNE, 19)
    val wed = "Available Wed 6/20"    -> Availability.AllDay(2018, Month.JUNE, 20)
    val thu = "Available Thurs 6/21"  -> Availability.AllDay(2018, Month.JUNE, 21)
    val fri = "Available Fri 6/22"    -> Availability.AllDay(2018, Month.JUNE, 22)

    val days = List(sun, mon, tue, wed, thu, fri)

    // compute availability
    days.foldLeft(Availability.NotAvailable){ case (acc,(key,avail)) =>
        if (row(key) == "Y"
            || (if (tentativeMeansAvailable) { row(key) == "T" } else { false })) {
          acc + avail
        } else {
          acc
        }
    }
  }

  def people(tentativeMeansAvailable: Boolean) : (Set[Person],Set[Person]) = {
    val allPeople = raw.flatMap { row =>
      if (row("Can Serve (confirmed)") == "Y") {
        val avail = availability(row, tentativeMeansAvailable)

        Some(
          Person(
            row("First Name"),
            row("Last Name"),
            avail,
            row("Location") == "L"
          )
        )
      } else {
        None
      }
    }.toSet

    allPeople.partition { person => person.availability != Availability.NotAvailable }
  }
}
