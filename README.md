# Pokémon Stadium Lite

## Taller de Desarrollo de Software III

Mini-aplicación de escritorio en Java Swing que simula el combate
estilo Pokémon Stadium entre dos Pokémon obtenidos en vivo desde PokeAPI. Esta app muestra imagen, datos básicos y ejecuta un combate por turnos con registro de
eventos.

## ¿Cómo funciona?

Cada jugador escoge su Pokémon escribiendo el nombre y dándole a **Load**, o le da a **Random** para que salga uno al azar. Cuando los dos ya están cargados se activa el botón **Fight!**

Al presionarse el botón **Fight!** el combate empieza, se va mostrando turno por turno en el log, mientras las barras de vida bajan. Y muestra el ganador

Si se escribe un nombre erróneo, sale un mensaje diciendo **No existe ningún Pokémon llamado ...**

## Cómo ejecutarlo

Se necesita internet, porque los Pokémon se descargan de la PokeAPI.

1. Abrir la carpeta del proyecto en IntelliJ (`File → Open`).
2. Revisar que tenga un JDK 11 o más nuevo en `File → Project Structure → Project`.
3. Revisar que esté la librería JSON en `File → Project Structure → Libraries`. Debe aparecer `json-20230227`. Si no está, se agrega con `+ → Java` escogiendo el archivo `lib/json-20230227.jar`.
4. En `File → Settings → Editor → GUI Designer`, dejar la opción `Generate GUI into` en `Binary class files`. Esto es necesario para que IntelliJ arme la ventana a partir del archivo `.form`.
5. Abrir `src/pokestadium/Main.java` y darle al botón verde ▶ al lado de `main`.

Si al abrir sale un error `NullPointerException`, casi siempre es porque no se generó la ventana del `.form`. Se arregla revisando el paso 4 y usando `Build → Rebuild Project`.

## Cómo está organizado

```
src/pokestadium/
├── Main.java                  Abre la ventana
├── model/
│   └── Pokemon.java           Los datos del Pokémon y su vida
├── api/
│   ├── PokeApiClient.java     Pide los Pokémon a la PokeAPI y lee el JSON
│   └── PokemonNotFoundException.java   Error cuando el Pokémon no existe
├── battle/
│   ├── Battle.java            Las reglas del combate
│   └── BattleListener.java    Los avisos que da el combate
└── ui/
    ├── BattleWindow.form      El diseño de la ventana
    └── BattleWindow.java      Lo que hace la ventana
```

## Diseño

Separé el proyecto en cuatro paquetes para que cada parte se encargara de una sola cosa: 

**`model`** guarda los datos del Pokémon

**`api`** se conecta con la PokeAPI

**`battle`** tiene las reglas del combate

**`ui`** es la ventana, esta es la única parte que usa Swing. Ninguna de las tres anteriores sabe nada de la ventana. Gracias a eso pude ir probando cada parte en consola antes de hacer la interfaz: primero el modelo, luego la conexión con la API y después un combate completo entre squirtle y charmander. Cuando ya funcionaba todo, conecté la ventana sin tener que cambiarle nada al combate.

Para que el combate pudiera avisar lo que pasa sin depender de la ventana, hice la interfaz `BattleListener` (esto se conoce como patrón Observer). `Battle` va avisando cuando alguien ataca, cuando cambia la vida y cuando termina, y la ventana recibe esos avisos y los muestra. En este proyecto las tareas lentas (cargar un Pokémon y el combate con sus pausas) se hacen en otro hilo con `SwingWorker`. Como la ventana solo se puede modificar desde su propio hilo, los avisos del combate la actualizan usando `SwingUtilities.invokeLater`.

## Fórmula de daño

En cada turno un Pokémon ataca y el otro defiende. Empieza el que tenga más velocidad (SPD), y si empatan se escoge al azar.

```
base = ATK × random(0 a 1) − DEF × random(0 a 1)
daño = base × efectividad × crítico   (redondeado)
```

- **Efectividad:** se mira solo el primer tipo de cada Pokémon. **Agua le gana a fuego, fuego a planta y planta a agua**: si gana el que ataca, el daño se multiplica por 1.3; si es al revés, por 0.7; y en cualquier otro caso queda igual (×1.0).
- **Crítico:** hay un 10 % de probabilidad de que el golpe haga 1.5 veces más daño.
- **Daño mínimo de 1:** lo agregué porque, cuando la defensa es más alta que el ataque, la resta puede dar negativa, y el golpe terminaría curando al otro Pokémon. Por eso, si la base da menos de 1, se deja en 1. En una prueba, charmander (ATK 52) atacó a squirtle (DEF 65) y le salió un crítico de solo 1 de daño: la base dio negativa, se subió a 1, y 1 × 0.7 × 1.5 = 1.05, que redondeado da 1.
- **La vida nunca baja de 0**, y el combate termina cuando uno de los dos llega a 0. Al empezar cada combate se recupera la vida de ambos, así que se puede hacer revancha.

Un caso que tuve en cuenta: si los dos jugadores escogen el mismo Pokémon (por ejemplo pikachu contra pikachu), la ventana no sabría qué barra actualizar, porque los avisos del combate usan el nombre. Por eso, en ese caso, el combate los llama "pikachu (J1)" y "pikachu (J2)".

### Realizado por: 
   Maria Alejandra Bernal 1913234-3743