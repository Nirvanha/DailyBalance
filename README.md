# 📊 Daily Balance

Aplicación Android para gestión de gastos diarios y seguimiento de hábitos, construida con las últimas tecnologías de desarrollo Android.

## 📱 Características

### Gestión de Hábitos
- **Registro de acciones**: Cigarrillos, cerveza y comidas
- **Descripciones personalizadas**: Añade notas a tus registros de comida
- **Historial completo**: Visualiza todos tus registros ordenados cronológicamente

### Gestión de Gastos
- **Registro de gastos diarios**: Cantidad, categoría, fecha y notas
- **Categorías inteligentes**: Autocompletado basado en categorías existentes
- **Múltiples orígenes**: Nómina, No Cuenta, Crédito, Eci
- **Historial ordenable**: Ordena por cantidad, categoría, fecha, origen o nota

### Funcionalidades Adicionales
- **Exportación a CSV**: Exporta registros y gastos para análisis externo
- **Tema claro/oscuro**: Interfaz adaptable con soporte para Material You
- **Edge-to-Edge**: Experiencia visual inmersiva

## 🏗️ Arquitectura

### Patrón de Diseño
**MVVM (Model-View-ViewModel)** con principios de Clean Architecture

```
┌─────────────────────────────────────────┐
│              UI Layer                   │
│         (Jetpack Compose)               │
│  HomeScreen, FoodScreen, ExpenseScreen  │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         ViewModel Layer                 │
│  MainViewModel, FoodViewModel, etc.     │
│         (StateFlow + Coroutines)        │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Data Layer                     │
│    Repositories + Room Database         │
│  ActionRecord, DailyExpense entities    │
└─────────────────────────────────────────┘
```

### Capas

#### UI Layer
- **100% Jetpack Compose** - UI declarativa moderna
- **Material 3** - Design system actualizado
- **Dynamic Colors** - Soporte para Material You (Android 12+)

#### Presentation Layer
- **ViewModels**: 6 ViewModels especializados
  - `MainViewModel` - Navegación y estado global
  - `FoodViewModel` - Lógica de registro de comidas
  - `ExpenseViewModel` - Lógica de gastos
  - `RecordsViewModel` - Gestión de registros de acciones
  - `ExpenseRecordsViewModel` - Gestión de registros de gastos
  - `ThemeViewModel` - Gestión del tema
- **StateFlow** - Gestión reactiva del estado
- **Unidirectional Data Flow** - Flujo de datos predecible

#### Data Layer
- **Room Database** - Persistencia local
  - `ActionRecord` - Tabla de registros de acciones
  - `DailyExpense` - Tabla de gastos diarios
- **DataStore** - Preferencias de usuario (tema)
- **Repository Pattern** - Abstracción de fuentes de datos

## 🛠️ Stack Tecnológico

### Lenguaje
- **Kotlin 2.1.21** - 100% del código

### Frameworks y Librerías

#### UI
- Jetpack Compose BOM `2024.09.00`
- Material 3
- Activity Compose `1.8.0`
- Accompanist System UI Controller `0.36.0`

#### Arquitectura
- AndroidX Core KTX `1.17.0`
- Lifecycle Runtime KTX `2.9.4`
- Kotlin Coroutines
- Kotlin Flow

#### Base de Datos
- Room `2.8.2`
  - room-runtime
  - room-ktx (soporte Coroutines)
  - room-compiler (kapt)

#### Inyección de Dependencias
- Hilt `2.57.2` (Dagger)

#### Persistencia
- DataStore Preferences `1.1.7`

#### Testing
- JUnit `4.13.2`
- AndroidX JUnit `1.1.5`
- Espresso `3.7.0`
- Compose UI Test

### Build Tools
- Gradle Kotlin DSL
- AGP `8.13.2`
- Version Catalogs

## 📋 Requisitos

### Mínimos
- **Android 7.0 (API 24)** o superior
- **JDK 11** para compilación

