# SwapNDie
**by RC RAJA GAMER 2.0**

Trap-building deathswap/PvP minigame plugin for Paper, targeting Minecraft **26.1.2 and up** (Mojang's new year-based versioning — 26.1.2 → 26.2 → 26.3...).

**Version note:** `pom.xml` points at an open-ended paper-api range (`[26.1.2.build,)`), so this same project keeps compiling and working as Mojang ships 26.2, 26.3, and beyond — no pom changes needed for future 26.x drops. It does **not** run on pre-26 servers (1.21.x and earlier) — that's a different, older API epoch entirely (different Java version too: 26.x needs Java 25, 1.21.x used Java 21), so a single jar can't straddle both. If you still run a 1.21.x server, that needs its own separately-built jar off an older paper-api version — say if you need that too.

## Kaam kaise karta hai
1. `/swapndie start` — naya trap slot banega (game1, game2...), tumko dur teleport karke Creative de dega, aur area flatten ho jayega.
2. `/swapndie additem spawn` aur `/swapndie additem win` — dono custom item milenge.
   - **Spawn Setter** (red bed) ko jaha rakhna hai opponent spawn hoga, waha right-click karo.
   - **Victory Trigger** (emerald block) ko jaha touch karke opponent jeetega, waha right-click karo.
3. Dono set hote hi trap "ARMED" ho jayega aur 5-min (customizable) countdown start ho jayega. Last 10 second me fullscreen number popup dikhega.
4. Timer khatam hote hi opponent tumhare trap me spawn ho jayega, 3 lives (customizable) ke saath.
5. Opponent Victory Trigger tak pahuncha to jeet gaya (green popup), 3 baar mara to haar gaya (red popup) — score automatically save ho jata hai.

## Commands
Run `/swapndie help` in-game for the full list.

## Mobile se compile kaise kare (No PC needed)
1. Ye poora folder GitHub repo me upload karo (GitHub app / mobile browser se).
2. Repo me already `.github/workflows/build.yml` hai (JDK 25 pe set hai, jo 26.x ke liye required hai) — jaise hi push karoge, GitHub Actions automatically Maven build chalayega.
3. Actions tab me jaake finished run kholo → **Artifacts** section me `SwapNDie.jar` milega, download karke apne server ke `plugins/` folder me daal do.

## Config
`src/main/resources/config.yml` me sab customizable hai: flatten radius, timer duration, lives, messages, aur integrations (WeaponMechanics guns, TurboVehicles/InfiniteVehicles cars, MythicMobs bosses, ItemsAdder/Oraxen custom items, Ghostblocks) — jo plugin server pe installed ho wahi enable karo `integrations:` section me.

## Dependencies (optional — sirf agar unka feature chahiye)
- WeaponMechanics (guns)
- TurboVehicles ya InfiniteVehicles (cars)
- MythicMobs (custom bosses)
- ItemsAdder ya Oraxen (custom textured items)
- Ghostblocks + ProtocolLib (ghost blocks)

Ye sab **soft-dependencies** hain — install nahi hai to bas wo feature disable rahega, baaki plugin normally chalega.

