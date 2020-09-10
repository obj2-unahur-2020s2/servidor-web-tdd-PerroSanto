package ar.edu.unahur.obj2.servidorWeb

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class ServidorWebTest : DescribeSpec({
  describe("Un servidor web") {
    val servidor = ServidorWeb()
    servidor.agregarModulo(
      Modulo(listOf("txt"), "todo bien", 100)
    )
    servidor.agregarModulo(
      Modulo(listOf("jpg", "gif"), "qué linda foto", 100)
    )

    it("devuelve 501 si recibe un pedido que no es HTTP") {
      val respuesta = servidor.realizarPedido("207.46.13.5", "https://pepito.com.ar/hola.txt", LocalDateTime.now())
      respuesta.codigo.shouldBe(CodigoHttp.NOT_IMPLEMENTED)
      respuesta.body.shouldBe("")
    }

    it("devuelve 200 si algún módulo puede trabajar con el pedido") {
      val respuesta = servidor.realizarPedido("207.46.13.5", "http://pepito.com.ar/hola.txt", LocalDateTime.now())
      respuesta.codigo.shouldBe(CodigoHttp.OK)
      respuesta.body.shouldBe("todo bien")
    }

    it("devuelve 404 si ningún módulo puede trabajar con el pedido") {
      val respuesta = servidor.realizarPedido("207.46.13.5", "http://pepito.com.ar/playa.png", LocalDateTime.now())
      respuesta.codigo.shouldBe(CodigoHttp.NOT_FOUND)
      respuesta.body.shouldBe("")
    }

    //-----------------------

    describe("Analizador de IPs Sospechosas"){

      val modulo1 = Modulo(listOf("doc"), "todo bien", 100)
      servidor.agregarModulo(modulo1)
      val modulo2 = Modulo(listOf("png"), "que fotos", 500)
      servidor.agregarModulo(modulo2)

      it("Pedidos de una IP sospechosa"){
        val analizadorIps1 = AnalizadorIPs(setOf("207.46.13.2"))
        servidor.agregarAnalizador(analizadorIps1)
        servidor.realizarPedido("207.46.13.1", "http://pepito.com.ar/hola.doc", LocalDateTime.now())
        analizadorIps1.cantidadDePedidos("207.46.13.2").shouldBe(0)
        servidor.realizarPedido("207.46.13.2", "http://pepito.com.ar/hola.doc", LocalDateTime.now())
        analizadorIps1.cantidadDePedidos("207.46.13.2").shouldBe(1)
      }

      it("Modulo mas consultado"){
        val analizadorIps2 = AnalizadorIPs(setOf("207.46.13.2","207.46.13.3"))
        servidor.agregarAnalizador(analizadorIps2)
        servidor.realizarPedido("207.46.13.1", "http://pepito.com.ar/playa.png", LocalDateTime.now())
        servidor.realizarPedido("207.46.13.2", "http://pepito.com.ar/hola.doc", LocalDateTime.now())
        servidor.realizarPedido("207.46.13.3", "http://pepito.com.ar/hola.doc", LocalDateTime.now())
        analizadorIps2.moduloMasConsultado().shouldBe(modulo1)
      }

      it("IPs sospechosas con una ruta determinada"){
        val analizadorIps2 = AnalizadorIPs(setOf("207.46.13.1","207.46.13.2","207.46.13.3"))
        servidor.agregarAnalizador(analizadorIps2)
        servidor.realizarPedido("207.46.13.1", "http://pepito.com.ar/playa.png", LocalDateTime.now())
        servidor.realizarPedido("207.46.13.2", "http://pepito.com.ar/hola.doc", LocalDateTime.now())
        servidor.realizarPedido("207.46.13.3", "http://pepito.com.ar/hola.doc", LocalDateTime.now())
        analizadorIps2.ipsSospechosasConRuta("http://pepito.com.ar/hola.doc").shouldBe(setOf("207.46.13.2","207.46.13.3"))
      }
    }

    describe("Analizador de Demoras"){

      val modulo1 = Modulo(listOf("doc"), "todo bien", 100)
      servidor.agregarModulo(modulo1)
      val modulo2 = Modulo(listOf("png"), "que fotos", 500)
      servidor.agregarModulo(modulo2)
      val analizadorDemoras1 = AnalizadorDemoras(200)
      servidor.agregarAnalizador(analizadorDemoras1)

      it("Detecta las demoras"){
        servidor.realizarPedido("207.46.13.1", "http://pepito.com.ar/hola.doc", LocalDateTime.now())
        analizadorDemoras1.cantidadDeDemoras().shouldBe(0)
        servidor.realizarPedido("207.46.13.2", "http://pepito.com.ar/playa.png", LocalDateTime.now())
        analizadorDemoras1.cantidadDeDemoras().shouldBe(1)
      }
    }

    describe("Analizador de Estadisticas"){
      val modulo1 = Modulo(listOf("doc"), "todo bien", 100)
      servidor.agregarModulo(modulo1)
      val modulo2 = Modulo(listOf("png"), "que fotos", 500)
      servidor.agregarModulo(modulo2)

      val analizadorEstadisticas1 = AnalizadorEstadisticas()
      servidor.agregarAnalizador(analizadorEstadisticas1)

      it("Tiempo de respuesta promedio"){
        servidor.realizarPedido("207.46.13.1", "http://pepito.com.ar/playa.png", LocalDateTime.now())
        servidor.realizarPedido("207.46.13.1", "http://pepito.com.ar/hola.doc", LocalDateTime.now())
        analizadorEstadisticas1.tiempoPromedio().shouldBe(300)
      }

      it("Cantidad de pedidos entre dos fechas"){
        servidor.realizarPedido("207.46.13.1", "http://pepito.com.ar/hola.doc", LocalDateTime.of(2000, 1, 1, 0, 0))
        servidor.realizarPedido("207.46.13.2", "http://pepito.com.ar/hola.doc", LocalDateTime.of(2010, 5, 2, 11, 30))
        servidor.realizarPedido("207.46.13.3", "http://pepito.com.ar/hola.doc", LocalDateTime.of(2020, 5, 30, 19, 30))
        val fecha1 = LocalDateTime.of(2006, 5, 30, 19, 30)
        val fecha2 = LocalDateTime.of(2020, 9, 10, 1, 31)
        analizadorEstadisticas1.cantidadDePedidosEntre(fecha1,fecha2).shouldBe(2)
      }

      it("Cantidad de respuestas cuyo body contiene un determinado string"){
        val modulo3 = Modulo(listOf("gif"), "bla", 100)
        servidor.agregarModulo(modulo3)
        servidor.realizarPedido("207.46.13.1", "http://pepito.com.ar/hola.doc", LocalDateTime.now())
        servidor.realizarPedido("207.46.13.2", "http://pepito.com.ar/playa.gif", LocalDateTime.now())
        analizadorEstadisticas1.cantidadDeRespuestaCon("bla").shouldBe(0)
      }

      it("Porcentaje de respuestas exitosas"){
        servidor.realizarPedido("207.46.13.1", "http://pepito.com.ar/hola.doc", LocalDateTime.now())
        servidor.realizarPedido("207.46.13.1", "http://pepito.com.ar/hola.bla", LocalDateTime.now())
        analizadorEstadisticas1.porcentajeExitoso().shouldBe(50)
      }
    }
  }
})
