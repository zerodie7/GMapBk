# Implementacion MVVM + Android JetPack 


### Extrae Informacion de json simulado como consumo de API desde github:

Despliega tarjetas con la siguiente información:
 
* id
* nombre
* direccion
* bicis disponibles
* espacios disponibles 
 
 Cuenta con un sistema de filtros implementados por FAB los cuales permiten :

* Obtiene cordenadas de lat y lon del dispotivo, posteriormente valida las cordenadas del usuario con las del movil, si estas se encuentran dentro de un radio de acción establecido, se mostrara los destinos.
* Ordenar de mayor a menor bicis disponibles"
* Ordena de menor a mayor espacios disponibles"

## Extras

* Se implemento evento onClick en cada tarjeta la cual permite navegar a un nuevo fragmento, el cual despliega en un mapa la ubicación de la estación
* Dentro del mapa se cuenta con los filtros descritos en el punto anterior.
* Al seleccionar el punto marcado en mapa se muestra en una tarjeta los detalles de dicho punto.

![alt text](https://github.com/zerodie7/GMapBk/blob/master/1.png?raw=true)

