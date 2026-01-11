import img1 from './джейхины.jpg';
import img2 from './клоризли.jpg';
import img3 from './софтикиэимики.jpg';
import img4 from './biliie.jpg';
import img5 from './pfp.jpg';
import img6 from './wenclair.jpg';

const mockUsers = {
  'user1': { id: 'user1', nickname: 'crow_666_wa', pfp: img5 },
  'user2': { id: 'user2', nickname: 'biliie_art', pfp: img4 },
  'user3': { id: 'user3', nickname: 'sofka_miki', pfp: img3 },
  'user4': { id: 'user4', nickname: 'wenclair', pfp: img6 },
  'user5': { id: 'user5', nickname: 'jeyhiny', pfp: img1 },
  'user6': { id: 'user6', nickname: 'klorizli', pfp: img2 },
};

export const mockArts = [
  { 
    id: '1', 
    image: img1, 
    description: 'Джейхины', 
    tags: '#duo #anime',
    ownerId: 'user5',
    isPrivate: false,
  },
  { 
    id: '2', 
    image: img2, 
    description: 'Клоризли', 
    tags: '#solo #mood',
    ownerId: 'user6',
    isPrivate: true,
  },
  { 
    id: '3', 
    image: img3, 
    description: 'Софтика и Эймика', 
    tags: '#cute #pastel',
    ownerId: 'user3',
    isPrivate: false,
  },
  { 
    id: '4', 
    image: img4, 
    description: 'Biliie', 
    tags: '#portrait #vibe',
    ownerId: 'user2',
    isPrivate: true,
  },
  { 
    id: '5', 
    image: img5, 
    description: 'PFP', 
    tags: '#avatar #simple',
    ownerId: 'user1',
    isPrivate: false,
  },
  { 
    id: '6', 
    image: img6, 
    description: 'Wenclair', 
    tags: '#wenclair #aesthetic',
    ownerId: 'user4',
    isPrivate: true,
  },
];

export const mockArtsMap = Object.fromEntries(mockArts.map(art => [art.id, art]));
export const mockUsersMap = mockUsers;