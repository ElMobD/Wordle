package com.wordle.backend.service;

import com.wordle.backend.model.Game;
import com.wordle.backend.model.SessionScore;
import com.wordle.backend.model.SessionScoreId;
import com.wordle.backend.repository.SessionScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class ScoreService {

    private static final Logger logger = Logger.getLogger(ScoreService.class.getName());
    
    // Constantes de scoring
    private static final int BASE_SCORE = 1000;
    private static final int PENALTY_PER_ATTEMPT = 100;
    private static final int BONUS_PER_SECOND = 5;
    
    private final SessionScoreRepository sessionScoreRepository;

    public ScoreService(SessionScoreRepository sessionScoreRepository) {
        this.sessionScoreRepository = sessionScoreRepository;
    }

    /**
     * Calcule le score pour un round gagné
     * Formule: BASE_SCORE - (tentatives * PENALTY_PER_ATTEMPT) + (temps_restant * BONUS_PER_SECOND)
     * 
     * @param attemptsUsed Nombre de tentatives utilisées
     * @param timeRemaining Temps restant en secondes
     * @return Le score calculé (minimum 0)
     */
    public int calculateRoundScore(int attemptsUsed, int timeRemaining) {
        int score = BASE_SCORE - (attemptsUsed * PENALTY_PER_ATTEMPT) + (timeRemaining * BONUS_PER_SECOND);
        return Math.max(0, score); // Le score ne peut pas être négatif
    }

    /**
     * Calcule le score pour un round perdu
     * 
     * @return 0 (pas de points pour un round perdu)
     */
    public int calculateRoundScoreLost() {
        return 0;
    }

    /**
     * Met à jour ou crée le score d'un joueur pour une session après un round
     * 
     * @param sessionId UUID de la session
     * @param userId ID du joueur
     * @param roundScore Score du round (calculé avec calculateRoundScore)
     * @param attemptsUsed Nombre de tentatives utilisées
     * @param timeRemaining Temps restant
     * @param won true si le round est gagné, false sinon
     */
    @Transactional
    public void updatePlayerScore(UUID sessionId, Long userId, int roundScore, int attemptsUsed, int timeRemaining, boolean won) {
        SessionScoreId scoreId = new SessionScoreId(sessionId, userId);
        Optional<SessionScore> existingScore = sessionScoreRepository.findById(scoreId);
        
        SessionScore sessionScore;
        if (existingScore.isPresent()) {
            sessionScore = existingScore.get();
        } else {
            // Créer un nouveau score si c'est le premier round du joueur
            sessionScore = new SessionScore(sessionId, userId);
        }
        
        // Ajouter les stats du round
        sessionScore.addRoundScore(roundScore, attemptsUsed, timeRemaining, won);
        
        sessionScoreRepository.save(sessionScore);
        
        logger.info(String.format("Score mis à jour pour userId=%d dans session %s: total=%d, rounds_won=%d, rounds_lost=%d",
            userId, sessionId, sessionScore.getTotalScore(), sessionScore.getRoundsWon(), sessionScore.getRoundsLost()));
    }

    /**
     * Récupère le classement complet d'une session
     * 
     * @param sessionId UUID de la session
     * @return Liste des scores triés par total_score DESC
     */
    public List<SessionScore> getSessionRanking(UUID sessionId) {
        return sessionScoreRepository.findBySessionIdOrderByTotalScoreDesc(sessionId);
    }

    /**
     * Récupère le score d'un joueur spécifique dans une session
     * 
     * @param sessionId UUID de la session
     * @param userId ID du joueur
     * @return Optional contenant le SessionScore si trouvé
     */
    public Optional<SessionScore> getPlayerScore(UUID sessionId, Long userId) {
        return sessionScoreRepository.findBySessionIdAndUserId(sessionId, userId);
    }

    /**
     * Initialise les scores pour tous les joueurs d'une session au début de la première partie
     * 
     * @param sessionId UUID de la session
     * @param userIds Liste des IDs des joueurs
     */
    @Transactional
    public void initializeSessionScores(UUID sessionId, List<Long> userIds) {
        for (Long userId : userIds) {
            SessionScoreId scoreId = new SessionScoreId(sessionId, userId);
            if (!sessionScoreRepository.existsById(scoreId)) {
                SessionScore sessionScore = new SessionScore(sessionId, userId);
                sessionScoreRepository.save(sessionScore);
                logger.info(String.format("Score initialisé pour userId=%d dans session %s", userId, sessionId));
            }
        }
    }
}
