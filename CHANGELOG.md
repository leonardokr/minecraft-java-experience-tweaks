# Changelog

## [1.8.0](https://github.com/leonardokr/minecraft-java-experience-tweaks/compare/v1.7.0...v1.8.0) (2026-07-21)


### Features

* add configurable water hydration radius and water below option ([320112c](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/320112c14c5e115981b287ec1695921df8f65e7a))
* add in-game config screen with client and server tabs ([10d6483](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/10d6483d54def90aab03027f1aa685c54a8b2c19))
* add milk bucket hunger restoration with configurable nutrition ([67d4fe9](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/67d4fe95a9b62bdfcf805bf518fccff5b547f873))
* add Switch Rail block with pulse redstone toggling and junction logic ([5939d3c](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/5939d3ca7549821220b1fadd6acdadcf06477e46))


### Bug Fixes

* consume item cost when taking result using anvil ([9e18b9f](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/9e18b9fa1b0ff9a42fc1776c66f3d197659818e1))
* fix grass slab side texture and overlay UV mapping ([cc7fa99](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/cc7fa99c8b1c892c7ae280b64061187d23402c39))
* fix sugar cane tinting and stacked offset propagation ([2efbb1c](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/2efbb1cc8509cc938a17016bd18095969d5980c7))

## [1.7.0](https://github.com/leonardokr/minecraft-java-experience-tweaks/compare/v1.6.0...v1.7.0) (2026-07-19)


### Features

* add loot tables for dirt, grass, farmland and dirt path slabs ([e74ffcb](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/e74ffcb2e527619a4d95b4b8f331a488b6207d8d))
* align crops visually when planted on bottom farmland slabs ([4810957](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/48109577ffe8ee2f869d36b719a1c092217b0b57))
* align sugar cane visually when planted on bottom dirt or grass slabs ([5fc73a2](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/5fc73a285d8bceb451dba4e61548d972baefe8b1))


### Bug Fixes

* correct item display rotation for farmland and dirt path slab models ([ba5132e](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/ba5132e86fbfc7783e8f84512e2c62b91f0b9df3))
* preserve BOTTOM_OFFSET when bone meal is applied to slab crops ([3d487b7](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/3d487b7c9abddb3fb62e15b6cb1edf5ad62ed16a))
* replace @Redirect with @ModifyArg to fix anvil item cost on servers with conflicting mods ([c890aad](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/c890aad2a85e6bbceaf5e52da12127806a847e05))

## [1.6.0](https://github.com/leonardokr/minecraft-java-experience-tweaks/compare/v1.5.0...v1.6.0) (2026-07-18)


### Features

* add allowMendingWithInfinity config option to combine Mending and Infinity enchantments ([365a88d](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/365a88d3f9adf36c011fc7e31ba588df0e15aeea))
* add anvil enchantment extraction to enchanted book via blank book ([a7848c4](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/a7848c44a7dac09233fc404ea2b24907eb1c1327))


### Bug Fixes

* resolve @Shadow inputSlots not found by using @Accessor on ItemCombinerMenu ([2a4b000](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/2a4b0002f5c82c82c32279ab6294fecefcc6a160))
* source item incorrectly destroyed when it still had remaining enchantments ([65c4a1c](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/65c4a1caa7deb6816cb80c7070eec2c0245cfa1e))
* update ClientColors to NeoForge 26.x API and remove debug file ([b6b0023](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/b6b0023fe3317c46e60769954309c2a02ad96dc7))

## [1.5.0](https://github.com/leonardokr/minecraft-java-experience-tweaks/compare/v1.4.0...v1.5.0) (2026-07-17)


### Features

