# Estructura del Documento Word - Semana 8
*(Copia y pega esta estructura en tu documento de Word e inserta las capturas de pantalla donde se indica).*

---

## 1. Configuración de Azure AD B2C
En esta sección se evidencia la correcta configuración del tenant y los roles en la nube de Azure.

- **Paso 1.1:** Creación del Registro de Aplicación (App Registration) para el API.
  > **[PEGAR PANTALLAZO AQUÍ: Evidencia del App Registration con su Client ID]**

- **Paso 1.2:** Creación de los Roles (Atributos de usuario y Notificaciones de la aplicación).
  > **[PEGAR PANTALLAZO AQUÍ: Evidencia de los Claims seleccionados en el Flujo de Usuario, especialmente el claim 'Role']**

- **Paso 1.3:** Configuración del Token de Compatibilidad (Emisor `tfp`).
  > **[PEGAR PANTALLAZO AQUÍ: Pantalla de Propiedades del flujo mostrando el emisor 'tfp' seleccionado en el menú desplegable]**

- **Paso 1.4:** Ejecución del flujo y obtención de Token.
  > **[PEGAR PANTALLAZO AQUÍ: Token decodificado (ej. en jwt.ms) demostrando el 'iss' con tfp y el rol asignado]**

---

## 2. Configuración en AWS API Gateway
En esta sección se evidencia el bloqueo perimetral de la API usando el JWT Authorizer.

- **Paso 2.1:** Creación del Autorizador JWT.
  > **[PEGAR PANTALLAZO AQUÍ: Formulario del JWT Authorizer en AWS con la URL del emisor exacta y la audiencia]**

- **Paso 2.2:** Integración y Rutas securizadas.
  > **[PEGAR PANTALLAZO AQUÍ: Vista de las rutas (routes) en AWS API Gateway mostrando que el Authorization está activado y vinculado al Autorizador JWT]**

---

## 3. Ejecución y Pruebas en Postman (API y Seguridad)
En esta sección se valida el funcionamiento del backend completo, las colas y la seguridad.

- **Prueba 3.1: Intento de Acceso sin Token**
  - **Acción:** Consumir un endpoint protegido (ej. `/api/transportes/guias/listar`) sin autorización.
  - **Resultado Esperado:** `401 Unauthorized`.
  > **[PEGAR PANTALLAZO AQUÍ: Request de Postman arrojando 401]**

- **Prueba 3.2: Acceso con Rol Correcto (Subida de Guía y Productor de RabbitMQ)**
  - **Acción:** Enviar un POST a `/api/transportes/guias/subir` usando un Token con el rol adecuado (ej. Admin).
  - **Resultado Esperado:** `201 Created` / El código procesa el PDF, lo sube a S3 y **envía asíncronamente el mensaje a la Cola 1**.
  > **[PEGAR PANTALLAZO AQUÍ: Request exitoso en Postman devolviendo la ruta en S3]**

- **Prueba 3.3: Acceso Restringido (Endpoint de Descarga)**
  - **Acción:** Solicitar una descarga de PDF usando un Token que solo tenga el rol de Descarga (`DESCARGA`).
  - **Resultado Esperado:** `200 OK` (Descarga exitosa).
  > **[PEGAR PANTALLAZO AQUÍ: Request exitoso del GET a descargar devolviendo el archivo PDF]**

- **Prueba 3.4: Validación de Roles estricta (Error 403)**
  - **Acción:** Intentar consumir un endpoint de Administrador (ej. `/subir` o `/eliminar`) usando el token de un usuario que solo tiene rol `DESCARGA`.
  - **Resultado Esperado:** `403 Forbidden` (AWS API Gateway o Spring Boot lo rechazan por no tener el nivel de permisos).
  > **[PEGAR PANTALLAZO AQUÍ: Request en Postman devolviendo 403 Forbidden]**

---

## 4. Evidencia de Colas Asíncronas (RabbitMQ) y Base de Datos (Oracle)
- **Prueba 4.1: Mensaje en RabbitMQ y Consumo**
  - **Acción:** Mostrar que el clúster de Docker está corriendo o mostrar el log de la consola de Spring Boot atrapando el mensaje de la cola.
  > **[PEGAR PANTALLAZO AQUÍ: Log de la consola de Spring Boot diciendo "Mensaje recibido de Cola 1" y "Mensaje guardado en base de datos correctamente"]**

- **Prueba 4.2: Inserción en Tabla Nueva de Oracle**
  - **Acción:** Mostrar en el motor de base de datos que el registro se guardó en la tabla exclusiva solicitada.
  > **[PEGAR PANTALLAZO AQUÍ: Pantallazo de SQL Developer o similar mostrando la tabla `guia_mensajes` con el registro de la ruta guardado]**
