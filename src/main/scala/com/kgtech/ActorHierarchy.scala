package com.kgtech

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._

object ActorHierarchy {
  def main(args: Array[String]) = {
    println("ActorHierarchy Running...")
    val system = ActorSystem("theActorSystem")
    val actor1 = system.actorOf(Props[ParentActor], "actor1")
    actor1 ! CreateChild
    actor1 ! SendPrintSignal(1)
    actor1 ! CreateChild
    actor1 ! CreateChild
    actor1 ! CreateChild
    actor1 ! SendPrintSignal(2)

    val actor2 = system.actorOf(Props[ParentActor], "parentActor2")
    actor2 ! CreateChild
    val childActor0 = system.actorSelection("akka://theActorSystem/user/parentActor2/childActor-0")
    childActor0 ! PrintMessage("Hello from ActorPath[Selection]")
    childActor0 ! DivideNumbers(10, 0)
    childActor0 ! DivideNumbers(10, 2)
    childActor0 ! BadStuff
    //system.terminate()
  }

  case object CreateChild
  case class SendPrintSignal(nul: Int)
  case class PrintMessage(message: String)
  case class DivideNumbers(n: Int, d: Int)
  case object BadStuff
  class ParentActor extends Actor {
    var num = 0
    def receive = {
        case SendPrintSignal(num) => {
          context.children.foreach(_ ! PrintMessage("Hello: "+num))
        }
        case CreateChild => {
          context.actorOf(Props[ChildActor], "childActor-"+num)
          num += 1
        }
    }

    override val supervisorStrategy = OneForOneStrategy(loggingEnabled = false) {
      case ae: ArithmeticException => Resume
      case _: Exception => {
        println("Exception happened")
        Restart
      }
    }
  }

  class ChildActor extends Actor {
    println("Child actor created...")
    def receive = {
        case PrintMessage(message) => {
            println(s"message: $message from $self")
        }
        case DivideNumbers(n, d) => println(s"Result = ${n/d}")
        case BadStuff => throw new RuntimeException("Something went wrong!")
    }

    override def preStart() = {
      super.preStart()
      println("Child actor started...")
    }
    override def postStop() = {
      super.postStop()
      println("Child actor stopped...")
    }
    override def preRestart(reason: Throwable, message: Option[Any]) = {
      super.preRestart(reason, message)
      println("Child actor being restarted...")
    }
    override def postRestart(reason: Throwable) = {
      super.postRestart(reason)
      println("Child actor restarted")
    }
  }
}
