# Eclipse error fixes

這個版本針對「專案可正常執行，但 Eclipse 顯示錯誤」做了以下修正：

1. `src/main/resources/META-INF/persistence.xml`
   - 將不存在的 `persistence_3_1.xsd` 改成官方目前可下載的 `persistence_3_0.xsd`。
   - `version="3.1"` 保留不變，因為專案仍是 Jakarta Persistence 3.1 / Jakarta EE 10。

2. `src/main/webapp/WEB-INF/web.xml`
   - 將 Jakarta EE namespace 改成 Eclipse 官方文件列出的 `http://jakarta.ee/xml/ns/jakartaee`。
   - schema 檔案仍使用 Servlet 6.0 的 `web-app_6_0.xsd`。

3. `.classpath`
   - 移除硬綁定的 Eclipse runtime：`Apache Tomcat v10.1`。
   - 避免你的 Eclipse runtime 名稱不同時出現 `Unbound classpath container` 或 build path error。
   - Servlet/Jakarta API 由 Maven 的 `jakarta.jakartaee-api` provided dependency 提供編譯支援。

## 匯入後建議操作

1. Eclipse：`File > Import > Existing Maven Projects` 匯入。
2. 對專案右鍵：`Maven > Update Project...`，勾選 `Force Update of Snapshots/Releases`。
3. `Project > Clean...` 清理專案。
4. 如仍有舊紅叉：`Window > Show View > Problems`，刪除舊 marker 或重開 Eclipse。
