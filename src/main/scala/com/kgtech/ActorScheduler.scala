package com.kgtech

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorSystem
import scala.concurrent.duration._
object ActorScheduler {
    def main(args: Array[String]): Unit = {
        println(s"Actor scheduler working...")
        val system = ActorSystem("actorSystem")
        val actor1 = system.actorOf(Props[SchedulerActor], "actor1")
        actor1 ! Count

        implicit val ec = system.dispatcher
        system.scheduler.scheduleOnce(1.second)(actor1 ! Count)
        val schedule = system.scheduler.schedule(0.second, 100.millis, actor1, Count)
        Thread.sleep(1000)
        schedule.cancel()
        system.terminate()
    }

    case object Count
    var num: Int = 0
    class SchedulerActor extends Actor {
        def receive = {
            case Count => {
                num += 1
                println(num)
            }
        }
    }
}
