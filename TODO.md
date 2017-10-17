## Flujo del proceso

1 - init()

- Cargar la configuracion del fichero
- Iniciar los pool de conexiones

2 - destroy()

- Desconectar de las BD

3 - service()

- Recuperar el DAD y validar si existe en la configuracion
- Establecer la conexion para la sesion
- Establecer el procedimiento para ser invocado
	+ Si utiliza el modelo de Felxible parameter 
	+ Si se trata de un procedimimento estandar, funciones no están permitidas. 
- Subida de ficheros
	+ Si el servlet es multipart, hacer las inserts a la tabla de _documents_ 
	+ Además se renombran los parametros del nombre del fichero. 
- Establecer las variables de entorno de CGI, CGI_ENV
- Establecer los parametros de entrada al procedimiento (dbProcedure, request, [flexible, standar])
- Invocar al procedimiento
	- Si el procedimiento no se ha definido invocar a la defaultPage
- Establecer las cabeceras de retorno. 
- Recuperar los parametros de salida
	+ Si se trata de la descarga de un fichero, recuperarlo (de la tabla de _documents_?)


### Clases a implementar

- Configuration
- DBConnection
- Gateway
	+ responseHeaders
	+ dbProcedure
	+ inputParams
	+ multipart
	+ cgiEnv


### Estado acutal

He creado la clase Gateway para implementar la logica en ella. 
Ya se puede invocar a los procedimientos con el flexible parameter y sin él. Solo en el caso de que sea flexible parameter se incluye el parametro dbax_data el cual incluye el body de la peticion. 

La subida de ficheros ya está implementada. Falta la descarga. 