const PAIR_THEMES = {
  nature: {
    label: { en: "Nature", es: "Naturaleza", "zh-Hans": "\u81EA\u7136", fil: "Kalikasan", vi: "Thi\xEAn nhi\xEAn" },
    pairs: [
      { emoji: "\u{1F343}", label: { en: "Leaf", es: "Hoja", "zh-Hans": "\u53F6\u5B50", fil: "Dahon", vi: "L\xE1 c\xE2y" } },
      { emoji: "\u{1F33C}", label: { en: "Flower", es: "Flor", "zh-Hans": "\u82B1\u6735", fil: "Bulaklak", vi: "Hoa" } },
      { emoji: "\u{1F426}", label: { en: "Bird", es: "P\xE1jaro", "zh-Hans": "\u5C0F\u9E1F", fil: "Ibon", vi: "Chim" } },
      { emoji: "\u2600\uFE0F", label: { en: "Sun", es: "Sol", "zh-Hans": "\u592A\u9633", fil: "Araw", vi: "M\u1EB7t tr\u1EDDi" } },
      { emoji: "\u2601\uFE0F", label: { en: "Cloud", es: "Nube", "zh-Hans": "\u4E91\u6735", fil: "Ulap", vi: "M\xE2y" } },
      { emoji: "\u{1F30A}", label: { en: "River", es: "R\xEDo", "zh-Hans": "\u6CB3\u6D41", fil: "Ilog", vi: "S\xF4ng" } },
      { emoji: "\u{1F330}", label: { en: "Acorn", es: "Bellota", "zh-Hans": "\u6A61\u679C", fil: "Bunga ng oak", vi: "Qu\u1EA3 s\u1ED3i" } },
      { emoji: "\u{1F344}", label: { en: "Mushroom", es: "Hongo", "zh-Hans": "\u8611\u83C7", fil: "Kabute", vi: "N\u1EA5m" } },
      { emoji: "\u{1F331}", label: { en: "Seedling", es: "Pl\xE1ntula", "zh-Hans": "\u5E7C\u82D7", fil: "Punla", vi: "C\xE2y non" } },
      { emoji: "\u{1F333}", label: { en: "Tree", es: "\xC1rbol", "zh-Hans": "\u6811", fil: "Puno", vi: "C\xE2y" } },
      { emoji: "\u{1F334}", label: { en: "Palm tree", es: "Palmera", "zh-Hans": "\u68D5\u6988\u6811", fil: "Puno ng palma", vi: "C\xE2y c\u1ECD" } },
      { emoji: "\u{1F335}", label: { en: "Cactus", es: "Cactus", "zh-Hans": "\u4ED9\u4EBA\u638C", fil: "Cactus", vi: "X\u01B0\u01A1ng r\u1ED3ng" } }
    ]
  },
  home: {
    label: { en: "Home", es: "Hogar", "zh-Hans": "\u5BB6\u5C45", fil: "Tahanan", vi: "Trong nh\xE0" },
    pairs: [
      { emoji: "\u2615", label: { en: "Cup", es: "Taza", "zh-Hans": "\u676F\u5B50", fil: "Tasa", vi: "C\u1ED1c" } },
      { emoji: "\u{1F4D8}", label: { en: "Book", es: "Libro", "zh-Hans": "\u4E66\u672C", fil: "Aklat", vi: "S\xE1ch" } },
      { emoji: "\u{1F4A1}", label: { en: "Lamp", es: "L\xE1mpara", "zh-Hans": "\u53F0\u706F", fil: "Ilaw", vi: "\u0110\xE8n" } },
      { emoji: "\u{1FA91}", label: { en: "Chair", es: "Silla", "zh-Hans": "\u6905\u5B50", fil: "Upuan", vi: "Gh\u1EBF" } },
      { emoji: "\u{1F511}", label: { en: "Key", es: "Llave", "zh-Hans": "\u94A5\u5319", fil: "Susi", vi: "Ch\xECa kh\xF3a" } },
      { emoji: "\u{1F570}\uFE0F", label: { en: "Clock", es: "Reloj", "zh-Hans": "\u65F6\u949F", fil: "Orasan", vi: "\u0110\u1ED3ng h\u1ED3" } },
      { emoji: "\u{1F37D}\uFE0F", label: { en: "Plate", es: "Plato", "zh-Hans": "\u76D8\u5B50", fil: "Pinggan", vi: "\u0110\u0129a" } },
      { emoji: "\u{1F3E0}", label: { en: "House", es: "Casa", "zh-Hans": "\u623F\u5B50", fil: "Bahay", vi: "Nh\xE0" } },
      { emoji: "\u{1F6CF}\uFE0F", label: { en: "Bed", es: "Cama", "zh-Hans": "\u5E8A", fil: "Kama", vi: "Gi\u01B0\u1EDDng" } },
      { emoji: "\u{1F9F9}", label: { en: "Broom", es: "Escoba", "zh-Hans": "\u626B\u5E1A", fil: "Walis", vi: "Ch\u1ED5i" } },
      { emoji: "\u{1F9FC}", label: { en: "Soap", es: "Jab\xF3n", "zh-Hans": "\u80A5\u7682", fil: "Sabon", vi: "X\xE0 ph\xF2ng" } },
      { emoji: "\u{1F9F0}", label: { en: "Toolbox", es: "Caja de herramientas", "zh-Hans": "\u5DE5\u5177\u7BB1", fil: "Kahon ng mga kasangkapan", vi: "H\u1ED9p d\u1EE5ng c\u1EE5" } }
    ]
  },
  food: {
    label: { en: "Food", es: "Comida", "zh-Hans": "\u98DF\u7269", fil: "Pagkain", vi: "M\xF3n \u0103n" },
    pairs: [
      { emoji: "\u{1F34E}", label: { en: "Apple", es: "Manzana", "zh-Hans": "\u82F9\u679C", fil: "Mansanas", vi: "T\xE1o" } },
      { emoji: "\u{1F35E}", label: { en: "Bread", es: "Pan", "zh-Hans": "\u9762\u5305", fil: "Tinapay", vi: "B\xE1nh m\xEC" } },
      { emoji: "\u{1F350}", label: { en: "Pear", es: "Pera", "zh-Hans": "\u68A8\u5B50", fil: "Peras", vi: "L\xEA" } },
      { emoji: "\u{1F963}", label: { en: "Soup", es: "Sopa", "zh-Hans": "\u6C64", fil: "Sabaw", vi: "S\xFAp" } },
      { emoji: "\u{1F35A}", label: { en: "Rice", es: "Arroz", "zh-Hans": "\u7C73\u996D", fil: "Bigas", vi: "C\u01A1m" } },
      { emoji: "\u{1F34C}", label: { en: "Banana", es: "Pl\xE1tano", "zh-Hans": "\u9999\u8549", fil: "Saging", vi: "Chu\u1ED1i" } },
      { emoji: "\u{1F955}", label: { en: "Carrot", es: "Zanahoria", "zh-Hans": "\u80E1\u841D\u535C", fil: "Karot", vi: "C\xE0 r\u1ED1t" } },
      { emoji: "\u{1F9C0}", label: { en: "Cheese", es: "Queso", "zh-Hans": "\u5976\u916A", fil: "Keso", vi: "Ph\xF4 mai" } },
      { emoji: "\u{1F33D}", label: { en: "Corn", es: "Ma\xEDz", "zh-Hans": "\u7389\u7C73", fil: "Mais", vi: "B\u1EAFp" } },
      { emoji: "\u{1F34A}", label: { en: "Orange", es: "Naranja", "zh-Hans": "\u6A58\u5B50", fil: "Dalandan", vi: "Cam" } },
      { emoji: "\u{1F353}", label: { en: "Strawberry", es: "Fresa", "zh-Hans": "\u8349\u8393", fil: "Strawberry", vi: "D\xE2u t\xE2y" } },
      { emoji: "\u{1F95A}", label: { en: "Egg", es: "Huevo", "zh-Hans": "\u9E21\u86CB", fil: "Itlog", vi: "Tr\u1EE9ng" } }
    ]
  }
};
const WORD_SEARCH_THEMES = {
  nature: {
    label: { en: "Nature", es: "Naturaleza", "zh-Hans": "\u81EA\u7136", fil: "Kalikasan", vi: "Thi\xEAn nhi\xEAn" },
    words: {
      en: ["FLOWER", "LEAF", "TREE", "BIRD", "CLOUD", "RIVER", "FOREST", "OCEAN", "GARDEN", "RAINBOW"],
      es: ["FLOR", "HOJA", "ARBOL", "AVE", "NUBE", "RIO", "BOSQUE", "MAR", "JARDIN", "ARCOIRIS"],
      "zh-Hans": ["\u82B1\u6735", "\u6811\u53F6", "\u6811\u6728", "\u5C0F\u9E1F", "\u4E91\u6735", "\u6CB3\u6D41", "\u68EE\u6797", "\u6D77\u6D0B", "\u82B1\u56ED", "\u5F69\u8679"],
      fil: ["BULAKLAK", "DAHON", "PUNO", "IBON", "ULAP", "ILOG", "GUBAT", "DAGAT", "ARAW", "BATO"],
      vi: ["HOA", "L\xC1", "C\xC2Y", "CHIM", "M\xC2Y", "S\xD4NG", "R\u1EEANG", "BI\u1EC2N", "V\u01AF\u1EDCN", "C\u1EA6U V\u1ED2NG"]
    }
  },
  home: {
    label: { en: "Home", es: "Hogar", "zh-Hans": "\u5BB6\u5C45", fil: "Tahanan", vi: "Trong nh\xE0" },
    words: {
      en: ["CUP", "BOOK", "LAMP", "CHAIR", "DOOR", "TABLE", "PLATE", "CLOCK", "PILLOW", "WINDOW"],
      es: ["TAZA", "LIBRO", "LUZ", "SILLA", "PUERTA", "MESA", "PLATO", "RELOJ", "CAMA", "VENTANA"],
      "zh-Hans": ["\u676F\u5B50", "\u4E66\u672C", "\u53F0\u706F", "\u6905\u5B50", "\u623F\u95E8", "\u684C\u5B50", "\u76D8\u5B50", "\u65F6\u949F", "\u6795\u5934", "\u7A97\u6237"],
      fil: ["BASO", "AKLAT", "ILAW", "SILYA", "PINTO", "MESA", "PLATO", "ORASAN", "UNAN", "BINTANA"],
      vi: ["C\u1ED0C", "S\xC1CH", "\u0110\xC8N", "GH\u1EBE", "C\u1EECA", "B\xC0N", "\u0110\u0128A", "\u0110\u1ED2NG H\u1ED2", "GI\u01AF\u1EDCNG", "C\u1EECA S\u1ED4"]
    }
  },
  food: {
    label: { en: "Food", es: "Comida", "zh-Hans": "\u98DF\u7269", fil: "Pagkain", vi: "M\xF3n \u0103n" },
    words: {
      en: ["APPLE", "BREAD", "PEAR", "SOUP", "RICE", "BANANA", "CARROT", "CHEESE", "ORANGE", "POTATO"],
      es: ["MANZANA", "PAN", "PERA", "SOPA", "ARROZ", "PLATANO", "ZANAHORIA", "QUESO", "NARANJA", "UVA"],
      "zh-Hans": ["\u82F9\u679C", "\u9762\u5305", "\u68A8\u5B50", "\u7C73\u996D", "\u8611\u83C7", "\u9999\u8549", "\u80E1\u841D\u535C", "\u5976\u916A", "\u9E21\u86CB", "\u571F\u8C46"],
      fil: ["MANSANAS", "TINAPAY", "PERAS", "SABAW", "BIGAS", "SAGING", "KAROT", "KESO", "DALANDAN", "ITLOG"],
      vi: ["T\xC1O", "B\xC1NH M\xCC", "L\xCA", "S\xDAP", "C\u01A0M", "CHU\u1ED0I", "C\xC0 R\u1ED0T", "PH\xD4 MAI", "CAM", "KHOAI"]
    }
  }
};
const PAIR_ASSETS = {
  nature: ["1F343", "1F33C", "1F426", "2600", "2601", "1F30A", "1F330", "1F344", "1F331", "1F333", "1F334", "1F335"],
  home: ["2615", "1F4D8", "1F4A1", "1FA91", "1F511", "1F570", "1F37D", "1F3E0", "1F6CF", "1F9F9", "1F9FC", "1F9F0"],
  food: ["1F34E", "1F35E", "1F350", "1F372", "1F35A", "1F34C", "1F955", "1F9C0", "1F33D", "1F34A", "1F353", "1F95A"]
};
export function pairThemes(language = "en") {
  return Object.entries(PAIR_THEMES).map(([id, theme]) => ({
    id,
    label: theme.label[language] || theme.label.en,
    pairs: theme.pairs.map((pair, index) => ({ pairId: `${id}-${index}`, emoji: pair.emoji, asset: `/media/openmoji/${PAIR_ASSETS[id][index]}.svg`, label: pair.label[language] || pair.label.en }))
  }));
}
export function wordSearchThemes(language = "en") {
  return Object.entries(WORD_SEARCH_THEMES).map(([id, theme]) => ({
    id,
    label: theme.label[language] || theme.label.en,
    words: theme.words[language] || theme.words.en
  }));
}
