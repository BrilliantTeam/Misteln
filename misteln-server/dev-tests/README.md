# dev-tests

Misteln 自己的 DFU 測試，**刻意不放在 `paper-patches/` 裡**，所以 paperweight 不會把它們套進建置。
這裡的 patch 只在要驗證跨版本資料轉換時手動套用，驗證完就丟掉。

## 跑測試

在專案根目錄執行：

```sh
./gradlew applyAllPatches
(cd paper-server && git am ../misteln-server/dev-tests/*.patch)
./gradlew :misteln-server:test --tests '*ItemMetaDataUpgradeTest' --tests '*UpgradeRenamesAndFormatsTest' --tests '*WorldBorderMigrationTest'
```

## 跑完還原

```sh
./gradlew :misteln-server:applyAllServerPatches
```

paperweight 會把 `paper-server` 重設回 patch 系列的狀態，測試檔就消失了。
**不要在套了這些 patch 的狀態下執行 `rebuildPaperServerPatches`**，否則測試會被寫回 `paper-patches/`。

## 內容

| patch | 測試檔 | 涵蓋範圍 |
| --- | --- | --- |
| 0001 | `inventory/ItemMetaDataUpgradeTest.java` | Bukkit 序列化的 ItemMeta 從 1.21.1 / 1.21.4 / 1.21.8 / 1.21.11 / 26.2 載入 |
| 0002 | `inventory/UpgradeRenamesAndFormatsTest.java` | chain 改名、JSON 物品與 bytes 路徑一致、26.2→26.3 的元件改動 |
| 0003 | `util/WorldBorderMigrationTest.java` | 世界邊界搬出 level.dat 的遷移 |

改完測試要更新這裡的 patch：在 `paper-server` 把改動 commit 起來，再用 `git format-patch` 匯出覆蓋對應檔案（只留 `src/test/` 的改動）。
