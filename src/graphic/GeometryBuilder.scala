package graphic

import javax.media.opengl._
import javax.media.opengl.fixedfunc.GLMatrixFunc
import javax.media.opengl.glu.GLU
import java.nio.{ByteBuffer, ByteOrder, FloatBuffer}
import java.util.ArrayList
import java.awt.Shape
import java.awt.geom._
import java.awt.{Font, Color, BasicStroke}

class GeometryBuilder {
  private val floatSize = 4
  var fixedArraySize = 4096//12210
  private val extendArraySize = 4096 // 4096 = 2048 two coord's verts = 8192 bytes
  private var verts = allocateCoordData(fixedArraySize)
  private var tmpVerts = new Array[Float](fixedArraySize)
  
  def vertsNum = verts.position/2//gvi/2
  var gvi = 0 // global vertex index
  var ind = 0 // index for path outline
  var nx = 0.0f
  var ny = 0.0f
  
  val arcVerts = new Array[Float](180)
  var arcInd = 0
  var endsAtStart = false
  var implicitClose = false
  
  def addVertex(x: Float, y: Float) {
    // extends array, increase global index
    if(gvi+2 < fixedArraySize) {      
      verts.put(x)
      verts.put(y)
      gvi += 2
    } else { // re-size the array      
      fixedArraySize = fixedArraySize + extendArraySize
      val tmpVertsBuffer = allocateCoordData(fixedArraySize)
      tmpVertsBuffer.put(verts)
      verts = tmpVertsBuffer
      verts.position(gvi)
      verts.put(x)
      verts.put(y)
      gvi += 2
      //resizeVBO
      println("Verts Array resized to: "+fixedArraySize+" elements")
      println("VBO resized to: "+fixedArraySize*4+" bytes")
    }
  }

  def addTmpVertex(x: Float, y: Float, index: Int) {    
    if(index+2 < fixedArraySize) {
      tmpVerts(index) = x
      tmpVerts(index+1) = y
    } else { // extends array tmp, does not increase global index
      val tempArray = new Array[Float](fixedArraySize)
      tmpVerts.copyToArray(tempArray)
      fixedArraySize = fixedArraySize + extendArraySize
      println("tmp verts Array resized to: "+fixedArraySize)
      tmpVerts = new Array[Float](fixedArraySize)
      tempArray.copyToArray(tmpVerts)
      tmpVerts(index) = x
      tmpVerts(index+1) = y
    }
  }
  
  def coordData: FloatBuffer = verts
  private def allocateCoordData(n: Int) = 
    ByteBuffer.allocateDirect(4*n).order(ByteOrder.nativeOrder).asFloatBuffer
    //FloatBuffer.allocate(n)//new Array[Float](fixedArraySize)
  def newCoordData() = allocateCoordData(fixedArraySize)
  
  def rewind() { 
    coordData.rewind() 
    gvi = 0  
  }
  
  def tmpVertex(idx: Int) = tmpVerts(idx)
  def coord(idx: Int) = verts.get(idx)
  
  def emitLineSeg(x: Float, y: Float, nx: Float, ny: Float) {
    addVertex(x + nx, y + ny)
    addVertex(x - nx, y - ny)
  }
 
  def flatnessFactor(shape: Shape): Double = {
    val flatTresh = 40.0 // treshold for decreasing flatness factor
    val size = math.min(shape.getBounds2D.getWidth, shape.getBounds2D.getHeight) - flatTresh
    val linearFactor = 500.0
    val flatMax = 1.0
    val fac1 = 5.0
    if(size > 0f) flatMax - math.log10(size)/fac1
    else 1.0
  }
}