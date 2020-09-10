package ar.edu.unahur.obj2.servidorWeb

import java.time.LocalDateTime

enum class CodigoHttp(val codigo: Int) {
  OK(200),
  NOT_IMPLEMENTED(501),
  NOT_FOUND(404),
}

class ServidorWeb {
  val modulos = mutableListOf<Modulo>()
  val analizadores = mutableListOf<Analizador>()

  fun realizarPedido(ip: String, url: String, fechaHora: LocalDateTime): Respuesta {
    var respuesta : Any?
    var modulo : Modulo? = null
    if (!url.startsWith("http:")) {
      respuesta = Respuesta(codigo = CodigoHttp.NOT_IMPLEMENTED, body = "", tiempo = 10)
    }
    else if (this.algunModuloSoporta(url)) {
      val moduloSeleccionado = this.modulos.find { it.puedeTrabajarCon(url) }!!
      modulo = moduloSeleccionado
      respuesta = Respuesta(CodigoHttp.OK, moduloSeleccionado.body, moduloSeleccionado.tiempoRespuesta)
    }
    else{
      respuesta = Respuesta(codigo = CodigoHttp.NOT_FOUND, body = "", tiempo = 10)
    }
    registrarPedido(Pedido(modulo,respuesta,ip,url,fechaHora))
    return respuesta


  }

  fun algunModuloSoporta(url: String) = this.modulos.any { it.puedeTrabajarCon(url) }

  fun agregarModulo(modulo: Modulo) = this.modulos.add(modulo)

  fun agregarAnalizador(analizador: Analizador) = analizadores.add(analizador)

  fun quitarAnalizador(analizador: Analizador) = analizadores.remove(analizador)

  fun registrarPedido(pedido: Pedido) = analizadores.forEach{it.agregarPedido(pedido)}

}

class Respuesta(val codigo: CodigoHttp, val body: String, val tiempo: Int)