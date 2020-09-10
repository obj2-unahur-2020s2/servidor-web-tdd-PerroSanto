package ar.edu.unahur.obj2.servidorWeb

import java.time.LocalDateTime

class Pedido(val modulo : Modulo?,val respuesta: Respuesta, val ip : String, val url : String, val fecha : LocalDateTime){}

abstract class Analizador(){
    val pedidos = mutableListOf<Pedido>()

    open fun agregarPedido(pedido : Pedido) : Unit {
        pedidos.add(pedido)
    }

    fun respuestas() = pedidos.map{it.respuesta}
}

class AnalizadorDemoras(val tiempoMinimo: Int) : Analizador(){

    fun cantidadDeDemoras() = pedidos.filter{ it.respuesta.tiempo > tiempoMinimo }.size
}

class AnalizadorIPs(val ipsSospechosas : Set<String>) : Analizador() {

    override fun agregarPedido(pedido : Pedido) : Unit {
        if(ipsSospechosas.contains(pedido.ip)){
            pedidos.add(pedido)
        }
    }

    fun cantidadDePedidos(ipSospechosa : String) = pedidos.filter{ it.ip == ipSospechosa}.count()

    fun moduloMasConsultado() = pedidos.groupBy{it.modulo}.maxBy{it.component2().size}?.component1()

    fun ipsSospechosasConRuta(ruta : String) = pedidos.filter{ it.url == ruta}.map{it.ip}.toSet()

}

class AnalizadorEstadisticas() : Analizador(){

    fun tiempoPromedio() = respuestas().sumBy{it.tiempo} / respuestas().count()

    fun cantidadDePedidosEntre(fecha1: LocalDateTime, fecha2: LocalDateTime) =
        pedidos.count { fecha1.isBefore(it.fecha) && fecha2.isAfter(it.fecha) }

    fun cantidadDeRespuestaCon(body: String) = respuestas().count { it.body.contains(body) }

    fun porcentajeExitoso() = respuestas().count { it.codigo == CodigoHttp.OK } * 100 / respuestas().count()
}