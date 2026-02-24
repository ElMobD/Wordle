import WebSocket from 'ws';

// Place ton token JWT valide ici
const token = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0cmFjZXJ0cmFvcmVAZ21haWwuY29tIiwibmFtZSI6IkFsYXNzYW5lIFRyYW9yZSIsImVtYWlsIjoidHJhY2VydHJhb3JlQGdtYWlsLmNvbSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQ2c4b2NJVk9jcE10SHNBSjhKNGtHT1RUMXluYXhwLUptaVpLb3d5QWd6TmpiTDNtVXc3dVZNQj1zOTYtYyIsImlhdCI6MTc3MTk1MzQ1MSwiZXhwIjoxNzcyMDM5ODUxfQ._bVpi-BfysIrcvmv28699TmHYVlA5nkTxgI0dDudVmhv6XT5FsAxH04uTYhvSeQPbbXP9-46-nQFp0ihfyU54A'; // Remplace par un vrai token
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
        sessionCode: "B4B9F271"
    }));
    setTimeout(() => {
      ws.send(JSON.stringify({
        type: 'CHAT',
        sessionCode: 'B4B9F271',
        message: 'Hello from script 2!'
      }));
      console.log('Message de chat envoyé');
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
