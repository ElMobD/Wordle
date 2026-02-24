// Script Node.js pour tester une connexion WebSocket avec un cookie
// Place ton token JWT dans la variable ci-dessous

import WebSocket from 'ws';


const token = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0cmFjZXJ0cmFvcmVAZ21haWwuY29tIiwibmFtZSI6IkFsYXNzYW5lIFRyYW9yZSIsImVtYWlsIjoidHJhY2VydHJhb3JlQGdtYWlsLmNvbSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQ2c4b2NJVk9jcE10SHNBSjhKNGtHT1RUMXluYXhwLUptaVpLb3d5QWd6TmpiTDNtVXc3dVZNQj1zOTYtYyIsImlhdCI6MTc3MTg3NDU1OCwiZXhwIjoxNzcxOTYwOTU4fQ.kGLvox5xlSIeUsDjKmbPdo55LvIHrlzmKGM7vokNtZnD5A5f2AXe1iGjm39VdKsROb1RTDrocbGFToi9RiQBNA'; // Remplace par ton vrai JWT
const cookieHeader = `token=${token}`;
console.log('Header Cookie envoyé :', cookieHeader);
const ws = new WebSocket('ws://localhost/ws/lobby', {
  headers: {
    'Cookie': cookieHeader
  }
});

ws.on('open', () => {
  console.log('Connecté au WebSocket!');
  ws.send(JSON.stringify({ type: 'join', sessionCode: '265276FC' }));
  // ...dans ws.on('open')
ws.send(JSON.stringify({ type: 'join', sessionCode: '265276FC' }));

// Exemple d'envoi d'un message dans la session (adapte le type/structure si besoin)
setTimeout(() => {
  ws.send(JSON.stringify({
    type: 'chat',
    sessionCode: '265276FC',
    message: 'Hello à tous !'
  }));
  console.log('Message envoyé dans la session !');
}, 1000);
});

ws.on('message', (data) => {
  console.log('Message reçu:', data.toString());
});

ws.on('close', () => {
  console.log('Déconnecté du WebSocket');
});

ws.on('error', (err) => {
  console.error('Erreur WebSocket:', err);
});
