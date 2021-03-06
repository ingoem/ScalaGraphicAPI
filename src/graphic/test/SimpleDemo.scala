package graphic
package test

import java.awt.{Canvas => _, _}
import java.awt.geom._
import math._

object SimpleDemo extends FPSDemo {
  //val image1 = loadImage("data/CoffeeBean.bmp", "bmp")
  //val image2 = loadImage("data/Island.jpg", "jpg")
  var animY1 = 0.01
  var dashOffset = 0f
  var dashWidth = 4f
  val solidShader: Shader = new Shader
  val hstripesShader: Shader = new Shader
  val vstripesShader: Shader = new Shader
  val chessShader: Shader = new Shader
  val circlesShader: Shader = new Shader
  val grad1Shader: Shader = new Shader
  
  /*def init() {
    solidShader.compileShadersFromFile(gl, "data/solid.fs", "data/solid.vs")
    hstripesShader.compileShadersFromFile(gl, "data/hstripes1.fs", "data/hstripes1.vs")
    vstripesShader.compileShadersFromFile(gl, "data/vstripes1.fs", "data/vstripes1.vs")
    chessShader.compileShadersFromFile(gl, "data/chessboard1.fs", "data/chessboard1.vs")
    circlesShader.compileShadersFromFile(gl, "data/circles1.fs", "data/circles1.vs")
    grad1Shader.compileShadersFromFile(gl, "data/grad1.fs", "data/grad1.vs")
      
    image1 = loadImage("data/CoffeeBean.bmp", "bmp")
    image2 = loadImage("data/Island.jpg", "jpg")
  }*/

  def draw(g: Canvas) {
    //testSet1(g)

    //blendingSet1(g, 0.5f)
    //dashOffset += 0.1f
    //strokeSet1(g, new BasicStroke(2))//, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 50, Array(18f, 20f), dashOffset))
    //clipSet1(g)
    //shadersSet1(g)
    tessCacheSet1(g)
  }

  def tessCacheSet1(g: Canvas){
    g.clear(Color.WHITE)    
      
    var z = 0
    val shapesNum = 4
    while(z < shapesNum){
      // the best case
      g.fill(textOutline(new Font("Times New Roman", Font.BOLD, 108), "ABCD", 10+z, 200+z))
//      g.fillWithCache(textOutline(new Font("Times New Roman", Font.BOLD, 108), "ABCD", 10+z, 200+z))
/*
// worst case
      val path = new Path2D.Float
      path.append(textOutline(new Font("Times New Roman", Font.BOLD, 108), "ABCD", 10, 200), false)
      path.moveTo(z*3, 50)
      path.lineTo(z*3, 50+30)
      path.lineTo(z*3+10, 50+30)
      path.closePath
//      g.fill(path)
      g.fillWithCache(path)
*/
      z+=1      
    }    
  }

  def shadersSet1(g: GLCanvas){
    g.clear(Color.WHITE)

    g.color = new Color(0.2f, 0.2f, 0.8f)
    g.shader = solidShader
    g.fill(new Ellipse2D.Float(50, 50, 150, 75))
    g.shader = vstripesShader
    g.fill(new Ellipse2D.Float(250, 50, 150, 75))
    g.shader = chessShader
    g.fill(new Ellipse2D.Float(50, 150, 150, 75))
    g.shader = circlesShader
    g.fill(new Ellipse2D.Float(250, 150, 150, 75))

    g.shader = grad1Shader
    g.shader.setUniformParameter2("middle", 125.0f, 287.5f)
    g.fill(new Ellipse2D.Float(50, 250, 150, 75))
    g.shader = hstripesShader
    g.fill(new Ellipse2D.Float(250, 250, 150, 75))
    g.shader = null
  }

