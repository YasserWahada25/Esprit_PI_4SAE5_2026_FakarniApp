package com.alzheimer.activite_educative_service.services;

import com.alzheimer.activite_educative_service.entities.ActiviteEducative;
import com.alzheimer.activite_educative_service.entities.ActivityType;
import com.alzheimer.activite_educative_service.entities.EducationalQuestion;
import com.alzheimer.activite_educative_service.entities.GameType;
import com.alzheimer.activite_educative_service.repositories.EducationalQuestionRepository;
import com.alzheimer.activite_educative_service.services.imports.ExternalContentImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Jeux prêts à l’emploi : si une activité GAME/QUIZ n’a aucune question en base,
 * on y attache un pack selon le {@link GameType} et l’identifiant de l’activité
 * (plusieurs banques pour éviter que tous les quiz soient identiques).
 */
@Service
public class PreconfiguredQuestionSeedService {

    private static final Logger log = LoggerFactory.getLogger(PreconfiguredQuestionSeedService.class);

    private static final String WIKI = "https://upload.wikimedia.org/wikipedia/commons/thumb/";

    /** Définition d’une question à insérer (imageUrl nullable pour QCM texte). */
    private record Seed(String prompt, String imageUrl, List<String> options, String correct) {
    }

    private static final List<List<Seed>> MEMORY_QUIZ_PACKS = List.of(
            List.of(
                    new Seed("Quel fruit est riche en potassium ?", null,
                            List.of("Pomme", "Banane", "Raisin"), "Banane"),
                    new Seed("Combien de couleurs comptent les couleurs primaires (additives) ?", null,
                            List.of("2", "3", "4"), "3"),
                    new Seed("Quelle est la capitale de la France ?", null,
                            List.of("Lyon", "Paris", "Marseille"), "Paris")
            ),
            List.of(
                    new Seed("En combien de temps la Terre fait-elle un tour complet sur elle-même ?", null,
                            List.of("12 heures", "24 heures", "365 jours"), "24 heures"),
                    new Seed("Combien de continents compte-t-on couramment ?", null,
                            List.of("5", "6", "7"), "7"),
                    new Seed("Quel est le plus grand océan ?", null,
                            List.of("Atlantique", "Pacifique", "Indien"), "Pacifique")
            ),
            List.of(
                    new Seed("Combien de côtés a un triangle ?", null,
                            List.of("2", "3", "4"), "3"),
                    new Seed("Quelle planète est la plus proche du Soleil ?", null,
                            List.of("Vénus", "Mercure", "Mars"), "Mercure"),
                    new Seed("Combien de minutes compte une heure ?", null,
                            List.of("30", "60", "100"), "60")
            ),
            List.of(
                    new Seed("Quel gaz est le plus abondant dans l’air ?", null,
                            List.of("Oxygène", "Azote", "Dioxyde de carbone"), "Azote"),
                    new Seed("Combien de saisons distingue-t-on en climat tempéré ?", null,
                            List.of("2", "3", "4"), "4"),
                    new Seed("Quelle couleur domine le ciel par temps clair ?", null,
                            List.of("Vert", "Bleu", "Rouge"), "Bleu")
            ),
            List.of(
                    new Seed("Combien de doigts sur une main humaine ?", null,
                            List.of("4", "5", "6"), "5"),
                    new Seed("Quel organe pompe le sang dans le corps ?", null,
                            List.of("Le cœur", "Le foie", "L’estomac"), "Le cœur"),
                    new Seed("Combien de jours compte une semaine ?", null,
                            List.of("5", "6", "7"), "7")
            )
    );

