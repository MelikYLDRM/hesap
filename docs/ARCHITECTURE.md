# Hesap - Architecture Guide

## Current Module Structure (Single Module)
```
app/
├── data/           # Data layer
│   ├── local/      # DataStore, Room DAOs
│   ├── remote/     # Retrofit API services
│   └── repository/ # Repository implementations
├── domain/         # Domain layer
│   ├── engine/     # Calculator & Finance engines
│   ├── model/      # Data models & entities
│   └── repository/ # Repository interfaces
├── ui/             # Presentation layer
│   ├── components/ # Reusable Compose components
│   ├── navigation/ # NavGraph & Screen definitions
│   ├── screens/    # Feature screens
│   └── theme/      # Theme, colors, typography
├── ads/            # AdMob integration
├── speech/         # Speech recognition
├── update/         # In-app update
└── widget/         # Glance widgets
```

## Planned Multi-Module Structure
```
:core:model          - Data models shared across modules
:core:data           - Repository implementations, database
:core:network        - Retrofit API services
:core:ui             - Shared Compose components, theme
:core:common         - Utility functions, extensions

:feature:calculator  - Basic & scientific calculator
:feature:finance     - Financial calculations
:feature:converter   - Unit converter
:feature:exchange    - Currency exchange
:feature:history     - History management
:feature:settings    - App settings

:app                 - Main app module, navigation, DI
```

## Key Design Patterns
- **MVVM** with StateFlow for reactive UI
- **Repository Pattern** for data abstraction
- **Hilt** for dependency injection
- **Room** for local persistence
- **Compose** for declarative UI

## Testing Strategy
- Unit tests: `CalculatorEngineTest`, `FinanceCalculatorTest`
- UI tests: Compose test rules (planned)
- Integration tests: Repository + DAO (planned)

