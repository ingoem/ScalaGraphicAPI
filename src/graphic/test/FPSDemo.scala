package graphic
package test

trait FPSDemo extends Demo {
  var t0, lastFPSUpdate = System.nanoTime
  var t = 0
  var t1 = 0L
  var framesCounter = 0L
  var fpsCounter = 0.0
  
  def step(canvas: Canvas) {
    draw(canvas)
    
    framesCounter +=1
    t1 = System.nanoTime
    var fps = 1000000000.0/(t1 - t0)
    fpsCounter += fps
    val avg = fpsCounter / framesCounter
    if(t1 - lastFPSUpdate > 1000000000) {  // display fps in each sec
      log("Fps: " + fps.toFloat +", Avg: " + avg.toFloat)
      lastFPSUpdate = t1
    }
    t0 = t1
  }
}