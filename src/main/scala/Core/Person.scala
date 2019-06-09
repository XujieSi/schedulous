package Core

case class Person(fname: String, lname: String, availability: Availability, local: Boolean) {
  def availableFor(dateslot: Dateslot) : Boolean = {
    if (local && dateslot.isTooEarly()) {
      println(dateslot + " is too early for " + fname + " " + lname)
      false
    }
    else{
     availability.schedule.foldLeft(false){ case (avail,(start,end)) =>
      val slotStartsBefore = dateslot.start.isBefore(start)
      val slotEndsAfter = dateslot.end.isAfter(end)
        avail || (!slotStartsBefore && !slotEndsAfter)
    }

//     availability.schedule.foldLeft(true){ case (avail,(start,end)) =>
//      val slotStartsBefore = dateslot.end.isBefore(start)
//      val slotEndsAfter = dateslot.start.isAfter(end)
//        avail && (slotStartsBefore || slotEndsAfter)
//    }

    }

  }

  def canonicalName: String = (fname + lname).replaceAll("[^a-zA-Z]", "")

  override def hashCode(): Int = {
    ((this.fname.hashCode.toLong + this.lname.hashCode.toLong) % Int.MaxValue).toInt
  }

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case p: Person =>
        this.fname == p.fname && this.lname == p.lname
      case _ => false
    }
  }

  override def toString: String = fname + " " + lname
}
