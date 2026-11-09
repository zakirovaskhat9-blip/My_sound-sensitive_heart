p = 'android/app/src/main/AndroidManifest.xml'
s = open(p, encoding='utf-8').read()
if 'android.intent.action.SEND' not in s:
    filters = '''
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="content" />
                <data android:scheme="file" />
                <data android:mimeType="audio/*" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="audio/*" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.SEND_MULTIPLE" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="audio/*" />
            </intent-filter>
        '''
    assert '</activity>' in s
    s = s.replace('</activity>', filters + '</activity>', 1)
    open(p, 'w', encoding='utf-8').write(s)
s = open(p, encoding='utf-8').read()
if 'READ_MEDIA_AUDIO' not in s:
    perms = '''
    <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
'''
    assert '</manifest>' in s
    s = s.replace('</manifest>', perms + '</manifest>', 1)
    open(p, 'w', encoding='utf-8').write(s)
s = open(p, encoding='utf-8').read()
if 'usesCleartextTraffic' not in s:
    assert '<application' in s
    s = s.replace('<application', '<application android:usesCleartextTraffic="true"', 1)
    open(p, 'w', encoding='utf-8').write(s)
print('manifest patched')