    /**
     * Images Wikimedia + intitulés cohérents (stable en production).
     */
    private static final List<List<Seed>> IMAGE_RECOGNITION_PACKS = List.of(
            List.of(
                    new Seed("Quel animal est représenté ?",
                            WIKI + "2/26/YellowLabradorLooking_new.jpg/360px-YellowLabradorLooking_new.jpg",
                            List.of("Chat", "Chien", "Cheval"), "Chien"),
                    new Seed("Quel animal est représenté ?",
                            WIKI + "3/3a/Cat03.jpg/360px-Cat03.jpg",
                            List.of("Chien", "Chat", "Lapin"), "Chat")
            ),
            List.of(
                    new Seed("Quel fruit est représenté ?",
                            WIKI + "1/15/Red_Apple.jpg/320px-Red_Apple.jpg",
                            List.of("Orange", "Pomme", "Citron"), "Pomme"),
                    new Seed("Quel fruit est représenté ?",
                            WIKI + "8/8a/Banana-Single.jpg/320px-Banana-Single.jpg",
                            List.of("Poire", "Banane", "Kiwi"), "Banane")
            ),
            List.of(
                    new Seed("Quel moyen de transport est représenté ?",
                            WIKI + "5/5d/2018_Tesla_Model_3.jpg/360px-2018_Tesla_Model_3.jpg",
                            List.of("Vélo", "Voiture", "Bus"), "Voiture"),
                    new Seed("Quel moyen de transport est représenté ?",
                            WIKI + "4/41/Left_side_of_Flying_Pigeon.jpg/320px-Left_side_of_Flying_Pigeon.jpg",
                            List.of("Moto", "Trottinette", "Vélo"), "Vélo")
            ),
            List.of(
                    new Seed("Quel environnement naturel est représenté ?",
                            WIKI + "2/20/Swiss_National_Park_093.JPG/360px-Swiss_National_Park_093.JPG",
                            List.of("Forêt", "Désert", "Glacier"), "Forêt"),
                    new Seed("Quel environnement est représenté ?",
                            WIKI + "c/c6/4406-Diving_Zanzibar_Nungwi_Beach_2018_01.jpg/360px-4406-Diving_Zanzibar_Nungwi_Beach_2018_01.jpg",
                            List.of("Plage", "Steppe", "Toundra"), "Plage")
            ),
            List.of(
                    new Seed("Quel monument ou lieu urbain est suggéré ?",
                            WIKI + "a/a6/Brandenburger_Tor_abends.jpg/360px-Brandenburger_Tor_abends.jpg",
                            List.of("Tour Eiffel", "Porte de Brandebourg", "Big Ben"), "Porte de Brandebourg"),
                    new Seed("Quel type de bâtiment est représenté ?",
                            WIKI + "4/4b/Cathedral_Notre-Dame_de_Paris_2013-07-24.jpg/320px-Cathedral_Notre-Dame_de_Paris_2013-07-24.jpg",
                            List.of("Château", "Cathédrale", "Gare"), "Cathédrale")
            )
    );

    /** Intitulés des QCM texte : si une activité IMAGE_RECOGNITION les contient encore, on remplace le pack. */
    private static final Set<String> MEMORY_QUIZ_PROMPTS = new HashSet<>();

    static {
        for (List<Seed> pack : MEMORY_QUIZ_PACKS) {
            for (Seed s : pack) {
                MEMORY_QUIZ_PROMPTS.add(normalizePrompt(s.prompt()));
            }
        }
    }

    private final EducationalQuestionRepository questionRepository;
    private final ObjectMapper objectMapper;
    private final ExternalContentImportService externalContentImportService;

    public PreconfiguredQuestionSeedService(
            EducationalQuestionRepository questionRepository,
            ObjectMapper objectMapper,
            ExternalContentImportService externalContentImportService
    ) {
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper;
        this.externalContentImportService = externalContentImportService;
    }

    @Transactional
    public void ensureDefaultQuestions(ActiviteEducative activity) {
        GameType gt = resolveGameType(activity);
        long activityId = activity.getId();
        if (questionRepository.countByActivityId(activityId) > 0) {
            if (gt == GameType.IMAGE_RECOGNITION) {
                try {
                    syncImageRecognitionQuestionBank(activity);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Failed to sync IMAGE_RECOGNITION questions for activity " + activityId, e);
                }
            } else if (gt == GameType.MEMORY_MATCH) {
                try {
                    repairMemoryMatchImageUrlsIfNeeded(activity);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Failed to repair MEMORY_MATCH image URLs for activity " + activityId, e);
                }
            }
            return;
        }
        if (gt == null) {
            log.warn("Activity id={} has no gameType; cannot attach default questions", activityId);
            return;
        }
        try {
            switch (gt) {
                case MEMORY_QUIZ -> seedMemoryQuiz(activity);
                case IMAGE_RECOGNITION -> seedImageRecognition(activity);
                case MEMORY_MATCH -> seedMemoryMatch(activity);
            }
            log.info("Seeded default {} questions for activity id={} (pack from id)", gt, activityId);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to seed default questions for activity " + activityId, e);
        }
    }

