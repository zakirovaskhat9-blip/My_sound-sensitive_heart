КАК ПОЛУЧИТЬ APK (бесплатно, без установки программ)

1. Зарегистрируйся на github.com (если ещё нет аккаунта).
2. Нажми «+» вверху справа -> New repository. Назови как хочешь, поставь Public, нажми Create repository.
3. Распакуй этот архив на компьютере.
4. В новом репозитории нажми «uploading an existing file» и перетащи ВСЁ содержимое распакованной папки
   (папки www, assets, .github и файлы package.json, capacitor.config.json). Нажми Commit changes.
   Если папка .github не загрузилась (она скрытая): Add file -> Create new file, в имени файла напиши
   .github/workflows/build-apk.yml и вставь туда текст из файла build-apk.yml.
5. Открой вкладку Actions. Сборка запустится сама (3-6 минут). Если нет: Build APK -> Run workflow.
6. Когда появится зелёная галочка, открой сборку и внизу в разделе Artifacts скачай
   my-sound-sensitive-heart-apk. Внутри лежит app-debug.apk.
7. Перекинь apk на телефон и открой. Android попросит разрешить установку из этого источника.
