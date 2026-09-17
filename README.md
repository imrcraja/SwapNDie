# SwapNDie
**by RC RAJA GAMER 2.0**

Trap-building deathswap/PvP minigame. Ye repo do hisso me bata hai:

- **`modules/`** — server-side **Paper plugin** (saara gameplay logic — traps, timer, lives, score, guns, car, boss, ghost block). Isi ka kaam hai.
- **`mod/`** — client-side **Fabric mod** (sirf ek chota add-on) — jo Creative inventory me ek **real "SwapNDie" tab** dikhata hai jisme sab custom item ready milte hain. Server pe install nahi karna, sirf player ke Minecraft client me.

**Dono milke complete package hain** — plugin bina mod ke bhi 100% chalta hai (`/swapndie catalog` GUI se sab item mil jate hain), mod sirf ek extra convenience hai jo real vanilla Creative-tab jaisa feel deta hai. Jab player mod ke tab se item click karta hai, Minecraft ka apna vanilla protocol wahi item (sahi NBT tag ke saath) server ko bhejta hai, aur plugin usko waise hi samajh leta hai jaise apne catalog se diya ho — koi extra server-side setup nahi.

## Teen version, teen jodiyan (triads)
| Minecraft version | Plugin (server) | Mod (client) |
|---|---|---|
| 26.1.2+ | `modules/modern-26.1.2` → `SwapNDie-26.1.2.jar` | `mod/fabric-26.1.2` |
| 1.21.4 | `modules/legacy-1.21.4` → `SwapNDie-1.21.4.jar` | `mod/fabric-1.21.4` |
| 1.20.6 | `modules/legacy-1.20.6` → `SwapNDie-1.20.6.jar` | `mod/fabric-1.20.6` |

Server pe jo bhi plugin jar chalti hai, players ko usi version ka matching mod install karna chahiye (server pe koi mod nahi lagana — sirf jar).

## Kaam kaise karta hai (gameplay)
1. `/swapndie start` — naya trap slot banega (game1, game2...), tumko dur teleport karke Creative de dega, aur area flatten ho jayega.
2. `/swapndie catalog` (ya `/swapndie additem spawn|win`) — sab custom item ek GUI se milte hain: Spawn Setter, Victory Trigger, SwapNDie Blaster (gun), SwapNDie Car, Trap Guardian Spawner (boss), Ghost Block Placer.
3. Spawn Setter aur Victory Trigger right-click karke set karo — dono set hote hi trap "ARMED" ho jayega, 5-min (customizable) countdown chalega. Last 10 second me fullscreen number popup.
4. Timer khatam hote hi opponent tumhare trap me spawn ho jayega, 3 lives (customizable) ke saath.
5. Victory point tak pahuncha to jeet gaya (green popup), 3 baar mara to haar gaya (red popup) — score automatically save ho jata hai.

Sab guns/car/boss/ghost-block **native code se bana hai — koi WeaponMechanics, MythicMobs, ItemsAdder, kuch bhi nahi chahiye.** (ItemsAdder integration hook config me hai but disabled by default kyuki **wo paid hai** — chhod do usko off.)

## Mobile se compile kaise kare (No PC needed)
1. Ye poora folder GitHub repo me upload karo.
2. `.github/workflows/build.yml` me 6 jobs hai — 3 plugin (26.1.2 / 1.21.4 / 1.20.6) + 3 mod (same 3 versions) — sab automatically parallel build hote hain jaise hi push karo.
3. Actions tab → finished run → **Artifacts** — jo bhi version chahiye download karo. Plugin jar server ke `plugins/` folder me, mod jar apne khud ke Minecraft client ke `mods/` folder me (Fabric Loader already installed hona chahiye).

## ⚠️ Ek baar zaroor karna: Fabric dependency versions verify karo
26.1.2 ek bilkul naya versioning epoch hai (Mojang ne recently switch kiya), isliye `mod/*/build.gradle` me daali gayi exact Fabric API/Loader/Loom version numbers time ke saath badal sakti hain. Pehli baar build karne se pehले `REPLIT_PROMPT.md` wala prompt kisi bhi AI assistant (Replit Agent/Ghostwriter) me paste karo — wo tumhe current sahi numbers de dega, jo tum teeno `build.gradle` files me daal do. Agar number galat hoga, GitHub Actions ka error clearly bata dega kaunsi dependency resolve nahi hui.

## Bug report / issue
Repo ke **Issues** tab me "New Issue" — template khud pooch lega kaunsi jar/mod, kaunsa server/client version, kya error aaya.

## Config
Har plugin module ke `src/main/resources/config.yml` me sab customizable hai: flatten radius, timer, lives, gun damage/range/ammo, boss health/damage, messages, aur optional integrations (sab disabled by default).

## Repo structure
```
swapndie/
├── modules/                    ← Paper plugin (Maven), 3 version modules
│   ├── modern-26.1.2/
│   ├── legacy-1.21.4/
│   └── legacy-1.20.6/
├── mod/                         ← Fabric client mod (Gradle), 3 version modules
│   ├── fabric-26.1.2/
│   ├── fabric-1.21.4/
│   └── fabric-1.20.6/
├── .github/
│   ├── workflows/build.yml     ← builds all 6 jars in one push
│   └── ISSUE_TEMPLATE/bug_report.md
├── REPLIT_PROMPT.md
└── README.md
```