### Recomendados
- **Android 14 (API 36)** para todas las características
- **Android 12+** para Dynamic Colors (Material You)

## 🚀 Instalación y Configuración

### Clonar el Repositorio
```bash
git clone <repository-url>
cd DailyBalance
```

### Compilar el Proyecto
```bash
./gradlew build
```

### Ejecutar en Dispositivo/Emulador
```bash
./gradlew installDebug
```

### Ejecutar Tests
```bash
# Tests unitarios
./gradlew test

# Tests instrumentados
./gradlew connectedAndroidTest
```

## 📁 Estructura del Proyecto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/myfristapplication/
│   │   │   ├── data/                      # Capa de datos
│   │   │   │   ├── ActionRecord.kt        # Entity: Registros de acciones
│   │   │   │   ├── ActionRecordDao.kt     # DAO para ActionRecord
│   │   │   │   ├── ActionRecordRepository.kt
│   │   │   │   ├── DailyExpense.kt        # Entity: Gastos diarios
│   │   │   │   ├── DailyExpenseDao.kt     # DAO para DailyExpense
│   │   │   │   ├── DailyExpenseRepository.kt
│   │   │   │   ├── AppDatabase.kt         # Room Database principal
│   │   │   │   └── ThemePreferences.kt    # DataStore preferencias
│   │   │   ├── di/                        # Inyección de dependencias
│   │   │   │   └── AppModule.kt           # Módulo Hilt
│   │   │   ├── ui/                        # Capa de presentación
│   │   │   │   ├── expense/               # Pantalla de gastos
│   │   │   │   ├── food/                  # Pantalla de comida
│   │   │   │   ├── home/                  # Pantalla principal
│   │   │   │   ├── records/               # Pantallas de registros
│   │   │   │   ├── shared/                # Componentes compartidos
│   │   │   │   ├── theme/                 # Tema y estilos
│   │   │   │   └── previews/              # Previews de Compose
│   │   │   ├── viewmodel/                 # ViewModels
│   │   │   │   ├── ExpenseRecordsViewModel.kt
│   │   │   │   ├── ExpenseViewModel.kt
│   │   │   │   ├── FoodViewModel.kt
│   │   │   │   ├── RecordsViewModel.kt
│   │   │   │   └── ThemeViewModel.kt
│   │   │   ├── MainActivity.kt            # Activity principal
│   │   │   ├── MainViewModel.kt           # ViewModel principal
│   │   │   └── MyApplication.kt           # Application class
│   │   └── res/                           # Recursos
│   │       ├── drawable/                  # Iconos
│   │       ├── values/                    # Strings, colors, themes
│   │       └── xml/                       # Backup rules
│   ├── test/                              # Tests unitarios
│   └── androidTest/                       # Tests instrumentados
├── schemas/                               # Room database schemas
│   └── com.example.myfristapplication.data.AppDatabase/
│       ├── 1.json                         # Schema v1
│       ├── 2.json                         # Schema v2
│       └── 3.json                         # Schema v3 (actual)
├── build.gradle.kts                       # Build del módulo
└── proguard-rules.pro                     # Reglas ProGuard
```

## 🗄️ Base de Datos

### Schema Actual (v3)

#### Tabla: `action_record`
| Columna | Tipo | Descripción |
|---------|------|-------------|
| `id` | INTEGER | Primary key (autoincrement) |
| `type` | TEXT | Tipo: "cigarette", "beer", "comida" |
| `timestamp` | INTEGER | Timestamp en milisegundos |
| `description` | TEXT | Descripción (opcional, para comida) |

#### Tabla: `daily_expense`
| Columna | Tipo | Descripción |
|---------|------|-------------|
| `id` | INTEGER | Primary key (autoincrement) |
| `amount` | REAL | Cantidad del gasto |
| `category` | TEXT | Categoría del gasto |
| `date` | INTEGER | Timestamp en milisegundos |
| `note` | TEXT | Nota adicional (opcional) |
| `origin` | TEXT | Origen: Nomina/NoCuenta/Credito/Eci |

### Migraciones
- **v1 → v2**: Añadida columna `description` a `action_record`
- **v2 → v3**: Creada tabla `daily_expense`

## 🎨 UI/UX

### Pantallas

#### 🏠 Home
- Botones para registrar acciones rápidas
- Acceso a historial de registros y gastos
- Opción de eliminar todos los registros

#### 🍔 Food
- Campo de descripción de comida
- Registro rápido con timestamp automático

#### 💰 Daily Expense
- Formulario completo de gasto
- Validación de campos
- Autocompletado de categorías
- Selector de origen

#### 📋 Records
- Lista de todos los registros de acciones
- Ordenado por fecha (más reciente primero)
- Exportación a CSV

#### 💸 Expense Records
- Tabla completa de gastos
- Ordenamiento por cualquier columna
- Filtrado visual
- Exportación a CSV

### Navegación
Sistema de navegación basado en estado gestionado por `MainViewModel`

### Temas
- **Modo claro/oscuro** con persistencia
- **Dynamic Colors** (Material You) en Android 12+
- **Preferencias guardadas** en DataStore

## 🔧 Configuración

### Gradle
El proyecto usa **Version Catalogs** para gestión centralizada de dependencias en `gradle/libs.versions.toml`

### ProGuard
Reglas básicas incluidas para builds de release (actualmente deshabilitado)

### KAPT
Configurado para Room con exportación de schemas a `/app/schemas/`

## 📊 Exportación de Datos

### Formato CSV

#### Registros de Acciones
```csv
Tipo,Fecha,Descripción
cigarette,2024/01/21 10:30:00,
beer,2024/01/21 14:15:00,
comida,2024/01/21 13:00:00,Pizza Margherita
```

#### Gastos Diarios
```csv
Cantidad,Categoría,Fecha,Origen,Nota
25.50,Comida,2024/01/21 12:00:00,NoCuenta,Almuerzo
150.00,Transporte,2024/01/21 08:00:00,Nomina,Gasolina
```

### Storage Access Framework (SAF)
La app usa SAF para permitir al usuario elegir la ubicación de exportación

## 🧪 Testing

### Estado Actual
- Infraestructura de testing configurada
- Tests de ejemplo incluidos
- **Pendiente**: Implementación de tests comprehensivos

### Tests Planificados
- Unit tests para ViewModels
- Integration tests para Repositories
- UI tests para Composables
- Database tests para DAOs

## 📝 Convenciones de Código

### Nomenclatura
- **Screens**: `*Screen.kt`
- **ViewModels**: `*ViewModel.kt`
- **DAOs**: `*Dao.kt`
- **Repositories**: `*Repository.kt`
- **Entities**: Nombres descriptivos sin sufijo

### Estilo
- Kotlin official code style
- Indentación: 4 espacios
- Max line length: 120 caracteres

## 🐛 Issues Conocidos

Ver `IMPROVEMENTS.md` para lista completa de mejoras pendientes y refactorizaciones planeadas.

## 📚 Recursos de Aprendizaje

Ver `doc/roadmpa_kotlin.md` para recursos de aprendizaje de Kotlin

## 🤝 Contribuir

### Proceso
1. Fork del repositorio
2. Crear branch de feature (`git checkout -b feature/AmazingFeature`)
3. Commit de cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

### Antes de Contribuir
- Ejecutar tests
- Seguir convenciones de código
- Actualizar documentación si es necesario

## 📄 Licencia

[Especificar licencia]

## ✍️ Autor

[Tu nombre/organización]

## 🙏 Agradecimientos

- Equipo de Android Jetpack
- Comunidad de Kotlin
- Contribuidores del proyecto

---

**Versión**: 1.0  
**Última actualización**: Enero 2026  
**Min SDK**: 24 (Android 7.0)  
**Target SDK**: 36 (Android 14+)
