# Replit prompt — paste this into a Replit Agent/Ghostwriter chat

I have a Fabric mod project targeting three Minecraft versions: 26.1.2, 1.21.4,
and 1.20.6. For EACH of these three versions, please look up on Modrinth
(https://modrinth.com/mod/fabric-api/versions and
https://modrinth.com/mod/cloth-config/versions) and Fabric's own site
(https://fabricmc.net/) the CURRENT correct values for:

1. The latest stable Fabric API version string that supports that exact
   Minecraft version (format looks like "0.148.2+26.1.2")
2. The latest stable Fabric Loader version number (format like "0.18.4")
3. The recommended Fabric Loom Gradle plugin version for that Minecraft
   version (format like "1.15-SNAPSHOT")

Give me the results as a simple table (Minecraft version | Fabric API | Fabric
Loader | Loom version), and also tell me if Fabric has switched that version
to a different mappings system than "official Mojang mappings" (loom.officialMojangMappings()) —
if so, tell me what to use instead.

I'll paste your table's values into the `dependencies { }` block and the
`plugins { id 'fabric-loom' version '...' }` line of each version's
build.gradle file myself.
