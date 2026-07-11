# 🚀 Sistema de Gestión de Pedidos y Guías de Despacho (Cloud Native)

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.x-ff6600.svg)](https://www.rabbitmq.com/)
[![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20S3%20%7C%20API%20Gateway-232F3E.svg)](https://aws.amazon.com/)
[![Azure B2C](https://img.shields.io/badge/Azure%20AD-B2C-0078D4.svg)](https://azure.microsoft.com/)
[![Oracle Cloud](https://img.shields.io/badge/Oracle%20Cloud-DB-F80000.svg)](https://www.oracle.com/cloud/)

Este repositorio contiene el código fuente y la arquitectura de infraestructura para un **Microservicio Cloud Native** diseñado para una empresa transportista. El sistema gestiona transacciones asíncronas, almacenamiento binario, y securitización en la nube, integrando múltiples proveedores mediante un enfoque de nube híbrida.

---

## Arquitectura del sistema Spring Boot gestionado mediante un API Manager y un IDaaS. Desarrollo de sistema asíncrono con utilización de colas.

Este proyecto fue estructurado en múltiples fases de desarrollo, logrando integrar con éxito las siguientes tecnologías y patrones de diseño:

### 1. Arquitectura Orientada a Eventos (EDA) y Colas de Mensajería
* **Clúster RabbitMQ en Docker:** Despliegue de un bróker de mensajería (nodos 1 y 2) contenedorizado mediante Docker en una instancia AWS EC2, logrando alta disponibilidad.
* **Productor y Consumidor (Spring AMQP):** Implementación de comunicación asíncrona. Un hilo Productor (`GuiaServiceImpl`) inyecta mensajes en la cola tras interactuar con S3, mientras que un hilo Consumidor (`RabbitMQConsumer`) procesa la cola principal concurridamente.
* **Dead Letter Queue (DLQ) y Tolerancia a Fallos:** Configuración defensiva con `RetryTemplate` (3 reintentos). Si un mensaje falla la validación del consumidor (ej. archivo corrupto o inválido), el interceptor lo deriva automáticamente hacia una cola forense secundaria (`cola.guias.dlq`) usando el patrón **Dead Letter Exchange (DLX)**, evitando la caída del sistema.

### 2. Infraestructura y Seguridad
* **Identity as a Service (IDaaS - Azure AD B2C):** Externalización completa de identidades creando un Tenant propio. Se implementó el flujo *OAuth 2.0* y la inyección de atributos de *Role* directamente en la firma de los tokens JWT.
* **AWS API Gateway:** Exposición de la aplicación mediante un proxy inverso (HTTP API) securizado por un *JWT Authorizer*, bloqueando peticiones ilegítimas antes de que alcancen el servidor EC2.
* **Control de Accesos Basado en Roles (RBAC):** Integración de *Spring Security* como *Resource Server*. El sistema intercepta las llamadas, verifica la firma criptográfica de Microsoft y autoriza los métodos HTTP basándose en privilegios estrictos (`Guia.Admin` para escritura/eliminación, `Guia.Descargar` exclusivo para lectura).

### 3. Orquestación de Nube Híbrida y CI/CD
* **Almacenamiento Distribuido:** Persistencia binaria de alta disponibilidad enviando documentos físicos a **Amazon S3**, mientras que la persistencia transaccional y relacional es controlada por **Oracle Cloud Autonomous Database** vía Hibernate/JPA.
* **Aislamiento de Dominios en Base de Datos:** Los consumidores asíncronos guardan el resultado del procesamiento de colas en una nueva tabla independiente (`guia_mensajes`), separando físicamente la transaccionalidad de los pedidos de la auditoría de colas.
* **Pipeline de Integración Continua (GitHub Actions):** Automatización del empaquetado del artefacto Java, construcción de la imagen en Docker Hub, y despliegue por conexión SSH en el servidor AWS EC2.

---

## Endpoints REST Implementados y probados mediante POSTMAN con la ruta de la API Gateway 

El microservicio expone los siguientes recursos transaccionales bajo el controlador `/api/transportes/guias`:

| Método | Endpoint | Descripción (Requisito de la Rúbrica) | Rol Requerido |
| :--- | :--- | :--- | :--- |
| `POST` | `/pedidos` | **Crear guías de despacho:** Crea el registro inicial del pedido transaccional en Oracle. | `Guia.Admin` |
| `POST` | `/guias/subir` | **Subir guías generadas a S3:** Carga el binario multipart a AWS, y emite el evento a la Cola 1. | `Guia.Admin` |
| `GET` | `/guias/descargar` | **Descargar guías con validación de permisos:** Descarga segura del binario exigiendo rol explícito. | `Guia.Descargar`, `Guia.Admin` |
| `PUT` | `/guias/actualizar` | **Modificar o actualizar guías:** Reemplaza el archivo en S3 y actualiza la entidad. | `Guia.Admin` |
| `DELETE` | `/guias/eliminar` | **Eliminar guías específicas:** Elimina el binario del Bucket S3 y el registro en Oracle. | `Guia.Admin` |
| `GET` | `/guias/buscar` | **Consultar guías por transportista y fecha:** Búsqueda combinada (JPQL) en base de datos. | `Guia.Admin` |

---

##  Pruebas Locales en Oracle SQL Developer y Despliegue

### Requisitos Previos
* **Java 21** instalado.
* Billetera de conexión segura (**Oracle Wallet** .zip).
* Credenciales programáticas (Access Keys) de **AWS**.
* Parámetros del Tenant en **Azure AD B2C**.

### Autenticación para Testing en Postman
Para probar la API a través de Postman, configure la autorización de tipo **OAuth 2.0**:
* **Callback URL:** `https://oauth.pstmn.io/v1/callback`
* **Auth & Token URL:** Apuntando a su flujo `B2C_1_...`
* Otorgue la autorización desde el popup de Microsoft y obtendrá su `Bearer Token` inyectado automáticamente.
