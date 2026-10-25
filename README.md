# Cappucare 📦💊

Aplicación móvil Android para el **control inteligente y oportuno de existencias** de productos e insumos médicos, desarrollada como proyecto semestral de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC, Escuela de Informática y Telecomunicaciones, CITT San Bernardo).

> ⚠️ Prototipo funcional académico. Todos los datos (productos, movimientos, ventas, usuarios) son **ficticios**. No se conecta a sistemas productivos de Cappucare.

---

## 📌 Contexto del caso

Cappucare vende productos e insumos médicos y necesita mantener un control preciso de sus existencias. Hoy la información puede quedar distribuida o desactualizada, lo que genera diferencias entre el stock registrado y el real, omisiones en movimientos, quiebres de stock y compras innecesarias.

**Objetivo de la app:** que el personal autorizado pueda, desde un dispositivo Android:

- Consultar la disponibilidad real de cada producto.
- Registrar ingresos y salidas (ventas) con trazabilidad.
- Detectar anticipadamente productos con stock bajo o próximos a vencer.
- Identificar productos rápidamente mediante códigos QR / de barras con la cámara.

---

## ✨ Funcionalidades

| Módulo | Descripción |
|---|---|
| **Inicio de sesión** | Pantalla de acceso con correo y contraseña (acceso simulado en este prototipo). |
| **Inicio (Home)** | Saludo al usuario, alerta de productos con stock bajo, resumen de inventario y accesos rápidos. |
| **Inventario** | Listado de productos con búsqueda (nombre, SKU o código de barras), filtro por categoría y resumen. Acciones rápidas: *Registrar venta* o *Pedir reposición* según el nivel de stock. |
| **Detalle de producto** | Ficha completa: información general, lotes y vencimientos, información médica (indicaciones, dosis, contraindicaciones) e historial de movimientos del producto. Permite editar y eliminar. |
| **Registro de movimientos** | Ingreso o salida (venta) con cantidad, motivo y responsable. Valida que no se pueda dejar el stock en negativo. |
| **Agregar / editar producto** | Formulario con validaciones por campo, listas desplegables (categoría y unidad), selector de fecha de vencimiento y número de lote. |
| **Escáner** | Lectura de códigos **QR, EAN-13 y EAN-8** con CameraX + ML Kit, linterna y entrada manual del código. Si el producto no existe, ofrece registrarlo con el código ya cargado. |
| **Historial** | Registro de todos los movimientos con búsqueda y filtro por categoría. |
| **Perfil** | Datos del usuario, actividad y cierre de sesión con confirmación. |

### Reglas de negocio implementadas

- Un **SKU** es único; no se pueden registrar dos productos con el mismo código.
- Una **salida** no puede superar el stock disponible.
- Un producto está en **stock bajo** cuando `stock ≤ stock mínimo`.
- Estado de **vencimiento**: vigente, por vencer (dentro de 60 días) o vencido.
- Cada movimiento queda registrado con fecha, tipo (ingreso/salida), cantidad, motivo y responsable.
- Los cambios de stock y el registro del movimiento se guardan en una **transacción** (ambos o ninguno).

---

## 🏗️ Arquitectura

El proyecto sigue el patrón **MVVM** con **Jetpack Compose** y **Material 3**.

```
app/src/main/java/com/duoc/cappucare/
├── MainActivity.kt
├── data/                  # Capa de datos (Room)
│   ├── AppDatabase.kt
│   ├── CappucareDao.kt
│   ├── ProductEntity.kt
│   ├── MovementEntity.kt
│   └── LotEntity.kt
├── model/
│   └── ProductForm.kt     # Estado, errores y validaciones del formulario
├── viewmodel/
│   ├── CappucareViewmodel.kt      # Estado global: productos, stock bajo, movimientos
│   └── ProductFormViewModel.kt    # Lógica del formulario agregar/editar
└── ui/
    ├── components/        # Componentes reutilizables (AppLogoHeader)
    ├── navigation/        # AppNavHost (rutas) y AppBottomBar
    ├── screens/           # Login, Home, Inventory, ProductDetail, ProductForm,
    │                      # Scanner, History, Profile
    └── theme/             # Color, Type, Theme
```

