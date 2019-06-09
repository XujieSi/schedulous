package Constraints

import java.time.Duration

import Core.{Approved, Schedule}
import smtlib.parser.Commands.{Assert, Command, DefineFun, FunDef}
import smtlib.parser.Terms._
import smtlib.theories.Core.{Equals, ITE}
import smtlib.theories.Ints.{Add, IntSort, NumeralLit}

//import smtlib.theories.Reals.{DecimalLit, RealSort}

// Constraint #5: Helper function to compute a person's workload in minutes.
case class ConsWorkload(peoplemap: People#PeopleMap, slotmap: Timeslots#SlotMap) extends Constraint {
  val (fname,fdef) = init()

  private def init() : (SSymbol,DefineFun) = {
    val fname = SSymbol(this.getClass.getName)
    val arg_person = SortedVar(SSymbol("person"), IntSort())

    val exprReducer = (lhs: Term, rhs: Term) => smtlib.theories.Ints.Add(lhs,rhs)

    val literals = slotmap.map { case (symb,slot) =>
      ITE(
        Equals(
          QualifiedIdentifier(SimpleIdentifier(symb)),
          QualifiedIdentifier(SimpleIdentifier(arg_person.name))
        ),
        //DecimalLit(Duration.between(slot.start,slot.end).toMinutes.toDouble),
        //DecimalLit(0)

        NumeralLit(Duration.between(slot.start,slot.end).toMinutes.toInt),
        NumeralLit(0)
      )
    }.toSeq

    val expr = literals.reduce(exprReducer)
    //val fdef = DefineFun(FunDef(fname, Seq(arg_person), RealSort(), expr))
    val fdef = DefineFun(FunDef(fname, Seq(arg_person), IntSort(), expr))

    (fname, fdef)
  }

  def asserts: List[Assert] = List.empty
  def definition: List[Command] = List(fdef)
  def name: SSymbol = fname
}
