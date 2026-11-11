// v2: сначала сеть, потом кэш (раньше кэш всегда выигрывал, и новые версии приложения не появлялись)
const C='heart-v2',F=['./','index.html','manifest.json','icon-192.png','logo-512.png'];
self.addEventListener('install',e=>{e.waitUntil(caches.open(C).then(c=>c.addAll(F)).then(()=>self.skipWaiting()))});
self.addEventListener('activate',e=>{e.waitUntil((async()=>{
  const ks=await caches.keys(),old=ks.filter(x=>x!==C);
  await Promise.all(old.map(x=>caches.delete(x)));
  await self.clients.claim();
  if(old.length){const cs=await self.clients.matchAll({type:'window'});cs.forEach(c=>{try{c.navigate(c.url)}catch(e){}})}
})())});
self.addEventListener('fetch',e=>{
  if(e.request.method!=='GET'||new URL(e.request.url).origin!==location.origin)return;
  e.respondWith(fetch(e.request).then(r=>{if(r&&r.ok){const cp=r.clone();caches.open(C).then(c=>c.put(e.request,cp)).catch(()=>{})}return r}).catch(()=>caches.match(e.request)));
});