* add anvil tweaks (too expensive bypass and item-cost options) ([fb9ea3d](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/fb9ea3dc7eaf276a9c7716babb155ffa87ba2eef))
* add auto-fishing quality of life tweak ([29b372b](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/29b372ba91b69ed32e6eb577070c9f22606806ba))
* add daily experience survival reward system ([91aa725](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/91aa725756965a96fb7202612370a63090f56bfd))
* add direct XP collection ([7f7602b](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/7f7602bd5d1d32ddec8aca804a0456f351bb4037))
* add dirt slab block, models, recipes, tags, and registrations ([9b4c70d](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/9b4c70d634740e265da920a92fd779a74c8cd4a3))
* add error handling and broadcast for invalid config values ([12cd110](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/12cd110790341c16e88ad83a5c0a4c210a58afc8))
* add experience tweak gameplay code ([4e0be45](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/4e0be45a4cd8e71bac1bfb5e4da0d34cc0a2c17a))
* add grass, dirt path, and farmland slab behaviors ([1884356](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/18843566fc77c4341478cd9925c3bc13bd44588d))
* add logo image ([a9a2831](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/a9a2831c8254cd9907ec8c11e4a08ac1fb79fcaf))
* initial mod setup from NeoForge template ([c87d231](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/c87d231620bd0ff94f08b069e5df75281c36d21c))
* localize command and tooltip messages ([2009a7c](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/2009a7cac718286d23647bc62829c56eda92f2dc))
* option to calculate cooldown based on last level needed to use the enchanting table instead of current level ([0b3e1d4](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/0b3e1d478a062c91fd81f912ea9a30a69c20b214))
* updated default values ([afbf5c3](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/afbf5c3904c978124d5eecd604e49bc17a7e0ecc))


### Bug Fixes

* last_level cooldown calculation was being miscalculated ([0286f53](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/0286f53c62a88d90cbceaf55c935563857f8b65a))
* last_level cooldown calculation was being miscalculated ([111d844](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/111d844e5da343138b6c70e0cda7df1ce68177f2))
* last_level cooldown calculation was being miscalculated ([36776c1](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/36776c1341dec92369fd7870ea693a27f1b39bd9))
* resolve Java 9+ identifier compatibility and compilation errors ([2e7c2fb](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/2e7c2fbb06ed564c13a76412668b9ad9a773dbef))
* symbol fixed on recordEnchant method ([065c130](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/065c130b3275f1f9d7eed29d30cb747c24852e2e))
* update config comment to better readability ([c99c9a1](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/c99c9a1f1324162b1e83224b508641cd41ac9d85))

## [1.4.0](https://github.com/leonardokr/minecraft-java-experience-tweaks/compare/v1.3.0...v1.4.0) (2026-07-17)


### Features

* add anvil tweaks (too expensive bypass and item-cost options) ([fb9ea3d](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/fb9ea3dc7eaf276a9c7716babb155ffa87ba2eef))
* add auto-fishing quality of life tweak ([29b372b](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/29b372ba91b69ed32e6eb577070c9f22606806ba))
* add dirt slab block, models, recipes, tags, and registrations ([9b4c70d](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/9b4c70d634740e265da920a92fd779a74c8cd4a3))


### Bug Fixes

* resolve Java 9+ identifier compatibility and compilation errors ([2e7c2fb](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/2e7c2fbb06ed564c13a76412668b9ad9a773dbef))

## [1.3.0](https://github.com/leonardokr/minecraft-java-experience-tweaks/compare/v1.2.0...v1.3.0) (2026-06-20)


### Features

* add daily experience survival reward system ([91aa725](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/91aa725756965a96fb7202612370a63090f56bfd))

## [1.2.0](https://github.com/leonardokr/minecraft-java-experience-tweaks/compare/v1.1.0...v1.2.0) (2026-06-20)


### Features

* add error handling and broadcast for invalid config values ([12cd110](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/12cd110790341c16e88ad83a5c0a4c210a58afc8))
* add logo image ([a9a2831](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/a9a2831c8254cd9907ec8c11e4a08ac1fb79fcaf))
* localize command and tooltip messages ([2009a7c](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/2009a7cac718286d23647bc62829c56eda92f2dc))
* option to calculate cooldown based on last level needed to use the enchanting table instead of current level ([0b3e1d4](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/0b3e1d478a062c91fd81f912ea9a30a69c20b214))


### Bug Fixes

* last_level cooldown calculation was being miscalculated ([0286f53](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/0286f53c62a88d90cbceaf55c935563857f8b65a))
* last_level cooldown calculation was being miscalculated ([111d844](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/111d844e5da343138b6c70e0cda7df1ce68177f2))
* last_level cooldown calculation was being miscalculated ([36776c1](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/36776c1341dec92369fd7870ea693a27f1b39bd9))
* update config comment to better readability ([c99c9a1](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/c99c9a1f1324162b1e83224b508641cd41ac9d85))

## [1.1.0](https://github.com/leonardokr/minecraft-java-experience-tweaks/compare/v1.0.0...v1.1.0) (2026-06-19)


### Features

* updated default values ([afbf5c3](https://github.com/leonardokr/minecraft-java-experience-tweaks/commit/afbf5c3904c978124d5eecd604e49bc17a7e0ecc))