  def clipSet1(g: GLCanvas){
    g.clear(Color.GRAY)
//    g.image = image2
    g.clip = textOutline(new Font("Times New Roman", Font.BOLD, 98), "S C A L A", 40, 150)
    g.clip = textOutline(new Font("Times New Roman", Font.BOLD, 120), "S C A L A", 0, 250)
    g.stroke = new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 50, Array(18f, 18f), dashOffset)
    g.clipStroke(textOutline(new Font("Times New Roman", Font.BOLD, 110), "S C A L A", 0, 450))
    //g.drawImage(image2, 0, 75, 500, 375)

  }

  def strokeSet1(g: Canvas, bs: BasicStroke){
    g.clear(Color.WHITE)

    g.stroke = bs
    g.color = new Color(0.2f, 0.2f, 0.8f)
    g.stroke(new Arc2D.Float(100, 200, 80, 80, 0, 300, Arc2D.CHORD))
    g.stroke(new Arc2D.Float(200, 200, 80, 80, 0, 300, Arc2D.OPEN))
    g.stroke(new Arc2D.Float(300, 200, 80, 80, 0, 300, Arc2D.PIE))

    g.color = new Color(0.2f, 0.8f, 0.2f)
    val p = new Path2D.Float
    p.moveTo(70, 250)
    p.curveTo(450, 200, 200, 300, 300, 100)
    p.quadTo(200 , 200, 400, 400)
    p.lineTo(70, 250)
    p.closePath()
    g.stroke(p)

    g.color = new Color(0.8f, 0.8f, 0.2f)
    g.stroke(new Rectangle2D.Float(100, 150, 300, 250))
    g.stroke(new Ellipse2D.Float(50, 100, 200, 100))
    g.stroke(new RoundRectangle2D.Float(350, 100, 100, 150, 50, 50))

    g.color = new Color(0.5f, 0.5f, 0.5f)
    g.stroke(textOutline(new Font("Times New Roman", Font.BOLD, 82), "Stroke Test 1", 20, 450))
  }

  def blendingSet1(g: Canvas, alpha: Float){
    g.clear(Color.WHITE)
    g.color = new Color(0.0f, 0.2f, 0.8f, alpha)
    g.fill(new Rectangle2D.Float(100, 150, 300, 250))

    g.color = new Color(0.0f, 0.8f, 0.2f, alpha)
    g.fill(new Ellipse2D.Float(50, 100, 200, 100))

    g.color = new Color(0.8f, 0.2f, 0.0f, alpha)
    g.fill(new RoundRectangle2D.Float(350, 100, 100, 150, 50, 50))
    
    g.stroke = new BasicStroke(10)
    g.color = new Color(0.8f, 0.4f, 0.2f, alpha)
    g.stroke(new Ellipse2D.Float(50, 50, 400, 400))
    var z = 0
    while(z<10){
      g.color = new Color(0.2f, 0.8f, 0.8f, alpha)
      g.stroke(new Ellipse2D.Float(z*50, 350, 200, 100))
      g.color = new Color(0.8f, 0.8f, 0.2f, alpha)
      g.fill(new Ellipse2D.Float(z*50, 350, 200, 100))
      z += 1
    }
  }

  val outline = textOutline(new Font("Times New Roman", Font.BOLD, 108), "H e l l o", 100, 400)
  
  def testSet1(g: Canvas) {
    g.clear(Color.WHITE)

    g.color = new Color(0.0f, 0.0f, 0.01f)
    g.stroke = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 50, Array(8f, 10f), dashOffset)
    g.stroke(new Ellipse2D.Float(200, 200, 200, 50))

    dashOffset += 0.2f
    //dashOffset %= 18

    g.stroke = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 50, Array(8f, 10f), dashOffset)
    g.stroke(new RoundRectangle2D.Float(290, 290, 80, 90, 30, 30))

    dashWidth += 0.05f
    dashWidth %= 10
    g.stroke = new BasicStroke(dashWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 50, Array(12f, 6f), dashOffset)
    g.stroke(new RoundRectangle2D.Float(390, 290, 80, 90, 30, 30))

    g.stroke = new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 50, Array(4f, 8f), dashOffset)
    g.stroke(outline)

//    g.color = new Color(1.0f, 0.5f, 0.3f)
//    g.font = new Font("Times New Roman", Font.BOLD, dashWidth.toInt*5)
//    g.drawTextOnPath("scala java kawa", new Ellipse2D.Float(100, 100, 150+(math.sin(animY1*5)*90.0).toInt, 100))

    animY1 +=0.01f
    val p = new Path2D.Float
    p.moveTo(200 - 150*sin(animY1), 200 + 80*cos(animY1))
    p.curveTo(200 + 300*sin(animY1), 200 + 300*cos(animY1),
              300 - 300*sin(animY1), 300 - 300*cos(animY1),
              350 + 50*sin(animY1), 350 - 150*cos(animY1))
    p.quadTo(200 - 50*sin(animY1), 200 + 50*cos(animY1),
             300 + 150*sin(animY1*3.5), 300 + 180*cos(animY1*2))
    p.lineTo(200 - 150*sin(animY1), 200 + 80*cos(animY1))
    p.closePath()

    g.stroke = new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 50, Array(10f, 15f), 0)
    g.color = new Color(0.8f, 0.2f, 0.2f)
    g.fill(p)
    g.color = new Color(0.2f, 0.8f, 0.2f, 0.5f)        
    g.stroke(p)    

    //g.drawImage(image1, 20, 20, 40, 40)

  }
}