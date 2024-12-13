package com.kgtech

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorSystem
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object AskPattern {
    def main(args: Array[String]): Unit = {
        println("AskPattern is running...")
        val system = ActorSystem("theActorSystem")
        val actor1: ActorRef = system.actorOf(Props(new KGActor("Kidane Gebremedhin Gidey")), "kgActor")

        implicit val duration = Timeout(1.seconds)
        val result = actor1 ? NameRequest
        result.foreach(nameResponse => println(nameResponse))
        system.terminate()
    }

    case object NameRequest
    case class NameResponse(name: String)

    class KGActor(val name: String) extends Actor {
        def receive = {
            case NameRequest => sender ! NameResponse(s"$name from $self")
        }
    }
}