**Flujo de datos:** `Room (DAO) → Flow → ViewModel (StateFlow) → collectAsState() → Composable`.

### Modelo de datos

- **`products`**: sku (PK), nombre, categoría, stock, stock mínimo, ubicación, vencimiento, código de barras, precio, marca, unidad, imagen, indicaciones, dosis y contraindicaciones.
- **`movements`**: id, productSku (FK), fecha, tipo (`INGRESO` / `SALIDA`), cantidad, motivo y responsable.
- **`lots`**: id, productSku (FK), número de lote, vencimiento, cantidad y ubicación.

Las relaciones usan `ON DELETE CASCADE`: al eliminar un producto se eliminan sus lotes y movimientos.

---

## 🧰 Tecnologías

- **Lenguaje:** Kotlin 2.1
- **UI:** Jetpack Compose + Material 3 (BOM 2026.02.01), Material Icons Extended
- **Navegación:** Navigation Compose 2.9.8
- **Persistencia local:** Room 2.6.1 (con KSP)
- **Estado:** ViewModel + StateFlow / Flow
- **Cámara y escaneo:** CameraX 1.6.1 + ML Kit Barcode Scanning 17.3.0
- **Imágenes:** Coil 2.7.0
- **Android Gradle Plugin:** 9.4.1
- **SDK:** minSdk 28 · targetSdk / compileSdk 37

---

## 🚀 Cómo ejecutar el proyecto

### Requisitos

- Android Studio actualizado (compatible con AGP 9.4.x)
- JDK 11 o superior
- Emulador (Device Manager) o dispositivo físico Android 9+ (API 28+)
- Para probar el escáner se recomienda un **dispositivo físico** con cámara

### Pasos

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/<usuario>/Ev2_Cappucare.git
   ```
2. Abrir la carpeta en Android Studio y esperar la sincronización de Gradle.
3. Seleccionar un dispositivo o emulador.
4. Ejecutar con ▶ **Run 'app'**.
5. En la pantalla de acceso, pulsar **Iniciar Sesión** (el acceso es simulado).

Al primer arranque la base de datos se **carga automáticamente con productos de ejemplo** ficticios (paracetamol, guantes de nitrilo, mascarillas, alcohol gel, jeringas), algunos con stock bajo para visualizar las alertas.

### Permisos

| Permiso | Uso |
|---|---|
| `CAMERA` | Escaneo de códigos QR y de barras (se solicita al abrir el escáner). |
| `POST_NOTIFICATIONS` | Declarado para avisos de stock mínimo (ver *Próximos pasos*). |

---

## 🗺️ Estado del proyecto y próximos pasos

**Implementado:** login simulado, navegación con barra inferior, inventario con búsqueda y filtros, detalle de producto, registro de movimientos con validación de stock, formulario con validaciones, escáner QR/códigos de barras, historial, perfil y persistencia local con Room.

**Pendiente / en desarrollo:**

- [ ] Selección de imagen del producto desde cámara y galería (el formulario ya muestra la imagen con Coil; falta conectar los lanzadores).
- [ ] Notificaciones de stock mínimo (permiso declarado; WorkManager ya está como dependencia).
- [ ] Autenticación y perfiles de usuario reales (hoy el acceso es simulado y los datos del perfil son de ejemplo).
- [ ] Diseño adaptativo para distintos tamaños de pantalla (Window Size Classes).
- [ ] Persistencia de preferencias con DataStore.
- [ ] Pruebas unitarias de las validaciones (`validateProductForm`) y del ViewModel.
- [ ] Integración futura con procesos de ventas, compras y facturación.

---

## 👥 Equipo

| Integrante | Rol |
|---|---|
| _Nombre 1_ | _Por completar_ |
| _Nombre 2_ | _Por completar_ |
| _Nombre 3_ | _Por completar_ |

**Docente / coordinadora:** Paz Morales — CITT San Bernardo, Duoc UC

**Gestión del trabajo:** repositorio en GitHub (ramas por *feature* y commits descriptivos) y tablero Trello (*Por hacer · En curso · Finalizado*).

---

## 📄 Licencia y uso

Proyecto con fines exclusivamente académicos. Todos los datos son ficticios o anonimizados y la aplicación no está destinada a publicación ni a uso en producción.