    /**
     * Une activité « jeu d’images » ne doit pas garder d’anciennes questions QCM texte ni d’URLs vides.
     * Détecte les intitulés du pack mémoire, ou une image manquante après réparation → remplace tout le pack image.
     */
    private void syncImageRecognitionQuestionBank(ActiviteEducative activity) throws Exception {
        List<EducationalQuestion> qs =
                questionRepository.findByActivityIdOrderByOrderIndexAsc(activity.getId());
        if (qs.isEmpty()) {
            return;
        }
        boolean wrongKind = qs.stream()
                .map(q -> normalizePrompt(q.getPrompt()))
                .anyMatch(MEMORY_QUIZ_PROMPTS::contains);
        if (wrongKind) {
            log.info(
                    "Activity id={}: IMAGE_RECOGNITION still had memory-quiz prompts; replacing with image pack",
                    activity.getId());
            questionRepository.deleteAllForActivity(activity.getId());
            seedImageRecognition(activity);
            return;
        }
        repairImageRecognitionUrlsIfMissing(activity);
        qs = questionRepository.findByActivityIdOrderByOrderIndexAsc(activity.getId());
        boolean stillMissing = qs.stream()
                .anyMatch(q -> q.getImageUrl() == null || q.getImageUrl().isBlank());
        if (stillMissing) {
            log.info(
                    "Activity id={}: IMAGE_RECOGNITION still missing imageUrl after repair; replacing with image pack",
                    activity.getId());
            questionRepository.deleteAllForActivity(activity.getId());
            seedImageRecognition(activity);
        }
    }

    private static String normalizePrompt(String p) {
        if (p == null || p.isBlank()) {
            return "";
        }
        return p.trim()
                .replace('\u2019', '\'')
                .replace('\u2018', '\'')
                .replace('`', '\'');
    }

    /**
     * Questions créées avant {@code imageUrl} ou sans URL : on complète depuis le pack image
     * dérivé de l’id d’activité (même logique que le seed initial, par {@code orderIndex}).
     */
    private void repairImageRecognitionUrlsIfMissing(ActiviteEducative activity) {
        List<EducationalQuestion> existing =
                questionRepository.findByActivityIdOrderByOrderIndexAsc(activity.getId());
        boolean anyMissing = existing.stream()
                .anyMatch(q -> q.getImageUrl() == null || q.getImageUrl().isBlank());
        if (!anyMissing || existing.isEmpty()) {
            return;
        }
        int pack = packIndex(activity.getId(), IMAGE_RECOGNITION_PACKS.size());
        List<Seed> seeds = IMAGE_RECOGNITION_PACKS.get(pack);
        for (EducationalQuestion q : existing) {
            if (q.getImageUrl() != null && !q.getImageUrl().isBlank()) {
                continue;
            }
            int idx = q.getOrderIndex() != null ? q.getOrderIndex() - 1 : existing.indexOf(q);
            if (idx >= 0 && idx < seeds.size()) {
                String url = seeds.get(idx).imageUrl();
                if (url != null && !url.isBlank()) {
                    q.setImageUrl(url);
                    questionRepository.save(q);
                }
            }
        }
        log.info("Repaired missing imageUrl for IMAGE_RECOGNITION activity id={}", activity.getId());
    }

    private GameType resolveGameType(ActiviteEducative activity) {
        if (activity.getGameType() != null) {
            return activity.getGameType();
        }
        if (activity.getType() == ActivityType.QUIZ || activity.getType() == ActivityType.GAME) {
            return GameType.MEMORY_QUIZ;
        }
        return null;
    }

    private void seedMemoryQuiz(ActiviteEducative activity) throws Exception {
        int pack = packIndex(activity.getId(), MEMORY_QUIZ_PACKS.size());
        List<Seed> seeds = MEMORY_QUIZ_PACKS.get(pack);
        persistPack(activity, seeds);
    }

    private void seedImageRecognition(ActiviteEducative activity) throws Exception {
        int pack = packIndex(activity.getId(), IMAGE_RECOGNITION_PACKS.size());
        List<Seed> seeds = IMAGE_RECOGNITION_PACKS.get(pack);
        persistPack(activity, seeds);
    }

