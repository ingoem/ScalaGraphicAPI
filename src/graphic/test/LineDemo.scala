package graphic
package test

import java.awt.{Color, BasicStroke}
import java.awt.geom.Line2D

object LineCountDemo extends FPSDemo {
  val colors = Array(Color.white, Color.black, Color.blue, Color.red, Color.cyan, Color.green, Color.yellow)
  val strokes = Array(new BasicStroke(1), new BasicStroke(1.5f), new BasicStroke(2), 
      new BasicStroke(2.5f), new BasicStroke(3), new BasicStroke(3.5f), new BasicStroke(4))
  
  def r = math.random.toFloat
  def chooseRandomly[T](arr: Array[T]) = arr((r*arr.length-1).toInt)
      
  def draw(g: Canvas) {
    var i = 0
    // draw a bunch at once to reduce the effect of event queue latency
    while(i < 1000) {
      //g.color = chooseRandomly(colors)
      //g.stroke = chooseRandomly(strokes)
      g.color = Color.blue
      g.stroke = new BasicStroke(10)
      g.stroke(new Line2D.Float(100, 100, 400, 400))
      
      g.color = Color.green
      g.stroke = new BasicStroke(5)
      g.stroke(new Line2D.Float(100, 400, 400, 100))
      //g.stroke(new Line2D.Float(r*500, r*500, r*500, r*500))
      i += 1
    }
  }
}