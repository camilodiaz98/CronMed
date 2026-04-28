# CronMed+ 💊

CronMed+ es una aplicación de Android moderna diseñada para ayudar a los usuarios a gestionar sus medicamentos y hábitos de hidratación de manera eficiente. Construida con **Jetpack Compose**, sigue las mejores prácticas de desarrollo en Android, ofreciendo una interfaz fluida, intuitiva y visualmente atractiva.

## 🚀 Características Principales

- **Gestión de Medicamentos:** Agregue, edite y active/desactive medicamentos. Incluye soporte para dosis, frecuencia horaria e imágenes personalizadas.
- **Recordatorios Automáticos:** Sistema de alarmas integrado que notifica al usuario exactamente cuándo debe tomar su medicación.
- **Historial de Tomas:** Registro detallado de cada dosis (tomada, omitida o pospuesta) con marcas de tiempo precisas.
- **Seguimiento de Hidratación:** Registro rápido de consumo de agua con recordatorios configurables para fomentar hábitos saludables.
- **Estadísticas Visuales:** Gráficas semanales de cumplimiento de medicación y niveles de hidratación.
- **Dashboard Inteligente:** Visualización clara del régimen diario y una línea de tiempo de las próximas tomas.

## 🛠️ Tecnologías Utilizadas

- **Lenguaje:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Arquitectura:** MVVM (Model-View-ViewModel)
- **Base de Datos:** Room Database para persistencia local.
- **Inyección de Dependencias:** Gestión manual de ViewModelProvider.Factory.
- **Tareas en Segundo Plano:** WorkManager para la gestión de recordatorios.
- **Carga de Imágenes:** Coil para el procesamiento de fotos de medicamentos.
- **Navegación:** Jetpack Navigation Compose.

## 📂 Estructura del Proyecto

- `data/local`: Entidades de Room, DAOs y configuración de la base de datos.
- `ui/screens`: Pantallas principales (Dashboard, Formulario, Historial).
- `ui/components`: Componentes reutilizables de la interfaz.
- `ui/viewmodel`: Lógica de negocio y gestión de estados de la UI.
- `util`: Clases de utilidad para gestión de alarmas y almacenamiento de imágenes.

## 👤 Desarrollado por
**camilodiaz desarrollo**
