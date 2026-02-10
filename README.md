# ♟️ EnroqueApp - Gestión de Torneos de Ajedrez

<p align="center">
<img width="800" height="600" alt="EnroqueApp" src="https://res.cloudinary.com/drnzeqcpu/image/upload/v1770751045/Enroque_App_c0xp7g.png" />

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/firebase-a08021?style=for-the-badge&logo=firebase&logoColor=ffcd34" />
</p>

**EnroqueApp** es una aplicación Android moderna diseñada para gestionar y dar seguimiento a torneos de ajedrez.

---

## Funcionalidades principales

### Usuarios y roles
- **Login/Register** con Firebase Authentication.
- **Roles:** 
  - **Aficionados:** Pueden marcar torneos como favoritos y seguir el progreso de las partidas.
  - **Jugadores:** Pueden inscribirse en torneos, marcar favoritos y seguir el progreso de las partidas.
  - **Administradores:** Pueden crear y gestionar torneos, administrar ubicaciones en el mapa, validar inscripciones y cargar movimientos en tiempo real.

### Gestión de torneos
- **Ciclo de vida:** Los torneos pasan por estados de *Próximo*, *Activo*, *Suspendido* y *Finalizado*.
- **Fixture automático:** Generación automática de partidas según la cantidad de inscriptos (2 a 8 jugadores).
- **Inscripciones:** Control de cupos con validación por parte del administrador.

### Seguimiento de partidas
- **Teclado de movimientos:** Interfaz personalizada para la carga de movimientos técnicos de ajedrez.
- **Visualización en tiempo real:** Resultados y movimientos actualizados dinámicamente.
- **Historial de partidas:** Almacenamiento y consulta del registro histórico de partidas por torneo.

### Mapa interactivo
- **Google Maps API:** Visualización de sedes de torneos y comercios asociados.
- **Geolocalización:** Marcadores personalizados con detalles de ubicación de torneos y beneficios de comercios.

---

## Tecnologías y librerías

- **Lenguaje:** Kotlin
- **Arquitectura:** MVVM (Model-View-ViewModel) con Clean Architecture.
- **Backend:** 
  - **Firebase Auth:** Autenticación de usuarios.
  - **Cloud Firestore:** Base de datos NoSQL en tiempo real para torneos, partidas y usuarios.
- **UI / Frontend:**
  - **Jetpack Compose:** Para componentes modernos y dinámicos.
  - **XML Layouts:** Con Material Components para una UI robusta y coherente.
  - **ViewBinding:** Para una interacción segura con las vistas.
- **Servicios:** **Google Maps Platform** para el mapa interactivo.
- **Navegación:** **Jetpack Navigation Component** con Safe Args para paso de datos entre pantallas.
- **Asincronía:** Corrutinas de Kotlin y Flow para flujos de datos reactivos.

---

## Capturas de pantalla
<p align="center">
  <img width="150" height="320" alt="Login" src="https://res.cloudinary.com/drnzeqcpu/image/upload/v1770655682/login_fh0mx3.png" />
  <img width="150" height="320" alt="Drawer" src="https://res.cloudinary.com/drnzeqcpu/image/upload/v1770751131/drawer_tpc7td.png" />
  <img width="150" height="320" alt="Perfil" src="https://res.cloudinary.com/drnzeqcpu/image/upload/v1770655684/perfil_byfmwk.png" />
  <img width="150" height="320" alt="Mapa interactivo" src="https://res.cloudinary.com/drnzeqcpu/image/upload/v1770655688/mapa_interactivo_pgv9p7.png" />
  <img width="150" height="320" alt="Torneos" src="https://res.cloudinary.com/drnzeqcpu/image/upload/v1770655681/torneos_zfq8pr.png" />
</p>
<p align="center">
  <img width="150" height="320" alt="Detalle torneo" src="https://res.cloudinary.com/drnzeqcpu/image/upload/v1770655684/detalle_torneo_bbu89c.png" />
  <img width="150" height="320" alt="Fixture" src="https://res.cloudinary.com/drnzeqcpu/image/upload/v1770655681/fixture_rztvwf.png" />
  <img width="150" height="320" alt="Detalle partida" src="https://res.cloudinary.com/drnzeqcpu/image/upload/v1770655682/detalle_partida_irqcgz.png" />
  <img width="150" height="320" alt="Movimientos" src="https://res.cloudinary.com/drnzeqcpu/image/upload/v1770655683/movimientos_efgl8z.png" />
  <img width="150" height="320" alt="Inscripciones" src="https://res.cloudinary.com/drnzeqcpu/image/upload/v1770655683/inscripciones_fk4aog.png" />
</p>


---

## Estructura del proyecto
- `ui/`: Fragmentos, ViewModels y Adapters organizados por módulos (torneos, fixture, mapa, movimientos).
- `data/`: Repositorios para la gestión de datos con Firebase.
- `model/`: Clases de datos (Torneo, Partida, Usuario, Marcador).

---
