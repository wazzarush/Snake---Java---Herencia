# Snake en Java — Actividad Formativa 3: Herencia

Proyecto de la Unidad 2 (juego Snake en Java con Swing) extendido en la
Unidad 3 aplicando **herencia** y **reutilización de código**.

## Cómo ejecutar

```bash
javac -d out *.java
java -cp out Main
```

Al ejecutar `Main`, primero se imprime en consola una prueba que demuestra
la herencia (instancia objetos de las subclases y llama a sus métodos),
y después se abre la ventana del juego, donde la herencia ya está
integrada en la mecánica de juego.

## Clases derivadas creadas para esta actividad

### Jerarquía 1 — `Comida` (clase base)
| Clase | Relación | Qué agrega / sobrescribe |
|---|---|---|
| `Comida` | Clase base | `posicion`, `random`, `valorPuntos` (protected). Métodos `getPuntos()`, `getColor()`, `getNombre()` pensados para ser sobrescritos. `generar(...)` se reutiliza tal cual en todas las subclases. |
| `ComidaEspecial extends Comida` | Derivada | Sobrescribe `getPuntos()` (x3), `getColor()` (dorado), `getNombre()`. Agrega atributo/métodos propios: `tiempoVidaTicks`, `avanzarTick()`, `estaPorExpirar()`, `expiro()`. |
| `ComidaVenenosa extends Comida` | Derivada | Sobrescribe `getPuntos()` (negativo), `getColor()` (morado), `getNombre()`. Agrega atributo/método propio: `segmentosAQuitar`, `getSegmentosAQuitar()`. |

### Jerarquía 2 — `Serpiente` (clase base)
| Clase | Relación | Qué agrega / sobrescribe |
|---|---|---|
| `Serpiente` | Clase base | `cuerpo` y `direccion` (protected). Toda la lógica de movimiento y colisión. |
| `SerpienteFantasma extends Serpiente` | Derivada | Sobrescribe `colisionaConsigoMisma()` (puede atravesar su cuerpo mientras el modo está activo, reutilizando `super.colisionaConsigoMisma()` cuando está apagado). Agrega: `modoFantasmaActivo`, `ticksRestantes`, `activarModoFantasma()`, `actualizarModoFantasma()`. |

### Integración en el juego (`SnakePanel`)
- El campo `comida` es de tipo **base** `Comida`, pero en tiempo de
  ejecución puede ser `Comida`, `ComidaEspecial` o `ComidaVenenosa`
  (polimorfismo). El panel siempre llama a los mismos métodos
  (`getPuntos()`, `getColor()`, `getNombre()`) y cada subclase responde
  distinto.
- El campo `serpiente` es de tipo **base** `Serpiente`, pero se instancia
  como `SerpienteFantasma`. Al comer una `ComidaEspecial` se activa el
  modo fantasma automáticamente.

## Estructura de archivos
```
Posicion.java
Comida.java              (clase base)
ComidaEspecial.java      (clase derivada)
ComidaVenenosa.java      (clase derivada)
Serpiente.java           (clase base)
SerpienteFantasma.java   (clase derivada)
SnakePanel.java
SnakeFrame.java
Main.java
```