    /**
     * Memory : deux lignes par image (même {@code correct} = identifiant de paire), imageUrl obligatoire.
     * Images via Picsum (seed stable par paire) : les URLs Wikimedia échouent souvent dans le navigateur
     * (référent / blocage), d’où l’absence de photos côté UI.
     */
    private void seedMemoryMatch(ActiviteEducative activity) throws Exception {
        int extLabels = externalContentImportService.fetchRemoteImageLabels().size();
        log.info("External / fallback image bank labels available: {}", extLabels);
        List<Seed> seeds = List.of(
                new Seed("Singe", stableMemoryImageUrl("PAIR_MONKEY"), List.of(), "PAIR_MONKEY"),
                new Seed("Singe", stableMemoryImageUrl("PAIR_MONKEY"), List.of(), "PAIR_MONKEY"),
                new Seed("Orange", stableMemoryImageUrl("PAIR_ORANGE"), List.of(), "PAIR_ORANGE"),
                new Seed("Orange", stableMemoryImageUrl("PAIR_ORANGE"), List.of(), "PAIR_ORANGE"),
                new Seed("Horloge", stableMemoryImageUrl("PAIR_CLOCK"), List.of(), "PAIR_CLOCK"),
                new Seed("Horloge", stableMemoryImageUrl("PAIR_CLOCK"), List.of(), "PAIR_CLOCK"),
                new Seed("Planète", stableMemoryImageUrl("PAIR_PLANET"), List.of(), "PAIR_PLANET"),
                new Seed("Planète", stableMemoryImageUrl("PAIR_PLANET"), List.of(), "PAIR_PLANET"),
                new Seed("Poisson", stableMemoryImageUrl("PAIR_FISH"), List.of(), "PAIR_FISH"),
                new Seed("Poisson", stableMemoryImageUrl("PAIR_FISH"), List.of(), "PAIR_FISH"),
                new Seed("Feuille", stableMemoryImageUrl("PAIR_LEAF"), List.of(), "PAIR_LEAF"),
                new Seed("Feuille", stableMemoryImageUrl("PAIR_LEAF"), List.of(), "PAIR_LEAF")
        );
        persistMemoryCards(activity, seeds);
    }

    /**
     * URL d’image déterministe par identifiant de paire (même paire = même image), servie en HTTPS
     * et adaptée aux balises {@code <img>} sans en-têtes spéciaux.
     */
    private static String stableMemoryImageUrl(String pairId) {
        String key = pairId == null ? "PAIR" : pairId.trim();
        if (key.isEmpty()) {
            key = "PAIR";
        }
        String safe = key.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "_");
        return "https://picsum.photos/seed/fakarni_mm_" + safe + "/400/400";
    }

    /**
     * Remplace les anciennes URLs Wikimedia (ou vides) pour les activités memory déjà en base.
     */
    private void repairMemoryMatchImageUrlsIfNeeded(ActiviteEducative activity) {
        List<EducationalQuestion> qs =
                questionRepository.findByActivityIdOrderByOrderIndexAsc(activity.getId());
        if (qs.isEmpty()) {
            return;
        }
        boolean changed = false;
        for (EducationalQuestion q : qs) {
            String url = q.getImageUrl();
            boolean bad = url == null
                    || url.isBlank()
                    || url.contains("wikimedia.org")
                    || url.contains("upload.wikimedia");
            if (bad) {
                String pair = q.getCorrectAnswer() != null ? q.getCorrectAnswer().trim() : "PAIR";
                q.setImageUrl(stableMemoryImageUrl(pair));
                changed = true;
            }
        }
        if (changed) {
            questionRepository.saveAll(qs);
            log.info("Repaired MEMORY_MATCH imageUrl(s) for activity id={}", activity.getId());
        }
    }

    private void persistMemoryCards(ActiviteEducative activity, List<Seed> seeds) throws Exception {
        int order = 1;
        List<EducationalQuestion> batch = new ArrayList<>();
        for (Seed s : seeds) {
            batch.add(build(activity, order++, s.prompt(), s.imageUrl(), s.options(), s.correct()));
        }
        questionRepository.saveAll(batch);
    }

    private static int packIndex(long activityId, int packCount) {
        return (int) (Math.floorMod(activityId, packCount));
    }

    private void persistPack(ActiviteEducative activity, List<Seed> seeds) throws Exception {
        int order = 1;
        List<EducationalQuestion> batch = new ArrayList<>();
        for (Seed s : seeds) {
            batch.add(build(activity, order++, s.prompt(), s.imageUrl(), s.options(), s.correct()));
        }
        questionRepository.saveAll(batch);
    }

    private EducationalQuestion build(
            ActiviteEducative activity,
            int order,
            String prompt,
            String imageUrl,
            List<String> options,
            String correct
    ) throws Exception {
        EducationalQuestion q = new EducationalQuestion();
        q.setActivity(activity);
        q.setOrderIndex(order);
        q.setPrompt(prompt);
        q.setImageUrl(imageUrl);
        q.setCorrectAnswer(correct);
        q.setOptionsJson(objectMapper.writeValueAsString(options));
        return q;
    }
}
