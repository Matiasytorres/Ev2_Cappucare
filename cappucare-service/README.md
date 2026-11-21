# cappucare-service

Microservicio REST (Spring Boot + Kotlin) del inventario de Cappucare. Lo consume la app Android mediante Retrofit.

## Ejecutar

Requiere JDK 17 o superior.

```bash
./gradlew bootRun        # Windows: gradlew.bat bootRun
```

- API: http://localhost:8080/api/products
- Consola H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:cappucare`, usuario `sa`, sin contraseña)

Los datos están en memoria: se reinician al detener el servidor.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/products` | Lista de productos |
| GET | `/api/products/low-stock` | Stock bajo (`stock ≤ minStock`) |
| GET | `/api/products/{sku}` | Detalle |
| POST | `/api/products` | Crear (409 si el SKU existe) |
| PUT | `/api/products/{sku}` | Actualizar |
| DELETE | `/api/products/{sku}` | Eliminar |
| GET | `/api/movements?sku=` | Movimientos |
| POST | `/api/movements` | Registrar `INGRESO` / `SALIDA` (422 si el stock no alcanza) |

## Probar con curl

```bash
curl http://localhost:8080/api/products/low-stock

curl -X POST http://localhost:8080/api/movements \
  -H "Content-Type: application/json" \
  -d '{"productSku":"CAP-MED-001","type":"SALIDA","quantity":2,"reason":"Venta","responsible":"Laura"}'
```

## Generar el JAR

```bash
./gradlew bootJar        # build/libs/cappucare-service-0.0.1.jar
java -jar build/libs/cappucare-service-0.0.1.jar
```
