// Script Node.js pour tester une connexion WebSocket avec un cookie
// Place ton token JWT dans la variable ci-dessous

import WebSocket from 'ws';


const token = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0cmFjZXJ0cmFvcmVAZ21haWwuY29tIiwibmFtZSI6IkFsYXNzYW5lIFRyYW9yZSIsImVtYWlsIjoidHJhY2VydHJhb3JlQGdtYWlsLmNvbSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQ2c4b2NJVk9jcE10SHNBSjhKNGtHT1RUMXluYXhwLUptaVpLb3d5QWd6TmpiTDNtVXc3dVZNQj1zOTYtYyIsImlhdCI6MTc3MjEzODI0MCwiZXhwIjoxNzcyMjI0NjQwfQ.7sd6WIk1QOBUsjGmilVsoshvZUkQ5Fy9StHkme-en-t2PSr2-7xTiw_UYOVSpVFI09DmDpACqFntHAlBsi7feQ'; // Remplace par ton vrai JWT
const cookieHeader = `token=${token}`;
console.log('Header Cookie envoyé :', cookieHeader);
const ws = new WebSocket('ws://localhost/ws/lobby', {
  headers: {
    'Cookie': cookieHeader
  }
});

ws.on('open', () => {
  console.log('Connecté au WebSocket!');
    ws.send(JSON.stringify({
        type: 'JOIN',
        sessionCode: "2829F530"
    }));
// Exemple d'envoi d'un message dans la session (adapte le type/structure si besoin)
setTimeout(() => {
  ws.send(JSON.stringify(
    { type: 'LOBBYINFOS', 
      sessionCode : "2829F530" }));
  console.log('Envoie de message après 1 seconde');
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
