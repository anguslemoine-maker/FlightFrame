/* FlightFrame – side-view aircraft drawings in airline colours.
   Original vector drawings, generated from a few shape parameters per family.
   Exposes: window.FFPlanes = { family(icaoType), svg(icaoType, airlineIcao, airlineIata, reg) } */
(function(){
  // ---------- airline liveries (keyed by ICAO airline code) ----------
  // body = upper fuselage, belly = lower fuselage, tail = fin, eng = engines, accent = cheatline
  var W='#f4f6f8';
  function L(tail,o){o=o||{};return {tail:tail,body:o.body||W,belly:o.belly||o.body||W,eng:o.eng||'#cfd6de',accent:o.accent||null}}
  var LIV={
    KLM:L('#00a1de',{body:'#00a1de',belly:W,accent:'#003a70',eng:'#00a1de'}),
    KLC:L('#00a1de',{body:'#00a1de',belly:W,accent:'#003a70',eng:'#00a1de'}),
    TRA:L('#00d66c',{eng:'#00d66c',accent:'#00d66c'}), TVF:L('#00d66c',{eng:'#00d66c',accent:'#00d66c'}),
    EZY:L('#ff6600',{eng:'#ff6600'}), EJU:L('#ff6600',{eng:'#ff6600'}), EZS:L('#ff6600',{eng:'#ff6600'}),
    RYR:L('#073590',{belly:'#073590',eng:'#073590',accent:'#f1c933'}), RUK:L('#073590',{belly:'#073590',eng:'#073590',accent:'#f1c933'}),
    MAY:L('#073590',{belly:'#073590',eng:'#073590',accent:'#f1c933'}), LDM:L('#ffffff',{eng:'#c8102e',accent:'#c8102e'}),
    DLH:L('#05164d',{eng:'#05164d'}), CLH:L('#05164d',{eng:'#05164d'}),
    BAW:L('#1e2b5c',{belly:'#1e2b5c',accent:'#d2232a',eng:'#1e2b5c'}), CFE:L('#1e2b5c',{belly:'#1e2b5c',accent:'#d2232a'}),
    AFR:L('#ffffff',{accent:'#002157',eng:'#dfe5ec'}), HOP:L('#ffffff',{accent:'#002157'}),
    VLG:L('#ffcc00',{belly:'#8e9196',eng:'#ffcc00'}),
    WZZ:L('#c6007e',{belly:'#c6007e',eng:'#c6007e',accent:'#2a1a5e'}), WMT:L('#c6007e',{belly:'#c6007e',eng:'#c6007e'}), WAZ:L('#c6007e',{belly:'#c6007e',eng:'#c6007e'}), WUK:L('#c6007e',{belly:'#c6007e',eng:'#c6007e'}),
    TFL:L('#70cbf4',{accent:'#d40e14',eng:'#70cbf4'}), TOM:L('#70cbf4',{accent:'#d40e14',eng:'#70cbf4'}), JAF:L('#70cbf4',{accent:'#d40e14',eng:'#70cbf4'}), TUI:L('#70cbf4',{accent:'#d40e14',eng:'#70cbf4'}),
    CND:L('#e30613',{accent:'#e30613',eng:'#e30613'}), CAI:L('#e30613',{accent:'#e30613',eng:'#e30613'}), CXI:L('#e30613',{accent:'#e30613',eng:'#e30613'}),
    SAS:L('#1b1f5e',{body:'#e8ebef',eng:'#1b1f5e'}),
    NOZ:L('#d81939',{eng:'#d81939'}), NSZ:L('#d81939',{eng:'#d81939'}), NAX:L('#d81939',{eng:'#d81939'}), IBK:L('#d81939',{eng:'#d81939'}),
    DAL:L('#0b2d5c',{belly:'#0b2d5c',accent:'#c8102e'}), UAL:L('#0b2348',{belly:'#1b4a90',eng:'#1b4a90'}), AAL:L('#0078d2',{body:'#c7ccd1',belly:'#c7ccd1',accent:'#c30019'}),
    ACA:L('#111111',{belly:'#111111',eng:'#111111'}),
    UAE:L('#d71921',{accent:'#d71921'}), QTR:L('#5c0632',{body:'#e4e6e9',belly:'#e4e6e9',eng:'#5c0632'}), ETD:L('#c8a467',{body:'#efe7da',belly:'#efe7da',eng:'#c8a467'}),
    THY:L('#e81932',{accent:'#e81932'}), SWR:L('#e2001a',{eng:'#e9ecef'}), EDW:L('#e2001a'), AUA:L('#e1001a',{belly:'#e1001a'}),
    BEL:L('#00337f',{eng:'#00337f'}), IBE:L('#d7192d',{accent:'#ffc600'}), IBS:L('#d7192d',{accent:'#ffc600'}), TAP:L('#da291c',{accent:'#2a9d3f'}),
    EIN:L('#00655b',{belly:'#8dc7b8',eng:'#00655b'}), EXS:L('#e31837',{belly:'#e31837',eng:'#e31837'}),
    EWG:L('#8a1538',{eng:'#8a1538',accent:'#8a1538'}), OCN:L('#05164d',{eng:'#05164d'}), CFG:L('#ffcb05',{accent:'#7ec8e3'}),
    FIN:L('#0b1560',{eng:'#e9ecef'}), LOT:L('#11397e',{eng:'#11397e'}), ICE:L('#002f5f',{accent:'#f6b800',eng:'#002f5f'}),
    PGT:L('#fdc300',{body:'#e7e7e7',belly:'#e7e7e7',eng:'#e6007e'}), SXS:L('#fbaa1b',{eng:'#fbaa1b'}), AEE:L('#004b87',{eng:'#004b87'}),
    BTI:L('#cddc29',{eng:'#cddc29'}), ROT:L('#002d72'), ITY:L('#004d9a',{body:'#004d9a',belly:'#004d9a',eng:'#004d9a'}),
    CSN:L('#0b72b6',{accent:'#0b72b6'}), CPA:L('#006564',{eng:'#e9ecef'}), SIA:L('#1d3b6e',{accent:'#f0ab00'}), KAL:L('#a8d4ef',{body:'#a8d4ef',belly:W}),
    SVA:L('#00553f',{body:'#efe9dc',belly:'#efe9dc'}), KQA:L('#cd2027'), MSR:L('#002d72'), RAM:L('#c8102e'), ELY:L('#0b2f6b'),
    DHK:L('#ffcc00',{body:'#ffcc00',belly:'#ffcc00',accent:'#d40511',eng:'#ffcc00'}), BCS:L('#ffcc00',{body:'#ffcc00',belly:'#ffcc00',accent:'#d40511',eng:'#ffcc00'}), DHL:L('#ffcc00',{body:'#ffcc00',belly:'#ffcc00',accent:'#d40511',eng:'#ffcc00'}),
    FDX:L('#4d148c',{body:'#ffffff',belly:'#ffffff',eng:'#4d148c'}), UPS:L('#351c15',{body:'#351c15',belly:'#351c15',accent:'#ffb500',eng:'#351c15'}),
    MPH:L('#00a1de',{accent:'#003a70'}), GEC:L('#05164d'), CLX:L('#0a3a73',{body:'#e9ecef',belly:'#e9ecef'}),
    VOE:L('#ff6b00'), BEE:L('#5b2c83'), VIR:L('#c8102e',{body:'#e9ecef',belly:'#e9ecef'}), AZA:L('#008f51')
  };
  var DEFAULT=L('#5b6575',{body:'#e3e8ee',belly:'#e3e8ee',eng:'#c3cbd5'});


  // ---------- original category badges (viewBox 0 0 100 100) ----------
  var CAT={
    coastguard:{bg:'#0d2a4f', liv:L('#0d2a4f',{accent:'#ff7a1a',eng:'#0d2a4f'}), label:'Coast Guard',
      art:'<circle cx="50" cy="50" r="27" fill="none" stroke="#fff" stroke-width="13"/>'+
          '<path d="M50 23 A27 27 0 0 1 77 50" fill="none" stroke="#ff7a1a" stroke-width="13"/><path d="M50 77 A27 27 0 0 1 23 50" fill="none" stroke="#ff7a1a" stroke-width="13"/>'},
    airforce:{bg:'#46505c', liv:L('#5d6774',{body:'#9aa3ad',belly:'#9aa3ad',eng:'#5d6774'}), label:'Air Force',
      art:'<path d="M50 38 L54 47 L64 47 L56 53 L59 63 L50 57 L41 63 L44 53 L36 47 L46 47 Z" fill="#fff"/>'+
          '<path d="M33 52 C24 48 16 42 10 34 C20 38 28 40 34 40 Z M67 52 C76 48 84 42 90 34 C80 38 72 40 66 40 Z" fill="#fff"/>'+
          '<path d="M32 58 C24 56 18 52 14 47 C21 49 27 50 33 49 Z M68 58 C76 56 82 52 86 47 C79 49 73 50 67 49 Z" fill="#fff" opacity="0.8"/>'},
    navy:{bg:'#1b2a44', liv:L('#2c3e5c',{body:'#8794a6',belly:'#8794a6',eng:'#2c3e5c'}), label:'Navy',
      art:'<circle cx="50" cy="26" r="6" fill="none" stroke="#fff" stroke-width="4"/><rect x="47.5" y="31" width="5" height="44" fill="#fff"/>'+
          '<rect x="36" y="40" width="28" height="5" rx="2" fill="#fff"/>'+
          '<path d="M24 58 C26 72 38 79 50 79 C62 79 74 72 76 58 L70 62 C67 70 59 73 50 73 C41 73 33 70 30 62 Z" fill="#fff"/>'},
    army:{bg:'#4b5320', liv:L('#4b5320',{body:'#626b3c',belly:'#626b3c',eng:'#3f4620'}), label:'Army',
      art:'<path d="M50 18 L76 28 L76 50 C76 66 64 77 50 83 C36 77 24 66 24 50 L24 28 Z" fill="none" stroke="#fff" stroke-width="5"/>'+
          '<path d="M36 46 L50 38 L64 46 L64 53 L50 45 L36 53 Z M36 58 L50 50 L64 58 L64 65 L50 57 L36 65 Z" fill="#fff"/>'},
    military:{bg:'#3a4048', liv:L('#5d6774',{body:'#9aa3ad',belly:'#9aa3ad',eng:'#5d6774'}), label:'Military',
      art:'<path d="M50 18 L76 28 L76 50 C76 66 64 77 50 83 C36 77 24 66 24 50 L24 28 Z" fill="none" stroke="#fff" stroke-width="5"/>'+
          '<path d="M50 34 L55 46 L68 46 L58 54 L62 67 L50 59 L38 67 L42 54 L32 46 L45 46 Z" fill="#fff"/>'},
    police:{bg:'#1d3f8f', liv:L('#1d3f8f',{accent:'#ffd200',eng:'#1d3f8f'}), label:'Police',
      art:'<path d="M50 16 L58 24 L70 22 L72 34 L82 41 L76 52 L80 64 L68 68 L63 80 L50 76 L37 80 L32 68 L20 64 L24 52 L18 41 L28 34 L30 22 L42 24 Z" fill="#fff"/>'+
          '<path d="M50 36 L54 46 L64 46 L56 52 L59 62 L50 56 L41 62 L44 52 L36 46 L46 46 Z" fill="#1d3f8f"/>'},
    medical:{bg:'#0f7a6c', liv:L('#ffd200',{accent:'#0f7a6c',eng:'#ffd200'}), label:'Air ambulance',
      art:'<path d="M50 78 C30 64 18 52 18 38 C18 28 26 21 35 21 C42 21 47 25 50 30 C53 25 58 21 65 21 C74 21 82 28 82 38 C82 52 70 64 50 78 Z" fill="#fff"/>'+
          '<path d="M24 47 L39 47 L44 37 L50 58 L56 43 L60 47 L76 47" fill="none" stroke="#0f7a6c" stroke-width="4.5" stroke-linejoin="round" stroke-linecap="round"/>'},
    private:{bg:'#2f5d62', liv:L('#2f5d62',{accent:'#2f5d62',eng:'#dfe5ec'}), label:'Private',
      art:'<path d="M50 14 C53 14 55 18 55 24 L55 40 L84 56 L84 63 L55 54 L55 72 L64 79 L64 85 L50 81 L36 85 L36 79 L45 72 L45 54 L16 63 L16 56 L45 40 L45 24 C45 18 47 14 50 14 Z" fill="#fff"/>'}
  };
  function badgeInner(cat){ var c=CAT[cat]; return c ? '<circle cx="50" cy="50" r="48" fill="'+c.bg+'"/><circle cx="50" cy="50" r="44" fill="none" stroke="#fff" stroke-width="2" opacity="0.5"/>'+c.art : '' }
  function badgeSVG(cat){ return '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100">'+badgeInner(cat)+'</svg>' }

  // ---------- type -> family ----------
  function family(t){
    t=(t||'').toUpperCase();
    if(/^(EC|H1|H60|A109|A119|A139|A169|A189|AS3|AS5|AS6|B06|B407|B412|B429|R22|R44|R66|NH90|EH10|S76|S92|CH47|H47|MI8|EC|BK17)/.test(t)) return 'heli';
    if(/^(B74)/.test(t)) return 'jumbo';
    if(/^(A38)/.test(t)) return 'superjumbo';
    if(/^(A34)/.test(t)) return 'quad';
    if(/^(A30|A31[0]|A33|A35|B76|B77|B78|MD11|IL96|B2|A3ST)/.test(t)) return 'wide';
    if(/^(CRJ|E135|E145|F70|F100|MD8|MD9|B712|RJ)/.test(t)) return 'rearjet';
    if(/^(C25|C5|C6|C7|CL30|CL35|CL60|GL|GLF|G150|G280|LJ|FA|F2TH|F900|E50P|E55P|E545|E550|PC24|H25|SF50|HDJT|BE40|PRM1|C680|C700)/.test(t)) return 'biz';
    if(/^(AT4|AT7|DH8|SF34|JS41|D328|B350|BE20|BE9|C130|A400|P180|PC12|SW4|F50|DHC6|C208|L410)/.test(t)) return 'turboprop';
    if(/^(C1[0-9][0-9]|C2[0-9][0-9]|P28|P32|PA|SR2|DA2|DA4|DA62|TB|DR40|RV|M20|BE3|BE5|BE6|G115|AQUI|ULAC)/.test(t)) return 'light';
    if(/^(E17|E19|E29|E75|BCS)/.test(t)) return 'regional';
    return 'narrow';
  }

  var FAM={
    narrow:    {L:560,h:56,fin:90,eng:'wing',ew:72,eh:32,tail:'conv'},
    regional:  {L:480,h:48,fin:80,eng:'wing',ew:58,eh:27,tail:'conv'},
    wide:      {L:630,h:66,fin:106,eng:'wing',ew:104,eh:48,tail:'conv'},
    quad:      {L:640,h:64,fin:104,eng:'wing2',ew:80,eh:34,tail:'conv'},
    jumbo:     {L:640,h:66,fin:110,eng:'wing2',ew:86,eh:38,tail:'conv',hump:true},
    superjumbo:{L:630,h:84,fin:106,eng:'wing2',ew:90,eh:42,tail:'conv'},
    rearjet:   {L:470,h:44,fin:76,eng:'rear',ew:66,eh:26,tail:'T'},
    biz:       {L:380,h:38,fin:68,eng:'rear',ew:58,eh:22,tail:'T'},
    turboprop: {L:470,h:48,fin:80,eng:'prop',ew:70,eh:22,tail:'T'},
    light:     {L:300,h:38,fin:54,eng:'nose',tail:'conv',high:true}
  };


  // ---------- real aircraft dimensions (metres) → drawing sizes ----------
  // len, fuselage height, fin height above fuselage, engine diameter, nose style, extras
  var S={};
  function T(codes,len,dia,fin,eng,nose,x){ codes.split(' ').forEach(function(c){ S[c]={len:len,dia:dia,fin:fin,eng:eng,nose:nose,x:x||{}} }) }
  // Airbus single-aisle
  T('A318',31.4,4.1,6.3,1.9,'A',{wl:'fence'}); T('A319',33.8,4.1,6.3,1.9,'A',{wl:'fence'}); T('A320',37.6,4.1,6.3,1.9,'A',{wl:'shark'}); T('A321',44.5,4.1,6.3,1.9,'A',{wl:'shark'});
  T('A19N',33.8,4.1,6.3,2.2,'A',{wl:'shark'}); T('A20N',37.6,4.1,6.3,2.2,'A',{wl:'shark'}); T('A21N',44.5,4.1,6.3,2.2,'A',{wl:'shark'});
  T('BCS1',35.0,3.7,6.6,2.1,'E',{}); T('BCS3',38.7,3.7,6.6,2.1,'E',{});
  // Boeing single-aisle
  T('B736',31.2,4.0,7.4,1.6,'B',{dorsal:1}); T('B737',33.6,4.0,7.4,1.6,'B',{dorsal:1,wl:'up'}); T('B738',39.5,4.0,7.4,1.6,'B',{dorsal:1,wl:'up'}); T('B739',42.1,4.0,7.4,1.6,'B',{dorsal:1,wl:'up'});
  T('B37M',35.6,4.0,7.4,1.9,'B',{dorsal:1,wl:'split'}); T('B38M',39.5,4.0,7.4,1.9,'B',{dorsal:1,wl:'split'}); T('B39M',42.2,4.0,7.4,1.9,'B',{dorsal:1,wl:'split'}); T('B3XM',43.8,4.0,7.4,1.9,'B',{dorsal:1,wl:'split'});
  T('B752',47.3,4.0,8.2,2.2,'B',{dorsal:1}); T('B753',54.4,4.0,8.2,2.2,'B',{dorsal:1});
  // Embraer / CRJ / regional
  T('E170',29.9,3.3,5.6,1.5,'E',{wl:'up'}); T('E175',31.7,3.3,5.6,1.5,'E',{wl:'up'}); T('E75L',31.7,3.3,5.6,1.5,'E',{wl:'up'}); T('E75S',31.7,3.3,5.6,1.5,'E',{wl:'up'});
  T('E190',36.2,3.3,6.2,1.7,'E',{wl:'up'}); T('E195',38.7,3.3,6.2,1.7,'E',{wl:'up'}); T('E290',36.2,3.3,6.4,2.0,'E',{}); T('E295',41.5,3.3,6.4,2.0,'E',{});
  T('CRJ2',26.8,2.7,4.6,1.3,'E',{}); T('CRJ7',32.5,2.7,5.0,1.4,'E',{}); T('CRJ9',36.2,2.7,5.0,1.4,'E',{}); T('CRJX',39.1,2.7,5.0,1.4,'E',{});
  T('E135',26.3,2.5,4.6,1.2,'E',{}); T('E145',29.9,2.5,4.6,1.2,'E',{}); T('F70',30.9,3.3,5.4,1.5,'E',{}); T('F100',35.5,3.3,5.4,1.5,'E',{});
  T('AT43',22.7,2.6,5.3,0,'E',{}); T('AT45',22.7,2.6,5.3,0,'E',{}); T('AT72',27.2,2.6,5.6,0,'E',{}); T('AT75',27.2,2.6,5.6,0,'E',{}); T('AT76',27.2,2.6,5.6,0,'E',{});
  T('DH8A',22.3,2.7,5.2,0,'E',{}); T('DH8C',25.7,2.7,5.2,0,'E',{}); T('DH8D',32.8,2.7,5.6,0,'E',{});
  // Airbus wide-body
  T('A332',58.8,5.9,10.5,2.9,'A',{wl:'up'}); T('A333',63.7,5.9,10.5,2.9,'A',{wl:'up'}); T('A338',58.8,5.9,10.5,3.2,'A',{wl:'shark'}); T('A339',63.7,5.9,10.5,3.2,'A',{wl:'shark'});
  T('A343',63.7,5.9,10.5,2.4,'A',{quad:1,wl:'up'}); T('A346',75.4,5.9,10.5,2.6,'A',{quad:1,wl:'up'});
  T('A359',66.8,6.1,11.0,3.3,'X',{mask:1,wl:'shark'}); T('A35K',73.8,6.1,11.0,3.4,'X',{mask:1,wl:'shark'});
  T('A388',72.7,8.4,13.5,3.0,'A',{quad:1,double:1,wl:'fence'});
  // Boeing wide-body
  T('B762',48.5,5.4,9.5,2.7,'B',{dorsal:1}); T('B763',54.9,5.4,9.5,2.7,'B',{dorsal:1,wl:'up'}); T('B764',61.4,5.4,9.5,2.7,'B',{dorsal:1});
  T('B772',63.7,6.2,11.2,3.4,'X',{blade:1}); T('B77L',63.7,6.2,11.2,3.6,'X',{blade:1}); T('B77W',73.9,6.2,11.2,3.6,'X',{blade:1}); T('B778',69.8,6.2,11.5,3.9,'X',{blade:1}); T('B779',76.7,6.2,11.5,3.9,'X',{blade:1});
  T('B788',56.7,5.9,10.6,3.0,'X',{chev:1}); T('B789',62.8,5.9,10.6,3.0,'X',{chev:1}); T('B78X',68.3,5.9,10.6,3.0,'X',{chev:1});
  T('B744',70.7,6.5,12.4,2.7,'B',{quad:1,hump:0.30,wl:'up'}); T('B748',76.3,6.5,12.8,3.0,'B',{quad:1,hump:0.33,chev:1}); T('B74F',70.7,6.5,12.4,2.7,'B',{quad:1,hump:0.30,wl:'up'});
  T('MD11',61.6,6.0,10.5,2.7,'B',{tri:1,wl:'up'}); T('DC10',55.5,6.0,10.5,2.7,'B',{tri:1});
  // families used when a type isn't listed
  var FAMSPEC={narrow:['A320'],regional:['E190'],wide:['A333'],quad:['A343'],jumbo:['B744'],superjumbo:['A388'],rearjet:['CRJ9'],turboprop:['AT76']};
  var BIZ={len:19,dia:2.1,fin:3.6,eng:1.0,nose:'E',x:{wl:'up'}}, LIGHT={len:8.3,dia:1.5,fin:1.7,eng:0,nose:'E',x:{}};
  var SCALE=8.6, MINLEN=220;

  function spec(type,famName){
    var d=S[(type||'').toUpperCase()] || (FAMSPEC[famName]?S[FAMSPEC[famName][0]]:null) || (famName==='biz'?BIZ:famName==='light'?LIGHT:S.A320);
    var k=Math.max(SCALE, (famName==='light'?170:MINLEN)/d.len), x=d.x;
    var tail = (famName==='rearjet'||famName==='biz'||famName==='turboprop'||/^(CRJ|E135|E145|F70|F100|AT|DH8)/.test(type||'')) ? 'T' : 'conv';
    var engKind = famName==='light' ? 'nose' : (famName==='turboprop'||/^(AT|DH8)/.test(type||'')) ? 'prop' : (tail==='T'&&famName!=='turboprop') ? 'rear' : (x.quad?'quad':'wing');
    return {L:d.len*k, h:d.dia*k, fin:d.fin*k, ew:d.eng*k*2.1, eh:d.eng*k, tail:tail, eng:engKind, high:famName==='light',
            nose:d.nose, dorsal:x.dorsal, wl:x.wl, mask:x.mask, blade:x.blade, quad:x.quad, double:x.double, hump:x.hump, tri:x.tri, chev:x.chev};
  }

  function shade(hex,amt){ // darken (amt<0) / lighten (amt>0)
    hex=hex.replace('#',''); if(hex.length===3) hex=hex.replace(/(.)/g,'$1$1');
    var n=parseInt(hex,16),r=n>>16,g=(n>>8)&255,b=n&255;
    function f(c){c=amt<0?c*(1+amt):c+(255-c)*amt;return Math.max(0,Math.min(255,Math.round(c)))}
    return '#'+((1<<24)+(f(r)<<16)+(f(g)<<8)+f(b)).toString(16).slice(1);
  }
  function lum(hex){hex=hex.replace('#','');var n=parseInt(hex,16);return (0.299*(n>>16)+0.587*((n>>8)&255)+0.114*(n&255))/255}
  function n(v){return Math.round(v*10)/10}
  var uid=0;

  function heliSVG(lv,reg,cat){
    var c=lv.tail, b=lv.body!==W?lv.body:(lv.tail!=='#5b6575'?lv.tail:'#e3e8ee');
    return '<g>'+
      '<rect x="160" y="60" width="440" height="5" rx="2.5" fill="#5f6b7a"/><rect x="372" y="50" width="16" height="16" rx="3" fill="#5f6b7a"/>'+
      '<path d="M190 112 L330 104 L330 124 L196 122 Z" fill="'+shade(b,-0.15)+'"/>'+
      '<path d="M180 96 L196 96 L214 116 L192 124 Z" fill="'+c+'"/>'+
      '<circle cx="186" cy="110" r="20" fill="none" stroke="#7d8898" stroke-width="2" opacity="0.6"/>'+
      '<path d="M320 98 C330 74 380 66 440 70 C500 74 540 92 548 118 C552 140 530 152 480 152 L360 152 C330 152 316 136 318 120 Z" fill="'+b+'"/>'+
      '<path d="M480 76 C512 82 536 98 544 118 L488 118 Z" fill="#253041"/>'+
      '<path d="M318 132 C330 148 350 152 380 152 L480 152 C520 152 545 142 549 126 L549 132 C545 148 520 158 480 158 L360 158 C336 158 322 148 318 132 Z" fill="'+c+'"/>'+
      '<path d="M350 158 L344 176 M500 158 L506 176 M318 176 L540 176" stroke="#5f6b7a" stroke-width="4" fill="none" stroke-linecap="round"/>'+
      (reg?'<text x="'+(cat?372:400)+'" y="138" font-size="13" fill="'+(lum(b)>0.5?'#3a4554':'#ffffff')+'" opacity="0.7" font-family="sans-serif-condensed,sans-serif">'+reg+'</text>':'')+
      (cat&&CAT[cat]?'<svg x="424" y="96" width="44" height="44" viewBox="0 0 100 100">'+badgeInner(cat)+'</svg>':'')+
      '</g>';
  }

  // opts: {tail:'#hex'} auto colour for airlines not in the table; {logoUrl:'..'} transparent ICAO logo drawn in white
  function svg(type, icao, iata, reg, opts){
    opts=opts||{};
    var famName=family(type), lv=(opts.cat&&CAT[opts.cat]?CAT[opts.cat].liv:null)||LIV[icao]||(opts.tail?L(opts.tail,{eng:opts.tail}):DEFAULT), id='p'+(uid++);
    var body='';
    if(famName==='heli') body='<g transform="translate(0,20)">'+heliSVG(lv,reg,opts.cat)+'</g>';
    else{
      var F=spec(type,famName), Ln=F.L, h=F.h, upE=F.fin+h/2+(F.hump?h*0.4:0), dnE=h/2+(F.eh?F.eh*0.85:24), cy=(240-(upE+dnE))/2+upE, x0=(720-Ln)/2+(famName==='light'?20:0), xn=x0+Ln, ty=cy-h/2, yb=cy+h/2, fin=F.fin;
      var bodyCol=lv.body, belly=lv.belly, tail=lv.tail, eng=lv.eng, edge=shade(bodyCol,-0.25);
      var nl = F.nose==='B'?0.12:(F.nose==='X'?0.15:(F.nose==='E'?0.14:0.16)), tip = F.nose==='B'?0.16:(F.nose==='X'?0.06:0.1);
      var tEnd = F.blade ? -0.04 : -0.2;
      var fus='M'+n(x0)+','+n(cy-h*0.42)+' L'+n(x0+0.2*Ln)+','+n(ty)+' L'+n(xn-nl*Ln)+','+n(ty)+
        ' C'+n(xn-nl*0.4*Ln)+','+n(ty)+' '+n(xn)+','+n(cy-h*(F.nose==='B'?0.02:0.12))+' '+n(xn)+','+n(cy+h*tip)+
        ' C'+n(xn)+','+n(cy+h*0.38)+' '+n(xn-0.05*Ln)+','+n(yb)+' '+n(xn-0.11*Ln)+','+n(yb)+
        ' L'+n(x0+0.3*Ln)+','+n(yb)+' C'+n(x0+0.2*Ln)+','+n(yb)+' '+n(x0+0.07*Ln)+','+n(cy-h*0.02)+' '+n(x0)+','+n(cy+h*tEnd)+' Z';
      // 747 hump / A380 double deck handled by fuselage height + hump overlay
      var hs=F.hump||0, hump=hs?'M'+n(xn-(hs+0.12)*Ln)+','+n(ty)+' C'+n(xn-hs*0.8*Ln)+','+n(ty-h*0.42)+' '+n(xn-0.12*Ln)+','+n(ty-h*0.36)+' '+n(xn-0.05*Ln)+','+n(ty+h*0.14)+' L'+n(xn-0.1*Ln)+','+n(ty+2)+' Z':'';

      // fin
      var fb1=x0+0.025*Ln, fb2=x0+(F.tail==='T'?0.19:0.215)*Ln, ft1=x0+(F.tail==='T'?0.045:0.012)*Ln, ft2=x0+(F.tail==='T'?0.115:0.088)*Ln, fty=ty-fin;
      var finP='M'+n(fb1)+','+n(cy-h*0.4)+' L'+n(ft1)+','+n(fty)+' L'+n(ft2)+','+n(fty)+' L'+n(fb2)+','+n(ty+3)+' Z';
      // horizontal stabiliser
      var stab = F.tail==='T'
        ? 'M'+n(ft1-0.035*Ln)+','+n(fty+1)+' L'+n(ft2+0.035*Ln)+','+n(fty+3)+' L'+n(ft2+0.03*Ln)+','+n(fty+8)+' L'+n(ft1-0.03*Ln)+','+n(fty+7)+' Z'
        : 'M'+n(x0+0.005*Ln)+','+n(cy-h*0.2)+' L'+n(x0+0.15*Ln)+','+n(cy-h*0.08)+' L'+n(x0+0.15*Ln)+','+n(cy-h*0.0)+' L'+n(x0-0.005*Ln)+','+n(cy-h*0.08)+' Z';

      var parts='', hiWing='';
      // wing (low or high)
      var wx=xn-0.5*Ln;
      if(F.high||F.eng==='prop'){
        var hw='M'+n(wx-0.13*Ln)+','+n(ty+1)+' L'+n(wx+0.1*Ln)+','+n(ty-5)+' Q'+n(wx+0.13*Ln)+','+n(ty-2)+' '+n(wx+0.12*Ln)+','+n(ty+4)+' L'+n(wx-0.15*Ln)+','+n(ty+8)+' Z';
        hiWing='<path d="'+hw+'" fill="'+shade(bodyCol==W?'#dfe5ec':bodyCol,-0.28)+'"/>';
      }
      // engines behind fuselage (far side) for quads
      function nacelle(cx,cyE,ew,eh,col,dark){
        var x1=cx-ew/2,x2=cx+ew/2;
        var np='M'+n(x1)+','+n(cyE-eh*0.38)+' L'+n(x2-6)+','+n(cyE-eh/2)+' Q'+n(x2+3)+','+n(cyE)+' '+n(x2-6)+','+n(cyE+eh/2)+' L'+n(x1)+','+n(cyE+eh*0.38)+' Q'+n(x1-ew*0.12)+','+n(cyE)+' '+n(x1)+','+n(cyE-eh*0.38)+' Z';
        return '<path d="'+np+'" fill="'+(dark?shade(col,-0.25):col)+'"/><path d="'+np+'" fill="url(#'+id+'ge)"/>'+
               '<path d="M'+n(x1+ew*0.35)+','+n(cyE-eh*0.44)+' L'+n(x1+ew*0.35)+','+n(cyE+eh*0.44)+'" stroke="#000" stroke-opacity="0.18" stroke-width="0.8"/>'+
               '<ellipse cx="'+n(x2-3)+'" cy="'+n(cyE)+'" rx="4.5" ry="'+n(eh/2-0.5)+'" fill="'+shade(col,0.35)+'"/>'+
               '<ellipse cx="'+n(x2-2.5)+'" cy="'+n(cyE)+'" rx="2.6" ry="'+n(eh/2-3)+'" fill="#11161d"/>'+
               '<ellipse cx="'+n(x2-4)+'" cy="'+n(cyE)+'" rx="4" ry="'+n(eh/2-1)+'" fill="'+shade(col,-0.45)+'"/>'+
               '<path d="M'+n(x1-ew*0.12)+','+n(cyE)+' L'+n(x1+4)+','+n(cyE-eh*0.22)+' L'+n(x1+4)+','+n(cyE+eh*0.22)+' Z" fill="#4a5361"/>';
      }
      var eY=yb+F.eh*0.18, eX=xn-(F.quad?0.39:0.42)*Ln, front='';
      var wingP='M'+n(wx-0.17*Ln)+','+n(yb-h*0.05)+' L'+n(wx+0.11*Ln)+','+n(yb-h*0.3)+' Q'+n(wx+0.135*Ln)+','+n(yb-h*0.26)+' '+n(wx+0.13*Ln)+','+n(yb-h*0.12)+' L'+n(wx-0.16*Ln)+','+n(yb+h*0.1)+' Z';
      var flaps=''; for(var fi=0;fi<3;fi++){ var fx=wx-0.13*Ln+fi*0.07*Ln, fy=yb+h*0.05-fi*h*0.07;
        flaps+='<path d="M'+n(fx)+','+n(fy-h*0.02)+' L'+n(fx+0.035*Ln)+','+n(fy-h*0.06)+' Q'+n(fx+0.02*Ln)+','+n(fy+h*0.1)+' '+n(fx-0.012*Ln)+','+n(fy+h*0.1)+' Z" fill="#000" opacity="0.22"/>'; }

      // fuselage + livery (clipped)
      var defs='<defs>'+
        '<linearGradient id="'+id+'gf" x1="0" y1="0" x2="0" y2="1">'+
          '<stop offset="0" stop-color="#000" stop-opacity="0.10"/><stop offset="0.12" stop-color="#fff" stop-opacity="0.45"/><stop offset="0.3" stop-color="#fff" stop-opacity="0.12"/>'+
          '<stop offset="0.55" stop-color="#000" stop-opacity="0.03"/><stop offset="0.8" stop-color="#000" stop-opacity="0.20"/><stop offset="1" stop-color="#000" stop-opacity="0.45"/></linearGradient>'+
        '<linearGradient id="'+id+'ge" x1="0" y1="0" x2="0" y2="1">'+
          '<stop offset="0" stop-color="#fff" stop-opacity="0.45"/><stop offset="0.35" stop-color="#fff" stop-opacity="0.05"/><stop offset="1" stop-color="#000" stop-opacity="0.38"/></linearGradient>'+
        '<linearGradient id="'+id+'gt" x1="0" y1="0" x2="1" y2="0">'+
          '<stop offset="0" stop-color="#000" stop-opacity="0.18"/><stop offset="0.6" stop-color="#fff" stop-opacity="0.08"/><stop offset="1" stop-color="#fff" stop-opacity="0.22"/></linearGradient>'+
        '<linearGradient id="'+id+'gw" x1="0" y1="0" x2="0" y2="1">'+
          '<stop offset="0" stop-color="#fff" stop-opacity="0.35"/><stop offset="1" stop-color="#000" stop-opacity="0.25"/></linearGradient>'+
        '</defs>';
      var clip='<clipPath id="'+id+'f"><path d="'+fus+'"/>'+(hump?'<path d="'+hump+'"/>':'')+'</clipPath>';
      var liv='<g clip-path="url(#'+id+'f)"><rect x="0" y="0" width="720" height="'+n(cy+h*0.12)+'" fill="'+bodyCol+'"/>'+
        '<rect x="0" y="'+n(cy+h*0.12)+'" width="720" height="300" fill="'+belly+'"/>'+
        (lv.accent?'<rect x="0" y="'+n(cy+h*0.08)+'" width="720" height="'+n(Math.max(3,h*0.08))+'" fill="'+lv.accent+'"/>':'')+
        '<rect x="0" y="'+n(ty)+'" width="720" height="'+n(h)+'" fill="url(#'+id+'gf)"/>'+
        (F.eng==='wing'||F.eng==='quad' ? '<ellipse cx="'+n(xn-0.5*Ln)+'" cy="'+n(yb)+'" rx="'+n(0.13*Ln)+'" ry="'+n(h*0.22)+'" fill="#000" opacity="0.10"/>' : '')+
        '<path d="M'+n(x0+0.22*Ln)+','+n(ty+h*0.13)+' L'+n(xn-0.14*Ln)+','+n(ty+h*0.13)+'" stroke="#fff" stroke-opacity="0.45" stroke-width="'+n(Math.max(1.2,h*0.05))+'" stroke-linecap="round"/>'+
        '</g>';
      var outline='<path d="'+fus+'" fill="none" stroke="'+edge+'" stroke-width="1" opacity="0.45"/>';
      // windows + cockpit + door
      var winY=cy-h*(F.hump?0.14:0.13), win='';
      if(famName!=='light'){
        (function(){
          var wy=winY, wh=Math.max(2.6,h*0.1), ww=Math.max(1.8,wh*0.62), gap=famName==='biz'?ww*3.2:ww*1.75;
          var xa=x0+0.25*Ln, xb=xn-0.19*Ln, doorF=xn-0.21*Ln, doorR=x0+0.28*Ln;
          for(var wx2=xa; wx2<xb; wx2+=gap){
            win+='<rect x="'+n(wx2)+'" y="'+n(wy-wh/2)+'" width="'+n(ww)+'" height="'+n(wh)+'" rx="'+n(ww*0.45)+'" fill="#1f2834"/>';
          }
          // doors (fore, aft) and over-wing exit outlines
          var dh=h*0.44, dw=Math.max(4,h*0.17);
          [doorF, doorR].forEach(function(dx){ win+='<rect x="'+n(dx)+'" y="'+n(wy-dh*0.32)+'" width="'+n(dw)+'" height="'+n(dh)+'" rx="'+n(dw*0.3)+'" fill="none" stroke="#000" stroke-opacity="0.28" stroke-width="0.9"/>' });
          if(F.eng==='wing'||F.eng==='quad') win+='<rect x="'+n(xn-0.53*Ln)+'" y="'+n(wy-wh*0.9)+'" width="'+n(dw*0.75)+'" height="'+n(wh*1.8)+'" rx="2" fill="none" stroke="#000" stroke-opacity="0.2" stroke-width="0.8"/>';
        })();
        if(false) win+='<line x1="'+n(x0+0.27*Ln)+'" y1="'+n(cy-h*0.34)+'" x2="'+n(xn-0.2*Ln)+'" y2="'+n(cy-h*0.34)+'" stroke="#222b38" stroke-width="'+n(h*0.07)+'" stroke-dasharray="3 5" opacity="0.85"/>';
        if(F.hump) win+='<line x1="'+n(xn-0.3*Ln)+'" y1="'+n(ty-h*0.12)+'" x2="'+n(xn-0.14*Ln)+'" y2="'+n(ty-h*0.12)+'" stroke="#222b38" stroke-width="'+n(h*0.07)+'" stroke-dasharray="3 5" opacity="0.85"/>';
      } else {
        win+='<path d="M'+n(xn-0.42*Ln)+','+n(ty+3)+' L'+n(xn-0.2*Ln)+','+n(ty+3)+' L'+n(xn-0.12*Ln)+','+n(cy)+' L'+n(xn-0.42*Ln)+','+n(cy)+' Z" fill="#222b38" opacity="0.85"/>';
      }
      var ckx=xn-(F.nose==='B'?0.072:0.085)*Ln, cky=cy-h*(F.nose==='B'?0.12:0.1);
      var ck=(famName==='light')?'':'<path d="M'+n(ckx)+','+n(cky)+' L'+n(ckx+0.012*Ln)+','+n(cy-h*0.31)+' L'+n(xn-0.032*Ln)+','+n(cy-h*0.2)+' L'+n(xn-0.02*Ln)+','+n(cky+h*0.05)+' Z" fill="#1a222d"/>'+
        '<path d="M'+n(ckx+0.024*Ln)+','+n(cy-h*0.27)+' L'+n(ckx+0.022*Ln)+','+n(cky+h*0.02)+' M'+n(ckx+0.042*Ln)+','+n(cy-h*0.22)+' L'+n(ckx+0.04*Ln)+','+n(cky+h*0.035)+'" stroke="'+bodyCol+'" stroke-width="1.6"/>'+
        '<path d="M'+n(ckx+0.004*Ln)+','+n(cky-h*0.04)+' L'+n(ckx+0.013*Ln)+','+n(cy-h*0.27)+'" stroke="#fff" stroke-opacity="0.25" stroke-width="1.2"/>';
      if(F.hump) ck='<path d="M'+n(xn-0.12*Ln)+','+n(ty-h*0.08)+' L'+n(xn-0.09*Ln)+','+n(ty-h*0.2)+' L'+n(xn-0.06*Ln)+','+n(ty-h*0.02)+' Z" fill="#1d2632"/>'+ck;

      // fin with logo emblem
      var finSvg='<path d="'+finP+'" fill="'+tail+'"/><path d="'+finP+'" fill="url(#'+id+'gt)"/>'+
        '<path d="M'+n(fb2-3)+','+n(ty+2)+' L'+n(ft2-1)+','+n(fty+1)+'" stroke="#fff" stroke-opacity="0.35" stroke-width="1.6"/>'+
        '<path d="'+finP+'" fill="none" stroke="'+shade(tail,-0.25)+'" stroke-width="1"/>';
      // fit the largest square logo inside the fin, centred on its chord
      function edgeX(x1,y1,x2,y2,y){ return x1+(x2-x1)*(y-y1)/(y2-y1) }
      function rearAt(y){ return edgeX(ft1,fty,fb1,cy-h*0.4,y) }
      function frontAt(y){ return edgeX(ft2,fty,fb2,ty+3,y) }
      var finBottom=ty-3, sz=fin*0.62, top, lcx, lcy;
      for(;sz>12;sz-=1){
        lcy=fty+(finBottom-fty)*0.52; top=lcy-sz/2;
        if(top<fty+4||lcy+sz/2>finBottom) continue;
        var rT=rearAt(top), fT=frontAt(top), rB=rearAt(lcy+sz/2), fB=frontAt(lcy+sz/2);
        var left=Math.max(rT,rB)+3, right=Math.min(fT,fB)-3;
        if(right-left>=sz){ lcx=(left+right)/2; break; }
      }
      if(iata && lcx){
        finSvg+='<clipPath id="'+id+'l"><rect x="'+n(lcx-sz/2)+'" y="'+n(lcy-sz/2)+'" width="'+n(sz)+'" height="'+n(sz)+'" rx="'+n(sz*0.24)+'"/></clipPath>'+
          '<image clip-path="url(#'+id+'l)" x="'+n(lcx-sz/2)+'" y="'+n(lcy-sz/2)+'" width="'+n(sz)+'" height="'+n(sz)+'" href="https://pics.avs.io/al_square/128/128/'+iata+'.png" xlink:href="https://pics.avs.io/al_square/128/128/'+iata+'.png" preserveAspectRatio="xMidYMid slice"/>';
      } else if(opts.cat && CAT[opts.cat] && lcx){
        var bs=sz*1.05;
        finSvg+='<svg x="'+n(lcx-bs/2)+'" y="'+n(lcy-bs/2)+'" width="'+n(bs)+'" height="'+n(bs)+'" viewBox="0 0 100 100">'+badgeInner(opts.cat)+'</svg>';
      } else if(opts.logoUrl && lcx){
        // transparent logo painted straight onto the fin, in white (or dark on a light fin)
        var light=lum(lv.tail)>0.7, mat=light?'0 0 0 0 0.1  0 0 0 0 0.12  0 0 0 0 0.16  0 0 0 1 0':'0 0 0 0 1  0 0 0 0 1  0 0 0 0 1  0 0 0 1 0';
        var ls=sz*1.1;
        finSvg+='<filter id="'+id+'w"><feColorMatrix type="matrix" values="'+mat+'"/></filter>'+
          '<image filter="url(#'+id+'w)" x="'+n(lcx-ls/2)+'" y="'+n(lcy-ls/2)+'" width="'+n(ls)+'" height="'+n(ls)+'" href="'+opts.logoUrl+'" xlink:href="'+opts.logoUrl+'" preserveAspectRatio="xMidYMid meet"/>';
      }
      var regTxt=reg?'<text x="'+n(x0+0.205*Ln)+'" y="'+n(cy+h*0.36)+'" font-size="'+n(Math.max(10,h*0.19))+'" font-family="sans-serif-condensed,sans-serif" fill="'+(lum(belly)>0.5?'#3a4554':'#ffffff')+'" opacity="0.65">'+reg+'</text>':'';

      var front='';
      if(F.eng==='quad'){
        front+='<path d="'+wingP+'" fill="#b7c0ca"/><path d="'+wingP+'" fill="url(#'+id+'gw)"/>'+flaps+'<path d="M'+n(wx-0.165*Ln)+','+n(yb-h*0.04)+' L'+n(wx+0.105*Ln)+','+n(yb-h*0.29)+'" stroke="#fff" stroke-opacity="0.55" stroke-width="1.3"/>';
        var oX=eX-0.17*Ln, oY=eY-F.eh*0.25;
        front+='<path d="M'+n(oX-F.ew*0.1)+','+n(oY-F.eh*0.4)+' L'+n(oX+F.ew*0.2)+','+n(yb-h*0.2)+' L'+n(oX+F.ew*0.32)+','+n(yb-h*0.2)+' L'+n(oX+F.ew*0.2)+','+n(oY-F.eh*0.4)+' Z" fill="'+shade(belly,-0.25)+'"/>';
        front+=nacelle(oX,oY,F.ew*0.95,F.eh*0.95,eng,true);
        front+='<path d="M'+n(eX-F.ew*0.15)+','+n(eY-F.eh*0.4)+' L'+n(eX+F.ew*0.25)+','+n(yb-h*0.15)+' L'+n(eX+F.ew*0.35)+','+n(yb-h*0.15)+' L'+n(eX+F.ew*0.2)+','+n(eY-F.eh*0.4)+' Z" fill="'+shade(belly,-0.2)+'"/>';
        front+=nacelle(eX,eY,F.ew,F.eh,eng,false);
      } else if(F.eng==='wing'){
        front+='<path d="'+wingP+'" fill="#b7c0ca"/><path d="'+wingP+'" fill="url(#'+id+'gw)"/>'+flaps+'<path d="M'+n(wx-0.165*Ln)+','+n(yb-h*0.04)+' L'+n(wx+0.105*Ln)+','+n(yb-h*0.29)+'" stroke="#fff" stroke-opacity="0.55" stroke-width="1.3"/>';
        front+='<path d="M'+n(eX-F.ew*0.15)+','+n(eY-F.eh*0.4)+' L'+n(eX+F.ew*0.25)+','+n(yb-h*0.15)+' L'+n(eX+F.ew*0.35)+','+n(yb-h*0.15)+' L'+n(eX+F.ew*0.2)+','+n(eY-F.eh*0.4)+' Z" fill="'+shade(belly,-0.2)+'"/>';
        front+=nacelle(eX,eY,F.ew,F.eh,eng,false);
      } else if(F.eng==='rear'){
        var rx=x0+0.29*Ln, ry=cy-h*0.42;
        front+='<path d="M'+n(rx-0.03*Ln)+','+n(ry+F.eh*0.3)+' L'+n(rx+0.02*Ln)+','+n(cy-h*0.05)+' L'+n(rx+0.06*Ln)+','+n(cy-h*0.05)+' L'+n(rx+0.03*Ln)+','+n(ry+F.eh*0.3)+' Z" fill="'+shade(bodyCol,-0.2)+'"/>';
        front+=nacelle(rx,ry,F.ew,F.eh,eng===W?'#e9edf2':eng,false);
        front+='<path d="'+wingP+'" fill="'+shade(belly,-0.32)+'"/>';
      } else if(F.eng==='prop'){
        var px=wx+0.07*Ln, py=ty+4, pe=eng===W?'#e9edf2':eng;
        front+='<path d="M'+n(px-0.15*Ln)+','+n(py-2)+' L'+n(px-0.02*Ln)+','+n(py-6)+' Q'+n(px+8)+','+n(py+h*0.22)+' '+n(px-0.02*Ln)+','+n(py+h*0.48)+' L'+n(px-0.13*Ln)+','+n(py+h*0.36)+' Q'+n(px-0.19*Ln)+','+n(py+h*0.1)+' '+n(px-0.15*Ln)+','+n(py-2)+' Z" fill="'+pe+'"/>';
        front+='<path d="M'+n(px-0.15*Ln)+','+n(py-2)+' L'+n(px-0.02*Ln)+','+n(py-6)+' Q'+n(px+8)+','+n(py+h*0.22)+' '+n(px-0.02*Ln)+','+n(py+h*0.48)+' L'+n(px-0.13*Ln)+','+n(py+h*0.36)+' Q'+n(px-0.19*Ln)+','+n(py+h*0.1)+' '+n(px-0.15*Ln)+','+n(py-2)+' Z" fill="none" stroke="'+shade(pe,-0.25)+'" stroke-width="1"/>';
        front+='<ellipse cx="'+n(px+6)+'" cy="'+n(py+h*0.21)+'" rx="3.5" ry="'+n(h*0.95)+'" fill="#c3cbd5" opacity="0.3"/><ellipse cx="'+n(px+5)+'" cy="'+n(py+h*0.21)+'" rx="7" ry="7" fill="#4a5361"/>';
      } else if(F.eng==='nose'){
        front+='<ellipse cx="'+n(xn+6)+'" cy="'+n(cy+2)+'" rx="3.5" ry="'+n(h*1.05)+'" fill="#c3cbd5" opacity="0.35"/><ellipse cx="'+n(xn+4)+'" cy="'+n(cy+2)+'" rx="6" ry="6" fill="#4a5361"/>';
        front+='<path d="M'+n(xn-0.5*Ln)+','+n(yb)+' L'+n(xn-0.55*Ln)+','+n(yb+20)+' M'+n(xn-0.2*Ln)+','+n(yb)+' L'+n(xn-0.18*Ln)+','+n(yb+18)+'" stroke="#5f6b7a" stroke-width="3"/>';
      }

      // ---- type details ----
      var extra='', back='', wlet='', tri='';
      if(F.wl && (F.eng==='wing'||F.eng==='quad'||F.eng==='rear')){
        var wtx=wx-0.17*Ln, wty=yb-h*0.02, wh=h*(F.wl==='shark'?0.62:0.52), wc=shade(tail===W?'#cfd6de':tail,-0.05);
        if(F.wl==='fence') wlet+='<path d="M'+n(wtx)+','+n(wty-wh*0.45)+' L'+n(wtx+5)+','+n(wty-wh*0.45)+' L'+n(wtx+7)+','+n(wty+wh*0.4)+' L'+n(wtx-2)+','+n(wty+wh*0.4)+' Z" fill="'+wc+'"/>';
        else if(F.wl==='split') wlet+='<path d="M'+n(wtx-4)+','+n(wty)+' L'+n(wtx-wh*0.35)+','+n(wty-wh)+' L'+n(wtx-wh*0.2)+','+n(wty-wh)+' L'+n(wtx+8)+','+n(wty)+' L'+n(wtx-wh*0.05)+','+n(wty+wh*0.45)+' L'+n(wtx-wh*0.15)+','+n(wty+wh*0.45)+' Z" fill="'+wc+'"/>';
        else wlet+='<path d="M'+n(wtx-4)+','+n(wty)+' Q'+n(wtx-3)+','+n(wty-wh*0.6)+' '+n(wtx-wh*0.35)+','+n(wty-wh)+' L'+n(wtx-wh*0.18)+','+n(wty-wh)+' Q'+n(wtx+6)+','+n(wty-wh*0.4)+' '+n(wtx+8)+','+n(wty)+' Z" fill="'+wc+'"/>';
      }
      if(F.dorsal){
        extra+='<path d="M'+n(fb2-0.04*Ln)+','+n(ty-fin*0.16)+' L'+n(fb2)+','+n(ty+3)+' L'+n(fb2+0.07*Ln)+','+n(ty+1.5)+' Q'+n(fb2+0.02*Ln)+','+n(ty-1)+' '+n(fb2-0.04*Ln)+','+n(ty-fin*0.16)+' Z" fill="'+tail+'"/>';
      }
      if(F.tri){
        var tx=x0+0.13*Ln, tyE=ty-F.eh*0.35;
        tri=nacelle(tx,tyE,F.ew*1.1,F.eh,eng,false);
      }
      if(F.mask){
        ck='<path d="M'+n(xn-0.095*Ln)+','+n(cy-h*0.08)+' L'+n(xn-0.08*Ln)+','+n(cy-h*0.34)+' L'+n(xn-0.02*Ln)+','+n(cy-h*0.16)+' L'+n(xn-0.006*Ln)+','+n(cy-h*0.02)+' L'+n(xn-0.03*Ln)+','+n(cy+h*0.02)+' Z" fill="#141a22"/>';
      }
      if(F.double){
        win+='<line x1="'+n(x0+0.27*Ln)+'" y1="'+n(cy-h*0.31)+'" x2="'+n(xn-0.17*Ln)+'" y2="'+n(cy-h*0.31)+'" stroke="#222b38" stroke-width="'+n(h*0.065)+'" stroke-dasharray="3 5" opacity="0.85"/>';
      }

      body = defs + clip + back + parts +
        '<path d="'+stab+'" fill="'+shade(tail===W||lum(tail)>0.9?'#d5dbe2':tail,-0.15)+'"/><path d="'+stab+'" fill="url(#'+id+'gw)"/>' +
        liv + (hump?'':'') + outline + win + ck + regTxt + extra + finSvg +
        (F.tail==='T'?'<path d="'+stab+'" fill="'+shade(tail,-0.1)+'"/>':'') + tri + hiWing + front + wlet;
    }
    return '<svg viewBox="0 0 720 240" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" preserveAspectRatio="xMidYMid meet"><g class="ffbob">'+body+'</g></svg>';
  }

  var IATA={KLM:'KL',KLC:'WA',TRA:'HV',TVF:'TO',EZY:'U2',EJU:'EC',EZS:'DS',RYR:'FR',RUK:'RK',MAY:'FR',LDM:'OE',DLH:'LH',CLH:'LH',BAW:'BA',CFE:'BA',AFR:'AF',HOP:'A5',
    VLG:'VY',WZZ:'W6',WMT:'W4',WAZ:'5W',WUK:'W9',TFL:'OR',TOM:'BY',JAF:'TB',CND:'CD',CAI:'XC',SAS:'SK',NOZ:'DY',NSZ:'D8',IBK:'D8',DAL:'DL',UAL:'UA',AAL:'AA',ACA:'AC',
    UAE:'EK',QTR:'QR',ETD:'EY',THY:'TK',SWR:'LX',EDW:'WK',AUA:'OS',BEL:'SN',IBE:'IB',IBS:'I2',TAP:'TP',EIN:'EI',EXS:'LS',EWG:'EW',OCN:'4Y',CFG:'DE',FIN:'AY',LOT:'LO',ICE:'FI',
    PGT:'PC',SXS:'XQ',AEE:'A3',BTI:'BT',ROT:'RO',ITY:'AZ',CSN:'CZ',CPA:'CX',SIA:'SQ',KAL:'KE',SVA:'SV',KQA:'KQ',MSR:'MS',RAM:'AT',ELY:'LY',FDX:'FX',UPS:'5X',VOE:'V7',VIR:'VS',CLX:'CV',DHK:'D0',BCS:'QY'};
  window.FFPlanes={family:family, svg:svg, iata:function(icao){return IATA[icao]||''}, known:function(icao){return !!LIV[icao]}, badgeSVG:badgeSVG, catLabel:function(c){return CAT[c]?CAT[c].label:''}};
})();
