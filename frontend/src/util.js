export const rupee = (n) => '₹' + Number(n).toLocaleString('en-IN');

export function timeAgo(iso) {
  const s = Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 1000));
  if (s < 60) return 'Just now';
  if (s < 3600) return Math.floor(s / 60) + ' min ago';
  if (s < 86400) return Math.floor(s / 3600) + ' hr ago';
  return new Date(iso).toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
}

export const dateTime = (iso) =>
  new Date(iso).toLocaleString('en-IN', { day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit' });

const RESTAURANT_PHOTOS = {
  'spice route biryani': 'photo-1563379091339-03246963d51a',
  'vizag tiffins': 'photo-1589301760014-d929f3979dbc',
  'pizza piazza': 'photo-1513104890138-7c749659a591',
  'dragon wok': 'photo-1569718212165-3a8278d5f624',
  'green bowl': 'photo-1512621776951-a57141f2eefd',
  'burger barn': 'photo-1568901346375-23c9450c58cd',
  'sweet tooth bakery': 'photo-1578985545062-69928b1d9587',
  'kebab junction': 'photo-1544025162-d76694265947',
  'coastal curry house': 'photo-1519708227418-c8fd9a32b7a2',
  'chai & chaat corner': 'photo-1601050690597-df0568f70950',
};

const FOOD_PHOTOS = [
  [/biryani|pulao|fried rice|jeera rice|pongal|andhra .*meals/i, [
    'photo-1563379091339-03246963d51a', 'photo-1631515243349-e0cb75fb8d3a',
    'photo-1599043513900-ed6fe01d3833', 'photo-1589301760014-d929f3979dbc',
  ]],
  [/dosa|idli|vada/i, [
    'photo-1589301760014-d929f3979dbc', 'photo-1630384060421-cb20d0e0649d',
    'photo-1631452180519-c014fe946bc7',
  ]],
  [/pizza/i, [
    'photo-1513104890138-7c749659a591', 'photo-1565299624946-b28f40a0ae38',
    'photo-1593560708920-61dd98c46a4e', 'photo-1571407970349-bc81e7e96d47',
  ]],
  [/pasta|penne/i, [
    'photo-1473093295043-cdd812d0e601', 'photo-1551183053-bf91a1d81141',
    'photo-1476124369491-e7a3f6e8f7a3',
  ]],
  [/burger|smash/i, [
    'photo-1568901346375-23c9450c58cd', 'photo-1550547660-d9450f859349',
    'photo-1565299507177-b0ac66763828', 'photo-1571091718767-18b5b1457add',
  ]],
  [/fries|nugget/i, [
    'photo-1573080496219-bb080dd4f877', 'photo-1630384060421-cb20d0e0649d',
  ]],
  [/noodle|manchurian|chilli chicken|crispy corn/i, [
    'photo-1569718212165-3a8278d5f624', 'photo-1585032226651-759b368d7246',
    'photo-1563245372-f21724e3856d', 'photo-1562967914-608f82629710',
  ]],
  [/salad|bowl|quinoa|sprouts|detox/i, [
    'photo-1512621776951-a57141f2eefd', 'photo-1490645935967-10de6ba17061',
    'photo-1540189549336-e6e99c3679fe', 'photo-1546069901-ba9599a7e63c',
  ]],
  [/kebab|tikka|tandoori|butter chicken|dal makhani|naan/i, [
    'photo-1544025162-d76694265947', 'photo-1631452180519-c014fe946bc7',
    'photo-1567188040759-fb8a883dc6d8', 'photo-1603894584-4f2a60d97ac',
  ]],
  [/fish|prawn|seafood|pomfret/i, [
    'photo-1519708227418-c8fd9a32b7a2', 'photo-1559737558-2f5a35f4523b',
    'photo-1535399831218-d5bd36d1a6b3',
  ]],
  [/cake|pastry|dessert|sundae|shake|ice cream|croissant|meetha/i, [
    'photo-1578985545062-69928b1d9587', 'photo-1571877227200-a0d98ea607e9',
    'photo-1563805042-7684c019e1cb', 'photo-1551024506-0cc6a1a1e2a2',
  ]],
  [/chaat|samosa|pav|puri/i, [
    'photo-1601050690597-df0568f70950', 'photo-1606491956689-2ea866880c84',
    'photo-1601050690117-94f5f6fa8bd7',
  ]],
  [/chai|coffee|juice|smoothie|lassi/i, [
    'photo-1509042239860-f550ce710b93', 'photo-1517701604599-bb29b565090c',
    'photo-1546173159-315724a31696', 'photo-1553530666-ba11a7da3888',
  ]],
  [/bread|naan|manchurian|starter/i, [
    'photo-1547592180-85f173990554', 'photo-1565299624946-b28f40a0ae38',
    'photo-1547592180-85f173990554',
  ]],
  [/.*/, [
    'photo-1504674900247-0877df9cc836', 'photo-1547592180-85f173990554',
    'photo-1631452180519-c014fe946bc7',
  ]],
];

const photoUrl = (id, width) => `https://images.unsplash.com/${id}?auto=format&fit=crop&w=${width}&q=85`;

function choosePhoto(text, id) {
  const entry = FOOD_PHOTOS.find(([pattern]) => pattern.test(text));
  if (!entry) return '';
  const photos = entry[1];
  const key = `${text}${id || ''}`;
  const index = [...key].reduce((hash, char) => (hash * 31 + char.charCodeAt(0)) >>> 0, 0) % photos.length;
  return photos[index];
}

export function restaurantPhoto(restaurant) {
  if (restaurant.imageUrl) return restaurant.imageUrl;
  const image = RESTAURANT_PHOTOS[restaurant.name.toLowerCase()] ||
    choosePhoto(`${restaurant.cuisines || ''} ${restaurant.name}`, restaurant.id);
  return image ? photoUrl(image, 900) : '';
}

export function foodPhoto(item) {
  if (item.imageUrl) return item.imageUrl;
  const image = choosePhoto(`${item.name} ${item.category || ''}`, item.id);
  return image ? photoUrl(image, 400) : '';
}

export const STATUS_STEPS = ['PLACED', 'CONFIRMED', 'PREPARING', 'OUT_FOR_DELIVERY', 'DELIVERED'];
export const STATUS_LABEL = {
  PLACED: 'Order placed',
  CONFIRMED: 'Order confirmed',
  PREPARING: 'Preparing',
  OUT_FOR_DELIVERY: 'On the way',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
};

const TILES = [
  'linear-gradient(135deg,#ffe3e6,#ffc2c9)',
  'linear-gradient(135deg,#fff1d6,#ffd98a)',
  'linear-gradient(135deg,#e2f5e9,#b4e4c6)',
  'linear-gradient(135deg,#e5ecff,#bccdff)',
  'linear-gradient(135deg,#f3e4ff,#dcb8ff)',
  'linear-gradient(135deg,#ffe9dc,#ffc7a3)',
];
export const tileBg = (id) => TILES[Number(id) % TILES.length];
