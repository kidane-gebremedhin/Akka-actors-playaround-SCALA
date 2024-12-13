package com.kgtech

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import scalafx.application.JFXApp
import scalafx.application.Platform
import scalafx.scene.image.PixelWriter
import scalafx.scene.paint.Color
import akka.routing.BalancingPool
import scalafx.scene.Scene
import scalafx.scene.Scene
import scalafx.scene.image.WritableImage
import scalafx.scene.image.ImageView
import scalafx.scene.image.Image

object MandelbrotActors extends JFXApp {
  val maxCount = 10000
  val imageSize = 600
  val xMin = -1.5
  val xMax = 0.5
  val yMin = -1.0
  val yMax = 1.0

  case class Complex(real: Double, imag: Double) {
    def +(that: Complex) = Complex(real + that.real, imag + that.imag)
    def *(that: Complex) = Complex(
      ((real * that.real) - (imag * that.imag)),
      ((real * that.imag) + imag * that.real)
    )
    def mag = math.sqrt(real * real + imag * imag)
  }
  def mandelCount(c: Complex): Int = {
    var count = 0
    var z = Complex(0, 0)
    while(count < maxCount && z.mag < 4) {
      z = z * z + c
      count += 1
    }
    count
  }

  case class Line(row: Int, y: Double)

  class LineActor(pw: PixelWriter) extends Actor {
    def receive = {
      case Line(row, y) => {
        for(j <- 0 until imageSize) {
          val x = xMin + j*(xMax - xMin) / imageSize
          val count = mandelCount(Complex(x, y))
          Platform.runLater {
            pw.setColor(j, row, if (count == maxCount) Color.Black else {
              val scale = 10 * math.sqrt(count.toDouble / maxCount) min 1.0
              Color(scale, 0, 0, 1)
            })
          }
        }
      }
    }
  }

  val system = ActorSystem("myActorSystem")
  stage = new JFXApp.PrimaryStage {
    title = "Actor Mandelbrot"
    scene = new Scene(imageSize, imageSize) {
      val writableImage = new WritableImage(imageSize, imageSize)
      val image = new Image(writableImage)
      content = new ImageView(image)

      val router = system.actorOf(BalancingPool(4).props(Props(new LineActor(writableImage.pixelWriter))), "Pool")
      for (x <- 0 until imageSize) {
        val y = yMin + x * (yMax - yMin)/imageSize
        router ! Line(x, y)
      }
    }
  }
}
