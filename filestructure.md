C:\Users\s152717\Documents\intelliJ projects\JetFighterGame\src\nl\NG\Jetfightergame
│   Identity.java					A class for universal ID generation
│   Player.java						THE Player
│   
├───AbstractEntities				All generic classes of entities
│   └───Hitbox						The functional code of collision detection
│           
├───ArtificalIntelligence			Currently unused, controllers for rockets and other AI will be added in the future
│       
├───Assets							All implementations of Entities and other things that make the game
│   ├───FighterJets					Implementations for jets
│   ├───GeneralEntities				Various entities not belonging to other catagories
│   ├───Scenarios					Maps and test-environments
│   ├───Shapes						Primitive models created using CustomShape or other software-based tools (unlike meshes)
│   ├───Weapons						All weapons (and maybe power-up) implementations
│   └───WorldObjects				    Shapes like tunnels and caves, to be used in Scenarios.
│           
├───Camera							All possible camera implementations and tools for camera usage
│       
├───Controllers						Various settings for input setup, including xbox controller support
│   └───InputHandling				A set of tools for input tracking and handling. Includes tools for subscribing for input
│           
├───Engine							The initialisation, gameloop and the global timer
│       
├───GameState						The world management, including collision detection and new entity spawn management
│       
├───Launcher						    The client launcher
│       
├───Primitives						Surfaces of models, includes primitive collision detection
│           
├───Rendering						All functionality for rendering the gamestate, including the main screen and material properties
│   ├───MatrixStack					All implementations of imitations of an GL object. These are used for transforming and mappings.
│   ├───Particles					Particle rendering and construction
│   └───Shaders						All shader implementations, excluding the particle shader
│           
├───ScreenOverlay					Everything that can be drawn on top of the 3D environment, including the implementation of the main menu and the HUD
│   ├───HUD							The actual heads-up display, including functionality for tracking objects in 3D
│   └───userinterface				all functionality for the menu
│           
├───ServerNetwork					Connection handling between server and client. Mostly, the server functionality is implemented here
│       
├───Settings						    A collection of utility classes where all settings are collected
│       
├───ShapeCreation					All code for loading and making models
│       
├───Sound							This doesn't work yet.
│       
└───Tools							A whole bunch of utility classes
    ├───DataStructures				Some custom data structures for various purposes
    ├───Interpolation				Mostly used for rendering, these structures allow interpolating and extraposating on ANY list of objects (if the given implementation exists)
    ├───Tracked						A datastructure with a 'current' and a 'previous'. There are multiple levels of abstractions available
    └───Vectors						All custom vectors and a color class