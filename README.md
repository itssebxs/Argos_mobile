# 🛡️ ARGOS — Seguridad personal y comunitaria en el bolsillo

**ARGOS** es una aplicación móvil de seguridad personal construida con Flutter: detecta situaciones de riesgo con los sensores del teléfono, envía alertas SOS con geolocalización a un círculo de personas de confianza («guardianes»), muestra santuarios y rutas sobre un mapa, y mantiene la protección activa en segundo plano, aunque el teléfono esté bloqueado.

- 📱 **Versión (pubspec):** 2.16.7+126
- 📥 **APK oficial:** [última release](https://github.com/itssebxs/Argos_mobile/releases/latest/download/app-argos_direct-release.apk)
- 🌐 **Sitio del proyecto:** [argospage.vercel.app](https://argospage.vercel.app) (repositorio `itssebxs/Argospage`)

> **Aviso de propiedad:** código público ≠ código abierto. Este repositorio es visible con fines de portafolio y revisión; no tiene licencia de uso libre. Todos los derechos reservados — Luis Shagñay (itssebxs). (Ver el aviso legal completo en el README del repositorio.)

---

## El problema que resuelve

En una emergencia real, desbloquear el teléfono, buscar un contacto y explicar dónde estás toma demasiado tiempo — y a veces ni siquiera es posible tocar la pantalla. ARGOS reduce ese momento a cero fricción: el propio teléfono vigila (sensores), el SOS sale con ubicación exacta hacia las personas que elegiste, y la red de guardianes puede ver el desplazamiento en tiempo real sobre el mapa.

## Funcionalidades (verificadas en el código)

La app está organizada por *features* en `lib/features/`, y cada módulo existe como carpeta propia:

- **👁️ Ojo Guardián** (`features/eye_guardian`) — Vigilancia 24/7 con el acelerómetro (`sensors_plus`) desde un servicio en segundo plano (`flutter_background_service`): una sacudida fuerte supera el umbral de magnitud y dispara el protocolo SOS. Incluye candado de concurrencia anti-spam y cooldown entre alertas automáticas para evitar falsos positivos y saturación ante un solo evento físico.
- **🆘 Protocolo SOS blindado** — Las pantallas de confirmación y clasificación del incidente bloquean el retroceso (PopScope): el usuario debe clasificar el incidente o cancelar la alerta explícitamente, y no puede enviarse una segunda alerta mientras una siga activa. La clasificación alimenta las zonas de peligro comunitarias.
- **👪 Círculo de Guardianes** (`features/family_circle`) — Red de contactos de confianza que reciben el SOS con geolocalización en tiempo real y pueden seguir al protegido en el mapa. Incluye el **Modo Travesía**: rastreo de alta frecuencia (cada 5 segundos, precisión alta de ubicación) durante un trayecto, con botón para finalizar el recorrido.
- **🗺️ Mapa, rutas y zonas de peligro** (`features/routes`) — Mapa interactivo con `flutter_map` (OpenStreetMap), motor de rutas OSRM, zonas de peligro que ajustan la valoración de seguridad de la ruta y búsqueda de lugares OSM.
- **🏠 Santuarios automáticos** (`features/santuaries`) — Lugares seguros con geofencing (radio automático de 200 m): el mapa consulta la Overpass API para localizar estaciones de policía, hospitales y refugios reales alrededor, y al registrar un lugar el círculo recibe una notificación.
- **🔐 Cuentas y perfil** (`features/auth`, `features/profile`) — Registro, inicio de sesión y gestión de permisos críticos del dispositivo.
- **🔔 Notificaciones críticas** — OneSignal (API REST + SDK) para push de alta prioridad a los guardianes, y notificaciones locales.
- **🔄 Actualizaciones OTA** — Actualización de la app dentro de la propia app (`ota_update`), con sincronización de versiones mediante Supabase; compilación oficial con GitHub Actions (workflow de release).

## Arquitectura

Arquitectura por *features* («Atomic Features»): cada capacidad vive en su propio silo bajo `lib/features/`, sobre un núcleo común en `lib/core/` (red, temas, componentes de UI `GlassBox`, utilidades), para que un fallo en el mapa no detenga la protección del Ojo Guardián.

- El **servicio en segundo plano** corre en un isolate propio que escucha el acelerómetro, aplica la lógica de decisión (umbral, anti-spam, cooldown de 3 minutos entre SOS automáticos) y se comunica de forma bidireccional con la UI.
- El **flujo de alerta**: sensor → isolate de fondo → inserción de la alerta en Supabase → push de alta prioridad vía OneSignal a los guardianes → el mapa del círculo se actualiza por streams en tiempo real de Supabase.
- **Sistema de diseño «Liquid Glass»**: interfaz glassmorphism con tokens de contraste dinámicos para modo claro y oscuro.

Detalle completo en el propio repositorio: [`TECHNICAL_GUIDE.md`](https://github.com/itssebxs/Argos_mobile/blob/main/TECHNICAL_GUIDE.md) y [`CHANGELOG.md`](https://github.com/itssebxs/Argos_mobile/blob/main/CHANGELOG.md).

## Tecnologías

| Capa | Tecnología |
|---|---|
| App móvil | Flutter 3.x · Dart |
| Sensores | sensors_plus (acelerómetro/giroscopio) |
| Ubicación | geolocator · latlong2 |
| Mapas | flutter_map + OpenStreetMap · Overpass API · OSRM |
| Segundo plano | flutter_background_service · flutter_local_notifications |
| Backend (BaaS) | Supabase (PostgreSQL, Realtime, Auth) — `supabase_flutter` |
| Push | OneSignal — `onesignal_flutter` + API REST |
| Actualizaciones | ota_update · GitHub Actions (release) |
| Configuración | flutter_dotenv (`.env`) · shared_preferences |

## Estructura del repositorio

- `lib/main.dart` (y variantes `main_direct.dart` / `main_store.dart`) — puntos de entrada
- `lib/core/` — núcleo: red, temas, UI modular y utilidades
- `lib/features/` — `auth` · `eye_guardian` · `family_circle` · `profile` · `routes` · `sanctuaries`
- `TECHNICAL_GUIDE.md` — manual de arquitectura del sistema
- `CHANGELOG.md` — historial de versiones
- `.github/` — workflow de compilación y release del APK

## Ejecución local

Requiere un archivo `.env` en la raíz con las claves del proyecto (Supabase y OneSignal; nunca se suben valores al repositorio):

```bash
flutter pub get
flutter run --release
# Build de producción:
flutter build apk --split-per-abi --release
```

## Estado

Aplicación en producción y en evolución continua, con releases públicas del APK en GitHub y sitio web propio.

---

Desarrollado por **Luis Shagñay** — diseño, desarrollo móvil y arquitectura completa de ARGOS. Más proyectos en su [portafolio](https://luisshagnay.vercel.app/proyectos).
