// Script Node.js pour tester une connexion WebSocket avec un cookie
// Place ton token JWT dans la variable ci-dessous

import WebSocket from 'ws';


const token = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJsdWZmeXRlc3RkZXZAZ21haWwuY29tIiwibmFtZSI6Ikx1ZmZ5IE1vbmtleSBEIiwiZW1haWwiOiJsdWZmeXRlc3RkZXZAZ21haWwuY29tIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hL0FDZzhvY0pYTzdVajhJNl9NUlM4YmUyMktEbDFOUTBNemtzY2dRRlFkeXU0ZEh0ZDlKRVA9czk2LWMiLCJpYXQiOjE3NzIzOTkwMzksImV4cCI6MTc3MjQ4NTQzOX0.6zX_WYuMMKKt7LFfLZnaZzRgyozPbyazLoQ55qqdi2mCdkuUl3DOKs4Zehc0ke6aQdvL8bpM7X9bq7XxERx6Vw'; // Remplace par ton vrai JWT
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
    { type: 'CHAT', sessionCode: "2829F530", message: "Bonjour depuis le test WebSocket!" }));
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
