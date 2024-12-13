package com.kgtech

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import akka.actor.ActorRef

object ActorCommunication {
    def main(args: Array[String]): Unit = {
        println("Akka is fun!")
        val system = ActorSystem("actorSystem")
        val actor1 = system.actorOf(Props[ActorFactory], "actor1")
        val actor2 = system.actorOf(Props[ActorFactory], "actor2")

        actor2 ! StartCounting(10, actor1)
    }

    case class StartCounting(n: Int, otherActor: ActorRef)
    case class CountDown(n: Int)
    class ActorFactory extends Actor {
        def receive = {
            case StartCounting(n, otherActor) => {
                println(s"Current Count: $n $self")
                otherActor ! CountDown(n-1)
            }
            case CountDown(n) => {
                if (n == 0) {
                    println("About to stop counting...")
                    context.system.terminate()
                    println("Stopped counting")
                } else {
                    println(s"Current Count: $n $self")
                    sender ! CountDown(n-1)
                }
            }
        }
    }
}