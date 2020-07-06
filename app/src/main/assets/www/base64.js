/** @license base64.js 2012 - imaya [ https://github.com/imaya/base64.js ] The MIT License */
(function() {'use strict';function l(e){var f=e.length,b="",a=l.b,c=0,d,g,h;if(1<f)for(;c<f-2;)d=e.charCodeAt(c),g=e.charCodeAt(c+1),h=e.charCodeAt(c+2),c+=3,b+=a[d>>2&63]+a[g>>4&15|d<<4&63]+a[h>>6&3|g<<2&63]+a[h&63];c<f&&(d=e.charCodeAt(c++),b+=a[d>>2&63],c<f?(g=e.charCodeAt(c),b+=a[g>>4&15|d<<4&63]+a[g<<2&63]):b+=a[d<<4&63]);d=4*((f+2)/3|0)-b.length;0<d&&(b+=2===d?"==":"=");return b}l.b="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".split("");
function m(e){for(var f=0,b=0,a=e.length,c="",d=0,g=m.c,h=m.a,j,i;"="===e.charAt(a-1);)--a;if(1===a%4||0<e.length&&0===a)throw Error("INVALID_CHARACTER_ERR");for(;b<a;){i=e.charCodeAt(b++);j=256>i?h[i]:-1;if(-1===j)throw Error("INVALID_CHARACTER_ERR");f=(f<<6)+j;d+=6;8<=d&&(d-=8,i=f>>d,c+=g[i],f^=i<<d)}return c}var n=m,p=Array(256),q;for(q=0;255>q;++q)p[q]=String.fromCharCode(q);n.c=p;var r=m,s=l.b,t=new ("undefined"!==typeof Int16Array?Int16Array:Array)(256),u=m.c,v;for(v=0;255>v;++v)t[v]=s.indexOf(u[v]);
r.a=t;function w(e){var f=e.length,b="",a=l.b,c=0,d,g,h;if(1<f)for(;c<f-2;)d=e[c],g=e[c+1],h=e[c+2],c+=3,b+=a[d>>2&63]+a[g>>4&15|d<<4&63]+a[h>>6&3|g<<2&63]+a[h&63];c<f&&(d=e[c++],b+=a[d>>2&63],c<f?(g=e[c],b+=a[g>>4&15|d<<4&63]+a[g<<2&63]):b+=a[d<<4&63]);d=4*((f+2)/3|0)-b.length;0<d&&(b+=2===d?"==":"=");return b}var x=new ("undefined"!==typeof Uint8Array?Uint8Array:Array)(64),y,z;y=0;for(z=x.length;y<z;++y)x[y]="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charCodeAt(y);w.b=x;
function A(e,f){for(var b=0,a=0,c=e.length,d,g=0,h=0,j=A.a,i,k;"="===e.charAt(c-1);)--c;d=f?f:new ("undefined"!==typeof Uint8Array?Uint8Array:Array)(3*((c+3)/4|0)-[0,0,2,1][c%4]);if(1===c%4||0<e.length&&0===c)throw Error("INVALID_CHARACTER_ERR");for(;a<c;){k=e.charCodeAt(a++);i=256>k?j[k]:-1;if(-1===i)throw Error("INVALID_CHARACTER_ERR");b=(b<<6)+i;h+=6;8<=h&&(h-=8,k=b>>h,d[g++]=k,b^=k<<h)}return f?g:d}
var B=A,C=w.b,D=new ("undefined"!==typeof Int16Array?Int16Array:Array)(256),E,F=C instanceof Array?C:Array.prototype.slice.call(C);for(E=0;255>E;++E)D[E]=F.indexOf(E);B.a=D;function G(e){for(var f="",b=e.length,a=G.a,c,d,g,h,j,i;"="===e.charAt(b-1);)--b;c=b%4;if(1===c||0<e.length&&0===b)throw Error("INVALID_CHARACTER_ERR");for(i=0;i<b;i+=4){d=e.charCodeAt(i);g=e.charCodeAt(i+1);h=e.charCodeAt(i+2);j=e.charCodeAt(i+3);if(255<d||-1===a[d]||255<g||-1===a[g]||255<h||-1===a[h]||255<j||-1===a[j])throw Error("INVALID_CHARACTER_ERR");f+=String.fromCharCode(a[d]<<2|a[g]>>4,(a[g]&15)<<4|a[h]>>2,(a[h]&3)<<6|a[j])}0<c&&(f=f.slice(0,f.length-[0,0,2,1][c]));return f}
var H=G,I=m.a,J="undefined"!==typeof Int16Array?new Int16Array(I):I.slice();J[61]=0;H.a=J;function K(e){for(var f,b=e.length,a=G.a,c,d,g,h,j,i,k=0;"="===e.charAt(b-1);)--b;c=b%4;f=new ("undefined"!==typeof Uint8Array?Uint8Array:Array)(3*((b+3)/4|0)-[0,0,2,1][c]);if(1===c||0<e.length&&0===b)throw Error("INVALID_CHARACTER_ERR");for(i=0;i<b;i+=4){d=e.charCodeAt(i);g=e.charCodeAt(i+1);h=e.charCodeAt(i+2);j=e.charCodeAt(i+3);if(255<d||-1===a[d]||255<g||-1===a[g]||255<h||-1===a[h]||255<j||-1===a[j])throw Error("INVALID_CHARACTER_ERR");f[k]=a[d]<<2|a[g]>>4;f[k+1]=(a[g]&15)<<4|a[h]>>2;f[k+2]=
(a[h]&3)<<6|a[j];k+=3}0<c&&f instanceof Array&&(f=f.slice(0,f.length-[0,0,2,1][c]));return f};var L=navigator.vendor,M=this.Base64=this.Base64||{};void 0===M.atob&&(M.atob=L&&-1!==L.indexOf("Apple")?G:m);void 0===M.btoa&&(M.btoa=l);void 0===M.atobArray&&(M.atobArray=-1!==navigator.userAgent.indexOf("Firefox")?K:A);void 0===M.btoaArray&&(M.btoaArray=w);}).call(this);
