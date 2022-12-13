# Multiagentes

## Intrucciones ejecución:

- Crear una carpeta llamada `Multiagentes-main` en el `Escritorio`
- Meter en ella los siguientes archivos:
	- `ACC.jar` (La generación del .jar se explica a continuacion)
	- `estructuraXML.sxd`
	- `comando.bat`, que debe contener las siguientes 2 lineas, donde el 3 de la primera linea se corresponde con el número de generaciones:<br>
		"C:/Program Files/Java/jdk-17.0.4.1/bin/java.exe" -jar ACC.jar 3 0 <br>
		exit
## Generación .jar:
- File -> ProjectStructure -> Artifacts -> + -> Jar -> From modules with dependencies -> ACC
- File -> Settings -> Build, Execution, Deployment -> Compiler -> Java Compiler -> Project bytecode version 17
- Build -> Build Project
- Build -> Build artifacts -> rebuild

## Ejecución del proyecto
- Ejecutar `comando.bat`
